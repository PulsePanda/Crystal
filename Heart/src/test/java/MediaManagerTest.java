import Utilities.Media.MediaManager;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Austin on 2/3/2017.
 */
public class MediaManagerTest {

    MediaManager m;

    public MediaManagerTest() {
        m = new MediaManager("F:/Media", "F:/Media/music", "F:/Media/movies");
    }

    @Test
    public void managerTest() throws InterruptedException {
        m.index(false, 1);
        while (!m.isIndexed())
            Thread.sleep(100);
        assert ((m.getSongList().size() + m.getMovieList().size()) > 0);

        m.close();

        assertEquals("\\Media\\music\\10 Years\\Feeding the Wolves\\Fix Me.mp3", m.getSong("Fix Me.mp3")[0]);
        assertEquals("\\Media\\music\\10 Years\\Feeding the Wolves\\Fix Me.mp3", m.getSong("fix me.mp3")[0]);

        assertEquals("\\Media\\movies\\Avatar (2009).mkv", m.getMovie("Avatar (2009).mkv")[0]);
        assertEquals("\\Media\\movies\\Avatar (2009).mkv", m.getMovie("avatar (2009).mkv")[0]);
    }
}
