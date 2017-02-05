import Utilities.Log;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by Austin on 2/3/2017.
 */
public class LogTest {

    Log log;

    public LogTest() {
        log = new Log();
    }

    @Test
    public void createLogTest() throws IOException {
        log.createLog("C:/");
    }

    @Test
    public void writeTest() throws IOException {
        log.write("test");
    }

    @Test
    public void closeTest() throws IOException {
        log.close();
    }
}
