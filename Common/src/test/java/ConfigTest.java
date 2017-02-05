import Exceptions.ConfigurationException;
import Utilities.Config;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by Austin on 2/3/2017.
 */
public class ConfigTest {

    Config c;

    public ConfigTest() throws IOException, ConfigurationException {
        File f = new File("C:/configTest.cfg");
        if (!f.exists()) f.createNewFile();
        c = new Config("C:/configTest.cfg");
    }

    @Test
    public void existsTest() {
        assert (c.exists());
    }

    @Test
    public void setTest() throws ConfigurationException {
        c.set("testValue", "test");
        c.save();
    }

    @Test
    public void getTest() {
        assertEquals("test", c.get("testValue"));
    }

    @Test
    public void reloadTest() throws ConfigurationException {
        c.reload();
    }

    @Test
    public void deleteTestFile() {
        File f = new File("C:/configTest.cfg");
        f.delete();
    }
}
