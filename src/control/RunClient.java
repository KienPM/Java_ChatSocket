/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Ken
 */
public class RunClient {

    public static void main(String[] args){
        try {
            ClientCtrl clientCtrl = new ClientCtrl();
            clientCtrl.start();
        } catch (IOException ex) {
            Logger.getLogger(RunClient.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Error! Can't connect to server!");
        }
    }
}
