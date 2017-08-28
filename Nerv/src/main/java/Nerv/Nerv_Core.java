/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

package Nerv;

import Utilities.Log;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class Nerv_Core {

    public static String baseDir = "/CrystalHomeSys/", baseLogDir = "Logs/";
    private String serverIP, systemName = "Nerv Controller";
    private int serverPort;
    private boolean logActive = false;

    private JFrame frame;
    private JTextPane textArea;

    private Log log;

    public Nerv_Core() {
        initVariables();

        redirectSystemStreams();

        initGUI();
    }

    private void initVariables() {
        log = new Log();
        baseDir = System.getProperty("user.home") + baseDir;
        baseLogDir = baseDir + baseLogDir;
    }

    private void initGUI() {
        frame = new JFrame(systemName);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);

        JButton exitButton = new JButton("Shutdown");
        exitButton.addActionListener(new ActionListener() {
            boolean allowShutdown = true;

            public void actionPerformed(ActionEvent e) {
                if (!allowShutdown) {
                    return;
                }

                allowShutdown = false;
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                stopHeartServer();
                System.out.println("");
                System.out.println("IT IS NOW SAFE TO CLOSE THE WINDOW");
                System.out.println("");
            }
        });
        exitButton.setBounds(new Rectangle(10, 10, 100, 40));

        JButton checkUpdate = new JButton("Check for Updates");
        checkUpdate.addActionListener(e -> updateChecker(false, false));
        checkUpdate.setBounds(new Rectangle(120, 10, 140, 40));

        JButton forceUpdate = new JButton("Force Update");
        forceUpdate.addActionListener(e -> updateChecker(false, true));
        forceUpdate.setBounds(new Rectangle(270, 10, 110, 40));

        JButton forceIndex = new JButton("Force Index");
//        forceIndex.addActionListener(e -> mediaManager.index(false, 0));
        forceIndex.setBounds(new Rectangle(390, 10, 100, 40));

        textArea = new JTextPane();
        textArea.setEditable(false);
//        textArea.setLineWrap(true);

        JButton clearLog = new JButton("Clear Log");
        clearLog.addActionListener(e -> textArea.setText(""));
        clearLog.setBounds(new Rectangle(500, 10, 100, 40));

//        JLabel heartVersionLabel = new JLabel("Heart_Version: " + HEART_VERSION);
//        heartVersionLabel.setBounds(new Rectangle(650, 5, 120, 25));

//        JLabel shardVersionLabel = new JLabel("Shard_Version: " + SHARD_VERSION);
//        shardVersionLabel.setBounds(new Rectangle(650, 35, 120, 25));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBounds(0, 60, frame.getWidth() - 5, frame.getHeight() - 85);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        frame.getContentPane().add(exitButton);
        frame.getContentPane().add(checkUpdate);
        frame.getContentPane().add(forceUpdate);
        frame.getContentPane().add(forceIndex);
        frame.getContentPane().add(clearLog);
//        frame.getContentPane().add(heartVersionLabel);
//        frame.getContentPane().add(shardVersionLabel);
        frame.getContentPane().add(scrollPane);
        frame.setVisible(true);
    }

    private void stopHeartServer() {

    }

    private void updateChecker(boolean keepRunning, boolean forceUpdate) {

    }

    /**
     * Function to redirect standard output streams to the write function
     */
    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                println(String.valueOf((char) b), Color.BLACK);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                println(new String(b, off, len), Color.BLACK);
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };

        OutputStream err = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                println(String.valueOf((char) b), Color.RED);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                println(new String(b, off, len), Color.RED);
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(err, true));
    }

    private boolean println(final String msg, Color color) {
        boolean success = true;

        SwingUtilities.invokeLater(() -> {
            appendToPane(textArea, msg, color);
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });

        if (logActive) {
            try {
                log.write(msg);
            } catch (IOException e) {
                logActive = false;
                System.err.println(
                        "Unable to write to log. IOException thrown. Deactivating log file, please reboot to regain access.");
                success = false;
            }
        }

        return success;
    }

    private void appendToPane(JTextPane tp, String msg, Color c) {
        tp.setEditable(true);
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = tp.getDocument().getLength();
        tp.setCaretPosition(len);
        tp.setCharacterAttributes(aset, false);
        tp.replaceSelection(msg);
        tp.setEditable(false);
    }
}
