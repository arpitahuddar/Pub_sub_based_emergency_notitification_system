package com.ssa.ens.lambda.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Random;

public class DatabaseConnect implements RequestHandler<String,Object> {
    /*public static void main(String[] args){
        DatabaseConnect testConnect = new DatabaseConnect();
        System.out.print(testConnect.getResult());*/
    //}
    private static final String URL = "jdbc:mysql://aa14c6ms0r0stwe.cqkyxlzk2mcr.us-west-2.rds.amazonaws.com:3306";
    private static final String USERNAME = "soundas1";
    private static final String PASSWORD = "04aws93!";


    public String getResult(String inputQuery) {

        String result = "unavailable";
        // Get time from DB server
        try {
            Connection conn = DriverManager.getConnection(URL,USERNAME,PASSWORD);
            Statement stmt = conn.createStatement();
            stmt.executeQuery(inputQuery);
            ResultSet resultSet = stmt.executeQuery(inputQuery);
            if (resultSet.next()) {
              result = resultSet.getObject(1).toString();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);

        }
        return result;
    }


    public void retrieveHelpId(String inputQuery) {
        String result = "unavailable";
        //Get time from DB server
        try {
            Connection conn = DriverManager.getConnection(URL,USERNAME,PASSWORD);

            Statement ps = conn.prepareStatement(inputQuery,
                    Statement.RETURN_GENERATED_KEYS);

            ((PreparedStatement) ps).executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    public int insertNewHelpRequest(int userId, int topicId) {

        int helpRequestId = Math.abs(new Random().nextInt());
        String sql = String.format("INSERT INTO innodb.Help(User_id, Topic_id, Help_status, Exhelp_id) values (%d, %d, '%s',%d)", userId, topicId, "in_progress", helpRequestId);
        retrieveHelpId(sql);
        return helpRequestId;
    }

    public void updateBestVolunteerDb(String Query) {
        try {
            Connection connect = DriverManager.getConnection(URL,USERNAME,PASSWORD);
            Statement stmt = connect.createStatement();
            stmt.executeUpdate(Query);
        } catch (SQLException e) {
            throw new RuntimeException(e);

        }
    }

    @Override
    public Object handleRequest(String s, Context context) {
        return null;
    }
}



