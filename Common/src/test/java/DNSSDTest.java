/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

import Utilities.DNSSD;
import org.junit.Test;

import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;

/**
 * Created by Austin on 2/3/2017.
 */
public class DNSSDTest {

    DNSSD d;

    public DNSSDTest() {
        d = new DNSSD();
    }

    @Test
    public void serviceRegistrationTest() throws UnknownHostException {
        d.registerService("_html._tcp.local.", "testService", 9999, "test service description", null);
    }

    @Test
    public void serviceDiscoveryTest() throws UnknownHostException, InterruptedException {
        d.discoverService("_html._tcp.local.", null);
        while (d.getServiceInfo().equals("")) {
            Thread.sleep(100);
        }
        assertEquals("testService", d.getServiceName());
        d.closeServiceDiscovery();
    }
}
