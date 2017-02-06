/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

import Utilities.SystemInfo;
import org.junit.Test;

/**
 * Created by Austin on 2/3/2017.
 */
public class SystemInfoTest {

    @Test
    public void getSystem_osTest() {
        if (SystemInfo.getSystem_os().toString().toLowerCase().equals("error")) {
            assert (false);
        } else {
            assert (true);
        }
    }
}
