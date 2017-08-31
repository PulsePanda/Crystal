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

import Exceptions.ConfigurationException;
import Heart.Heart_Core;
import Utilities.FileChooser;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

public class FirstTimeSetupManager {

    private Heart_Core c;

    /**
     * Handles first time setup
     *
     * @param heart_core Heart_Core object that FTS is being performed for
     */
    public FirstTimeSetupManager(Heart_Core heart_core) {
        c = heart_core;
    }

    /**
     * Walk the user through the first time setup
     */
    protected void start() {
        // First frame, welcome to first time setup
        JLabel label = new JLabel();
        Font font = label.getFont();
        StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
        style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
        style.append("font-size:" + font.getSize() + "pt;");
        JEditorPane ep = new JEditorPane("text/html", "<html><body style=\"" + style + "\">" //
                + "Config file not found. Welcome to the first time setup.\nFor information regarding usage or troubleshooting, check out the <a href=\"https://github.com/PulsePanda/Crystal/wiki\">wiki</a>." //
                + "</body></html>");
        ep.addHyperlinkListener(e -> {
            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
                try {
                    Desktop.getDesktop().browse(URI.create(e.getURL().toString()));
                } catch (IOException e1) {
                    System.err.println("Error when trying to open link to wiki.");
                }
        });
        ep.setEditable(false);
        ep.setBackground(label.getBackground());
        JOptionPane.showMessageDialog(null, ep);

        // Second frame, ask for system name
        String systemName = JOptionPane.showInputDialog(c.getGuiManager().frame, "What do you want to call this device?");

        // Third frame, ask for root media folder
        JOptionPane.showMessageDialog(c.getGuiManager().frame, "Enter the root media folder.");
        String mediaDir = FileChooser.chooseFile();

        // Try to automatically detect music folder within root
        String musicDir;
        if (new File(mediaDir + "/music").exists()) {
            musicDir = mediaDir + "/music";
        } else if (new File(mediaDir + "/songs").exists()) {
            musicDir = mediaDir + "/songs";
        } else {
            // Manual music folder entry frame
            JOptionPane.showMessageDialog(c.getGuiManager().frame, "Enter the root music folder.");
            musicDir = FileChooser.chooseFile();
        }

        // Try to automatically detect movie folder within root
        String movieDir;
        if (new File(mediaDir + "/movie").exists()) {
            movieDir = mediaDir + "/movie";
        } else if (new File(mediaDir + "/movies").exists()) {
            movieDir = mediaDir + "/movies";
        } else {
            // Manual movie folder entry frame
            JOptionPane.showMessageDialog(c.getGuiManager().frame, "Enter the root movie folder.");
            movieDir = FileChooser.chooseFile();
        }

        // Fourth frame, ask for activation command (ie: Ok Crystal)
        String commandKey = JOptionPane.showInputDialog(c.getGuiManager().frame, "What voice command will you use to wake up Crystal?");

        // Generate UUID for Heart
        String uuid = UUID.randomUUID().toString();

        // Fifth frame, ask for library scan frequency (in minutes)
        String mediaIndexDelay = JOptionPane.showInputDialog(c.getGuiManager().frame, "How often do you want to index your media library? (In Minutes)");

        // Sixth frame, ask for software update scan frequency (in minutes)
        String updateCheckDelay = JOptionPane.showInputDialog(c.getGuiManager().frame, "How often do you want to check for software updates? (In Minutes)");

        // Set all of the configuration keys
        c.getConfigurationManager().cfg.set("cfg_set", "True"); // TODO nullpointerexception thrown on kayleighs computer on config creation, before and after input helper
        c.getConfigurationManager().cfg.set("systemName", systemName);
        c.getConfigurationManager().cfg.set("mediaDir", mediaDir);
        c.getConfigurationManager().cfg.set("musicDir", musicDir);
        c.getConfigurationManager().cfg.set("movieDir", movieDir);
        c.getConfigurationManager().cfg.set("commandKey", commandKey);
        c.getConfigurationManager().cfg.set("uuid", uuid);
        c.getConfigurationManager().cfg.set("mediaIndexDelay", mediaIndexDelay);
        c.getConfigurationManager().cfg.set("updateCheckDelay", updateCheckDelay);
        try {
            c.getConfigurationManager().cfg.save();
        } catch (ConfigurationException e) {
            System.err.println("Error saving settings to the config file. Error: " + e.getMessage());
            JOptionPane.showMessageDialog(c.getGuiManager().frame, "Error saving settings to the config file. Error: " + e.getMessage());
        }
    }
}
