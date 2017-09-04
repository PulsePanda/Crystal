/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

import Exceptions.ServerInitializationException;
import Heart.Heart_Core;
import Heart.Manager.ServerManager;
import org.junit.Test;

public class ServerManagerTest {

    private Heart_Core c;
    private ServerManager sm;

    public ServerManagerTest() {
        c = new Heart_Core(false, true);
        sm = new ServerManager(c);
    }

    @Test
    public void testStartHeartServer() throws ServerInitializationException {
        sm.startHeartServer(6987);
    }

    @Test
    public void testInitDNSSD() {
        sm.initDNSSD();
    }

    @Test
    public void testStopHeartServer() {
        sm.stopHeartServer();
    }
}