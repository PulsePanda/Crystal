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
 * Shard Connection Thread
 * <p>
 * Handles the connection for the Shard
 */
public class ShardConnectionThread implements Runnable {

    private Shard_Core sc;
    private boolean keepRunning, patchOnly;

    /**
     * Default constructor
     *
     * @param keepRunning Boolean keep trying to connect
     * @param patchOnly   Boolean is the connection only for patching
     */
    public ShardConnectionThread(boolean keepRunning, boolean patchOnly) {
        sc = Shard_Core.getShardCore();
        this.keepRunning = keepRunning;
        this.patchOnly = patchOnly;
    }

    /**
     * Run constantly. Every 10 seconds, if the Shard is not connected to the
     * Heart, try to connect again.
     */
    @Override
    public void run() {
        while (true) {
            try {
                if (!patchOnly)
                    sc.startShardClient();
                sc.initPatcher();
            } catch (ClientInitializationException e) {
                // If the shard is already connected, this is executed. Don't need to do anything, just keep looping
            }
            if (!keepRunning)
                return;
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
            }
        }
    }
}
