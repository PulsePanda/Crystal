package Shard;

import Exceptions.ClientInitializationException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShardDriver {

    private static Shard_Core shardCore;

    private static void startCore(boolean headless) {
        try {
            shardCore = new Shard_Core(headless);
            shardCore.Init();
        } catch (ClientInitializationException ex) {
            System.err.println("Error starting Shard Core. Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        // Init Shard graphics and settings
        // String arg = args[0];
        // arg = arg.toLowerCase();
        String arg = "-gui";

        if (arg == "-headless") {
            startCore(true);
        } else if (arg == "-gui") {
            startCore(false);
        } else {
            System.err.println("No argument passed on call. Must specify if headless! Add -headless or -gui");
            System.exit(0);
        }

        // Init Shard networking, try to connect to the Heart
//        try {
//            shardCore.StartShardClient("localhost", 6987);
//        } catch (ClientInitializationException e) {
//            System.err.println("Error starting Shard client! Error message: " + e.getMessage());
//        }
        ShardConnectionThread sct = new ShardConnectionThread("localhost", 6987);
        Thread t = new Thread(sct);
        t.start();

        // TESTING ////////////////////
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e1) {
//            // TODO Auto-generated catch block
//            e1.printStackTrace();
//        }
        ///////////////////////////////
        // Establish connection and ready use with core
    }
}
