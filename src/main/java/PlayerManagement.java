import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class PlayerManagement {

    public static void menu() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== PLAYER MANAGEMENT ===");
            System.out.println("1) Create Player");
            System.out.println("2) List All Players");
            System.out.println("3) Find Player by ID");
            System.out.println("4) Find Player by Username");
            System.out.println("5) View Tournaments for Player (JOIN)");
            System.out.println("0) Back");
            System.out.print("Choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    createPlayer();
                    break;
                case "2":
                    listPlayers();
                    break;
                case "3":
                    findById();
                    break;
                case "4":
                    findByUsername();
                    break;
                case "5":
                    tournamentsForPlayer();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    public static void createPlayer() {
        Scanner sc = new Scanner(System.in);
        try (Connection c = DriverManager.getConnection(DBConnection.URL, DBConnection.USERNAME, DBConnection.PASSWORD)) {

            System.out.print("Player ID (int): ");
            int id = Integer.parseInt(sc.nextLine().trim());

            if (playerExists(c, id)) {
                System.out.println("Error: player_id already exists.");
                return;
            }

            System.out.print("Username: ");
            String username = sc.nextLine().trim();
            System.out.print("Email: ");
            String email = sc.nextLine().trim();

            String sql = "INSERT INTO players (player_id, username, email, registration_date, total_wins, total_losses) " +
                         "VALUES (?, ?, ?, ?, 0, 0)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.setString(2, username);
                ps.setString(3, email);
                ps.setDate(4, Date.valueOf(LocalDate.now()));
                ps.executeUpdate();
                System.out.println("✔ Player created.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid id.");
        } catch (SQLException e) {
            System.out.println("DB error creating player.");
            // could be duplicate email/username (unique constraints)
            System.out.println(e.getMessage());
        }
    }

    public static void listPlayers() {
        String q = "SELECT player_id, username, email, registration_date, total_wins, total_losses FROM players ORDER BY player_id";
        try (Connection c = DriverManager.getConnection(DBConnection.URL, DBConnection.USERNAME, DBConnection.PASSWORD);
             PreparedStatement ps = c.prepareStatement(q);
             ResultSet rs = ps.executeQuery()) {

            System.out.printf("%-10s | %-20s | %-25s | %-12s | %-5s | %-6s%n",
                    "ID", "Username", "Email", "Registered", "Wins", "Losses");
            System.out.println("----------------------------------------------------------------------------");
            boolean any = false;
            while (rs.next()) {
                any = true;
                System.out.printf("%-10d | %-20s | %-25s | %-12s | %-5d | %-6d%n",
                        rs.getInt("player_id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        String.valueOf(rs.getDate("registration_date")),
                        rs.getInt("total_wins"),
                        rs.getInt("total_losses"));
            }
            if (!any) System.out.println("(no players)");
        } catch (SQLException e) {
            System.out.println("DB error.");
            e.printStackTrace();
        }
    }

    public static void findById() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Player ID: ");
        String in = sc.nextLine().trim();
        try {
            int id = Integer.parseInt(in);
            String q = "SELECT player_id, username, email, registration_date, total_wins, total_losses FROM players WHERE player_id = ?";
            try (Connection c = DriverManager.getConnection(DBConnection.URL, DBConnection.USERNAME, DBConnection.PASSWORD);
                 PreparedStatement ps = c.prepareStatement(q)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.printf("ID: %d | %s | %s | registered %s | W:%d L:%d%n",
                                rs.getInt("player_id"),
                                rs.getString("username"),
                                rs.getString("email"),
                                String.valueOf(rs.getDate("registration_date")),
                                rs.getInt("total_wins"),
                                rs.getInt("total_losses"));
                    } else System.out.println("Not found.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid id.");
        } catch (SQLException e) {
            System.out.println("DB error.");
            e.printStackTrace();
        }
    }

    public static void findByUsername() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Username: ");
        String username = sc.nextLine().trim();
        String q = "SELECT player_id, username, email, registration_date, total_wins, total_losses FROM players WHERE username = ?";
        try (Connection c = DriverManager.getConnection(DBConnection.URL, DBConnection.USERNAME, DBConnection.PASSWORD);
             PreparedStatement ps = c.prepareStatement(q)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.printf("ID: %d | %s | %s | registered %s | W:%d L:%d%n",
                            rs.getInt("player_id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            String.valueOf(rs.getDate("registration_date")),
                            rs.getInt("total_wins"),
                            rs.getInt("total_losses"));
                } else System.out.println("Not found.");
            }
        } catch (SQLException e) {
            System.out.println("DB error.");
            e.printStackTrace();
        }
    }

    public static void tournamentsForPlayer() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Player ID: ");
        String in = sc.nextLine().trim();
        try {
            int id = Integer.parseInt(in);
            String q =
                    "SELECT t.tournament_id, t.name, t.start_date, t.end_date, t.is_open " +
                    "FROM participants p " +
                    "JOIN tournaments t ON p.tournament_id = t.tournament_id " +
                    "WHERE p.player_id = ? " +
                    "ORDER BY t.start_date";
            try (Connection c = DriverManager.getConnection(DBConnection.URL, DBConnection.USERNAME, DBConnection.PASSWORD);
                 PreparedStatement ps = c.prepareStatement(q)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    System.out.printf("%-12s | %-25s | %-12s | %-12s | %-5s%n",
                            "Tourn ID", "Name", "Start", "End", "Open");
                    System.out.println("---------------------------------------------------------------");
                    boolean any = false;
                    while (rs.next()) {
                        any = true;
                        System.out.printf("%-12d | %-25s | %-12s | %-12s | %-5s%n",
                                rs.getInt("tournament_id"),
                                rs.getString("name"),
                                String.valueOf(rs.getDate("start_date")),
                                String.valueOf(rs.getDate("end_date")),
                                rs.getBoolean("is_open") ? "true" : "false");
                    }
                    if (!any) System.out.println("(no tournaments for this player)");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid id.");
        } catch (SQLException e) {
            System.out.println("DB error.");
            e.printStackTrace();
        }
    }

    private static boolean playerExists(Connection c, int id) throws SQLException {
        String q = "SELECT COUNT(*) FROM players WHERE player_id = ?";
        try (PreparedStatement ps = c.prepareStatement(q)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1) > 0; }
        }
    }
}
