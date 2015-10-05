/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JOptionPane;
import model.entity.Client;
import model.entity.Constant;
import model.entity.Message;
import model.entity.OnlineUser;
import model.entity.User;
import view.ClientFrm;
import view.LoginFrm;
import view.OnlineUserRenderer;

/**
 *
 * @author Ken
 */
public class ClientCtrl extends Thread {

    private ClientFrm clientFrm;
    private final LoginFrm loginFrm;
    private ArrayList<OnlineUser> onlineUsers;
    private final Client client;
    private User user = null;

    public ClientCtrl() throws IOException {
        client = new Client();
        System.out.println("Start");
        loginFrm = new LoginFrm();
        addLoginFrmListener();
        loginFrm.setVisible(true);
        onlineUsers = new ArrayList<>();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Message msgFromServer = client.receiveMessage();
                System.out.println(msgFromServer.getStatus());
                switch (msgFromServer.getStatus()) {
                    case Constant.SIGNUP_SUCCESS:
                        onSignupSuscess(msgFromServer.getContent());
                        break;
                    case Constant.SIGNUP_FAIL:
                        onSignupFail(msgFromServer.getContent());
                        break;
                    case Constant.LOGIN_SUCCESS:
                        onLoginSuccess(msgFromServer.getContent());
                        break;
                    case Constant.LOGIN_FAIL:
                        onSignupFail(msgFromServer.getContent());
                        break;
                    case Constant.COME_MESSAGE:
                        onComeMessage(msgFromServer.getContent());
                        break;
                    case Constant.UPDATE_ONLINE_USERS:
                        onUpdateOnlineUsers(msgFromServer.getContent());
                        break;
                }
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(ClientCtrl.class.getName()).log(Level.SEVERE, null, ex);
                break;
            }
        }
    }

    private void addLoginFrmListener() {
        loginFrm.getBtnLogin().addActionListener((ActionEvent e) -> {
            onClickLogin();
        });
        loginFrm.getBtnSignup().addActionListener((ActionEvent e) -> {
            onClickSignup();
        });
    }

    private void addClientFrmListener() {
        MouseListener mouseListener;
        mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                JList theList = (JList) mouseEvent.getSource();
                if (mouseEvent.getClickCount() == 2) {
                    int index = theList.locationToIndex(mouseEvent.getPoint());
                    if (index >= 0) {
                        OnlineUser onlineUser = (OnlineUser) theList.getModel().getElementAt(index);
                        JOptionPane.showMessageDialog(clientFrm, onlineUser.getUsername());
                    }
                }
            }
        };
        clientFrm.getListOnlineUsers().addMouseListener(mouseListener);
    }

    public void onSignupSuscess(Object content) {
        user = (User) content;
        startClientFrm();
    }

    public void onSignupFail(Object content) {
        String msg = content.toString();
        JOptionPane.showMessageDialog(loginFrm, msg);
    }

    public void onLoginSuccess(Object content) {
        user = (User) content;
        startClientFrm();
    }

    public void onLoginFail(Object content) {
        String msg = content.toString();
        JOptionPane.showMessageDialog(loginFrm, msg);
    }

    public void onComeMessage(Object content) {

    }

    public void onUpdateOnlineUsers(Object content) {
        onlineUsers = (ArrayList<OnlineUser>) content;
        System.out.println(onlineUsers.size());
        loadOnlineUserList();
    }

    public void onClickLogin() {
        String username = loginFrm.getTxtUsername().getText();
        String password = loginFrm.getTxtPassword().getText();
        if (username.equals("") || password.equals("")) {
            JOptionPane.showMessageDialog(loginFrm, "Enter username and password!");
            return;
        }
        User tmp = new User(username, password);
        try {
            client.sendMessage(new Message(Constant.LOGIN, tmp));
        } catch (IOException ex) {
            Logger.getLogger(ClientCtrl.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(loginFrm, "Something wrong! Please try again!");
        }
    }

    public void onClickSignup() {
        String username = loginFrm.getTxtUsername().getText();
        String password = loginFrm.getTxtPassword().getText();
        if (username.equals("") || password.equals("")) {
            JOptionPane.showMessageDialog(loginFrm, "Enter username and password!");
            return;
        }
        if (username.contains(" ") || username.contains("|")) {
            JOptionPane.showMessageDialog(loginFrm, "Username can't contants space or '|'");
            return;
        }
        User tmp = new User(username, password);
        try {
            client.sendMessage(new Message(Constant.SIGNUP, tmp));
        } catch (IOException ex) {
            Logger.getLogger(ClientCtrl.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(loginFrm, "Something wrong! Please try again!");
        }
    }

    public void startClientFrm() {
        loginFrm.setVisible(false);
        clientFrm = new ClientFrm();
        Image img = user.getAvatar().getImage();
        ImageIcon avatar = new ImageIcon(img.getScaledInstance(55, 55, Image.SCALE_SMOOTH));
        clientFrm.getLblAvatar().setIcon(avatar);
        clientFrm.getLblUsername().setText(user.getUsername().toUpperCase());
        loadOnlineUserList();
        addClientFrmListener();
        clientFrm.setVisible(true);
    }

    public void loadOnlineUserList() {
        DefaultListModel<OnlineUser> model = new DefaultListModel<>();
        model.clear();
        for (OnlineUser onlineUser : onlineUsers) {
            model.addElement(onlineUser);
        }
        clientFrm.getListOnlineUsers().setModel(model);
        clientFrm.getListOnlineUsers().setCellRenderer(new OnlineUserRenderer());
    }
}
