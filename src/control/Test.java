/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import java.awt.Color;
import model.entity.ChatBubble;
import view.ChatBubbleRenderer;
import view.ChatFrm;

/**
 *
 * @author Ken
 */
public class Test {
    public static void main(String[] args) {
//        ChatFrm cf = new ChatFrm(null);
//        cf.setVisible(true);
//        cf.appendMsg(new ChatBubble(null, "abc", Color.yellow));
//        cf.appendMsg(new ChatBubble(null, "abcddddddddddddddddddddddddddddddddddddddddddddddddddddddddddsssssssssssssssssssssssssssssssssssddddddddddddddddddddddddddddddddddddddddddddddddddddd", Color.yellow));
        String strContent = "user1|abc";
        int index = strContent.indexOf("|");
        String from = strContent.substring(0, index);
        String msg = strContent.substring(index + 1);
        System.out.println(from);
        System.out.println(msg);
    }
    
}
