/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shard;

import javax.swing.JOptionPane;

/**
 *
 * @author Austin
 */
public class HandleMessage {

    private String message;

    public HandleMessage(String message) {
        this.message = message;
        JOptionPane.showMessageDialog(null, message);
    }
}
