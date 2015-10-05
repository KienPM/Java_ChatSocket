/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.entity;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import model.dao.MySQLConnector;
import model.dao.UserDAO;

/**
 *
 * @author Ken
 */
public class ServerThread extends Thread {

    private final Socket clientSocket;
    private final ObjectInputStream ois;
    private final ObjectOutputStream oos;
    private final Connection conn;

    /**
     *
     * @param clientSocket
     * @throws java.lang.Exception
     */
    public ServerThread(Socket clientSocket) throws Exception {
        this.clientSocket = clientSocket;
        ois = new ObjectInputStream(clientSocket.getInputStream());
        oos = new ObjectOutputStream(clientSocket.getOutputStream());
        conn = MySQLConnector.getConnection(Constant.DB_NAME);
    }

    @Override
    public void run() {
        User user = null;
        while (true) {
            try {
                Message clientMsg = (Message) ois.readObject();
                switch (clientMsg.getStatus()) {
                    case Constant.SIGNUP:
                        user = (User) clientMsg.getContent();
                        System.out.println(user.getUsername() + " signup");
                        onSignup(user);
                        break;
                    case Constant.LOGIN:
                        user = (User) clientMsg.getContent();
                        System.out.println(user.getUsername() + " login");
                        onLogin(user);
                        break;
                    case Constant.LOGOUT:
                        String username = (String) clientMsg.getContent();
                        System.out.println(username + " logout");
                        onLogout(username);
                        break;
                    case Constant.MESSAGE_TO_ONE:
                        onMessageToOne(clientMsg.getContent());
                        break;
                }
            } catch (IOException | ClassNotFoundException ex) {
                if (user != null) {
                    System.out.println(user.getUsername() + " disconnect");
                    onLogout(user.getUsername());
                }
                break;
            }
        }
    }

    public void onSignup(User user) {
        UserDAO userDAO = new UserDAO(conn);
        Message msg = null;
        int result = 0;
        try {
            result = userDAO.addUser(user);
            if (result == - 1) {
                msg = new Message(Constant.SIGNUP_FAIL, "This username is already used!");
            } else if (result == 1) {
                ImageIcon avatar = null;
                try {
                    Image img = ImageIO.read(new File(Constant.DEFAULT_AVATAR));
                    avatar = new ImageIcon(img);
                } catch (IOException ex) {
                    Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                }

                user.setAvatar(avatar);
                Server.serverThreads.put(user.getUsername().toLowerCase(), this);
                Server.onlineUsers.add(new OnlineUser(user.getUsername(), avatar));
                msg = new Message(Constant.SIGNUP_SUCCESS, user);
            } else {
                msg = new Message(Constant.SIGNUP_FAIL, "Something wrong! Please try again!");
            }
        } catch (SQLException ex) {
            msg = new Message(Constant.SIGNUP_FAIL, "Something wrong! Please try again!");
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            sendMessage(msg);
            if (result == 1) {
                Server.updateOnlineUsers();
            }
        }
    }

    public void onLogin(User user) {
        UserDAO userDAO = new UserDAO(conn);
        Message msg = null;
        boolean success = false;
        try {
            user = userDAO.checkLogin(user.getUsername(), user.getPassword());
            if (user != null) {
                if (Server.serverThreads.containsKey(user.getUsername().toLowerCase())) {
                    msg = new Message(Constant.LOGIN_FAIL, "Someone is using this account!");
                } else {
                    msg = new Message(Constant.LOGIN_SUCCESS, user);
                    success = true;
                    Server.serverThreads.put(user.getUsername().toLowerCase(), this);
                    Server.onlineUsers.add(new OnlineUser(user.getUsername(), user.getAvatar()));
                }
            } else {
                msg = new Message(Constant.LOGIN_FAIL, "Invalid user name or password!");
            }
        } catch (SQLException ex) {
            msg = new Message(Constant.LOGIN_FAIL, "Something wrong! Please try again!");
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            sendMessage(msg);
            if (success) {
                Server.updateOnlineUsers();
            }
        }
    }

    public void onLogout(String username) {
        Server.serverThreads.remove(username);
        for (OnlineUser onlineUser : Server.onlineUsers) {
            if (onlineUser.getUsername().equalsIgnoreCase(username)) {
                Server.onlineUsers.remove(onlineUser);
                break;
            }
        }
        Server.updateOnlineUsers();
    }

    public void onMessageToOne(Object content) {
        String strContent = content.toString();
        int index = strContent.indexOf("|");
        String to = strContent.substring(0, index);
        String subContent = strContent.substring(index + 1);
        if (Server.serverThreads.containsKey(to)) {
            Server.serverThreads.get(to).sendMessage(new Message(Constant.COME_MESSAGE, subContent));
        }
    }

    public void sendMessage(Message msg) {
        try {
            oos.writeObject(msg);
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
