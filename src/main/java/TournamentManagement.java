import java.util.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TournamentManagement {
    private static final String URL = "jdbc:mysql://localhost:3306/lab4_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "user@mysql";

    public static void CreateTournament() {
try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD); Statement statement = connection.createStatement()) {
        String tournamentName;
        Integer tournamentID;
        java.util.Date start_date;
        java.util.Date end_date;
        Integer max_participants;
        //Create a new tourament
        System.out.println("Enter the attributes of the tournament");
        Scanner scanner = new Scanner(System.in);
        try {
            System.err.println("Enter tournament name:");
            tournamentName = scanner.nextLine();
            System.err.println("Enter tournament ID:");
            tournamentID = Integer.parseInt(scanner.nextLine());
            System.err.println("Enter start date (yyyy-MM-dd):");
            start_date = new SimpleDateFormat("yyyy-MM-dd").parse(scanner.nextLine());
            System.err.println("Enter end date (yyyy-MM-dd):");
            end_date = new SimpleDateFormat("yyyy-MM-dd").parse(scanner.nextLine());
            System.err.println("Enter max participants:");
            max_participants = Integer.parseInt(scanner.nextLine());
            // Check if tournament ID already exists
            if (tournamentIDExists(tournamentID)) {
                System.out.println("Error: Tournament ID " + tournamentID + " is already being used!");
                return;
            }
            // Insert the new tournament into the database
            statement.executeUpdate("INSERT INTO tournaments (tournament_id, tournament_name, start_date, end_date, max_participants) VALUES (" + tournamentID + ", '" + tournamentName + "', '" + new java.sql.Date(start_date.getTime()) + "', '" + new java.sql.Date(end_date.getTime()) + "', " + max_participants + ")");
        } catch (Exception e) {
            System.out.println("Invalid input. Try again.");
            return;
        }
        System.out.println("Tournament " + " created successfully!");
    }catch (SQLException e) {
        System.out.println("Database error occurred!");
        e.printStackTrace();
}}
    

    public static boolean tournamentIDExists(int tournamentID) {
        String query = "SELECT COUNT(*) FROM tournaments WHERE tournament_id = ?";
        
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            preparedStatement.setInt(1, tournamentID);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0; // Returns true if ID exists, false if it doesn't
            }
            
        } catch (SQLException e) {
            System.out.println("Error checking tournament ID in database!");
            e.printStackTrace();
        }
        return false;
    }

    public static void DisplayTournaments() {
        String query = "SELECT * FROM tournaments";
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query)) {
            
            System.out.printf("Tournament ID" + "|" +  "Tournament Name" + "|" +   "Start Date" + "|" +  "End Date" + "|" +  "Max Participants");
            
            while (rs.next()) {
                int id = rs.getInt("tournament_id");
                String name = rs.getString("tournament_name");
                Date startDate = rs.getDate("start_date");
                Date endDate = rs.getDate("end_date");
                int maxParticipants = rs.getInt("max_participants");
                
                System.out.printf(id + "|" +  name + "|" + startDate.toString()+ "|" +  endDate.toString() + "|" + maxParticipants);
            }
            
        } catch (SQLException e) {
            System.out.println("Error retrieving tournaments from database!");
            e.printStackTrace();
    
} }
    public static void SelectTournamentByID(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the tournament ID you want to view:");
        int tournamentID = Integer.parseInt(scanner.nextLine());
        String query = "SELECT * FROM tournaments WHERE tournament_id =" + tournamentID;
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            Statement statement = connection.createStatement()) {

            ResultSet rs = statement.executeQuery(query);
            System.out.printf("Tournament ID" + "|" +  "Tournament Name" + "|" +   "Start Date" + "|" +  "End Date" + "|" +  "Max Participants");

            if (rs.next()) {
                int id = rs.getInt("tournament_id");
                String name = rs.getString("tournament_name");
                Date startDate = rs.getDate("start_date");
                Date endDate = rs.getDate("end_date");
                int maxParticipants = rs.getInt("max_participants");
                
                System.out.printf(id + "|" +  name + "|" + startDate.toString()+ "|" +  endDate.toString() + "|" + maxParticipants);
            } else {
                System.out.println("Tournament with ID " + tournamentID + " not found.");
            }
            
        } catch (SQLException e) {
            System.out.println("Error retrieving tournament from database!");
            e.printStackTrace();
    }}



public static void ViewByStatus(){
    System.out.println("Enter the status of the tournaments you want to view(TRUE/FALSE)");
    Scanner scanner = new Scanner(System.in);
    boolean status = Boolean.parseBoolean(scanner.nextLine());
    String query = "SELECT * FROM tournaments WHERE is_active =" + status;
    try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Statement statement = connection.createStatement()) {
        ResultSet rs = statement.executeQuery(query);

        System.out.printf("Tournament ID" + "|" +  "Tournament Name" + "|" +   "Start Date" + "|" +  "End Date" + "|" +  "Max Participants");

            if (rs.next()) {
                int id = rs.getInt("tournament_id");
                String name = rs.getString("tournament_name");
                Date startDate = rs.getDate("start_date");
                Date endDate = rs.getDate("end_date");
                int maxParticipants = rs.getInt("max_participants");
                
                System.out.printf(id + "|" +  name + "|" + startDate.toString()+ "|" +  endDate.toString() + "|" + maxParticipants);
            } else {
                System.out.println("No Tournaments found.");
            }
        

}catch (SQLException e) {
    System.out.println("Error retrieving tournament from database");
    e.printStackTrace();
}
}

public static void PlayerParticipating(){
    System.out.println("Enter the tournament ID to view participating players:");
    Scanner scanner = new Scanner(System.in);
    int tournamentID = Integer.parseInt(scanner.nextLine());
    String query = "SELECT p.player_id, p.player_name FROM players p JOIN tournaments t JOIN participants par WHERE par.tournament_id = t.tournament_id AND par.player_id = p.player_id AND t.tournament_id = " + tournamentID;
    try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Statement statement = connection.createStatement()){
        ResultSet rs = statement.executeQuery(query);

        System.out.printf("Player ID" + "|" +  "Player Name");
            if (rs.next()) {
                int id = rs.getInt("player_id");
                String name = rs.getString("player_name");
                
                System.out.printf(id + "|" +  name);
            } else {
                System.out.println("No Players found for this tournament.");
            }
}catch (SQLException e) {
    System.out.println("Error retrieving tournament from database");
    e.printStackTrace();
}}


}