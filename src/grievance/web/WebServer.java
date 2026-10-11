package grievance.web;

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
import grievance.db.ComplaintDao;
import grievance.exception.ComplaintNotFoundException;
import grievance.exception.InvalidComplaintException;
import grievance.model.Complaint;
import grievance.service.ComplaintEngine;
import grievance.service.ResolverTask;

// [Practical 5] Packages: grievance.web
// Web server using only com.sun.net.httpserver.HttpServer (built into JDK)
public class WebServer {

    private int port;
    private ComplaintEngine engine;
    private ComplaintDao dao;
    private HttpServer server;

    public WebServer(int port, ComplaintEngine engine, ComplaintDao dao) {
        this.port = port;
        this.engine = engine;
        this.dao = dao;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Web routes
        server.createContext("/", new HomeHandler());
        server.createContext("/submit", new SubmitHandler());
        server.createContext("/track", new TrackHandler());
        server.createContext("/admin", new AdminHandler());
        server.createContext("/concepts", new ConceptsHandler());
        server.createContext("/style.css", new StyleHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("[Web Server] Running at http://localhost:" + port + "/");
    }

    // GET / -> index.html
    class HomeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!"GET".equalsIgnoreCase(ex.getRequestMethod()) && !"/".equals(ex.getRequestURI().getPath())) {
                sendResponse(ex, 404, "Not Found", "text/plain");
                return;
            }
            String html = loadTemplate("index.html");
            html = html.replace("{{error}}", "")
                       .replace("{{name}}", "")
                       .replace("{{text}}", "");
            sendResponse(ex, 200, html, "text/html; charset=UTF-8");
        }
    }

    // POST /submit -> process complaint and show result.html
    class SubmitHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                sendResponse(ex, 405, "Method Not Allowed", "text/plain");
                return;
            }

            String body = readRequestBody(ex);
            String name = getParam(body, "name");
            String ward = getParam(body, "ward");
            String text = getParam(body, "text");

            try {
                // [Practical 7] Calling method that throws custom InvalidComplaintException
                Complaint c = engine.register(name, text, ward);

                // [Practical 10] Save to database
                if (dao != null) {
                    dao.save(c);
                }

                // [Practical 4] Route to department via Routable interface
                String actionMsg = engine.route(c);

                // [Practical 8] Start asynchronous ResolverTask thread (resolves after 45 seconds for demo)
                Thread resolverThread = new Thread(new ResolverTask(c, dao, 45));
                resolverThread.start();

                String html = loadTemplate("result.html");
                html = html.replace("{{id}}", String.valueOf(c.getId()))
                           .replace("{{name}}", escapeHtml(c.getName()))
                           .replace("{{ward}}", escapeHtml(c.getWard()))
                           .replace("{{category}}", escapeHtml(c.getCategory()))
                           .replace("{{priority}}", String.valueOf(c.getPriority()))
                           .replace("{{status}}", escapeHtml(c.getStatus()))
                           .replace("{{actionMessage}}", escapeHtml(actionMsg))
                           .replace("{{preview}}", escapeHtml(c.getPreview()));

                sendResponse(ex, 200, html, "text/html; charset=UTF-8");

            } catch (InvalidComplaintException e) {
                // [Practical 7] Catch custom exception and display red error line on page
                String html = loadTemplate("index.html");
                html = html.replace("{{error}}", "Error: " + escapeHtml(e.getMessage()))
                           .replace("{{name}}", escapeHtml(name))
                           .replace("{{text}}", escapeHtml(text));
                sendResponse(ex, 200, html, "text/html; charset=UTF-8");
            } catch (Exception e) {
                String html = loadTemplate("index.html");
                html = html.replace("{{error}}", "System error: " + escapeHtml(e.getMessage()))
                           .replace("{{name}}", escapeHtml(name))
                           .replace("{{text}}", escapeHtml(text));
                sendResponse(ex, 200, html, "text/html; charset=UTF-8");
            }
        }
    }

    // GET /track?id= -> track.html
    class TrackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String query = ex.getRequestURI().getRawQuery();
            String idStr = getParam(query, "id");

            String html = loadTemplate("track.html");
            html = html.replace("{{searchId}}", escapeHtml(idStr));

            if (idStr == null || idStr.trim().length() == 0) {
                html = html.replace("{{error}}", "").replace("{{details}}", "");
                sendResponse(ex, 200, html, "text/html; charset=UTF-8");
                return;
            }

            try {
                int id = Integer.parseInt(idStr.trim());

                // [Practical 10] First attempt DB lookup, then fallback to queue
                Complaint c = null;
                if (dao != null) {
                    c = dao.findById(id);
                }
                if (c == null) {
                    // [Practical 7] Throws ComplaintNotFoundException if not found
                    c = engine.find(id);
                }

                String statusClass = "status-open";
                // [Practical 9] String method: equalsIgnoreCase
                if ("ESCALATED".equalsIgnoreCase(c.getStatus())) {
                    statusClass = "status-escalated";
                } else if ("RESOLVED".equalsIgnoreCase(c.getStatus())) {
                    statusClass = "status-resolved";
                }

                String details = "<h4>Ticket Details</h4>" +
                    "<table>" +
                    "<tr><td>Ticket ID:</td><td><b>#" + c.getId() + "</b></td></tr>" +
                    "<tr><td>Citizen Name:</td><td>" + escapeHtml(c.getName()) + "</td></tr>" +
                    "<tr><td>Ward:</td><td>" + escapeHtml(c.getWard()) + "</td></tr>" +
                    "<tr><td>Category:</td><td>" + escapeHtml(c.getCategory()) + "</td></tr>" +
                    "<tr><td>Priority:</td><td>" + c.getPriority() + " / 10</td></tr>" +
                    "<tr><td>Status:</td><td><span class=\"" + statusClass + "\">" + escapeHtml(c.getStatus()) + "</span></td></tr>" +
                    "<tr><td>Complaint Description:</td><td>" + escapeHtml(c.getText()) + "</td></tr>" +
                    "</table>";

                html = html.replace("{{error}}", "").replace("{{details}}", details);
                sendResponse(ex, 200, html, "text/html; charset=UTF-8");

            } catch (NumberFormatException e) {
                html = html.replace("{{error}}", "Please enter a valid numeric Ticket ID.")
                           .replace("{{details}}", "");
                sendResponse(ex, 200, html, "text/html; charset=UTF-8");
            } catch (ComplaintNotFoundException e) {
                // [Practical 7] Display not found error
                html = html.replace("{{error}}", "Error: " + escapeHtml(e.getMessage()))
                           .replace("{{details}}", "");
                sendResponse(ex, 200, html, "text/html; charset=UTF-8");
            }
        }
    }

    // GET /admin?cat= -> admin.html
    class AdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String query = ex.getRequestURI().getRawQuery();
            String cat = getParam(query, "cat");

            Vector<Complaint> list = null;
            if (dao != null) {
                list = dao.findByCategory(cat);
            }

            // If DB is empty or unavailable, show in-memory queue
            if (list == null || list.size() == 0) {
                list = new Vector<Complaint>();
                Vector<Complaint> q = engine.getQueue();
                synchronized (q) {
                    for (int i = 0; i < q.size(); i++) {
                        Complaint c = q.elementAt(i);
                        if (cat == null || cat.length() == 0 || "ALL".equalsIgnoreCase(cat) || c.getCategory().equalsIgnoreCase(cat)) {
                            list.addElement(c);
                        }
                    }
                }
            }

            StringBuilder rows = new StringBuilder();
            if (list.size() == 0) {
                rows.append("<tr><td colspan=\"7\">No complaints found for this filter.</td></tr>");
            } else {
                for (int i = 0; i < list.size(); i++) {
                    Complaint c = list.elementAt(i);
                    String statusClass = "status-open";
                    if ("ESCALATED".equalsIgnoreCase(c.getStatus())) {
                        statusClass = "status-escalated";
                    } else if ("RESOLVED".equalsIgnoreCase(c.getStatus())) {
                        statusClass = "status-resolved";
                    }

                    rows.append("<tr>")
                        .append("<td>#").append(c.getId()).append("</td>")
                        .append("<td>").append(escapeHtml(c.getName())).append("</td>")
                        .append("<td>").append(escapeHtml(c.getWard())).append("</td>")
                        .append("<td>").append(escapeHtml(c.getCategory())).append("</td>")
                        .append("<td>").append(c.getPriority()).append("</td>")
                        .append("<td><span class=\"").append(statusClass).append("\">").append(escapeHtml(c.getStatus())).append("</span></td>")
                        .append("<td>").append(escapeHtml(c.getPreview())).append("</td>")
                        .append("</tr>");
                }
            }

            String html = loadTemplate("admin.html");
            // [Practical 2] Showing Vector size() and capacity() on the admin page
            html = html.replace("{{queueSize}}", String.valueOf(engine.getQueue().size()))
                       .replace("{{queueCapacity}}", String.valueOf(engine.getQueue().capacity()))
                       .replace("{{complaintRows}}", rows.toString());

            sendResponse(ex, 200, html, "text/html; charset=UTF-8");
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

    // GET /style.css -> style.css
    class StyleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String css = loadTemplate("style.css");
            sendResponse(ex, 200, css, "text/css; charset=UTF-8");
        }
    }

    // Read form body from InputStream
    private String readRequestBody(HttpExchange ex) throws IOException {
        InputStream is = ex.getRequestBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len = is.read(buf)) != -1) {
            baos.write(buf, 0, len);
        }
        return new String(baos.toByteArray(), "UTF-8");
    }

    // Parse URL-encoded query or form body manually
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

    // Escape < and > to prevent HTML injection
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }

    // Load template from web/ folder
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
            return "<html><body><h3>Template loading error for " + fileName + ": " + e.getMessage() + "</h3></body></html>";
        }
    }

    // Send HTTP response
    private void sendResponse(HttpExchange ex, int status, String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes("UTF-8");
        ex.getResponseHeaders().set("Content-Type", contentType);
        ex.sendResponseHeaders(status, bytes.length);
        OutputStream os = ex.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
