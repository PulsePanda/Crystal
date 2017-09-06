/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

package Shard;

import Exceptions.ClientInitializationException;

/**
 * Driver for Shard. Initializes needed objects and starts threads
 */
public class ShardDriver {

    private static Shard_Core shardCore;
    private static boolean headlessArg = false, dev = false;

    public static void main(String[] args) {
        for (String s : args) {
            s = s.toLowerCase();
            switch (s) {
                case "-h":
                    headlessArg = true;
                    break;
                case "-dev":
                    dev = true;
                    break;
            }
        }

        startShard();
    }

    private static void startShard() {
        try {
            shardCore = new Shard_Core(headlessArg, dev);
            shardCore.init();
        } catch (ClientInitializationException ex) {
            System.err.println("Error starting Shard Core. Error: " + ex.getMessage());
        }

        shardCore.getConnectionManager().startConnectionThread();
    }
}
