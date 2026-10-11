package grievance.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;
import grievance.model.Complaint;

// [Practical 5] Packages: grievance.db
// [Practical 10] Data Access Object using JDBC, PreparedStatement, and ResultSet
public class ComplaintDao {

    // [Practical 10] Save complaint to MySQL
    public boolean save(Complaint c) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean success = false;
        try {
            conn = DbConnection.getConnection();
            if (conn == null) {
                return false;
            }
            String sql = "INSERT INTO complaint (id, name, text, ward, category, priority, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, c.getId());
            ps.setString(2, c.getName());
            ps.setString(3, c.getText());
            ps.setString(4, c.getWard());
            ps.setString(5, c.getCategory());
            ps.setInt(6, c.getPriority());
            ps.setString(7, c.getStatus());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                success = true;
            }
        } catch (Exception e) {
            System.out.println("[DAO] Save error: " + e.getMessage());
        } finally {
            closeQuietly(ps, conn);
        }
        return success;
    }

    // [Practical 10] Find complaint by primary key id
    public Complaint findById(int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Complaint c = null;
        try {
            conn = DbConnection.getConnection();
            if (conn == null) {
                return null;
            }
            String sql = "SELECT id, name, text, ward, category, priority, status FROM complaint WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                c = new Complaint(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("text"),
                    rs.getString("ward"),
                    rs.getString("category"),
                    rs.getInt("priority"),
                    rs.getString("status")
                );
            }
        } catch (Exception e) {
            System.out.println("[DAO] findById error: " + e.getMessage());
        } finally {
            closeQuietly(rs, ps, conn);
        }
        return c;
    }

    // [Practical 10] Filter complaints by category using Vector<Complaint>
    public Vector<Complaint> findByCategory(String cat) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Vector<Complaint> list = new Vector<Complaint>();
        try {
            conn = DbConnection.getConnection();
            if (conn == null) {
                return list;
            }
            String sql;
            if (cat == null || cat.trim().length() == 0 || "ALL".equalsIgnoreCase(cat)) {
                sql = "SELECT id, name, text, ward, category, priority, status FROM complaint ORDER BY id DESC";
                ps = conn.prepareStatement(sql);
            } else {
                sql = "SELECT id, name, text, ward, category, priority, status FROM complaint WHERE category = ? ORDER BY id DESC";
                ps = conn.prepareStatement(sql);
                ps.setString(1, cat.trim().toUpperCase());
            }

            rs = ps.executeQuery();
            while (rs.next()) {
                Complaint c = new Complaint(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("text"),
                    rs.getString("ward"),
                    rs.getString("category"),
                    rs.getInt("priority"),
                    rs.getString("status")
                );
                list.addElement(c);
            }
        } catch (Exception e) {
            System.out.println("[DAO] findByCategory error: " + e.getMessage());
        } finally {
            closeQuietly(rs, ps, conn);
        }
        return list;
    }

    // Update status for SLA or Resolver
    public boolean updateStatus(int id, String status) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean ok = false;
        try {
            conn = DbConnection.getConnection();
            if (conn == null) {
                return false;
            }
            String sql = "UPDATE complaint SET status = ? WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, id);
            ok = (ps.executeUpdate() > 0);
        } catch (Exception e) {
            System.out.println("[DAO] updateStatus error: " + e.getMessage());
        } finally {
            closeQuietly(ps, conn);
        }
        return ok;
    }

    // [Practical 6] Get max id from table to initialize Complaint.counter
    public int getMaxId() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        int max = 100;
        try {
            conn = DbConnection.getConnection();
            if (conn == null) {
                return max;
            }
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT MAX(id) FROM complaint");
            if (rs.next()) {
                int val = rs.getInt(1);
                if (val > max) {
                    max = val;
                }
            }
        } catch (Exception e) {
            System.out.println("[DAO] getMaxId error: " + e.getMessage());
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
