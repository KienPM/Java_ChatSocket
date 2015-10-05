/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.entity.Server;

/**
 *
 * @author Ken
 */
public class RunServer {
    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.start();
            System.out.println("Server is running...");
        } catch (IOException ex) {
            Logger.getLogger(RunServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
