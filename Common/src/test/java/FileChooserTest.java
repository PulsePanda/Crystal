/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

import Utilities.FileChooser;
import org.junit.Test;

import javax.swing.*;

import static org.junit.Assert.*;

public class FileChooserTest {

    public FileChooserTest(){}

    @Test
    public void testChooseFile(){
        JOptionPane.showMessageDialog(null, "Testing file chooser. \nChoose a file in your C: drive.");
        String file = FileChooser.chooseFile();
        assertTrue(file.toLowerCase().contains("c:"));


        JOptionPane.showMessageDialog(null, "Testing file chooser. \nClick cancel, do not choose a file");
        String file1 = FileChooser.chooseFile();
        assertTrue(file1.equals(""));
    }
}