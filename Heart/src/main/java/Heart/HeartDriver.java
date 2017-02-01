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
        heartCore.Init();

        // Init Heart networking, start listening for Shards
        try {
            System.out.println("Starting Heart server on port " + corePort);
            heartCore.StartServer(corePort);

//            while(!heartCore.IsServerActive()){
//                heartCore.StopHeartServer();
//                heartCore.StartServer(++corePort);
//            }

            heartCore.InitDNSSD();
        } catch (ServerInitializationException e) {
            System.err.println("Error starting Heart server! Error message: " + e.getMessage());
        }
    }
}
