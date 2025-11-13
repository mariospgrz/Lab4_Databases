import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class MatchManagement {

    public static void menu() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== MATCH MANAGEMENT ===");
            System.out.println("1) Create Match");
            System.out.println("2) List Matches (All)");
            System.out.println("3) List Matches by Tournament");
            System.out.println("4) List Matches by Player");
            System.out.println("0) Back");
            System.out.print("Choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    createMatch();
                    break;
                case "2":
                    listMatchesAll();
                    break;
                case "3":
                    listMatchesByTournament();
                    break;
                case "4":
                    listMatchesByPlayer();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    public static void createMatch() {
        Scanner sc = new Scanner(System.in);
        try (Connection c = DriverManager.getConnection(DBConnection.URL, DBConnection.USERNAME, DBConnection.PASSWORD)) {

            System.out.print("Match ID (int): ");
            int matchId = Integer.parseInt(sc.nextLine().trim());

            System.out.print("Tournament ID (int): ");
            int tId = Integer.parseInt(sc.nextLine().trim());
            if (!exists(c, "tournaments", "tournament_id", tId)) {
                System.out.println("No such tournament.");
                return;
            }

            System.out.print("Player1 ID (int): ");
            int p1 = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Player2 ID (int): ");
            int p2 = Integer.parseInt(sc.nextLine().trim());

            if (p1 == p2) {
                System.out.println("Players cannot be the same.");
                return;
            }
            if (!exists(c, "players", "player_id", p1) || !exists(c, "players", "player_id", p2)) {
                System.out.println("Player does not exist.");
                return;
            }

            // Optional: ensure both players are registered in this tournament
            if (!participantExists(c, p1, tId) || !participantExists(c, p2, tId)) {
                System.out.println("Both players must be registered in this tournament.");
                return;
            }

            System.out.print("Match date (yyyy-MM-dd): ");
            java.util.Date md = parseDate(sc.nextLine().trim());

            System.out.print("Player1 score (>=0): ");
            int s1 = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Player2 score (>=0): ");
            int s2 = Integer.parseInt(sc.nextLine().trim());

            if (s1 < 0 || s2 < 0) {
                System.out.println("Scores must be non-negative.");
                return;
            }

            Integer winner = null;
            if (s1 > s2) winner = p1;
            else if (s2 > s1) winner = p2;
            // if tie -> winner remains null (allowed by schema/check)

            String sql = "INSERT INTO matches (match_id, tournament_id, player1_id, player2_id, match_date, " +
                         "player1_score, player2_score, winner_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, matchId);
                ps.setInt(2, tId);
                ps.setInt(3, p1);
                ps.setInt(4, p2);
                ps.setDate(5, new Date(md.getTime()));
                ps.setInt(6, s1);
                ps.setInt(7, s2);
                if (winner == null) ps.setNull(8, Types.INTEGER);
                else ps.setInt(8, winner);

                ps.executeUpdate();
                
                // Update win/loss counts for players when there's a winner
                if (winner != null) {
                    // Increment winner's total_wins
                    String updateWinner = "UPDATE players SET total_wins = total_wins + 1 WHERE player_id = ?";
                    try (PreparedStatement psWinner = c.prepareStatement(updateWinner)) {
                        psWinner.setInt(1, winner);
                        psWinner.executeUpdate();
                    }
                    
                    // Increment loser's total_losses
                    int loser = (winner == p1) ? p2 : p1;
                    String updateLoser = "UPDATE players SET total_losses = total_losses + 1 WHERE player_id = ?";
                    try (PreparedStatement psLoser = c.prepareStatement(updateLoser)) {
                        psLoser.setInt(1, loser);
                        psLoser.executeUpdate();
                    }
                }
                
                System.out.println("✔ Match created.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
        } catch (ParseException e) {
            System.out.println("Invalid date. Use yyyy-MM-dd.");
        } catch (SQLException e) {
            System.out.println("DB error creating match.");
            System.out.println(e.getMessage());
        }
    }

    public static void listMatchesAll() {
        String q =
                "SELECT m.match_id, m.match_date, " +
                "p1.username AS player1, m.player1_score, " +
                "p2.username AS player2, m.player2_score, " +
                "tw.name AS tournament, w.username AS winner " +
                "FROM matches m " +
                "JOIN players p1 ON m.player1_id = p1.player_id " +
                "JOIN players p2 ON m.player2_id = p2.player_id " +
                "JOIN tournaments tw ON m.tournament_id = tw.tournament_id " +
                "LEFT JOIN players w ON m.winner_id = w.player_id " +
                "ORDER BY m.match_date";
        try (Connection c = DriverManager.getConnection(DBConnection.URL, DBConnection.USERNAME, DBConnection.PASSWORD);
             PreparedStatement ps = c.prepareStatement(q);
             ResultSet rs = ps.executeQuery()) {
            printMatchHeader();
            boolean any = false;
            while (rs.next()) {
                any = true;
                printMatchRow(rs);
            }
            if (!any) System.out.println("(no matches)");
        } catch (SQLException e) {
            System.out.println("DB error.");
            e.printStackTrace();
        }
    }

    public static void listMatchesByTournament() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Tournament ID: ");
        String in = sc.nextLine().trim();
        try {
            int tId = Integer.parseInt(in);
            String q =
                    "SELECT m.match_id, m.match_date, " +
                    "p1.username AS player1, m.player1_score, " +
                    "p2.username AS player2, m.player2_score, " +
                    "tw.name AS tournament, w.username AS winner " +
                    "FROM matches m " +
                    "JOIN players p1 ON m.player1_id = p1.player_id " +
                    "JOIN players p2 ON m.player2_id = p2.player_id " +
                    "JOIN tournaments tw ON m.tournament_id = tw.tournament_id " +
                    "LEFT JOIN players w ON m.winner_id = w.player_id " +
                    "WHERE m.tournament_id = ? " +
                    "ORDER BY m.match_date";
            try (Connection c = DriverManager.getConnection(DBConnection.URL, DBConnection.USERNAME, DBConnection.PASSWORD);
                 PreparedStatement ps = c.prepareStatement(q)) {
                ps.setInt(1, tId);
                try (ResultSet rs = ps.executeQuery()) {
                    printMatchHeader();
                    boolean any = false;
                    while (rs.next()) {
                        any = true;
                        printMatchRow(rs);
                    }
                    if (!any) System.out.println("(no matches for this tournament)");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid id.");
        } catch (SQLException e) {
            System.out.println("DB error.");
            e.printStackTrace();
        }
    }

    public static void listMatchesByPlayer() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Player ID: ");
        String in = sc.nextLine().trim();
        try {
            int pId = Integer.parseInt(in);
            String q =
                    "SELECT m.match_id, m.match_date, " +
                    "p1.username AS player1, m.player1_score, " +
                    "p2.username AS player2, m.player2_score, " +
                    "tw.name AS tournament, w.username AS winner " +
                    "FROM matches m " +
                    "JOIN players p1 ON m.player1_id = p1.player_id " +
                    "JOIN players p2 ON m.player2_id = p2.player_id " +
                    "JOIN tournaments tw ON m.tournament_id = tw.tournament_id " +
                    "LEFT JOIN players w ON m.winner_id = w.player_id " +
                    "WHERE m.player1_id = ? OR m.player2_id = ? " +
                    "ORDER BY m.match_date";
            try (Connection c = DriverManager.getConnection(DBConnection.URL, DBConnection.USERNAME, DBConnection.PASSWORD);
                 PreparedStatement ps = c.prepareStatement(q)) {
                ps.setInt(1, pId);
                ps.setInt(2, pId);
                try (ResultSet rs = ps.executeQuery()) {
                    printMatchHeader();
                    boolean any = false;
                    while (rs.next()) {
                        any = true;
                        printMatchRow(rs);
                    }
                    if (!any) System.out.println("(no matches for this player)");
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

    private static boolean participantExists(Connection c, int playerId, int tournamentId) throws SQLException {
        String q = "SELECT COUNT(*) FROM participants WHERE player_id = ? AND tournament_id = ?";
        try (PreparedStatement ps = c.prepareStatement(q)) {
            ps.setInt(1, playerId);
            ps.setInt(2, tournamentId);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1) > 0; }
        }
    }

    private static java.util.Date parseDate(String s) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        return sdf.parse(s);
    }

    private static void printMatchHeader() {
        System.out.printf("%-8s | %-12s | %-20s | %-3s | %-20s | %-3s | %-20s | %-20s%n",
                "MatchID", "Date", "Player1", "S1", "Player2", "S2", "Tournament", "Winner");
        System.out.println("------------------------------------------------------------------------------------------------------------------");
    }

    private static void printMatchRow(ResultSet rs) throws SQLException {
        System.out.printf("%-8d | %-12s | %-20s | %-3d | %-20s | %-3d | %-20s | %-20s%n",
                rs.getInt("match_id"),
                String.valueOf(rs.getDate("match_date")),
                rs.getString("player1"),
                rs.getInt("player1_score"),
                rs.getString("player2"),
                rs.getInt("player2_score"),
                rs.getString("tournament"),
                rs.getString("winner") == null ? "-" : rs.getString("winner"));
    }
}
