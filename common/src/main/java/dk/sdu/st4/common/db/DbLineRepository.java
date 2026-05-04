package dk.sdu.st4.common.db;

import java.sql.*;
import java.util.*;

public class DbLineRepository {

    // ── Lines ────────────────────────────────────────────────────────────────

    public static List<Map<String, Object>> getAllLines() {
        String sql = "SELECT id, name, product, status, cycles, success_rate, warnings " +
                     "FROM production_lines ORDER BY id";
        List<Map<String, Object>> lines = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("id");
                Map<String, Object> line = new LinkedHashMap<>();
                line.put("id",       id);
                line.put("name",     rs.getString("name"));
                line.put("product",  rs.getString("product"));
                line.put("status",   rs.getString("status"));
                line.put("cycles",   rs.getInt("cycles"));
                line.put("success",  rs.getDouble("success_rate"));
                line.put("warnings", rs.getInt("warnings"));
                line.put("machines", getMachinesForLine(id));
                line.put("operators", getEmployeesForLine(id));
                lines.add(line);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get lines", e);
        }
        return lines;
    }

    public static void createLine(String id, String name, String product,
                                  List<String> machines, List<String> operators) {
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO production_lines (id, name, product) VALUES (?, ?, ?)")) {
            ps.setString(1, id);
            ps.setString(2, name);
            ps.setString(3, product);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create line " + id, e);
        }
        setMachinesForLine(id, machines);
        setEmployeesForLine(id, operators);
    }

    public static void updateLine(String id, String name, String product,
                                  List<String> machines, List<String> operators) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE production_lines SET name = ?, product = ? WHERE id = ?")) {
            ps.setString(1, name);
            ps.setString(2, product);
            ps.setString(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update line " + id, e);
        }
        setMachinesForLine(id, machines);
        setEmployeesForLine(id, operators);
    }

    public static void updateLineStatus(String id, String status, int cycles,
                                        double success, int warnings) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE production_lines SET status=?, cycles=?, success_rate=?, warnings=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setInt(2, cycles);
            ps.setDouble(3, success);
            ps.setInt(4, warnings);
            ps.setString(5, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update line status " + id, e);
        }
    }

    public static void deleteLine(String id) {
        try (PreparedStatement ps = conn().prepareStatement(
                "DELETE FROM production_lines WHERE id = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete line " + id, e);
        }
    }

    // ── Machine assignments ──────────────────────────────────────────────────

    public static List<String> getMachinesForLine(String lineId) {
        List<String> result = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT serial_no FROM line_machines WHERE line_id = ?")) {
            ps.setString(1, lineId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(rs.getString("serial_no"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get machines for line " + lineId, e);
        }
        return result;
    }

    private static void setMachinesForLine(String lineId, List<String> serialNos) {
        try {
            try (PreparedStatement del = conn().prepareStatement(
                    "DELETE FROM line_machines WHERE line_id = ?")) {
                del.setString(1, lineId);
                del.executeUpdate();
            }
            for (String sn : serialNos) {
                try (PreparedStatement ins = conn().prepareStatement(
                        "INSERT INTO line_machines (line_id, serial_no) VALUES (?, ?)")) {
                    ins.setString(1, lineId);
                    ins.setString(2, sn);
                    ins.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set machines for line " + lineId, e);
        }
    }

    // ── Employees ────────────────────────────────────────────────────────────

    public static List<Map<String, Object>> getAllEmployees() {
        List<Map<String, Object>> emps = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT id, name, username, role, pic, since FROM employees ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> emp = new LinkedHashMap<>();
                emp.put("id",       rs.getString("id"));
                emp.put("name",     rs.getString("name"));
                emp.put("username", rs.getString("username"));
                emp.put("role",     rs.getString("role"));
                emp.put("pic",      rs.getString("pic"));
                emp.put("since",    rs.getString("since"));
                emps.add(emp);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get employees", e);
        }
        return emps;
    }

    public static void createEmployee(String id, String name, String username,
                                      String role, String pic, String since, String password) {
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO employees (id, name, username, role, pic, since, password_plain)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, id);
            ps.setString(2, name);
            ps.setString(3, username);
            ps.setString(4, role);
            ps.setString(5, pic);
            ps.setString(6, since);
            ps.setString(7, password);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create employee " + id, e);
        }
    }

    public static void updateEmployee(String id, String name, String username,
                                      String role, String pic, String password) {
        try {
            if (password != null && !password.isEmpty()) {
                try (PreparedStatement ps = conn().prepareStatement(
                        "UPDATE employees SET name=?, username=?, role=?, pic=?, password_plain=? WHERE id=?")) {
                    ps.setString(1, name); ps.setString(2, username);
                    ps.setString(3, role); ps.setString(4, pic);
                    ps.setString(5, password); ps.setString(6, id);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn().prepareStatement(
                        "UPDATE employees SET name=?, username=?, role=?, pic=? WHERE id=?")) {
                    ps.setString(1, name); ps.setString(2, username);
                    ps.setString(3, role); ps.setString(4, pic);
                    ps.setString(5, id);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update employee " + id, e);
        }
    }

    public static void deleteEmployee(String id) {
        try (PreparedStatement ps = conn().prepareStatement(
                "DELETE FROM employees WHERE id = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete employee " + id, e);
        }
    }

    // ── Employee-line assignments ────────────────────────────────────────────

    public static List<String> getEmployeesForLine(String lineId) {
        List<String> result = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT employee_id FROM line_employees WHERE line_id = ?")) {
            ps.setString(1, lineId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(rs.getString("employee_id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get employees for line " + lineId, e);
        }
        return result;
    }

    private static void setEmployeesForLine(String lineId, List<String> employeeIds) {
        try {
            try (PreparedStatement del = conn().prepareStatement(
                    "DELETE FROM line_employees WHERE line_id = ?")) {
                del.setString(1, lineId);
                del.executeUpdate();
            }
            for (String eid : employeeIds) {
                try (PreparedStatement ins = conn().prepareStatement(
                        "INSERT INTO line_employees (line_id, employee_id) VALUES (?, ?)")) {
                    ins.setString(1, lineId);
                    ins.setString(2, eid);
                    ins.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set employees for line " + lineId, e);
        }
    }

    // ── Task templates ───────────────────────────────────────────────────────

    public static List<Map<String, Object>> getTemplatesByLine(String lineId) {
        List<Map<String, Object>> result = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT id, name, seq FROM task_templates WHERE line_id = ? ORDER BY created_at")) {
            ps.setString(1, lineId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> t = new LinkedHashMap<>();
                    t.put("id",     rs.getInt("id"));
                    t.put("lineId", lineId);
                    t.put("name",   rs.getString("name"));
                    t.put("seq",    rs.getString("seq"));
                    result.add(t);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get templates for line " + lineId, e);
        }
        return result;
    }

    public static int createTemplate(String lineId, String name, String seqJson) {
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO task_templates (line_id, name, seq) VALUES (?, ?, ?) RETURNING id")) {
            ps.setString(1, lineId);
            ps.setString(2, name);
            ps.setString(3, seqJson);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create template", e);
        }
        return -1;
    }

    public static void deleteTemplate(int id) {
        try (PreparedStatement ps = conn().prepareStatement(
                "DELETE FROM task_templates WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete template " + id, e);
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private static Connection conn() {
        return DBConnection.getInstance().getConnection();
    }
}
