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
