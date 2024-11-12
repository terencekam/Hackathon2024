package hk.terencekam.CathayHackaon.backend.SQL;

import hk.terencekam.CathayHackaon.backend.Response;

import org.json.JSONObject;

import javax.json.JsonObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static java.net.HttpURLConnection.*;


public class Query {
    public static Connection connection;
    public Query() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/Hackathon?useSSL=FALSE&autoReconnect=TRUE", "user", "12345678");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Response createAccount(String Password, String Username, String FirstName, String LastName, String CompanyName, String AvtarUrl, String Email, String PhoneNo, String CompanyPhoneNo, String AddressOne, String AddressTwo, String Type, boolean Activate){
        try{
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Client(Password ,Username, FirstName, LastName, CompanyName, AvtarUrl, Email, PhoneNo, CompanyPhoneNo, AddressOne, AddressTwo, Type, Activated) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            preparedStatement.setString(1, Password);
            preparedStatement.setString(2, Username);
            preparedStatement.setString(3, FirstName);
            preparedStatement.setString(4, LastName);
            preparedStatement.setString(5, CompanyName);
            preparedStatement.setString(6, AvtarUrl);
            preparedStatement.setString(7, Email);
            preparedStatement.setString(8, PhoneNo);
            preparedStatement.setString(9, CompanyPhoneNo);
            preparedStatement.setString(10, AddressOne);
            preparedStatement.setString(11, AddressTwo);
            preparedStatement.setString(12, Type);
            preparedStatement.setBoolean(13, Activate);
            preparedStatement.execute();
            return new Response(HTTP_OK , new JSONObject());
        }catch (Exception e){
            e.printStackTrace();
            return new Response(HTTP_SERVER_ERROR, new JSONObject());
        }
        }


    public Response createAccount(String Password, String Username, String FirstName, String LastName, String CompanyName, String AvtarUrl, String Email, String PhoneNo, String CompanyPhoneNo, String AddressOne, String AddressTwo, String Type){
        return createAccount(Password ,Username, FirstName, LastName, CompanyName, AvtarUrl, Email, PhoneNo, CompanyPhoneNo, AddressOne, AddressTwo, Type, false);
    }

    public Response getUser(String clientUserID){
        try{
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Client WHERE ClientUserID = ?");
            preparedStatement.setString(1 , clientUserID);
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
            if(resultSet.next()){
                return new Response(HTTP_ACCEPTED, new JSONObject()
                        .put("ClientUserID", resultSet.getString("ClientUserID"))
                        .put("Password", resultSet.getString("Password"))
                        .put("Username", resultSet.getString("Username"))
                        .put("FirstName", resultSet.getString("FirstName"))
                        .put("LastName", resultSet.getString("LastName"))
                        .put("CompanyName", resultSet.getString("CompanyName"))
                        .put("AvtarUrl", resultSet.getString("AvtarUrl"))
                        .put("Email", resultSet.getString("Email"))
                        .put("PhoneNo", resultSet.getString("PhoneNo"))
                        .put("CompanyPhoneNo", resultSet.getString("CompanyPhoneNo"))
                        .put("AddressOne", resultSet.getString("AddressOne"))
                        .put("AddressTwo", resultSet.getString("AddressTwo"))
                        .put("Type", resultSet.getString("Type")));
            }
            return new Response(HTTP_BAD_REQUEST, new JSONObject());
        }catch (Exception e){
            e.printStackTrace();
            return new Response(HTTP_SERVER_ERROR, new JSONObject());
        }
    }

    public Response getFullUserList(){
        try{
            List<JSONObject> jsonObject = new ArrayList<>();
            Statement statement = connection.createStatement();
            statement.execute("SELECT Username , Email, PhoneNo FROM Client");
            ResultSet resultSet = statement.getResultSet();
            while (resultSet.next()){
                jsonObject.add(new JSONObject()
                        .put("Username" ,resultSet.getString("Username"))
                        .put("Email", resultSet.getString("Email"))
                        .put("PhoneNo", resultSet.getString("PhoneNo")));
            }
            return new Response(HTTP_OK, new JSONObject().put("Users" ,jsonObject));
        }catch (Exception e){
            e.printStackTrace();
            return new Response(HTTP_SERVER_ERROR, new JSONObject());
        }
    }

    public Response modifyUser(String clientUserID, String parameter, String value){
        try{
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Client SET ? = ? WHERE clientUserID = ?");
            preparedStatement.setString(1 , parameter);
            preparedStatement.setString(2, value);
            preparedStatement.execute();
            return new Response(HTTP_ACCEPTED, new JSONObject());

        }catch (Exception e){
            return new Response(HTTP_BAD_REQUEST, new JSONObject());
        }
    }

    public Response passwordReset(String clientUserID, String oldPassword, String newPassword){
        try{
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT Password FROM Client WHERE ClientID=?");
            preparedStatement.setString(1 , clientUserID);
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
            if(resultSet.next()){
                String password = resultSet.getString("Password");
                if(password.equals(oldPassword)){
                    return passwordReset(clientUserID , newPassword);
                }
            }

        }catch (Exception e){
            return new Response(HTTP_BAD_REQUEST, new JSONObject());
        }
        return new Response(HTTP_SERVER_ERROR, new JSONObject());
    }

    public Response passwordReset(String clientUserID, String newPassword){
        try{
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Client SET Password = ? WHERE ClientUserID = ?");
            preparedStatement.setString(1 , newPassword);
            preparedStatement.setString(2, clientUserID);
            preparedStatement.execute();
        }catch (Exception e){
            e.printStackTrace();
            return new Response(HTTP_SERVER_ERROR, new JSONObject());
        }
        return new Response(HTTP_OK, new JSONObject());

    }

    public Response verify(String clientID, String clientSecret){
        try{
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Secret WHERE ClientID = ?");
            preparedStatement.setString(1, clientID);
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
            if(resultSet.next()){
                String secret = resultSet.getString("ClientSecret");
                if(!secret.equalsIgnoreCase(clientSecret)){
                    return new Response(HTTP_NOT_AUTHORITATIVE, new JSONObject());
                }
                String ClientUserID = resultSet.getString("ClientUserID");
                Statement statement = connection.createStatement();
                statement.execute("SELECT * FROM Client WHERE ClientUserID = '%s'".formatted(ClientUserID));
                resultSet = statement.getResultSet();
                resultSet.next();
                return new Response(HTTP_OK, new JSONObject().put("ClientUserID", resultSet.getString("ClientUserID")).put("Type",resultSet.getString("Type")));
            }else{
                return new Response(HTTP_NOT_AUTHORITATIVE, new JSONObject());
            }
        }catch (Exception e){
            return new Response(HTTP_NOT_AUTHORITATIVE, new JSONObject());
        }
    }
}
