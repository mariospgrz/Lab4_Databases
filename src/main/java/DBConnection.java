/* Main entry + shared DB config + top-level menu */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class DBConnection {

    // ====== EDIT THESE IF NEEDED ======
    public static final String URL = "jdbc:mysql://localhost:3306/lab4_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "user@mysql";
    // ==================================

    public static void main(String[] args) {
        // quick connection test
        try (Connection ignored = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            System.out.println("Connected successfully!");
        } catch (SQLException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();
        }

        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== MAIN MENU ===");
            System.out.println("1) Manage Players");
            System.out.println("2) Manage Tournaments");
            System.out.println("3) Manage Participants");
            System.out.println("4) Manage Matches");
            System.out.println("0) Exit");
            System.out.print("Choice: ");
            String choice = input.nextLine().trim();

            switch (choice) {
                case "1":
                    PlayerManagement.menu();
                    break;
                case "2":
                    TournamentManagement.tournamentMenu();
                    break;
                case "3":
                    ParticipantManagement.menu();
                    break;
                case "4":
                    MatchManagement.menu();
                    break;
                case "0":
                    System.out.println("Bye!");
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
}
