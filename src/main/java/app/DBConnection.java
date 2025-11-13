package app;
/* This class is used to connect to the database Lab4_db */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/lab4_db";
        String username = "root";
        String password = "user@mysql";

        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            System.out.println("Connected successfully!");
        } catch (SQLException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();
        }
    }
}


