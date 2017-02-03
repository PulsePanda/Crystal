import Utilities.Media.ListItem;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Austin on 2/3/2017.
 */
public class ListItemTest {

    ListItem l;

    public ListItemTest() {
        l = new ListItem("test", "testPath", ListItem.MEDIA_TYPE.Music);
    }

    @Test
    public void itemTest() {
        assertEquals("test", l.getName());
        assertEquals("testPath", l.getPath());
        assertEquals("music", l.getMediaType());
    }
}
