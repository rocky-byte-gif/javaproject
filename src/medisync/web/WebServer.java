package medisync.web;

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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import medisync.db.ClinicDao;
import medisync.exception.InvalidBillException;
import medisync.exception.InvalidPatientException;
import medisync.exception.PatientNotFoundException;
import medisync.exception.SlotNotAvailableException;
import medisync.model.Appointment;
import medisync.model.Bill;
import medisync.model.Doctor;
import medisync.model.MedicalRecord;
import medisync.model.Patient;
import medisync.service.BookingThread;
import medisync.service.ClinicService;

// [Practical 5] Packages: medisync.web
// Plain Java HTTP server using only built-in JDK classes
public class WebServer {

    private int port;
    private ClinicService service;
    private ClinicDao dao;
    private HttpServer server;

    public WebServer(int port, ClinicService service, ClinicDao dao) {
        this.port = port;
        this.service = service;
        this.dao = dao;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new DashboardHandler());
        server.createContext("/patients", new PatientsHandler());
        server.createContext("/patients/add", new AddPatientHandler());
        server.createContext("/patients/delete", new DeletePatientHandler());
        server.createContext("/patient", new PatientDetailsHandler());
        server.createContext("/records/add", new AddRecordHandler());
        server.createContext("/cancel", new CancelHandler());
        server.createContext("/doctors", new DoctorsHandler());
        server.createContext("/book", new BookHandler());
        server.createContext("/rush", new RushHandler());
        server.createContext("/bills", new BillsHandler());
        server.createContext("/bills/status", new BillStatusHandler());
        server.createContext("/concepts", new ConceptsHandler());
        server.createContext("/style.css", new StyleHandler());

        // Team member image handlers (JPG photos and SVG fallbacks)
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
            String msg = getParam(query, "msg");
            String err = getParam(query, "error");

            Vector<Doctor> docs = service.getDoctors();
            StringBuilder rows = new StringBuilder();

            synchronized (service) {
                for (int i = 0; i < docs.size(); i++) {
                    Doctor d = docs.elementAt(i);
                    boolean[] b = d.getBooked();

                    rows.append("<tr>")
                        .append("<td>#").append(d.getId()).append("</td>")
                        .append("<td><b>").append(escapeHtml(d.getName())).append("</b></td>")
                        .append("<td>").append(escapeHtml(d.getSpecialization())).append("</td>")
                        .append("<td><b>").append(d.getFreeCount()).append(" / 6</b></td>");

                    for (int s = 0; s < Doctor.SLOTS_PER_DOCTOR; s++) {
                        if (b[s]) {
                            rows.append("<td><span class=\"status-booked\">Booked</span></td>");
                        } else {
                            rows.append("<td><span class=\"status-free\">Free</span></td>");
                        }
                    }
                    rows.append("</tr>");
                }
            }

            Vector<Patient> pats = service.getPatients();
            String html = loadTemplate("dashboard.html");
            html = html.replace("{{msg}}", escapeHtml(msg))
                       .replace("{{error}}", escapeHtml(err))
                       .replace("{{summary}}", escapeHtml(service.getSummary()))
                       .replace("{{vectorInfo}}", "Size = " + pats.size() + " | Capacity = " + pats.capacity())
                       .replace("{{waitlistCount}}", String.valueOf(service.getWaitingList().size()))
                       .replace("{{doctorSlotsRows}}", rows.toString());

