/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

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
