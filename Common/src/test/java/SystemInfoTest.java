/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

import Utilities.SystemInfo;
import org.junit.Test;

import java.net.UnknownHostException;

import static org.junit.Assert.assertTrue;

public class SystemInfoTest {

    public SystemInfoTest() {
    }

    @Test
    public void testSystem_os() {
        SystemInfo.SYSTEM_OS os = SystemInfo.getSystem_os();
        assertTrue(os.toString().toLowerCase().contains("windows"));
    }

    @Test
    public void testSystemIP() throws UnknownHostException {
        String IP = SystemInfo.getSystemLocalIP();
        assertTrue(IP.contains("192.168."));
    }

    @Test
    public void testHostname() throws UnknownHostException {
        String hostname = SystemInfo.getSystemHostname();
        assertTrue(hostname.equals("SHOCKWAVE"));
    }
}
