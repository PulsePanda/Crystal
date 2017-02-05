package Heart;

import Exceptions.ServerInitializationException;

public class HeartDriver {

    private static Heart_Core heartCore;
    private static int corePort = 6987;

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

        heartCore = new Heart_Core(headlessArg, dev);
        heartCore.init();

        // init Heart networking, start listening for Shards
        try {
            System.out.println("Starting Heart server on port " + corePort);
            heartCore.startHeartServer(corePort);

            Thread.sleep(1000);

            // If the server is unable to host on the specified port, try another one
            while (!heartCore.isServerActive()) {
                System.out.println("Attempting to host Heart server on port " + ++corePort);
                heartCore.stopHeartServer();
                heartCore.startHeartServer(corePort);

                if (!heartCore.isServerActive())
                    Thread.sleep(3000);
            }

            heartCore.initDNSSD();
        } catch (ServerInitializationException e) {
            System.err.println("Error starting Heart server! Error message: " + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
