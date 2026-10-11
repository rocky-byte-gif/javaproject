package medisync.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;
import medisync.model.Appointment;
import medisync.model.Bill;
import medisync.model.Doctor;
import medisync.model.MedicalRecord;
import medisync.model.Patient;

// [Practical 5] Packages: medisync.db
// [Practical 10] Data Access Object using JDBC PreparedStatement and ResultSet
public class ClinicDao {

    // [Practical 10] Save patient to database
    public boolean savePatient(Patient p) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean ok = false;
        try {
            conn = DbConnection.getConnection();
            if (conn == null) return false;
            String sql = "INSERT INTO patient (id, name, age, phone, blood_group, allergies) VALUES (?, ?, ?, ?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE name = VALUES(name), age = VALUES(age), phone = VALUES(phone), blood_group = VALUES(blood_group), allergies = VALUES(allergies)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, p.getId());
            ps.setString(2, p.getName());
            ps.setInt(3, p.getAge());
            ps.setString(4, p.getPhone());
            ps.setString(5, p.getBloodGroup());
            ps.setString(6, p.getAllergies());
            ok = ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("[DAO] savePatient error: " + e.getMessage());
        } finally {
            closeQuietly(ps, conn);
        }
        return ok;
    }

    // Delete patient by ID
    public boolean deletePatient(String id) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean ok = false;
        try {
            conn = DbConnection.getConnection();
            if (conn == null) return false;
            String sql = "DELETE FROM patient WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            ok = ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("[DAO] deletePatient error: " + e.getMessage());
        } finally {
            closeQuietly(ps, conn);
        }
        return ok;
    }

    // [Practical 10] Load patients
    public Vector<Patient> loadPatients() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        Vector<Patient> list = new Vector<Patient>();
        try {
            conn = DbConnection.getConnection();
            if (conn == null) return list;
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT id, name, age, phone, blood_group, allergies FROM patient ORDER BY id ASC");
            while (rs.next()) {
                Patient p = new Patient(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getInt("age"),
                    rs.getString("phone"),
                    rs.getString("blood_group"),
                    rs.getString("allergies")
                );
                list.addElement(p);
            }
        } catch (Exception e) {
            System.out.println("[DAO] loadPatients error: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }
        return list;
    }

    // [Practical 10] Load doctors
    public Vector<Doctor> loadDoctors() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        Vector<Doctor> list = new Vector<Doctor>();
        try {
            conn = DbConnection.getConnection();
            if (conn == null) return list;
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT id, name, specialization FROM doctor ORDER BY id ASC");
            while (rs.next()) {
                Doctor d = new Doctor(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("specialization")
                );
                list.addElement(d);
            }
        } catch (Exception e) {
            System.out.println("[DAO] loadDoctors error: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }
        return list;
    }

    // [Practical 10] Save appointment
    public boolean saveAppointment(Appointment a) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean ok = false;
        try {
            conn = DbConnection.getConnection();
            if (conn == null) return false;
            String sql = "INSERT INTO appointment (patient_id, doctor_id, slot_time, status) VALUES (?, ?, ?, ?)";
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, a.getPatientId());
            ps.setInt(2, a.getDoctorId());
            ps.setString(3, a.getSlotTime());
            ps.setString(4, a.getStatus());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                ResultSet gk = ps.getGeneratedKeys();
                if (gk.next()) {
                    a.setId(gk.getInt(1));
                }
                gk.close();
                ok = true;
            }
        } catch (Exception e) {
            System.out.println("[DAO] saveAppointment error: " + e.getMessage());
        } finally {
            closeQuietly(ps, conn);
        }
        return ok;
    }

    // Update appointment status
    public boolean updateAppointmentStatus(int id, String status) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean ok = false;
        try {
            conn = DbConnection.getConnection();
            if (conn == null) return false;
            String sql = "UPDATE appointment SET status = ? WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, id);
            ok = ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("[DAO] updateAppointmentStatus error: " + e.getMessage());
        } finally {
            closeQuietly(ps, conn);
        }
        return ok;
    }

    // Load appointments
    public Vector<Appointment> loadAppointments() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        Vector<Appointment> list = new Vector<Appointment>();
        try {
            conn = DbConnection.getConnection();
            if (conn == null) return list;
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT id, patient_id, doctor_id, slot_time, status FROM appointment ORDER BY id DESC");
            while (rs.next()) {
                Appointment a = new Appointment(
                    rs.getInt("id"),
                    rs.getString("patient_id"),
                    rs.getInt("doctor_id"),
                    rs.getString("slot_time"),
                    rs.getString("status")
                );
                list.addElement(a);
            }
        } catch (Exception e) {
            System.out.println("[DAO] loadAppointments error: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }
        return list;
    }

    // Save medical record
    public boolean saveRecord(MedicalRecord r) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean ok = false;
        try {
            conn = DbConnection.getConnection();
            if (conn == null) return false;
            String sql = "INSERT INTO record (patient_id, diagnosis, prescription, doctor_name, date_text) VALUES (?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, r.getPatientId());
            ps.setString(2, r.getDiagnosis());
            ps.setString(3, r.getPrescription());
            ps.setString(4, r.getDoctorName());
            ps.setString(5, r.getDateText());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                ResultSet gk = ps.getGeneratedKeys();
                if (gk.next()) {
                    r.setId(gk.getInt(1));
                }
                gk.close();
                ok = true;
            }
        } catch (Exception e) {
            System.out.println("[DAO] saveRecord error: " + e.getMessage());
        } finally {
            closeQuietly(ps, conn);
        }
        return ok;
    }

    // Get medical records for patient
    public Vector<MedicalRecord> getRecords(String patientId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Vector<MedicalRecord> list = new Vector<MedicalRecord>();
        try {
            conn = DbConnection.getConnection();
            if (conn == null) return list;
            String sql = "SELECT id, patient_id, diagnosis, prescription, doctor_name, date_text FROM record WHERE patient_id = ? ORDER BY id DESC";
            ps = conn.prepareStatement(sql);
            ps.setString(1, patientId);
            rs = ps.executeQuery();
            while (rs.next()) {
                MedicalRecord r = new MedicalRecord(
                    rs.getInt("id"),
                    rs.getString("patient_id"),
                    rs.getString("diagnosis"),
                    rs.getString("prescription"),
                    rs.getString("doctor_name"),
                    rs.getString("date_text")
                );
                list.addElement(r);
            }
        } catch (Exception e) {
            System.out.println("[DAO] getRecords error: " + e.getMessage());
        } finally {
            closeQuietly(rs, ps, conn);
        }
        return list;
    }

    // Save bill
    public boolean saveBill(Bill b) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean ok = false;
        try {
            conn = DbConnection.getConnection();
            if (conn == null) return false;
            String sql = "INSERT INTO bill (id, patient_id, consultation, medicine, test_fee, total, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, b.getId());
            ps.setString(2, b.getPatientId());
            ps.setDouble(3, b.getConsultation());
            ps.setDouble(4, b.getMedicine());
            ps.setDouble(5, b.getTestFee());
            ps.setDouble(6, b.getTotal());
            ps.setString(7, b.getStatus());
            ok = ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("[DAO] saveBill error: " + e.getMessage());
        } finally {
            closeQuietly(ps, conn);
        }
        return ok;
    }

    // Update bill status
    public boolean updateBillStatus(int id, String status) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean ok = false;
        try {
            conn = DbConnection.getConnection();
            if (conn == null) return false;
            String sql = "UPDATE bill SET status = ? WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, id);
            ok = ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("[DAO] updateBillStatus error: " + e.getMessage());
        } finally {
            closeQuietly(ps, conn);
        }
        return ok;
    }

    // Get all bills
    public Vector<Bill> getBills() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        Vector<Bill> list = new Vector<Bill>();
        try {
            conn = DbConnection.getConnection();
            if (conn == null) return list;
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT id, patient_id, consultation, medicine, test_fee, total, status FROM bill ORDER BY id DESC");
            while (rs.next()) {
                Bill b = new Bill(
                    rs.getInt("id"),
                    rs.getString("patient_id"),
                    rs.getDouble("consultation"),
                    rs.getDouble("medicine"),
                    rs.getDouble("test_fee"),
                    rs.getDouble("total"),
                    rs.getString("status")
                );
                list.addElement(b);
            }
        } catch (Exception e) {
            System.out.println("[DAO] getBills error: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }
        return list;
    }

    // Get bills by status filter
    public Vector<Bill> getBillsByStatus(String status) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Vector<Bill> list = new Vector<Bill>();
        try {
            conn = DbConnection.getConnection();
            if (conn == null) return list;
            String sql = "SELECT id, patient_id, consultation, medicine, test_fee, total, status FROM bill WHERE status = ? ORDER BY id DESC";
            ps = conn.prepareStatement(sql);
            ps.setString(1, status.trim().toUpperCase());
            rs = ps.executeQuery();
            while (rs.next()) {
                Bill b = new Bill(
                    rs.getInt("id"),
                    rs.getString("patient_id"),
                    rs.getDouble("consultation"),
                    rs.getDouble("medicine"),
                    rs.getDouble("test_fee"),
                    rs.getDouble("total"),
                    rs.getString("status")
                );
                list.addElement(b);
            }
        } catch (Exception e) {
            System.out.println("[DAO] getBillsByStatus error: " + e.getMessage());
        } finally {
            closeQuietly(rs, ps, conn);
        }
        return list;
    }

    // [Practical 6] Get max patient serial number to initialize Patient.counter
    public int getMaxPatientNumber() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        int max = 0;
        try {
            conn = DbConnection.getConnection();
            if (conn == null) return 0;
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT id FROM patient");
            while (rs.next()) {
                String id = rs.getString(1);
                // [Practical 9] String method: split and substring
                if (id != null && id.startsWith("MS-2026-")) {
                    String[] parts = id.split("-");
                    if (parts.length == 3) {
                        try {
                            int num = Integer.parseInt(parts[2]);
                            if (num > max) max = num;
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[DAO] getMaxPatientNumber error: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }
        return max;
    }

    // Get max bill ID
    public int getMaxBillId() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        int max = 0;
        try {
            conn = DbConnection.getConnection();
            if (conn == null) return 0;
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT MAX(id) FROM bill");
            if (rs.next()) {
                max = rs.getInt(1);
            }
        } catch (Exception e) {
            System.out.println("[DAO] getMaxBillId error: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }
        return max;
    }

    private void closeQuietly(AutoCloseable... closeables) {
        for (int i = 0; i < closeables.length; i++) {
            AutoCloseable ac = closeables[i];
            if (ac != null) {
                try {
                    ac.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
