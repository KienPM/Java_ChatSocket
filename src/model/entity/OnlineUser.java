/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.entity;

import java.io.Serializable;
import javax.swing.ImageIcon;

/**
 *
 * @author Ken
 */
public class OnlineUser implements Serializable {
    private String username;
    private ImageIcon avatar;

    public OnlineUser() {
    }

    public OnlineUser(String username, ImageIcon avatar) {
        this.username = username;
        this.avatar = avatar;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ImageIcon getAvatar() {
        return avatar;
    }

    public void setAvatar(ImageIcon avatar) {
        this.avatar = avatar;
    }

    
}
