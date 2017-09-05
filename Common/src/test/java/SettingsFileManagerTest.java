/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

import Exceptions.ConfigurationException;
import Utilities.SettingsFileManager;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class SettingsFileManagerTest {

    SettingsFileManager sfm;
    String userHome = System.getProperty("user.home");

    public SettingsFileManagerTest() throws ConfigurationException, IOException {
        new File(userHome + "/testconfig.cfg").createNewFile();
        sfm = new SettingsFileManager(userHome + "/testconfig.cfg");
    }

    @Test
    public void testExists() {
        assertTrue(sfm.exists());
    }

    @Test
    public void testSetSaveGet() throws ConfigurationException {
        sfm.set("testKey", "testValue");
        sfm.save();
        assertTrue(sfm.get("testKey").equals("testValue"));
    }

    @Test
    public void testReload() throws ConfigurationException {
        sfm.reload();
    }

    @Test
    public void testNumberOfSettings() {
        assertTrue(sfm.numberOfSettings() == 1);
    }
}
