/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

package Shard.Manager;

import Netta.Connection.Packet;
import Netta.Exceptions.SendPacketException;
import Shard.ShardConnectionThread;
import Shard.Shard_Core;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUIManager {

    public static JTextPane textArea;
    public JFrame frame;
    public JLabel connectionStatus;
    Shard_Core c;
    private JPanel consolePanel, commandPanel;
    private JTabbedPane tabbedPane;


    public GUIManager(Shard_Core shard_core) {
        c = shard_core;
    }

    /**
     * Create the GUI for the Shard. The GUI will handle everything from the
     * console output, being the System.out and System.err output, displaying
     * information from the Heart, and handling input from the user.
     */
    public void initGUI() {
        // Frame setup
        frame = new JFrame(ConfigurationManager.systemName);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int) screenSize.getWidth(), (int) screenSize.getHeight());
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        tabbedPane = new JTabbedPane();
        tabbedPane.setBounds(0, 0, frame.getWidth(), frame.getHeight() - 10);

        // Command panel setup
        commandPanel = new JPanel();
        commandPanel.setLayout(new FlowLayout());

        connectionStatus = new JLabel("DISCONNECTED");
        connectionStatus.setForeground(Color.RED);

        JButton checkUpdate = new JButton("Check for Updates");
        checkUpdate.addActionListener(e -> {
            if (!ConnectionManager.patchReady)
                return;
            new Thread(new ShardConnectionThread(false, true)).start();
        });

        JButton goodMorning = new JButton("Good Morning");
        goodMorning.addActionListener(e -> {
            if (!ConnectionManager.patchReady)
                return;

            Packet p = new Packet(Packet.PACKET_TYPE.Command, c.getConfigurationManager().uuid.toString());
            p.packetString = "Good Morning";
            try {
                c.getConnectionManager().sendPacket(p, true);
            } catch (SendPacketException ex) {
                System.err.println("Error sending Good Morning packet to Heart. Error: " + ex.getMessage());
            }
        });

        JButton btcPrice = new JButton("BTC Price");
        btcPrice.addActionListener(e -> {
            if (!ConnectionManager.patchReady)
                return;

            Packet p = new Packet(Packet.PACKET_TYPE.Command, c.getConfigurationManager().uuid.toString());
            p.packetString = "BTC Price";
            try {
                c.getConnectionManager().sendPacket(p, true);
            } catch (SendPacketException ex) {
                System.err.println("Error sending BTC Price packet to Heart. Error: " + ex.getMessage());
            }
        });

        JButton weather = new JButton("Weather");
        weather.addActionListener(e -> {
            if (!ConnectionManager.patchReady)
                return;

            Packet p = new Packet(Packet.PACKET_TYPE.Command, c.getConfigurationManager().uuid.toString());
            p.packetString = "Weather";
            try {
                c.getConnectionManager().sendPacket(p, true);
            } catch (SendPacketException ex) {
                System.err.println("Error sending Weather packet to Heart. Error: " + ex.getMessage());
            }
        });

        JButton playMusic = new JButton("Play Music");
        playMusic.addActionListener(e -> {
            if (!ConnectionManager.patchReady)
                return;

            Packet p = new Packet(Packet.PACKET_TYPE.Command, c.getConfigurationManager().uuid.toString());
            p.packetString = "Play Music";
            String song = JOptionPane.showInputDialog(null, "Song name");
            p.packetStringArray = new String[]{song};
            try {
                c.getConnectionManager().sendPacket(p, true);
            } catch (SendPacketException e1) {
                System.err.println("Error sending Play Music packet to Heart. Error: " + e1.getMessage());
            }
        });

        JButton playMovie = new JButton("Play Movie");
        playMovie.addActionListener(e -> {
            if (!ConnectionManager.patchReady)
                return;

            Packet p = new Packet(Packet.PACKET_TYPE.Command, c.getConfigurationManager().uuid.toString());
            p.packetString = "Play Movie";
            String movie = JOptionPane.showInputDialog(null, "Movie name");
            p.packetStringArray = new String[]{movie};
            try {
                c.getConnectionManager().sendPacket(p, true);
            } catch (SendPacketException e1) {
                System.err.println("Error sending Play Movie packet to Heart. Error: " + e1.getMessage());
            }
        });

        JButton stopMedia = new JButton("Stop Media");
        stopMedia.addActionListener(e -> {
            if (!ConnectionManager.patchReady)
                return;

//            mediaPlayback.stop();
        });

        commandPanel.add(connectionStatus);
        commandPanel.add(checkUpdate);
        commandPanel.add(goodMorning);
        commandPanel.add(btcPrice);
        commandPanel.add(weather);
        commandPanel.add(playMusic);
        commandPanel.add(playMovie);
        commandPanel.add(stopMedia);

        // Console panel setup
        consolePanel = new JPanel();
        consolePanel.setLayout(new BorderLayout());

        JButton exitButton = new JButton("Shutdown");
        exitButton.addActionListener(new ActionListener() {
            boolean allowShutdown = true;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!allowShutdown) {
                    return;
                }

                allowShutdown = false;
                try {
                    c.getConnectionManager().stopShardClient();
                } catch (Exception ex) {
                    System.err.println("Error when closing the connection to the Heart. Error: " + ex.getMessage());
                }
                System.out.println("Shard is shut down and exiting");
                System.exit(0);
            }
        });
        exitButton.setBounds(new Rectangle(10, 10, 100, 40));

        textArea = new JTextPane();
        textArea.setEditable(false);
//        textArea.setLineWrap(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBounds(0, 60, frame.getWidth() - 5, frame.getHeight() - 115);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        consolePanel.add(exitButton, BorderLayout.NORTH);
        consolePanel.add(scrollPane, BorderLayout.CENTER);

        commandPanel.add(exitButton);

        // Finish GUI
        tabbedPane.addTab("Commands", commandPanel);
        tabbedPane.addTab("Console", consolePanel);
        frame.add(tabbedPane);
        frame.setVisible(true);
    }


    public void appendToPane(JTextPane tp, String msg, Color c) {
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
