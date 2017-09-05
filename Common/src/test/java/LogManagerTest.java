/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

import Utilities.LogManager;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertTrue;

public class LogManagerTest {

    LogManager lm;
    String currentDate;
    String userHome;
    String filePath;

    public LogManagerTest() {
        lm = new LogManager();
        currentDate = "";
        DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        Date date = new Date();
        currentDate = dateFormat.format(date);

        userHome = System.getProperty("user.home");

        filePath = userHome + "/" + currentDate + ".log";
    }

    @Test
    public void testCreateLog() throws IOException {
        lm.createLog(userHome + "/");
        assertTrue(new File(filePath).exists());
        new File(filePath).delete();
    }

//    @Test
//    public void testWrite() throws IOException {
//        final String testLine = "Write log";
//        lm.write(testLine);
//
//        BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
//        String line = br.readLine();
//        assertTrue(line.contains(testLine));
//
////        new File(filePath).delete();
//    }
}
