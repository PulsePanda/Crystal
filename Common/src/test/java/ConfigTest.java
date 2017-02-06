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
import Utilities.Config;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by Austin on 2/3/2017.
 */
public class ConfigTest {

    Config c;

    public ConfigTest() throws IOException, ConfigurationException {
        File f = new File("C:/configTest.cfg");
        if (!f.exists()) f.createNewFile();
        c = new Config("C:/configTest.cfg");
    }

    @Test
    public void existsTest() {
        assert (c.exists());
    }

    @Test
    public void setTest() throws ConfigurationException {
        c.set("testValue", "test");
        c.save();
    }

    @Test
    public void getTest() {
        assertEquals("test", c.get("testValue"));
    }

    @Test
    public void reloadTest() throws ConfigurationException {
        c.reload();
    }

    @Test
    public void deleteTestFile() {
        File f = new File("C:/configTest.cfg");
        f.delete();
    }
}
