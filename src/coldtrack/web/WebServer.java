package coldtrack.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.Vector;
import coldtrack.db.ColdTrackDao;
import coldtrack.exception.TruckNotFoundException;
import coldtrack.model.Alert;
import coldtrack.model.Reading;
import coldtrack.model.Truck;
import coldtrack.service.ColdChainService;
import coldtrack.service.SensorThread;

// [Practical 5] Packages: coldtrack.web
// Plain Java HTTP server built into JDK 8+
public class WebServer {

    private int port;
    private ColdChainService service;
    private ColdTrackDao dao;
    private HttpServer server;

    public WebServer(int port, ColdChainService service, ColdTrackDao dao) {
        this.port = port;
        this.service = service;
        this.dao = dao;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new DashboardHandler());
        server.createContext("/truck", new TruckHandler());
        server.createContext("/alerts", new AlertsHandler());
        server.createContext("/add", new AddHandler());
        server.createContext("/toggle", new ToggleHandler());
        server.createContext("/remove", new RemoveHandler());
        server.createContext("/concepts", new ConceptsHandler());
        server.createContext("/style.css", new StyleHandler());

        // Asset handlers for team member photos
        server.createContext("/sanskar.jpg", new ImageHandler("sanskar.jpg", "image/jpeg"));
        server.createContext("/sarvesh.jpg", new ImageHandler("sarvesh.jpg", "image/jpeg"));
        server.createContext("/aamer.jpg", new ImageHandler("aamer.jpg", "image/jpeg"));
        server.createContext("/rohan.jpg", new ImageHandler("rohan.jpg", "image/jpeg"));
        server.createContext("/sanskar.svg", new ImageHandler("sanskar.svg", "image/svg+xml"));
        server.createContext("/sarvesh.svg", new ImageHandler("sarvesh.svg", "image/svg+xml"));
        server.createContext("/aamer.svg", new ImageHandler("aamer.svg", "image/svg+xml"));
        server.createContext("/rohan.svg", new ImageHandler("rohan.svg", "image/svg+xml"));

