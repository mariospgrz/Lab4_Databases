import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class ParticipantManagement {

    public static void menu() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== PARTICIPANT MANAGEMENT ===");
            System.out.println("1) Register Player in Tournament");
            System.out.println("2) List Participants for a Tournament");
            System.out.println("0) Back");
            System.out.print("Choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    register();
                    break;
                case "2":
                    listForTournament();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    public static void register() {
        Scanner sc = new Scanner(System.in);
        try (Connection c = DriverManager.getConnection(DBConnection.URL, DBConnection.USERNAME, DBConnection.PASSWORD)) {

            System.out.print("Participant ID (int): ");
            int pid = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Player ID (int): ");
            int pl = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Tournament ID (int): ");
            int tr = Integer.parseInt(sc.nextLine().trim());

            if (!exists(c, "players", "player_id", pl)) {
                System.out.println("No such player.");
                return;
            }
            if (!exists(c, "tournaments", "tournament_id", tr)) {
                System.out.println("No such tournament.");
                return;
            }

            String sql = "INSERT INTO participants (participant_id, player_id, tournament_id, registration_date) " +
                         "VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, pid);
                ps.setInt(2, pl);
                ps.setInt(3, tr);
                ps.setDate(4, Date.valueOf(LocalDate.now()));
                ps.executeUpdate();
                System.out.println("✔ Registered.");
            } catch (SQLException e) {
                // unique (player_id, tournament_id) or PK conflict
                System.out.println("Failed to register: " + e.getMessage());
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
        } catch (SQLException e) {
            System.out.println("DB error.");
            e.printStackTrace();
        }
    }

    public static void listForTournament() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Tournament ID: ");
        String in = sc.nextLine().trim();
        try {
            int tr = Integer.parseInt(in);
            String q =
                    "SELECT p.participant_id, pl.player_id, pl.username, p.registration_date " +
                    "FROM participants p " +
                    "JOIN players pl ON p.player_id = pl.player_id " +
                    "WHERE p.tournament_id = ? " +
                    "ORDER BY p.registration_date";
            try (Connection c = DriverManager.getConnection(DBConnection.URL, DBConnection.USERNAME, DBConnection.PASSWORD);
                 PreparedStatement ps = c.prepareStatement(q)) {
                ps.setInt(1, tr);
                try (ResultSet rs = ps.executeQuery()) {
                    System.out.printf("%-12s | %-10s | %-20s | %-12s%n",
                            "ParticipantID", "PlayerID", "Username", "RegDate");
                    System.out.println("----------------------------------------------------------------");
                    boolean any = false;
                    while (rs.next()) {
                        any = true;
                        System.out.printf("%-12d | %-10d | %-20s | %-12s%n",
                                rs.getInt("participant_id"),
                                rs.getInt("player_id"),
                                rs.getString("username"),
                                String.valueOf(rs.getDate("registration_date")));
                    }
                    if (!any) System.out.println("(no participants)");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid id.");
        } catch (SQLException e) {
            System.out.println("DB error.");
            e.printStackTrace();
        }
    }

    private static boolean exists(Connection c, String table, String col, int val) throws SQLException {
        String q = "SELECT COUNT(*) FROM " + table + " WHERE " + col + " = ?";
        try (PreparedStatement ps = c.prepareStatement(q)) {
            ps.setInt(1, val);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1) > 0; }
        }
    }
}
