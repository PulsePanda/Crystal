/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

import Heart.Heart_Core;
import Heart.Manager.GUIManager;
import org.junit.Test;

import java.awt.*;

public class GUIManagerTest {

    private GUIManager gm;
    private Heart_Core c;

    public GUIManagerTest() {
        c = new Heart_Core(false, true);
        gm = new GUIManager(c);
    }

    @Test
    public void testInitGUI() {
        gm.initGUI();
    }

    @Test
    public void testAppend() {
        gm.appendToPane("test", Color.BLACK);
    }
}
