import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Scanner;

public class TournamentManagement {

    public static void tournamentMenu() {
        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== TOURNAMENT MANAGEMENT ===");
            System.out.println("1) Create Tournament");
            System.out.println("2) Display All Tournaments");
            System.out.println("3) View Tournament by ID");
            System.out.println("4) View Tournaments by Status (is_open)");
            System.out.println("5) View Players Participating (JOIN)");
            System.out.println("6) View Tournament by Name");
            System.out.println("0) Back");
            System.out.print("Choice: ");
            String choice = input.nextLine().trim();

            switch (choice) {
                case "1":
                    createTournament();
                    break;
                case "2":
                    displayTournaments();
                    break;
                case "3":
                    selectTournamentByID();
                    break;
                case "4":
                    viewByStatus();
                    break;
                case "5":
                    playersParticipating();
                    break;
                case "6":
                    selectTournamentByName();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    public static void createTournament() {
        Scanner sc = new Scanner(System.in);
        try (Connection connection = DriverManager.getConnection(DBConnection.URL, DBConnection.USERNAME, DBConnection.PASSWORD)) {

            System.out.print("Enter tournament ID (int): ");
            int tournamentID = Integer.parseInt(sc.nextLine().trim());

            if (tournamentIDExists(connection, tournamentID)) {
                System.out.println("Error: Tournament ID already exists.");
                return;
            }

            System.out.print("Enter tournament name: ");
            String name = sc.nextLine().trim();

            System.out.print("Enter start date (yyyy-MM-dd): ");
            java.util.Date start = parseDate(sc.nextLine().trim());

            System.out.print("Enter end date (yyyy-MM-dd): ");
            java.util.Date end = parseDate(sc.nextLine().trim());

            System.out.print("Enter max participants (int): ");
            int max = Integer.parseInt(sc.nextLine().trim());

            // business: is_open false if end date before today
            boolean isOpen = !end.before(java.sql.Date.valueOf(LocalDate.now()));

            String sql = "INSERT INTO tournaments (tournament_id, name, start_date, end_date, max_participants, is_open) " +
                         "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, tournamentID);
                ps.setString(2, name);
                ps.setDate(3, new Date(start.getTime()));
                ps.setDate(4, new Date(end.getTime()));
                ps.setInt(5, max);
                ps.setBoolean(6, isOpen);
                ps.executeUpdate();
                System.out.println("✔ Tournament created.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
        } catch (ParseException e) {
            System.out.println("Invalid date. Use yyyy-MM-dd.");
        } catch (SQLException e) {
            System.out.println("DB error creating tournament.");
            e.printStackTrace();
        }
    }

    private static boolean tournamentIDExists(Connection connection, int id) throws SQLException {
        String q = "SELECT COUNT(*) FROM tournaments WHERE tournament_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(q)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public static void displayTournaments() {
        String q = "SELECT tournament_id, name, start_date, end_date, max_participants, is_open FROM tournaments ORDER BY start_date";
        try (Connection c = DriverManager.getConnection(DBConnection.URL, DBConnection.USERNAME, DBConnection.PASSWORD);
             PreparedStatement ps = c.prepareStatement(q);
             ResultSet rs = ps.executeQuery()) {

            printTournamentHeader();
            boolean any = false;
            while (rs.next()) {
                any = true;
                printTournamentRow(rs);
            }
            if (!any) System.out.println("(no tournaments)");

        } catch (SQLException e) {
            System.out.println("DB error listing tournaments.");
            e.printStackTrace();
        }
    }

    public static void selectTournamentByID() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter tournament ID: ");
        String in = sc.nextLine().trim();
        try {
            int id = Integer.parseInt(in);
            String q = "SELECT tournament_id, name, start_date, end_date, max_participants, is_open FROM tournaments WHERE tournament_id = ?";
            try (Connection c = DriverManager.getConnection(DBConnection.URL, DBConnection.USERNAME, DBConnection.PASSWORD);
                 PreparedStatement ps = c.prepareStatement(q)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    printTournamentHeader();
                    if (rs.next()) printTournamentRow(rs);
                    else System.out.println("Not found.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
        } catch (SQLException e) {
            System.out.println("DB error.");
            e.printStackTrace();
        }
    }

    public static void selectTournamentByName() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter exact tournament name: ");
        String name = sc.nextLine().trim();
        String q = "SELECT tournament_id, name, start_date, end_date, max_participants, is_open FROM tournaments WHERE name = ?";
        try (Connection c = DriverManager.getConnection(DBConnection.URL, DBConnection.USERNAME, DBConnection.PASSWORD);
             PreparedStatement ps = c.prepareStatement(q)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                printTournamentHeader();
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    printTournamentRow(rs);
                }
                if (!any) System.out.println("No tournaments with that name.");
            }
        } catch (SQLException e) {
            System.out.println("DB error.");
            e.printStackTrace();
        }
    }

    public static void viewByStatus() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Status (true=open / false=closed): ");
        String in = sc.nextLine().trim();
        boolean status = Boolean.parseBoolean(in);

        String q = "SELECT tournament_id, name, start_date, end_date, max_participants, is_open FROM tournaments WHERE is_open = ?";
        try (Connection c = DriverManager.getConnection(DBConnection.URL, DBConnection.USERNAME, DBConnection.PASSWORD);
             PreparedStatement ps = c.prepareStatement(q)) {
            ps.setBoolean(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                printTournamentHeader();
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    printTournamentRow(rs);
                }
                if (!any) System.out.println("(no tournaments with status " + status + ")");
            }
        } catch (SQLException e) {
            System.out.println("DB error.");
            e.printStackTrace();
        }
    }

    public static void playersParticipating() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Tournament ID: ");
        String in = sc.nextLine().trim();
        try {
            int tid = Integer.parseInt(in);
            String q =
                    "SELECT p.player_id, p.username " +
                    "FROM participants par " +
                    "JOIN players p ON par.player_id = p.player_id " +
                    "WHERE par.tournament_id = ? " +
                    "ORDER BY p.username";

            try (Connection c = DriverManager.getConnection(DBConnection.URL, DBConnection.USERNAME, DBConnection.PASSWORD);
                 PreparedStatement ps = c.prepareStatement(q)) {
                ps.setInt(1, tid);
                try (ResultSet rs = ps.executeQuery()) {
                    System.out.printf("%-10s | %-20s%n", "Player ID", "Username");
                    System.out.println("----------------------------------");
                    boolean any = false;
                    while (rs.next()) {
                        any = true;
                        System.out.printf("%-10d | %-20s%n",
                                rs.getInt("player_id"),
                                rs.getString("username"));
                    }
                    if (!any) System.out.println("(no players for that tournament)");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
        } catch (SQLException e) {
            System.out.println("DB error.");
            e.printStackTrace();
        }
    }

    // helpers
    private static java.util.Date parseDate(String s) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        return sdf.parse(s);
    }

    private static void printTournamentHeader() {
        System.out.printf("%-12s | %-25s | %-12s | %-12s | %-5s | %-5s%n",
                "ID", "Name", "Start Date", "End Date", "Max", "Open");
        System.out.println("----------------------------------------------------------------------------");
    }

    private static void printTournamentRow(ResultSet rs) throws SQLException {
        System.out.printf("%-12d | %-25s | %-12s | %-12s | %-5d | %-5s%n",
                rs.getInt("tournament_id"),
                rs.getString("name"),
                String.valueOf(rs.getDate("start_date")),
                String.valueOf(rs.getDate("end_date")),
                rs.getInt("max_participants"),
                rs.getBoolean("is_open") ? "true" : "false");
    }
}
