/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JOptionPane;
import model.entity.ChatBubble;
import model.entity.Client;
import model.entity.Constant;
import model.entity.Message;
import model.entity.OnlineUser;
import model.entity.User;
import view.ChatFrm;
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
    private HashMap<String, ChatFrm> chatWindows;

    public ClientCtrl() throws IOException {
        client = new Client();
        System.out.println("Start");
        loginFrm = new LoginFrm();
        addLoginFrmListener();
        loginFrm.setVisible(true);
        onlineUsers = new ArrayList<>();
        chatWindows = new HashMap<>();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Message msgFromServer = client.receiveMessage();
                System.out.println(msgFromServer.getStatus() + "/" + msgFromServer.getContent());
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
//                        System.out.println(msgFromServer.getContent().toString());
                        onSignupFail(msgFromServer.getContent());
                        break;
                    case Constant.COME_MESSAGE:
                        onComeMessage(msgFromServer.getContent());
                        break;
                    case Constant.UPDATE_ONLINE_USERS:
                        onUpdateOnlineUsers(msgFromServer.getContent());
                        break;
                    case Constant.DISCONNECT:
                        onUserDisconnect(msgFromServer.getContent());
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
        clientFrm.getBtnLogout().addActionListener((ActionEvent e) -> {
            onClickLogout();
        });
        MouseListener mouseListener;
        mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                JList theList = (JList) mouseEvent.getSource();
                if (mouseEvent.getClickCount() == 2) {
                    int index = theList.locationToIndex(mouseEvent.getPoint());
                    if (index >= 0) {
                        OnlineUser onlineUser = (OnlineUser) theList.getModel().getElementAt(index);
                        openChatWindow(onlineUser);
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
        String strContent = content.toString();
        int index = strContent.indexOf("|");
        String from = strContent.substring(0, index);
        String msg = strContent.substring(index + 1);
        if (chatWindows.containsKey(from)) {
            ImageIcon avatar = chatWindows.get(from).getAvatar();
            chatWindows.get(from).appendMsg(new ChatBubble(avatar,
                    msg, Color.CYAN));
        } else {
            for (OnlineUser onlineUser : onlineUsers) {
                if (from.equalsIgnoreCase(onlineUser.getUsername())) {
                    openChatWindow(onlineUser, msg);
                }
            }
        }
    }

    public void onUpdateOnlineUsers(Object content) {
        onlineUsers = (ArrayList<OnlineUser>) content;
//        System.out.println(onlineUsers.size());
        loadOnlineUserList();
    }

    public void onUserDisconnect(Object content) {
        String username = content.toString();
        if (chatWindows.containsKey(username)) {
            chatWindows.get(username).appendMsg(new ChatBubble(null, username + " is offline!", Color.RED));
            chatWindows.remove(username);
        }
        for (int i = 0; i < onlineUsers.size(); ++i) {
            if (username.equalsIgnoreCase(onlineUsers.get(i).getUsername())) {
                onlineUsers.remove(i);
            }
        }
        loadOnlineUserList();
    }

    public void onClickLogin() {
        String username = loginFrm.getTxtUsername().getText();
        String password = loginFrm.getTxtPassword().getText();
        if (username.equals("") || password.equals("")) {
            JOptionPane.showMessageDialog(loginFrm, "Enter username and password!");
            return;
        }
        User tmp = new User(username.toLowerCase(), password);
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
        User tmp = new User(username.toLowerCase(), password);
        try {
            client.sendMessage(new Message(Constant.SIGNUP, tmp));
        } catch (IOException ex) {
            Logger.getLogger(ClientCtrl.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(loginFrm, "Something wrong! Please try again!");
        }
    }

    public void onClickLogout() {
        try {
            client.sendMessage(new Message(Constant.LOGOUT, user.getUsername()));
            clientFrm.dispose();
            user = null;
            for (String key : chatWindows.keySet()) {
                chatWindows.get(key).dispose();
            }
            chatWindows.clear();
            loginFrm.getTxtUsername().setText("");
            loginFrm.getTxtPassword().setText("");
            loginFrm.setVisible(true);
        } catch (IOException ex) {
            Logger.getLogger(ClientCtrl.class.getName()).log(Level.SEVERE, null, ex);
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
            if (!onlineUser.getUsername().equalsIgnoreCase(user.getUsername())) {
                model.addElement(onlineUser);
            }
        }
        clientFrm.getListOnlineUsers().setModel(model);
        clientFrm.getListOnlineUsers().setCellRenderer(new OnlineUserRenderer());
    }

    public void openChatWindow(OnlineUser chatWith, String msg) {
        ChatFrm chatFrm = new ChatFrm(chatWith);
        chatWindows.put(chatWith.getUsername(), chatFrm);
        chatFrm.appendMsg(new ChatBubble(chatWith.getAvatar(), msg, Color.CYAN));
        chatFrm.getBtnSend().addActionListener((ActionEvent e) -> {
            String content = chatFrm.getChatInput();
            if (!content.equals("")) {
                chatFrm.resetChatInput();
                String str = content.substring(content.indexOf("|") + 1);
                chatFrm.appendMsg(new ChatBubble(user.getAvatar(), str, Color.LIGHT_GRAY));
                content = chatWith.getUsername() + "|" + user.getUsername() + "|" + str;
                try {
                    client.sendMessage(new Message(Constant.MESSAGE_TO_ONE, content));
                } catch (IOException ex) {
                    Logger.getLogger(ClientCtrl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        chatFrm.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                chatWindows.remove(chatWith.getUsername());
                super.windowClosing(e);
            }

        });
        chatFrm.setVisible(true);
    }

    public void openChatWindow(OnlineUser chatWith) {
        ChatFrm chatFrm = new ChatFrm(chatWith);
        chatWindows.put(chatWith.getUsername(), chatFrm);
        chatFrm.getBtnSend().addActionListener((ActionEvent e) -> {
            String content = chatFrm.getChatInput();
            if (!content.equals("")) {
                chatFrm.resetChatInput();
                chatFrm.appendMsg(new ChatBubble(user.getAvatar(), content, Color.LIGHT_GRAY));
                content = chatWith.getUsername() + "|" + user.getUsername() + "|" + content.trim();
                try {
                    client.sendMessage(new Message(Constant.MESSAGE_TO_ONE, content));
                } catch (IOException ex) {
                    Logger.getLogger(ClientCtrl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        chatFrm.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                chatWindows.remove(chatWith.getUsername());
                super.windowClosing(e);
            }

        });
        chatFrm.setVisible(true);
    }
}
