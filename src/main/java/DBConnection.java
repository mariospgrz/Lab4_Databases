/* This class is used to connect to the database Lab4_db */
import java.util.*;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/lab4_db";
        String username = "root";
        String password = "user@mysql";
        Scanner input = new Scanner(System.in);

        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            System.out.println("Connected successfully!");
        } catch (SQLException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();
        }
    int choice = 0;
    while (choice != 4) {
    System.out.println("MENU");
    System.out.println("1.Mangage Players");
    System.out.println("2.Manage Tournaments");
    System.out.println("3.Manage Matches");
    System.out.println("4.Exit");
    System.out.println("Enter your choice:");
    choice = input.nextInt();
    
    if(choice == 1){

    } else if (choice == 2) {
        TournamentManagement.tournamentMenu();
    } else if (choice == 3) {
    } else if (choice == 4) {
        System.out.println("Exited");
        System.exit(0);
    } else {
        System.out.println("Invalid choice.Try again.");
    }
}}}


