/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

import Utilities.Media.ListItem;
import Utilities.Media.MediaList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Austin on 2/3/2017.
 */
public class MediaListTest {

    MediaList m;

    public MediaListTest() {
        m = new MediaList();
    }

    @Test
    public void itemTest() {
        assert (m.addItem("testMusic", "testMusicPath", ListItem.MEDIA_TYPE.Music));
        assert (m.addItem("testMovie", "testMoviePath", ListItem.MEDIA_TYPE.Movie));

        assert (m.contains("testMusic"));
        assert (m.contains("testMusicPath"));
        assert (m.contains("testMovie"));
        assert (m.contains("testMoviePath"));

        assert (m.size() == 2);

        assertEquals("testMusic", m.get("testMusic")[0].getName());
        assertEquals("testMusic", m.get("testMusicPath")[0].getName());
        assertEquals("testMusicPath", m.get("testMusic")[0].getPath());
        assertEquals("testMusicPath", m.get("testMusicPath")[0].getPath());

        assert (m.removeItem("testMusic"));
        assert (m.removeItem("testMoviePath"));

        assert (!m.contains("testMusic"));
        assert (!m.contains("testMusicPath"));
        assert (!m.contains("testMovie"));
        assert (!m.contains("testMoviePath"));
    }
}