            sendResponse(ex, 200, html, "text/html; charset=UTF-8");
        }
    }

    // GET /patients -> patients.html
    class PatientsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String query = ex.getRequestURI().getRawQuery();
            String search = getParam(query, "search");
            String msg = getParam(query, "msg");
            String err = getParam(query, "error");

            Vector<Patient> list = service.searchPatients(search);
            StringBuilder rows = new StringBuilder();

            for (int i = 0; i < list.size(); i++) {
                Patient p = list.elementAt(i);
                rows.append("<tr>")
                    .append("<td><b>").append(p.getId()).append("</b></td>")
                    .append("<td><a href=\"/patient?id=").append(p.getId()).append("\">").append(escapeHtml(p.getDisplayName())).append("</a></td>")
                    .append("<td>").append(p.getAge()).append("</td>")
                    .append("<td>").append(escapeHtml(p.getPhone())).append("</td>")
                    .append("<td>").append(escapeHtml(p.getBloodGroup())).append("</td>")
                    .append("<td>").append(escapeHtml(p.getAllergies())).append("</td>")
                    .append("<td>").append(escapeHtml(p.getRole())).append("</td>")
                    .append("<td>")
                    .append("<form action=\"/patients/delete?id=").append(p.getId()).append("\" method=\"post\" style=\"display:inline;\">")
                    .append("<input type=\"submit\" value=\"Delete\">")
                    .append("</form>")
                    .append("</td>")
                    .append("</tr>");
            }
            if (rows.length() == 0) {
                rows.append("<tr><td colspan=\"8\">No patients found.</td></tr>");
            }

            String html = loadTemplate("patients.html");
            html = html.replace("{{msg}}", escapeHtml(msg))
                       .replace("{{error}}", escapeHtml(err))
                       .replace("{{search}}", escapeHtml(search))
                       .replace("{{patientRows}}", rows.toString());

            sendResponse(ex, 200, html, "text/html; charset=UTF-8");
        }
    }

    // POST /patients/add -> add patient
    class AddPatientHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                String body = readBody(ex);
                String name = getParam(body, "name");
                String ageStr = getParam(body, "age");
                String phone = getParam(body, "phone");
                String blood = getParam(body, "blood");
                String allergies = getParam(body, "allergies");

                try {
                    int age = Integer.parseInt(ageStr.trim());
                    Patient p = service.addPatient(name, age, phone, blood, allergies);
                    redirect(ex, "/patients?msg=Patient+" + p.getId() + "+registered+successfully.");
                } catch (InvalidPatientException e) {
                    redirect(ex, "/patients?error=" + urlEncode(e.getMessage()));
                } catch (Exception e) {
                    redirect(ex, "/patients?error=Invalid+form+data:+" + urlEncode(e.getMessage()));
                }
            } else {
                redirect(ex, "/patients");
            }
        }
    }

    // POST /patients/delete?id= -> delete patient
    class DeletePatientHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String query = ex.getRequestURI().getRawQuery();
            String id = getParam(query, "id");
            if (id != null && id.length() > 0) {
                service.removePatient(id);
                redirect(ex, "/patients?msg=Patient+" + id + "+deleted.");
            } else {
                redirect(ex, "/patients");
            }
        }
    }

    // GET /patient?id= -> patient.html
    class PatientDetailsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String query = ex.getRequestURI().getRawQuery();
            String id = getParam(query, "id");
            String msg = getParam(query, "msg");
            String err = getParam(query, "error");

            Patient p = null;
            try {
                p = service.findPatient(id);
            } catch (PatientNotFoundException e) {
                err = e.getMessage();
            }

            String html = loadTemplate("patient.html");

            if (p != null) {
                // Appointments rows
                Vector<Appointment> appts = service.getAppointments();
                StringBuilder aRows = new StringBuilder();
                for (int i = 0; i < appts.size(); i++) {
                    Appointment a = appts.elementAt(i);
                    if (a.getPatientId().equalsIgnoreCase(p.getId())) {
                        Doctor d = service.findDoctor(a.getDoctorId());
                        String docName = d != null ? d.getName() : ("Doctor #" + a.getDoctorId());
                        String stClass = "BOOKED".equalsIgnoreCase(a.getStatus()) ? "status-booked" : "status-cancelled";
                        String cancelBtn = "BOOKED".equalsIgnoreCase(a.getStatus())
                            ? "<form action=\"/cancel?id=" + a.getId() + "\" method=\"post\" style=\"display:inline;\"><input type=\"submit\" value=\"Cancel Slot\"></form>"
                            : "-";

                        aRows.append("<tr>")
                             .append("<td>#").append(a.getId()).append("</td>")
                             .append("<td>").append(escapeHtml(docName)).append("</td>")
                             .append("<td><b>").append(a.getSlotTime()).append("</b></td>")
                             .append("<td><span class=\"").append(stClass).append("\">").append(a.getStatus()).append("</span></td>")
                             .append("<td>").append(cancelBtn).append("</td>")
                             .append("</tr>");
                    }
                }
                if (aRows.length() == 0) aRows.append("<tr><td colspan=\"5\">No appointments booked.</td></tr>");

                // Records rows
                Vector<MedicalRecord> recs = dao != null ? dao.getRecords(p.getId()) : new Vector<MedicalRecord>();
                StringBuilder rRows = new StringBuilder();
                for (int i = 0; i < recs.size(); i++) {
                    MedicalRecord r = recs.elementAt(i);
                    rRows.append("<tr>")
                         .append("<td>#").append(r.getId()).append("</td>")
                         .append("<td>").append(escapeHtml(r.getDiagnosis())).append("</td>")
                         .append("<td>").append(escapeHtml(r.getPrescription())).append("</td>")
                         .append("<td>").append(escapeHtml(r.getDoctorName())).append("</td>")
                         .append("<td>").append(escapeHtml(r.getDateText())).append("</td>")
                         .append("</tr>");
                }
                if (rRows.length() == 0) rRows.append("<tr><td colspan=\"5\">No medical records on file.</td></tr>");

                // Bills rows
                Vector<Bill> bills = dao != null ? dao.getBills() : new Vector<Bill>();
                StringBuilder bRows = new StringBuilder();
                for (int i = 0; i < bills.size(); i++) {
                    Bill b = bills.elementAt(i);
                    if (b.getPatientId().equalsIgnoreCase(p.getId())) {
                        String stClass = "PAID".equalsIgnoreCase(b.getStatus()) ? "status-paid" : "PENDING".equalsIgnoreCase(b.getStatus()) ? "status-pending" : "status-cancelled";
                        bRows.append("<tr>")
                             .append("<td>#").append(b.getId()).append("</td>")
                             .append("<td>INR ").append(b.getConsultation()).append("</td>")
                             .append("<td>INR ").append(b.getMedicine()).append("</td>")
                             .append("<td>INR ").append(b.getTestFee()).append("</td>")
                             .append("<td><b>INR ").append(b.getTotal()).append("</b></td>")
                             .append("<td><span class=\"").append(stClass).append("\">").append(b.getStatus()).append("</span></td>")
                             .append("</tr>");
                    }
                }
                if (bRows.length() == 0) bRows.append("<tr><td colspan=\"6\">No invoices found.</td></tr>");

                html = html.replace("{{msg}}", escapeHtml(msg))
                           .replace("{{error}}", escapeHtml(err))
                           .replace("{{patientId}}", p.getId())
                           .replace("{{patientName}}", escapeHtml(p.getDisplayName()))
                           .replace("{{patientAge}}", String.valueOf(p.getAge()))
                           .replace("{{patientPhone}}", escapeHtml(p.getPhone()))
                           .replace("{{patientBlood}}", escapeHtml(p.getBloodGroup()))
                           .replace("{{patientAllergies}}", escapeHtml(p.getAllergies()))
                           .replace("{{patientRole}}", escapeHtml(p.getRole()))
                           .replace("{{appointmentRows}}", aRows.toString())
                           .replace("{{recordRows}}", rRows.toString())
                           .replace("{{billRows}}", bRows.toString());
            } else {
                html = html.replace("{{msg}}", "")
                           .replace("{{error}}", "Patient not found: " + escapeHtml(err))
                           .replace("{{patientId}}", id != null ? id : "")
                           .replace("{{patientName}}", "Unknown")
                           .replace("{{patientAge}}", "0")
                           .replace("{{patientPhone}}", "-")
                           .replace("{{patientBlood}}", "-")
                           .replace("{{patientAllergies}}", "-")
                           .replace("{{patientRole}}", "-")
                           .replace("{{appointmentRows}}", "")
                           .replace("{{recordRows}}", "")
                           .replace("{{billRows}}", "");
            }

            sendResponse(ex, 200, html, "text/html; charset=UTF-8");
        }
    }

    // POST /records/add -> add medical record
    class AddRecordHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                String body = readBody(ex);
                String pid = getParam(body, "patient_id");
                String diag = getParam(body, "diagnosis");
                String presc = getParam(body, "prescription");
                String doc = getParam(body, "doctor_name");
                String now = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

                MedicalRecord r = new MedicalRecord(0, pid, diag, presc, doc, now);
                service.addRecord(r);
                redirect(ex, "/patient?id=" + pid + "&msg=Medical+record+added.");
            } else {
                redirect(ex, "/patients");
            }
        }
    }

    // POST /cancel?id= -> cancel appointment
    class CancelHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String query = ex.getRequestURI().getRawQuery();
            String idStr = getParam(query, "id");
            try {
                int aid = Integer.parseInt(idStr);
                service.cancelAppointment(aid);
                redirect(ex, "/doctors?msg=Appointment+cancelled+and+slot+freed.");
            } catch (Exception e) {
                redirect(ex, "/doctors?error=Cancel+failed:+" + urlEncode(e.getMessage()));
            }
        }
    }

    // GET /doctors -> doctors.html
    class DoctorsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String query = ex.getRequestURI().getRawQuery();
            String msg = getParam(query, "msg");
            String err = getParam(query, "error");

            Vector<Doctor> docs = service.getDoctors();
            StringBuilder dRows = new StringBuilder();
            StringBuilder options = new StringBuilder();

            synchronized (service) {
                for (int i = 0; i < docs.size(); i++) {
                    Doctor d = docs.elementAt(i);
                    boolean[] b = d.getBooked();

                    options.append("<option value=\"").append(d.getId()).append("\">")
                           .append(escapeHtml(d.getName())).append(" (").append(escapeHtml(d.getSpecialization()))
                           .append(") - ").append(d.getFreeCount()).append(" slots free</option>");

                    dRows.append("<tr>")
                         .append("<td><b>").append(escapeHtml(d.getName())).append("</b></td>")
                         .append("<td>").append(escapeHtml(d.getSpecialization())).append("</td>")
                         .append("<td><b>").append(d.getFreeCount()).append(" / 6</b></td>");

                    for (int s = 0; s < Doctor.SLOTS_PER_DOCTOR; s++) {
                        if (b[s]) {
                            dRows.append("<td><span class=\"status-booked\">Booked</span></td>");
                        } else {
                            dRows.append("<td><span class=\"status-free\">Free</span></td>");
                        }
                    }
                    dRows.append("</tr>");
                }
            }

            String html = loadTemplate("doctors.html");
            html = html.replace("{{msg}}", escapeHtml(msg))
                       .replace("{{error}}", escapeHtml(err))
                       .replace("{{doctorRows}}", dRows.toString())
                       .replace("{{doctorOptions}}", options.toString());

            sendResponse(ex, 200, html, "text/html; charset=UTF-8");
        }
    }

    // POST /book -> book slot
    class BookHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                String body = readBody(ex);
                String pid = getParam(body, "patient_id");
                String didStr = getParam(body, "doctor_id");
                String emergStr = getParam(body, "emergency");
                boolean emergency = "true".equalsIgnoreCase(emergStr);

                try {
                    int did = Integer.parseInt(didStr.trim());
                    Appointment a = service.bookAppointment(pid, did, emergency);
                    redirect(ex, "/doctors?msg=Booked+slot+" + a.getSlotTime() + "+for+patient+" + pid);
                } catch (PatientNotFoundException e) {
                    redirect(ex, "/doctors?error=" + urlEncode(e.getMessage()));
                } catch (SlotNotAvailableException e) {
                    redirect(ex, "/doctors?error=" + urlEncode(e.getMessage()));
                } catch (Exception e) {
                    redirect(ex, "/doctors?error=Booking+error:+" + urlEncode(e.getMessage()));
                }
            } else {
                redirect(ex, "/doctors");
            }
        }
    }

    // POST /rush -> run 8-thread rush booking test
    class RushHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String didStr = "1";
            if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                String body = readBody(ex);
                didStr = getParam(body, "doctor_id");
            }

            int did = 1;
            try {
                did = Integer.parseInt(didStr.trim());
            } catch (Exception ignored) {
            }

            Doctor targetDoc = service.findDoctor(did);
            String docName = targetDoc != null ? targetDoc.getName() : ("Doctor #" + did);

            // Ensure at least 8 patients exist for the test
            Vector<Patient> testPatients = new Vector<Patient>();
            Vector<Patient> currentPats = service.getPatients();
            for (int i = 0; i < currentPats.size() && testPatients.size() < 8; i++) {
                testPatients.addElement(currentPats.elementAt(i));
            }
            while (testPatients.size() < 8) {
                try {
                    Patient demo = service.addPatient("Demo Patient " + (testPatients.size() + 1), 30, "980000000" + testPatients.size());
                    testPatients.addElement(demo);
                } catch (Exception e) {
                    break;
                }
            }

            // [Practical 8] Start 8 BookingThread objects simultaneously
            Vector<String> logList = new Vector<String>();
            BookingThread[] threads = new BookingThread[8];
            for (int i = 0; i < 8; i++) {
                Patient p = testPatients.elementAt(i);
                threads[i] = new BookingThread(p, did, service, logList);
                threads[i].start();
            }

            // [Practical 8] Use join() to wait for all 8 threads to finish
            for (int i = 0; i < 8; i++) {
                try {
                    threads[i].join();
                } catch (InterruptedException e) {
                    System.out.println("[Rush] Thread join interrupted: " + e.getMessage());
                }
            }

            // Verify double bookings
            int doubleBookings = 0;
            Vector<Appointment> appts = service.getAppointments();
            String[] times = targetDoc != null ? targetDoc.getSlotTimes() : new String[0];
            for (int i = 0; i < times.length; i++) {
                int countForSlot = 0;
                for (int j = 0; j < appts.size(); j++) {
                    Appointment a = appts.elementAt(j);
                    if (a.getDoctorId() == did && "BOOKED".equalsIgnoreCase(a.getStatus()) && times[i].equalsIgnoreCase(a.getSlotTime())) {
                        countForSlot++;
                    }
                }
                if (countForSlot > 1) {
                    doubleBookings += (countForSlot - 1);
                }
            }

            StringBuilder logRows = new StringBuilder();
            for (int i = 0; i < logList.size(); i++) {
                String line = logList.elementAt(i);
                String outcome = line.contains("BOOKED") ? "<span class=\"status-free\">SUCCESS</span>" : "<span class=\"status-pending\">WAITLISTED</span>";
                logRows.append("<tr>")
                       .append("<td>Thread ").append(i + 1).append("</td>")
                       .append("<td>").append(escapeHtml(line)).append("</td>")
                       .append("<td>").append(outcome).append("</td>")
                       .append("</tr>");
            }

            String html = loadTemplate("rush.html");
            html = html.replace("{{targetDoctor}}", escapeHtml(docName))
                       .replace("{{doubleBookingCheck}}", "Double bookings found: " + doubleBookings)
                       .replace("{{threadLogRows}}", logRows.toString());

            sendResponse(ex, 200, html, "text/html; charset=UTF-8");
        }
    }

    // GET /bills and POST /bills
    class BillsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                String query = ex.getRequestURI().getRawQuery();
                String status = getParam(query, "status");
                String msg = getParam(query, "msg");
                String err = getParam(query, "error");

                Vector<Bill> list;
                if (status != null && status.length() > 0 && !"ALL".equalsIgnoreCase(status) && dao != null) {
                    list = dao.getBillsByStatus(status);
                } else if (dao != null) {
                    list = dao.getBills();
                } else {
                    list = new Vector<Bill>();
                }

                StringBuilder rows = new StringBuilder();
                for (int i = 0; i < list.size(); i++) {
                    Bill b = list.elementAt(i);
                    String stClass = "PAID".equalsIgnoreCase(b.getStatus()) ? "status-paid" : "PENDING".equalsIgnoreCase(b.getStatus()) ? "status-pending" : "status-cancelled";
                    String actions = "-";
                    if ("PENDING".equalsIgnoreCase(b.getStatus())) {
                        actions = "<form action=\"/bills/status?id=" + b.getId() + "&status=PAID\" method=\"post\" style=\"display:inline;\"><input type=\"submit\" value=\"Mark Paid\"></form> " +
                                  "<form action=\"/bills/status?id=" + b.getId() + "&status=CANCELLED\" method=\"post\" style=\"display:inline;\"><input type=\"submit\" value=\"Cancel\"></form>";
                    }

                    rows.append("<tr>")
                        .append("<td>#").append(b.getId()).append("</td>")
                        .append("<td><a href=\"/patient?id=").append(b.getPatientId()).append("\">").append(escapeHtml(b.getPatientId())).append("</a></td>")
                        .append("<td>INR ").append(b.getConsultation()).append("</td>")
                        .append("<td>INR ").append(b.getMedicine()).append("</td>")
                        .append("<td>INR ").append(b.getTestFee()).append("</td>")
                        .append("<td><b>INR ").append(b.getTotal()).append("</b></td>")
                        .append("<td><span class=\"").append(stClass).append("\">").append(b.getStatus()).append("</span></td>")
                        .append("<td>").append(actions).append("</td>")
                        .append("</tr>");
                }
                if (rows.length() == 0) {
                    rows.append("<tr><td colspan=\"8\">No bills found for this filter.</td></tr>");
                }

                String html = loadTemplate("bills.html");
                html = html.replace("{{msg}}", escapeHtml(msg))
                           .replace("{{error}}", escapeHtml(err))
                           .replace("{{billRows}}", rows.toString());

                sendResponse(ex, 200, html, "text/html; charset=UTF-8");
                return;
            }

            if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                String body = readBody(ex);
                String pid = getParam(body, "patient_id");
                String cStr = getParam(body, "consultation");
                String mStr = getParam(body, "medicine");
                String tStr = getParam(body, "test_fee");

                try {
                    double consultation = Double.parseDouble(cStr);
                    double medicine = Double.parseDouble(mStr);
                    double testFee = Double.parseDouble(tStr);

                    // [Practical 6] Creating bill using Bill.Builder method chaining
                    Bill bill = new Bill.Builder()
                        .patientId(pid)
                        .consultation(consultation)
                        .medicine(medicine)
                        .testFee(testFee)
                        .build();

                    service.addBill(bill);
                    redirect(ex, "/bills?msg=Bill+%23" + bill.getId() + "+generated+successfully.");
                } catch (InvalidBillException e) {
                    // [Practical 7] Custom exception catch
                    redirect(ex, "/bills?error=" + urlEncode(e.getMessage()));
                } catch (Exception e) {
                    redirect(ex, "/bills?error=Invalid+bill+input:+" + urlEncode(e.getMessage()));
                }
            }
        }
    }

    // POST /bills/status?id=&status= -> update bill status
    class BillStatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String query = ex.getRequestURI().getRawQuery();
            String idStr = getParam(query, "id");
            String status = getParam(query, "status");
            try {
                int id = Integer.parseInt(idStr);
                service.updateBillStatus(id, status);
                redirect(ex, "/bills?msg=Bill+%23" + id + "+marked+as+" + status);
            } catch (Exception e) {
                redirect(ex, "/bills?error=Failed+to+update+bill:+" + urlEncode(e.getMessage()));
            }
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

    // Helper: Parse URL parameter
    private String getParam(String data, String key) {
        if (data == null || key == null) return "";
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

    // Helper: Escape HTML
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }

    // Helper: URL encode
    private String urlEncode(String text) {
        if (text == null) return "";
        try {
            return java.net.URLEncoder.encode(text, "UTF-8");
        } catch (Exception e) {
            return text.replace(" ", "+");
        }
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
