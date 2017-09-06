/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

package Utilities;

import java.io.IOException;
import java.io.OutputStream;

public class MediaManagerProcessBuilder {

    private String mediaManagerPath = "../lib/MediaManager.jar";
    private String[] args;

    public MediaManagerProcessBuilder(String[] args) {
        this.args = new String[args.length + 3];
        this.args[0] = "java";
        this.args[1] = "-jar";
        this.args[2] = mediaManagerPath;

        for (int i = 0; i < args.length; i++) {
            this.args[i + 3] = args[i];
        }
    }

    public Process start() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.inheritIO();
        Process p = pb.start();

        return p;
    }
}
