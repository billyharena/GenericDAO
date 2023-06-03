/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.sql.*;

/**
 *
 * @author billy
 */
public class DatabaseConnexion {
    private Connection cnt;
    private Statement stmt;
    private ResultSet rs;

    // Open a postgreSQL connection
    public Connection getPostgreSQLConnexion(String user, String databasename, String password, String port) {
        try {
            Class.forName("org.postgresql.Driver");
            this.cnt = DriverManager.getConnection("jdbc:postgresql://localhost:" + port + "/" + databasename, user, password);
            if (cnt != null) System.out.println("Connected in PostgreSQL database = " +databasename );
            else System.out.println("Connection failed");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return cnt;
    }

    // Close the statement
    public void closeStatement(){
        if (stmt != null){
            try {
                stmt.close();
                System.out.println("Statement closed");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Close the resultset
    public void closeResultSet(){
        if (rs != null){
            try {
                rs.close();
                System.out.println("ResultSet closed");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
    // Close the connection
    public void closeConnection(){
        if (cnt != null){
            try {
                cnt.close();
                System.out.println("Connection closed");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
