package fr.correntin.android.toilets_paris;

import org.junit.Test;

import fr.correntin.android.toilets_paris.utils.Utils;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UtilsUnitTest {

    @Test
    public void formatStreet_Test() {

        String str1 = "Boulevard Hausseman";
        String str2 = Utils.formatStreet("Boulevard Hausseman");
        assertEquals(true, str2.equals(str1));

        str2 = Utils.formatStreet("BOULEVARD HAUSSEMAN");
        assertEquals(true, str2.equals(str1));

        str2 = Utils.formatStreet("  BOULEVARD HAUSSEMAN");
        assertEquals(true, str2.equals(str1));

        str1 = "23 Rue Des Étoiles";
        str2 = Utils.formatStreet("23 rue des étoiles");
        assertEquals(true, str2.equals(str1));
    }

}