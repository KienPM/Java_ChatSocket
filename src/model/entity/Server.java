/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.entity;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ken
 */
public class Server extends Thread {

    public static HashMap<String, ServerThread> serverThreads;
    public static ArrayList<OnlineUser> onlineUsers;
    private final ServerSocket serverSocket;

    public Server() throws IOException {
        serverThreads = new HashMap<>();
        onlineUsers = new ArrayList<>();
        serverSocket = new ServerSocket(Constant.PORT);
    }

    @Override
    public void run() {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println(clientSocket.toString() + " connect");
                new ServerThread(clientSocket).start();
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void updateOnlineUsers() {
        ArrayList<OnlineUser> tmp = new ArrayList<>();
        tmp.addAll(onlineUsers);
        Message msg = new Message(Constant.UPDATE_ONLINE_USERS, tmp);
        for (String key : serverThreads.keySet()) {
            System.out.println("Update for " + key + " " + onlineUsers.size());
            serverThreads.get(key).sendMessage(msg);
        }
    }

}
