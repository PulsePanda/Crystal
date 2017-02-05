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
