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
