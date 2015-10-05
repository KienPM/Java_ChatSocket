/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.dao;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import model.entity.Constant;
import model.entity.User;

/**
 *
 * @author Ken
 */
public class UserDAO {

    private final Connection conn;

    public UserDAO() throws Exception {
        conn = MySQLConnector.getConnection(Constant.DB_NAME);
    }

    public UserDAO(Connection conn) {
        this.conn = conn;
    }

    public int addUser(User user) throws SQLException {
        String sql = "SELECT * FROM `chatsocketdb`.`user` WHERE `username`=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, user.getUsername());
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return -1;
        }
        sql = "INSERT INTO `chatsocketdb`.`user`(`username`,`password`,`avatar_url`) "
                + "VALUES(?,?,?)";
        ps = conn.prepareStatement(sql);
        ps.setString(1, user.getUsername());
        ps.setString(2, user.getPassword());
        ps.setString(3, Constant.DEFAULT_AVATAR);
        return ps.executeUpdate();
    }

    public User checkLogin(String username, String password) throws SQLException {
        User user = null;
        String sql = "SELECT * FROM `chatsocketdb`.`user` WHERE username=? AND password=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            String pass = rs.getString("password");
            if (pass.equals(password)) {
                String avatarURL = rs.getString("avatar_url");
                Image avatar = null;
                try {
                    avatar = ImageIO.read(new File(avatarURL));
                } catch (IOException ex) {
                    Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
                }
                user = new User(username, password, new ImageIcon(avatar));
            }
        }
        return user;
    }
}
