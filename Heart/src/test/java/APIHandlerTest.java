import Utilities.APIHandler;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by Austin on 2/3/2017.
 */
public class APIHandlerTest {

    APIHandler a;

    public APIHandlerTest() {
    }

    @Test
    public void apiTest() throws IOException {
        // Test initialize
        a = new APIHandler("https://jsonplaceholder.typicode.com/users/1");

        // test getStringItem
        assertEquals("Leanne Graham", a.getStringItem("name"));

        // test getIntItem
        assertEquals(1, a.getIntItem("id"));

        // test getJSONObject
        JSONObject jo = a.getJSONObject("address");
        assertEquals("Gwenborough", jo.getString("city"));

        // test getCurrentURL
        assertEquals("https://jsonplaceholder.typicode.com/users/1", a.getCurrentURL());
    }
}
