package Heart;

import Exceptions.ServerInitializationException;
import Utilities.DNSSD;

import java.io.IOException;

public class HeartDriver {

    private static Heart_Core heartCore;
    public static UpdateCheckerThread updateCheckerThread;
    private static int corePort = 6987, dnsPort = 6980;

    public static void main(String[] args) {
        boolean headlessArg = false;
        // TODO set dev to default false
        boolean dev = true;

        for (String s : args) {
            s = s.toLowerCase();
            switch (s) {
                case "-h":
                    headlessArg = true;
                    break;
                case "-dev":
                    dev = true;
            }
        }

        DNSSD dnssd = new DNSSD();
        dnssd.registerService("_heartServer", "Heart Core Server", dnsPort, "Heart Core Test Service");
		dnssd.discoverService("_heartServer");

        heartCore = new Heart_Core(headlessArg, dev);
        heartCore.Init();

        // Init Heart networking, start listening for Shards
        try {
            System.out.println("Starting Heart server on corePort " + corePort);
            heartCore.StartServer(corePort);
        } catch (ServerInitializationException e) {
            System.err.println("Error starting Heart server! Error message: " + e.getMessage());
        }
    }
}