        server.setExecutor(null);
        server.start();
        System.out.println("[Web Server] Running at http://localhost:" + port + "/");
    }

    // GET / -> dashboard.html
    class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!"/".equals(ex.getRequestURI().getPath())) {
                sendResponse(ex, 404, "Not Found", "text/plain");
                return;
            }

            String query = ex.getRequestURI().getRawQuery();
            String search = getParam(query, "search");

            Vector<Truck> list = service.getTrucks();
            StringBuilder rows = new StringBuilder();

            synchronized (service) {
                for (int i = 0; i < list.size(); i++) {
                    Truck t = list.elementAt(i);

                    // [Practical 9] String method: contains and toLowerCase for name search
                    if (search != null && search.trim().length() > 0) {
                        if (!t.getName().toLowerCase().contains(search.trim().toLowerCase())) {
                            continue;
                        }
                    }

                    String statusClass = "status-ok";
                    if ("WARNING".equalsIgnoreCase(t.getStatus())) {
                        statusClass = "status-warning";
                    } else if ("DANGER".equalsIgnoreCase(t.getStatus())) {
                        statusClass = "status-danger";
                    }

                    String toggleBtn = t.isFailing()
                        ? "<input type=\"submit\" value=\"Fix Heater\" class=\"btn-green\">"
                        : "<input type=\"submit\" value=\"Simulate Failure\" class=\"btn-red\">";

                    rows.append("<tr>")
                        .append("<td>#").append(t.getId()).append("</td>")
                        .append("<td><a href=\"/truck?id=").append(t.getId()).append("\">").append(escapeHtml(t.getName())).append("</a></td>")
                        .append("<td><b>").append(escapeHtml(t.getCargo().toUpperCase())).append("</b></td>")
                        .append("<td>").append(t.getMinTemp()).append(" to ").append(t.getMaxTemp()).append(" &deg;C</td>")
                        .append("<td><b>").append(t.getCurrentTemp()).append(" &deg;C</b></td>")
                        .append("<td>").append(t.getCurrentHumidity()).append(" %</td>")
                        .append("<td><span class=\"").append(statusClass).append("\">").append(t.getStatus()).append("</span></td>")
                        .append("<td>")
                        .append("<form action=\"/toggle?id=").append(t.getId()).append("\" method=\"post\" style=\"display:inline;\">")
                        .append(toggleBtn)
                        .append("</form>")
                        .append("</td>")
                        .append("<td>")
                        .append("<form action=\"/remove?id=").append(t.getId()).append("\" method=\"post\" style=\"display:inline;\">")
                        .append("<input type=\"submit\" value=\"Remove\">")
                        .append("</form>")
                        .append("</td>")
                        .append("</tr>");
                }
            }

            if (rows.length() == 0) {
                rows.append("<tr><td colspan=\"9\">No refrigerated trucks found matching criteria.</td></tr>");
            }

            String html = loadTemplate("dashboard.html");
            html = html.replace("{{summary}}", escapeHtml(service.getSummary()))
                       .replace("{{vectorInfo}}", "Trucks Count = " + list.size() + " | Vector Capacity = " + list.capacity())
                       .replace("{{search}}", escapeHtml(search))
                       .replace("{{truckRows}}", rows.toString());

            sendResponse(ex, 200, html, "text/html; charset=UTF-8");
        }
    }

    // GET /truck?id= -> truck.html
    class TruckHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String query = ex.getRequestURI().getRawQuery();
            String idStr = getParam(query, "id");

            int id = 1;
            try {
                if (idStr != null && idStr.length() > 0) {
                    id = Integer.parseInt(idStr);
                }
            } catch (Exception ignored) {
            }

            Truck t = null;
            String errorMsg = "";
            try {
                t = service.findTruck(id);
            } catch (TruckNotFoundException e) {
                errorMsg = "Error: " + e.getMessage();
            }

            String html = loadTemplate("truck.html");

            if (t != null) {
                // [Practical 2] Format last10 array values
                double[] arr = t.getLast10();
                StringBuilder arrStr = new StringBuilder("[ ");
                for (int i = 0; i < arr.length; i++) {
                    arrStr.append(arr[i]);
                    if (i < arr.length - 1) {
                        arrStr.append(", ");
                    }
                }
                arrStr.append(" ]");

                // Latest 20 readings from DB
                Vector<Reading> readings = dao != null ? dao.getReadings(t.getId(), 20) : new Vector<Reading>();
                StringBuilder rRows = new StringBuilder();
                for (int i = 0; i < readings.size(); i++) {
                    Reading r = readings.elementAt(i);
                    rRows.append("<tr>")
                         .append("<td>").append(r.getId()).append("</td>")
                         .append("<td><b>").append(r.getTemperature()).append(" &deg;C</b></td>")
                         .append("<td>").append(r.getHumidity()).append(" %</td>")
                         .append("<td>").append(r.getStatus()).append("</td>")
                         .append("<td>").append(r.getTimeText()).append("</td>")
                         .append("</tr>");
                }
                if (rRows.length() == 0) {
                    rRows.append("<tr><td colspan=\"5\">No recorded readings yet in database.</td></tr>");
                }

                html = html.replace("{{error}}", "")
                           .replace("{{truckId}}", String.valueOf(t.getId()))
                           .replace("{{truckName}}", escapeHtml(t.getName()))
                           .replace("{{cargo}}", escapeHtml(t.getCargo().toUpperCase()))
                           .replace("{{minTemp}}", String.valueOf(t.getMinTemp()))
                           .replace("{{maxTemp}}", String.valueOf(t.getMaxTemp()))
                           .replace("{{currentTemp}}", String.valueOf(t.getCurrentTemp()))
                           .replace("{{currentHumidity}}", String.valueOf(t.getCurrentHumidity()))
                           .replace("{{status}}", escapeHtml(t.getStatus()))
                           .replace("{{last10Values}}", arrStr.toString())
                           .replace("{{readingRows}}", rRows.toString());
            } else {
                html = html.replace("{{error}}", errorMsg)
                           .replace("{{truckId}}", String.valueOf(id))
                           .replace("{{truckName}}", "Not Found")
                           .replace("{{cargo}}", "-")
                           .replace("{{minTemp}}", "-")
                           .replace("{{maxTemp}}", "-")
                           .replace("{{currentTemp}}", "-")
                           .replace("{{currentHumidity}}", "-")
                           .replace("{{status}}", "-")
                           .replace("{{last10Values}}", "[]")
                           .replace("{{readingRows}}", "");
            }

            sendResponse(ex, 200, html, "text/html; charset=UTF-8");
        }
    }

    // GET /alerts -> alerts.html
    class AlertsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String query = ex.getRequestURI().getRawQuery();
            String truckFilter = getParam(query, "truck");

            Vector<Alert> list;
            int filterId = -1;
            if (truckFilter != null && truckFilter.length() > 0) {
                try {
                    filterId = Integer.parseInt(truckFilter);
                } catch (Exception ignored) {
                }
            }

            if (filterId > 0 && dao != null) {
                list = dao.getAlertsByTruck(filterId);
            } else if (dao != null) {
                list = dao.getAlerts();
            } else {
                list = new Vector<Alert>();
            }

            StringBuilder aRows = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                Alert a = list.elementAt(i);
                aRows.append("<tr>")
                     .append("<td><b>#").append(a.getId()).append("</b></td>")
                     .append("<td><a href=\"/truck?id=").append(a.getTruckId()).append("\">Truck #").append(a.getTruckId()).append("</a></td>")
                     .append("<td><span class=\"status-danger\">").append(escapeHtml(a.getMessage())).append("</span></td>")
                     .append("<td><b>").append(a.getTemperature()).append(" &deg;C</b></td>")
                     .append("<td>").append(a.getTimeText()).append("</td>")
                     .append("</tr>");
            }
            if (aRows.length() == 0) {
                aRows.append("<tr><td colspan=\"5\">No temperature breach alerts recorded. System operating safely.</td></tr>");
            }

            // Quick filter links
            StringBuilder links = new StringBuilder();
            Vector<Truck> trucks = service.getTrucks();
            for (int i = 0; i < trucks.size(); i++) {
                Truck t = trucks.elementAt(i);
                links.append(" | <a href=\"/alerts?truck=").append(t.getId()).append("\">").append(escapeHtml(t.getName())).append("</a>");
            }

            String html = loadTemplate("alerts.html");
            html = html.replace("{{truckFilterLinks}}", links.toString())
                       .replace("{{alertRows}}", aRows.toString());

            sendResponse(ex, 200, html, "text/html; charset=UTF-8");
        }
    }

    // GET /add and POST /add
    class AddHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                String html = loadTemplate("add.html");
                html = html.replace("{{error}}", "")
                           .replace("{{name}}", "")
                           .replace("{{min}}", "")
                           .replace("{{max}}", "");
                sendResponse(ex, 200, html, "text/html; charset=UTF-8");
                return;
            }

            if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                String body = readBody(ex);
                String name = getParam(body, "name");
                String cargo = getParam(body, "cargo");
                String minStr = getParam(body, "min");
                String maxStr = getParam(body, "max");

                try {
                    Truck newTruck;
                    if (minStr != null && minStr.trim().length() > 0 && maxStr != null && maxStr.trim().length() > 0) {
                        double min = Double.parseDouble(minStr.trim());
                        double max = Double.parseDouble(maxStr.trim());
                        newTruck = service.addTruck(name, cargo, min, max);
                    } else {
                        newTruck = service.addTruck(name, cargo);
                    }

                    // [Practical 8] Start new SensorThread dynamically for newly added truck
                    SensorThread st = new SensorThread(newTruck, service.getInbox());
                    st.setDaemon(true);
                    st.start();

                    // 302 Redirect back to dashboard
                    redirect(ex, "/");

                } catch (IllegalArgumentException e) {
                    // [Practical 7] Validation error display
                    String html = loadTemplate("add.html");
                    html = html.replace("{{error}}", "Validation Error: " + escapeHtml(e.getMessage()))
                               .replace("{{name}}", escapeHtml(name))
                               .replace("{{min}}", escapeHtml(minStr))
                               .replace("{{max}}", escapeHtml(maxStr));
                    sendResponse(ex, 200, html, "text/html; charset=UTF-8");
                } catch (Exception e) {
                    String html = loadTemplate("add.html");
                    html = html.replace("{{error}}", "Error: " + escapeHtml(e.getMessage()))
                               .replace("{{name}}", escapeHtml(name))
                               .replace("{{min}}", escapeHtml(minStr))
                               .replace("{{max}}", escapeHtml(maxStr));
                    sendResponse(ex, 200, html, "text/html; charset=UTF-8");
                }
            }
        }
    }

    // POST /toggle?id= -> toggle failure simulation
    class ToggleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                String query = ex.getRequestURI().getRawQuery();
                String idStr = getParam(query, "id");
                try {
                    int id = Integer.parseInt(idStr);
                    Truck t = service.findTruck(id);
                    t.setFailing(!t.isFailing());
                    System.out.println("[WebServer] Truck #" + id + " failing flag set to: " + t.isFailing());
                } catch (Exception ignored) {
                }
            }
            redirect(ex, "/");
        }
    }

    // POST /remove?id= -> remove truck
    class RemoveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                String query = ex.getRequestURI().getRawQuery();
                String idStr = getParam(query, "id");
                try {
                    int id = Integer.parseInt(idStr);
                    service.removeTruck(id);
                } catch (Exception ignored) {
                }
            }
            redirect(ex, "/");
        }
    }

    // GET /concepts -> concepts.html
    class ConceptsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String html = loadTemplate("concepts.html");
            sendResponse(ex, 200, html, "text/html; charset=UTF-8");
        }
    }

    // GET /style.css
    class StyleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String css = loadTemplate("style.css");
            sendResponse(ex, 200, css, "text/css; charset=UTF-8");
        }
    }

    // Image handler for team member portrait photos (JPG) and SVGs
    class ImageHandler implements HttpHandler {
        private String fileName;
        private String mimeType;

        public ImageHandler(String fileName, String mimeType) {
            this.fileName = fileName;
            this.mimeType = mimeType;
        }

        @Override
        public void handle(HttpExchange ex) throws IOException {
            File f = new File("web", fileName);
            if (!f.exists()) {
                f = new File("public", fileName);
            }
            if (!f.exists()) {
                f = new File(fileName);
            }
            if (!f.exists()) {
                sendResponse(ex, 404, "Not Found", "text/plain");
                return;
            }
            FileInputStream fis = new FileInputStream(f);
            byte[] buf = new byte[(int) f.length()];
            fis.read(buf);
            fis.close();

            ex.getResponseHeaders().set("Content-Type", mimeType);
            ex.sendResponseHeaders(200, buf.length);
            OutputStream os = ex.getResponseBody();
            os.write(buf);
            os.close();
        }
    }

    // Helper: 302 HTTP Redirect
    private void redirect(HttpExchange ex, String location) throws IOException {
        ex.getResponseHeaders().set("Location", location);
        ex.sendResponseHeaders(302, -1);
        ex.close();
    }

    // Helper: Read request body
    private String readBody(HttpExchange ex) throws IOException {
        InputStream is = ex.getRequestBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len = is.read(buf)) != -1) {
            baos.write(buf, 0, len);
        }
        return new String(baos.toByteArray(), "UTF-8");
    }

    // Helper: Parse URL parameters
    private String getParam(String data, String key) {
        if (data == null || key == null) {
            return "";
        }
        String[] pairs = data.split("&");
        for (int i = 0; i < pairs.length; i++) {
            String[] kv = pairs[i].split("=");
            if (kv.length >= 1 && kv[0].equals(key)) {
                String val = kv.length > 1 ? kv[1] : "";
                try {
                    return URLDecoder.decode(val, "UTF-8");
                } catch (Exception e) {
                    return val;
                }
            }
        }
        return "";
    }

    // Helper: Escape HTML characters
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }

    // Helper: Load template file from web/ folder
    private String loadTemplate(String fileName) {
        try {
            File f = new File("web", fileName);
            if (!f.exists()) {
                f = new File(fileName);
            }
            FileInputStream fis = new FileInputStream(f);
            byte[] buf = new byte[(int) f.length()];
            fis.read(buf);
            fis.close();
            return new String(buf, "UTF-8");
        } catch (Exception e) {
            return "<html><body><p>Template error (" + fileName + "): " + e.getMessage() + "</p></body></html>";
        }
    }

    // Helper: Send HTTP response
    private void sendResponse(HttpExchange ex, int status, String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes("UTF-8");
        ex.getResponseHeaders().set("Content-Type", contentType);
        ex.sendResponseHeaders(status, bytes.length);
        OutputStream os = ex.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
