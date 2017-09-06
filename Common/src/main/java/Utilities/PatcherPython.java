/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

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

/**
 * UnZipPython
 * <p>
 * Utility to unzip zip files into output folders
 */
public class PatcherPython {

    /**
     * Launch the patcher script
     *
     * @throws IOException thrown if there is an error spawning the script
     */
    public static void patch(boolean devBuild, boolean launchAfter, boolean forceHeart, boolean forceShard) throws IOException {
        String[] params = null;
        String dev = "", launch = "", forceH = "", forceS = "";
        if (devBuild)
            dev = "-dev";
        if (launchAfter)
            launch = "-launch";
        if (forceHeart)
            forceH = "-forceHeart";
        if (forceShard)
            forceS = "-forceShard";

        if (SystemInfo.getSystem_os() == SystemInfo.SYSTEM_OS.Windows)
            params = new String[]{"py", "../lib/Crystal_Install_Script.py", dev, launch, forceH, forceS};
        else if (SystemInfo.getSystem_os() == SystemInfo.SYSTEM_OS.Linux)
            params = new String[]{"python3", "../lib/Crystal_Install_Script.py", dev, launch, forceH, forceS};

        Runtime.getRuntime().exec(params);
    }
}
