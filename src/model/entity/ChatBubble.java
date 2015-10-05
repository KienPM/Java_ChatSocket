/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.entity;

import java.awt.Color;
import javax.swing.ImageIcon;

/**
 *
 * @author Ken
 */
public class ChatBubble {
    private ImageIcon avatar;
    private String content;
    private Color color;

    public ChatBubble() {
    }

    public ChatBubble(ImageIcon avatar, String content, Color color) {
        this.avatar = avatar;
        this.content = content;
        this.color = color;
    }

    public ImageIcon getAvatar() {
        return avatar;
    }

    public void setAvatar(ImageIcon avatar) {
        this.avatar = avatar;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
    
}
