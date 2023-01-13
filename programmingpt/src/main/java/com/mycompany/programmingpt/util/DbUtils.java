package com.mycompany.programmingpt.util;

import com.mycompany.programmingpt.model.MenuItem;
import com.mycompany.programmingpt.model.OrderItem;
import com.mycompany.programmingpt.model.User;




import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.swing.JTable;


public class DbUtils  {
    
    
    private static Connection getConnection() throws SQLException {
        Properties config = ResourceUtils.getConfig();
        String url = (String) config.get("database.url");
        String user = (String) config.get("database.user");
        String pass = (String) config.get("database.pass");

        return DriverManager.getConnection(url, user, pass);
    }

    public static List<MenuItem> getMenuItems() {
        String query = "SELECT * FROM menu_items";
        List<MenuItem> menuItemList = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                MenuItem menuItem = new MenuItem();
                menuItem.setId(resultSet.getInt("id"));
                menuItem.setName(resultSet.getString("name"));
                menuItem.setPrice(resultSet.getDouble("price"));
                menuItemList.add(menuItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return menuItemList;
    }
    
public static void insertintoOrderItem(JTable jTable1,Map<Integer, OrderItem> menuItemIdToOrderItemMap, double subtotal){
    String query = "INSERT INTO order_items (menu,qty,subtotal,created_at) VALUES (?, ?, ?,?)";
    String checkquery = "SELECT * FROM order_items WHERE menu = ? AND created_at = ?";
    try (Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            PreparedStatement checkstmt = connection.prepareStatement(checkquery)) {
             
        for(int i = 0; i < jTable1.getRowCount(); i++){
            String menu = jTable1.getValueAt(i, 0).toString();
            int qty = Integer.parseInt(jTable1.getValueAt(i,1).toString());
            subtotal = Double.parseDouble(jTable1.getValueAt(i, 2).toString());
            Timestamp created_at = Timestamp.from(Instant.now());
            
            if(qty <=0){
                continue;
            }
            
            checkstmt.setString(1,menu);
            checkstmt.setTimestamp(2,created_at);
            ResultSet checkrs = checkstmt.executeQuery();
            
            if(!checkrs.next()){
                preparedStatement.setString(1, menu);
                preparedStatement.setInt(2, qty);
                preparedStatement.setDouble(3, subtotal);
                preparedStatement.setTimestamp(4,created_at);

                preparedStatement.executeUpdate();
            }
        }
        
      String insertorder = "INSERT INTO orders (uuid, discount, total, datetime) VALUES (?, ?, ?, ?)";
      String checkquery2 = "SELECT * FROM orders WHERE uuid = ? AND total = ? AND datetime = ?";

      
      subtotal = 0;
      double discount = 0;
      double total = 0;
      String uuid = UUID.randomUUID().toString();
      Timestamp timestamp = Timestamp.from(Instant.now());
      
      PreparedStatement insertOrder = connection.prepareStatement(insertorder);
      PreparedStatement checkstmt2 = connection.prepareStatement(checkquery2);
      
        for (OrderItem orderItem : menuItemIdToOrderItemMap.values()) {
         subtotal += orderItem.getSubTotal();
        discount += orderItem.getSubTotal() * 0.10;
        total += orderItem.getSubTotal() - orderItem.getSubTotal() * 0.10;
  }
        checkstmt2.setString(1,uuid);
        checkstmt2.setDouble(2, total);
        checkstmt2.setTimestamp(3, timestamp);
        ResultSet checkrs = checkstmt2.executeQuery();
  
        if(!checkrs.next()){
        insertOrder.setString(1, uuid); 
        insertOrder.setDouble(2, discount); 
        insertOrder.setDouble(3, total); 
        insertOrder.setTimestamp(4, timestamp); 
        insertOrder.executeUpdate();
  }
}catch (Exception e){
    e.printStackTrace();
}
}
public static void insertintoOrderItemNoDiscount(JTable jTable1,Map<Integer, OrderItem> menuItemIdToOrderItemMap, double subtotal){
    String query = "INSERT INTO order_items (menu,qty,subtotal,created_at) VALUES (?, ?, ?,?)";
    String checkquery = "SELECT * FROM order_items WHERE menu = ? AND created_at = ?";
    try (Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            PreparedStatement checkstmt = connection.prepareStatement(checkquery)) {
             
        for(int i = 0; i < jTable1.getRowCount(); i++){
            String menu = jTable1.getValueAt(i, 0).toString();
            int qty = Integer.parseInt(jTable1.getValueAt(i,1).toString());
            subtotal = Double.parseDouble(jTable1.getValueAt(i, 2).toString());
            Timestamp created_at = Timestamp.from(Instant.now());
            
            if(qty <=0){
                continue;
            }
            
            checkstmt.setString(1,menu);
            checkstmt.setTimestamp(2,created_at);
            ResultSet checkrs = checkstmt.executeQuery();
            
            if(!checkrs.next()){
                preparedStatement.setString(1, menu);
                preparedStatement.setInt(2, qty);
                preparedStatement.setDouble(3, subtotal);
                preparedStatement.setTimestamp(4,created_at);

                preparedStatement.executeUpdate();
            }
        }
        
      String insertorder = "INSERT INTO orders (uuid, discount, total, datetime) VALUES (?, ?, ?, ?)";
      
      

      
      subtotal = 0;
      double discount = 0;
      double total = 0;
      String uuid = UUID.randomUUID().toString();
      Timestamp timestamp = Timestamp.from(Instant.now());
      
      PreparedStatement insertOrder = connection.prepareStatement(insertorder);
      
      
      
        
      
        for (OrderItem orderItem : menuItemIdToOrderItemMap.values()) {
         subtotal += orderItem.getSubTotal();
        discount += 0;
        total = subtotal;
  }
        insertOrder.setString(1, uuid);
        insertOrder.setDouble(2, discount);
        insertOrder.setDouble(3, total);
        insertOrder.setTimestamp(4, timestamp);
        insertOrder.executeUpdate();
  
        
  
}catch (Exception e){
    e.printStackTrace();
}
}

    public static User getUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username=? AND password=? LIMIT 1";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setObject(1, username);
            preparedStatement.setObject(2, encryptPassword(password));
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                User user = new User();
                user.setId(resultSet.getInt("id"));
                user.setName(resultSet.getString("name"));
                user.setUsername(resultSet.getString("username"));

                return user;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String encryptPassword(String pass) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(pass.getBytes());
        byte[] bytes = md.digest();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
    

}

