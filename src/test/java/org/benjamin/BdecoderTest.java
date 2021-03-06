package org.benjamin;

import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;

import java.io.IOException;
import java.util.*;

import static java.nio.charset.StandardCharsets.*;
import static org.testng.Assert.assertEquals;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

@Test
@SuppressWarnings("PMD.TooManyMethods")
public class BdecoderTest {

    Bdecoder bdecoder;

    @DataProvider
    Object[][] integers() {
        return new Object[][] {
            { "i42e"        , 42          },
            { "i0e"         , 0           },
            { "i-47e"       , -47         },
            { "i8589934592e", 8589934592L }, // bytes in 8Gb
        };
    }

    @Test(dataProvider = "integers")
    void decodeInteger(String encodedInt, long decoded) throws IOException {
        assertEquals(new Bdecoder(UTF_8, encodedInt).decodeInt(), decoded);
    }

    @DataProvider
    Object[][] invalidIntegers() {
        return new Object[][] {
            { ""      }, // empty stream
            { "n-47e" }, // invalid prefix
            { "i47"   }, // no end mark
            { "i09e"  }, // zero padding
            // Bencode specification says that only significant digits should be used
            // in integers and explicitly states that negative zero is prohibited
            { "i-0e"  }
        };
    }

    @Test(dataProvider = "invalidIntegers", expectedExceptions = IllegalStateException.class)
    void decodeInvalidInteger(String invalidInteger) throws IOException {
        new Bdecoder(UTF_8, invalidInteger).decodeInt();
    }

    @DataProvider
    Object[][] strings() {
        return new Object[][] {
            { "3:sun"         , "sun"         },
            { "7:smileΩ"      , "smileΩ"      }, // Unicode string
            { "0:"            , ""            }, // empty string
            // strings containing separators aren't encoded in any special way
            { "11::in:the:sky", ":in:the:sky" }
        };
    }

    @Test(dataProvider = "strings")
    void decodeString(String encodedString, String decoded) throws IOException {
        assertEquals(new Bdecoder(UTF_8, encodedString).decodeString(), decoded);
    }

    @DataProvider
    Object[][] invalidStrings() {
        return new Object[][] {
            { "6:four" }, // too short string
            { "4four"  }, // missing separator
            { ":four"  }, // missing length
            { ""       }, // empty stream
        };
    }

    @Test(dataProvider = "invalidStrings", expectedExceptions = IllegalStateException.class)
    void decodeInvalidString(String invalidString) throws IOException {
        new Bdecoder(UTF_8, invalidString).decodeString();
    }

    @Test
    void decodeBytes() throws IOException {
        bdecoder = new Bdecoder(UTF_8, "4:2397");

        assertEquals(bdecoder.decodeBytes(), new byte[]{0x32, 0x33, 0x39, 0x37});
    }

    @DataProvider
    Object[][] invalidBytes() {
        return new Object[][] {
            { "9:2532" }, // too short bytes
            { "43331"  }, // missing separator
            { ":82382" }, // missing length
            { ""       }  // empty stream
        };
    }

    @Test(dataProvider = "invalidBytes", expectedExceptions = IllegalStateException.class)
    void decodeInvalidBytes(String invalidBytes) throws IOException {
        new Bdecoder(UTF_8, invalidBytes).decodeBytes();
    }

    @DataProvider
    Object[][] lists() {
        return new Object[][] {
            { "le"          , Collections.emptyList()                      },
            { "l4:lanei47ee", Arrays.asList("lane", 47L)   },
            {
                "l2:coi47el5:spacei42eed4:lifei42eee",
                Arrays.asList(
                    "co",
                    47L,
                    Arrays.asList("space", 42L),
                    new HashMap<String, Long>() {{
                        put("life", 42L);
                    }}
                )
            }
        };
    }

    @Test(dataProvider = "lists")
    void decodeList(String encodedList, List<Object> decoded) throws IOException {
        assertReflectionEquals(decoded, new Bdecoder(UTF_8, encodedList).decodeList());
    }

    @DataProvider
    Object[][] invalidLists() {
        return new Object[][] {
            { "4:lanei47ee" }, // prefix missing
            { "l4:lanei47e" }, // end mark missing
            { ""            }  // empty stream
        };
    }

    @Test(dataProvider = "invalidLists",
            expectedExceptions = IllegalStateException.class)
    void decodeInvalidList(String invalidList) throws IOException {
        new Bdecoder(UTF_8, invalidList).decodeList();
    }

    @DataProvider
    Object[][] dictionaries() {
        return new Object[][] {
            { "de", Collections.emptyMap() },
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
                    put("dictionary", new HashMap<String, Object>() {{
                        put("key", "value");
                    }});
                }}
            }
        };
    }

    @Test(dataProvider = "dictionaries")
    void decodeDictionary(
            String encodedDictionary,
            Map<String, Object> decoded
    ) throws IOException {
        assertReflectionEquals(decoded, new Bdecoder(UTF_8, encodedDictionary).decodeDict());
    }

    @DataProvider
    Object[][] binaryDictionaries() {
        return new Object[][] {
            {
               new String[]{"bin"},
               "d3:str2:Δ3:bin5:bytese",
               new HashMap<String, Object>() {{
                    put("str", "Δ");
                    put("bin", "bytes".getBytes(US_ASCII));
               }}
            },
            {
                new String[]{"dict.bin"},
                "d4:dictd3:bin5:bytese3:str6:stringe",
                new HashMap<String, Object>() {{
                    put("dict", new HashMap<String, Object>(){{
                        put("bin", "bytes".getBytes(US_ASCII));
                    }});
                    put("str", "string");
                }}
            }
        };
    }

    @Test(dataProvider = "binaryDictionaries")
    void decodeDictionaryBinary(String[] byteStrings, String encodedDictionary,
            Map<String, Object> decoded) throws IOException {
        assertReflectionEquals(
            decoded,
            new Bdecoder(UTF_8, encodedDictionary).decodeDict(byteStrings)
        );
    }

    @DataProvider
    Object[][] invalidDictionaries() {
        return new Object[][] {
            { "3:key5:valueee" }, // missing prefix
            { "d3:key5:value"  }, // end mark missing
            { ""               }  // empty stream
        };
    }

    @Test(dataProvider = "invalidDictionaries", expectedExceptions = IllegalStateException.class)
    void decodeInvalidDictionary(String invalidDictionary) throws IOException {
        new Bdecoder(UTF_8, invalidDictionary).decodeDict();
    }
}
