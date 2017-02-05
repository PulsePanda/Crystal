import Utilities.Media.Music;
import org.junit.Test;

/**
 * Created by Austin on 2/3/2017.
 */
public class MusicTest {

    Music m;

    public MusicTest() {
        m = new Music("file://shockwave/Media/music/10 Years/Feeding the Wolves/Fix Me.mp3");
    }

    @Test
    public void playTest() {
        m.play();
    }
}
