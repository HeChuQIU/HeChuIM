package net.HeChu.Server;

import net.HeChu.Common.UserInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.logging.Logger;

public class Server {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private static final String SALT = "salt";

    private static String hashPassword(String password) {
        password = password + SALT;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static boolean verifyPassword(String password, String hash) {
        return hashPassword(password).equals(hash);
    }

    private static boolean login(String username, String password) {
        try(ResultSet resultSet = query("SELECT * FROM users WHERE username = '" + username + "'")) {
            if (!resultSet.next()) {
                return false;
            }
            String hash = resultSet.getString("password");
            return verifyPassword(password, hash);
        } catch (SQLException e) {
            LOGGER.warning("Error logging in: " + e.getMessage());
            return false;
        }
    }

    private static ResultSet query(String sql) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:server.db");
        Statement statement = connection.createStatement();
        return statement.executeQuery(sql);
    }

    private static void update(String sql) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:server.db");
        Statement statement = connection.createStatement();
        statement.executeUpdate(sql);
    }

    private static UserInfo[] getAllUsers() {
        try(ResultSet resultSet = query("SELECT * FROM users")) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            UserInfo[] users = new UserInfo[columnCount];
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String username = resultSet.getString("username");
                users[id] = new UserInfo() {
                    @Override
                    public int getId() {
                        return id;
                    }

                    @Override
                    public String getName() {
                        return username;
                    }
                };
            }
            return users;
        } catch (SQLException e) {
            LOGGER.warning("Error getting all users: " + e.getMessage());
            return new UserInfo[0];
        }
    }

    private static void createUser(String username, String password) {
        try {
            update("INSERT INTO users (username, password) VALUES ('" + username + "', '" + hashPassword(password) + "')");
        } catch (SQLException e) {
            LOGGER.warning("Error creating user: " + e.getMessage());
        }
    }



    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8888);
            System.out.println("Server is running...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("A client has connected.");

                new Thread(() -> {
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

                        String message = reader.readLine();
                        System.out.println("Received from client: " + message);

                        writer.println("Hello, client!");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}