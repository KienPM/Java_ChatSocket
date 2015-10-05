/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.entity;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ken
 */
public class Client {

    private final Socket socket;
    private final ObjectInputStream ois;
    private final ObjectOutputStream oos;

    public Client() throws IOException {
        Scanner scanner = new Scanner(new FileInputStream("host.config"));
        String host = scanner.nextLine().trim();
        socket = new Socket(host, Constant.PORT);
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());
    }

    public void sendMessage(Message msg) throws IOException {
        oos.writeObject(msg);
    }

    public Message receiveMessage() throws IOException, ClassNotFoundException {
        return (Message) ois.readObject();
    }
    
    public void closeSocket() {
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
