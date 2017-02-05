package Shard;

import Exceptions.ClientInitializationException;

public class ShardDriver {

    private static Shard_Core shardCore;
    private static boolean headlessArg = false;

	public static void main(String[] args) {
        for (String s : args) {
            s = s.toLowerCase();
            switch (s) {
                case "-h":
                    headlessArg = true;
                    break;
            }
        }

        try {
            shardCore = new Shard_Core(headlessArg);
            shardCore.init();
        } catch (ClientInitializationException ex) {
            System.err.println("Error starting Shard Core. Error: " + ex.getMessage());
        }

        ShardConnectionThread sct = new ShardConnectionThread(true, false);
        Thread t = new Thread(sct);
        t.start();
    }
}
