package org.benjamin;

import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.*;
import static org.testng.Assert.assertEquals;

/**
 * Test for {@code Bdecoder}.
 */
@SuppressWarnings("SpellCheckingInspection")
public class BdecoderTest {

    private Bdecoder bdecoder;

    @DataProvider
    private Object[][] integers() {
        return new Object[][] {
            { "i42e"     , 42             },
            { "i0e"      , 0              },
            { "i-47e"    , -47            },
            { "i8589934592e", 8589934592L }, // bytes in 8Gb
        };
    }

    @Test(dataProvider = "integers")
    public void decodeInteger(String encodedInt, long decoded) throws IOException {
        assertEquals(
                new Bdecoder(UTF_8, encodedInt).readInt(),
                decoded,
                "Integer is not decoded properly"
        );
    }

    @DataProvider
    private Object[][] invalidIntegers() {
        return new Object[][] {
            { ""      }, // empty stream
            { "n-47e" }, // invalid prefix
            { "i47"   }  // no end mark
        };
    }

    @Test(dataProvider = "invalidIntegers",
            expectedExceptions = IllegalStateException.class)
    public void decodeInvalidInteger(String invalidInteger) throws IOException {
        new Bdecoder(UTF_8, invalidInteger).readInt();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void negativeZero() throws IOException {
        bdecoder = new Bdecoder(UTF_8, "i-0e");
        bdecoder.readInt();
    }

    @DataProvider
    private Object[][] strings() {
        return new Object[][] {
            { "3:sun"   , "sun"    },
            { "7:smileΩ", "smileΩ" }, // Unicode string
            { "0:"      , ""       }  // empty string
        };
    }

    @Test(dataProvider = "strings")
    public void decodeString(String encodedString, String decoded) throws IOException {
        assertEquals(
                new Bdecoder(UTF_8, encodedString).readString(),
                decoded,
                "String is not decoded properly"
        );
    }

    @DataProvider
    private Object[][] invalidStrings() {
        return new Object[][] {
            { "6:four" }, // too short string
            { "4four"  }, // missing separator
            { ":four"  }, // missing length
            { ""       }, // empty stream
        };
    }

    @Test(dataProvider = "invalidStrings",
            expectedExceptions = IllegalStateException.class)
    public void decodeInvalidString(String invalidString) throws IOException {
        new Bdecoder(UTF_8, invalidString).readString();
    }

    @Test
    public void decodeBytes() throws IOException {
        bdecoder = new Bdecoder(UTF_8, "4:2397");

        //noinspection OctalInteger
        assertEquals(bdecoder.readBytes(), new byte[]{062, 063, 071, 067},
                "Byte string is not decoded properly");
    }

    @DataProvider
    private Object[][] invalidBytes() {
        return new Object[][] {
            { "9:2532" }, // too short bytes
            { "43331"  }, // missing separator
            { ":82382" }, // missing length
            { ""       }  // empty stream
        };
    }

    @Test(dataProvider = "invalidBytes",
            expectedExceptions = IllegalStateException.class)
    public void decodeInvalidBytes(String invalidBytes) throws IOException {
        new Bdecoder(UTF_8, invalidBytes).readBytes();
    }

    @DataProvider
    private Object[][] lists() {
        return new Object[][] {
            { "l4:lanei47ee", Arrays.asList( new Object[]{ "lane", 47L } ) },
            {
                "l2:coi47eli47ei42eed4:lifei42eee",
                Arrays.asList(
                    "co",
                    47L,
                    Arrays.asList(47L, 42L),
                    new HashMap<String, Long>() {{
                        put("life", 42L);
                    }}
                )
            }
        };
    }

    @Test(dataProvider = "lists")
    public void decodeList(String encodedList, List<?> decoded) throws IOException {
        assertEquals(
                new Bdecoder(UTF_8, encodedList).readList(),
                decoded,
                "List decoded not properly"
        );
    }

    @DataProvider
    private Object[][] invalidLists() {
        return new Object[][] {
            { "4:lanei47ee" }, // prefix missing
            { "l4:lanei47e" }, // end mark missing
            { ""            }  // empty stream
        };
    }

    @Test(dataProvider = "invalidLists",
            expectedExceptions = IllegalStateException.class)
    public void decodeInvalidList(String invalidList) throws IOException {
        new Bdecoder(UTF_8, invalidList).readList();
    }

    @DataProvider
    private Object[][] dictionaries() {
        return new Object[][] {
            {
                "d3:key5:value3:sun5:grass1:ni5ee",
                new HashMap<String, Object>() {{
                    put("key", "value");
                    put("n", 5L);
                    put("sun", "grass");
                }}
            },
            {
                "d4:listl2:co4:worke10:dictionaryd3:key5:valueee",
                new HashMap<String, Object>() {{
                    put("list", Arrays.asList("co", "work"));
                    put("dictionary", new HashMap<String, String>() {{
                        put("key", "value");
                    }});
                }}
            }
        };
    }

    @Test(dataProvider = "dictionaries")
    public void decodeDictionary(String encodedDictionary, Map<?,?> decoded) throws IOException {
        assertEquals(
                new Bdecoder(UTF_8, encodedDictionary).readDictionary(),
                decoded,
                "Dictionary decoded not properly"
        );
    }

    @DataProvider
    private Object[][] invalidDictionaries() {
        return new Object[][] {
            { "3:key5:valueee" }, // missing prefix
            { "d3:key5:value"  }, // end mark missing
            { ""               }  // empty stream
        };
    }

    @Test(dataProvider = "invalidDictionaries",
            expectedExceptions = IllegalStateException.class)
    public void decodeInvalidDictionary(String invalidDictionary) throws IOException {
        new Bdecoder(UTF_8, invalidDictionary).readDictionary();
    }
}
