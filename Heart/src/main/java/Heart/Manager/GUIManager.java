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

package Heart.Manager;

import Heart.Heart_Core;
import Utilities.UpdateCheckerThread;

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

public class GUIManager {

    protected static JTextPane textArea;
    // GUI elements
    protected JFrame frame;
    protected JLabel shardVersionLabel;
    private Heart_Core c;
    public OutputStream outputStream;
    public OutputStream errStream;

    public GUIManager(Heart_Core heart_core) {
        c = heart_core;
    }

    //TODO Removing this method for service migration

    /**
     * Sets up and starts the GUI associated with the Heart
     */
    public void initGUI() {
        // Frame and it's settings
        frame = new JFrame(ConfigurationManager.systemName);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);

        // Exit Button
        JButton exitButton = new JButton("Shutdown");
        exitButton.addActionListener(new ActionListener() {
            boolean allowShutdown = true;

            public void actionPerformed(ActionEvent e) {
                if (!allowShutdown) {
                    return;
                }

                allowShutdown = false;
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                c.shutdownHeart();
                System.out.println("");
                System.out.println("IT IS NOW SAFE TO CLOSE THE WINDOW");
                System.out.println("");
            }
        });
        exitButton.setBounds(new Rectangle(10, 10, 100, 40));

        // Check Update Button
        JButton checkUpdate = new JButton("Check for Updates");
        checkUpdate.addActionListener(e -> new Thread(new UpdateCheckerThread(false, false)).start());
        checkUpdate.setBounds(new Rectangle(120, 10, 140, 40));

        // Force Update Button
        JButton forceUpdate = new JButton("Force Update");
        forceUpdate.addActionListener(e -> new Thread(new UpdateCheckerThread(false, true)).start());
        forceUpdate.setBounds(new Rectangle(270, 10, 110, 40));

        // Force Media Index Button
        JButton forceIndex = new JButton("Force Index");
        forceIndex.addActionListener(e -> c.getMediaManager().index(false, 0));
        forceIndex.setBounds(new Rectangle(390, 10, 100, 40));

        // Text Area
        textArea = new JTextPane();
        textArea.setEditable(false);
//        textArea.setLineWrap(true);

        // Clear text button
        JButton clearLog = new JButton("Clear LogManager");
        clearLog.addActionListener(e -> textArea.setText(""));
        clearLog.setBounds(new Rectangle(500, 10, 100, 40));

        // Heart Version Label
        JLabel heartVersionLabel = new JLabel("Heart_Version: " + ConfigurationManager.HEART_VERSION);
        heartVersionLabel.setBounds(new Rectangle(650, 5, 120, 25));

        // Shard Version Label
        shardVersionLabel = new JLabel("Shard_Version: " + ConfigurationManager.SHARD_VERSION);
        shardVersionLabel.setBounds(new Rectangle(650, 35, 120, 25));

        // Scroll pane holding the text area
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBounds(0, 60, frame.getWidth() - 5, frame.getHeight() - 85);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        // Add everything to the frame
        frame.getContentPane().add(exitButton);
        frame.getContentPane().add(checkUpdate);
        frame.getContentPane().add(forceUpdate);
        frame.getContentPane().add(forceIndex);
        frame.getContentPane().add(clearLog);
        frame.getContentPane().add(heartVersionLabel);
        frame.getContentPane().add(shardVersionLabel);
        frame.getContentPane().add(scrollPane);
        frame.setVisible(true);

        redirectSystemStreams();
    }

    /**
     * Function to redirect standard output streams to the write function
     */
    private void redirectSystemStreams() {
        outputStream = new OutputStream() {
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

        errStream = new OutputStream() {
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

        System.setOut(new PrintStream(outputStream, true));
        System.setErr(new PrintStream(errStream, true));
    }

    /**
     * Writes to the Standard Output Stream, as well as calls 'write' on the
     * local logManager object
     *
     * @param msg   String message to be displayed and written
     * @param color Color to set the line of text
     * @return Returns TRUE if successful at writing to the logManager, FALSE if not
     */
    public boolean println(final String msg, Color color) {
        boolean success = true;

        SwingUtilities.invokeLater(() -> appendToPane(msg, color));

        if (ConfigurationManager.isLogActive()) {
            try {
                Heart_Core.getCore().logManager.write(msg);
            } catch (IOException e) {
                ConfigurationManager.setLogActive(false);
                System.err.println(
                        "Unable to write to log. IOException thrown. Deactivating log file, please reboot to regain access.");
                success = false;
            }
        }

        return success;
    }

    /**
     * Append to the GUI Text Area. Automatically moves caret to end of document
     *
     * @param content String containing content to append
     * @param color   Color of content's text
     */
    public void appendToPane(String content, Color color) {
        textArea.setEditable(true);
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = textArea.getDocument().getLength();
        textArea.setCaretPosition(len);
        textArea.setCharacterAttributes(aset, false);
        textArea.replaceSelection(content);
        textArea.setEditable(false);
    }
}
