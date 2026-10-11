package coldtrack.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;
import coldtrack.model.Alert;
import coldtrack.model.FoodTruck;
import coldtrack.model.FrozenTruck;
import coldtrack.model.Reading;
import coldtrack.model.Truck;
import coldtrack.model.VaccineTruck;

// [Practical 5] Packages: coldtrack.db
// [Practical 10] Data Access Object using JDBC PreparedStatement and ResultSet
public class ColdTrackDao {

    // [Practical 10] Save a truck into database
    public boolean saveTruck(Truck t) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean ok = false;
        try {
            conn = DbConnection.getConnection();
            if (conn == null) {
                return false;
            }
            String sql = "INSERT INTO truck (id, name, cargo, min_temp, max_temp) VALUES (?, ?, ?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE name = VALUES(name), cargo = VALUES(cargo), min_temp = VALUES(min_temp), max_temp = VALUES(max_temp)";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, t.getId());
            ps.setString(2, t.getName());
            ps.setString(3, t.getCargo());
            ps.setDouble(4, t.getMinTemp());
            ps.setDouble(5, t.getMaxTemp());
            ok = ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("[DAO] saveTruck error: " + e.getMessage());
        } finally {
            closeQuietly(ps, conn);
        }
        return ok;
    }

    // [Practical 10] Delete a truck by ID
    public boolean deleteTruck(int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean ok = false;
        try {
            conn = DbConnection.getConnection();
            if (conn == null) {
                return false;
            }
            String sql = "DELETE FROM truck WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ok = ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("[DAO] deleteTruck error: " + e.getMessage());
        } finally {
            closeQuietly(ps, conn);
        }
        return ok;
    }

    // [Practical 10] Load all trucks from database
    public Vector<Truck> loadTrucks() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        Vector<Truck> list = new Vector<Truck>();
        try {
            conn = DbConnection.getConnection();
            if (conn == null) {
                return list;
            }
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT id, name, cargo, min_temp, max_temp FROM truck ORDER BY id ASC");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String cargo = rs.getString("cargo");
                double min = rs.getDouble("min_temp");
                double max = rs.getDouble("max_temp");

                Truck t;
                // [Practical 1] Switch statement based on cargo type
                if ("vaccine".equalsIgnoreCase(cargo)) {
                    t = new VaccineTruck(id, name, min, max);
                } else if ("frozen".equalsIgnoreCase(cargo)) {
                    t = new FrozenTruck(id, name, min, max);
                } else {
                    t = new FoodTruck(id, name, min, max);
                }
                list.addElement(t);
            }
        } catch (Exception e) {
            System.out.println("[DAO] loadTrucks error: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }
        return list;
    }

    // [Practical 10] Save sensor reading
    public boolean saveReading(Reading r) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean ok = false;
        try {
            conn = DbConnection.getConnection();
            if (conn == null) {
                return false;
            }
            String sql = "INSERT INTO reading (truck_id, temperature, humidity, status, time_text) VALUES (?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, r.getTruckId());
            ps.setDouble(2, r.getTemperature());
            ps.setDouble(3, r.getHumidity());
            ps.setString(4, r.getStatus());
            ps.setString(5, r.getTimeText());
            ok = ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("[DAO] saveReading error: " + e.getMessage());
        } finally {
            closeQuietly(ps, conn);
        }
        return ok;
    }

    // [Practical 10] Get latest readings for a truck
    public Vector<Reading> getReadings(int truckId, int limit) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Vector<Reading> list = new Vector<Reading>();
        try {
            conn = DbConnection.getConnection();
            if (conn == null) {
                return list;
            }
            String sql = "SELECT id, truck_id, temperature, humidity, status, time_text FROM reading WHERE truck_id = ? ORDER BY id DESC LIMIT ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, truckId);
            ps.setInt(2, limit);
            rs = ps.executeQuery();
            while (rs.next()) {
                Reading r = new Reading(
                    rs.getInt("id"),
                    rs.getInt("truck_id"),
                    rs.getDouble("temperature"),
                    rs.getDouble("humidity"),
                    rs.getString("status"),
                    rs.getString("time_text")
                );
                list.addElement(r);
            }
        } catch (Exception e) {
            System.out.println("[DAO] getReadings error: " + e.getMessage());
        } finally {
            closeQuietly(rs, ps, conn);
        }
        return list;
    }

    // [Practical 10] Save alert record
    public boolean saveAlert(Alert a) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean ok = false;
        try {
            conn = DbConnection.getConnection();
            if (conn == null) {
                return false;
            }
            String sql = "INSERT INTO alert (id, truck_id, message, temperature, time_text) VALUES (?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, a.getId());
            ps.setInt(2, a.getTruckId());
            ps.setString(3, a.getMessage());
            ps.setDouble(4, a.getTemperature());
            ps.setString(5, a.getTimeText());
            ok = ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("[DAO] saveAlert error: " + e.getMessage());
        } finally {
            closeQuietly(ps, conn);
        }
        return ok;
    }

    // [Practical 10] Load all alerts
    public Vector<Alert> getAlerts() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        Vector<Alert> list = new Vector<Alert>();
        try {
            conn = DbConnection.getConnection();
            if (conn == null) {
                return list;
            }
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT id, truck_id, message, temperature, time_text FROM alert ORDER BY id DESC");
            while (rs.next()) {
                Alert a = new Alert(
                    rs.getInt("id"),
                    rs.getInt("truck_id"),
                    rs.getString("message"),
                    rs.getDouble("temperature"),
                    rs.getString("time_text")
                );
                list.addElement(a);
            }
        } catch (Exception e) {
            System.out.println("[DAO] getAlerts error: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }
        return list;
    }

    // [Practical 10] Filter alerts by truck ID
    public Vector<Alert> getAlertsByTruck(int truckId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Vector<Alert> list = new Vector<Alert>();
        try {
            conn = DbConnection.getConnection();
            if (conn == null) {
                return list;
            }
            String sql = "SELECT id, truck_id, message, temperature, time_text FROM alert WHERE truck_id = ? ORDER BY id DESC";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, truckId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Alert a = new Alert(
                    rs.getInt("id"),
                    rs.getInt("truck_id"),
                    rs.getString("message"),
                    rs.getDouble("temperature"),
                    rs.getString("time_text")
                );
                list.addElement(a);
            }
        } catch (Exception e) {
            System.out.println("[DAO] getAlertsByTruck error: " + e.getMessage());
        } finally {
            closeQuietly(rs, ps, conn);
        }
        return list;
    }

    // [Practical 6] Get maximum alert ID for counter initialization
    public int getMaxAlertId() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        int max = 0;
        try {
            conn = DbConnection.getConnection();
            if (conn == null) {
                return 0;
            }
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT MAX(id) FROM alert");
            if (rs.next()) {
                max = rs.getInt(1);
            }
        } catch (Exception e) {
            System.out.println("[DAO] getMaxAlertId error: " + e.getMessage());
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
