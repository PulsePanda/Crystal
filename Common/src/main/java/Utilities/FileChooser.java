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

package Utilities;

import javax.swing.*;
import java.io.File;

/**
 * File Chooser utility
 */
public class FileChooser {
    /**
     * Launches a file chooser for the user to choose a system path
     *
     * @return String system path chosen
     */
    public static String chooseFile() {
        LookAndFeel previousLF = UIManager.getLookAndFeel();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }

        JFileChooser jfc = new JFileChooser();

        try {
            UIManager.setLookAndFeel(previousLF);
        } catch (UnsupportedLookAndFeelException e) {
        }

        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        if (jfc.showDialog(null, "Select") == JFileChooser.APPROVE_OPTION) {
            File file = jfc.getSelectedFile();
            String filePath = file.getPath();
//            filePath = filePath.replace("\\", "");
//            filePath = filePath.replaceFirst("\\", "");
//            filePath = filePath.replace("\\", "/");
            return filePath;
        } else {
            return "";
        }
    }
}
