package org.benjamin;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static java.nio.charset.StandardCharsets.*;
import static org.testng.Assert.assertEquals;

@Test
public class BencoderTest {

    ByteArrayOutputStream output;
    Bencoder bencoder;

    @BeforeMethod
    void setUp() {
        output = new ByteArrayOutputStream();
        bencoder = new Bencoder(UTF_8, output);
    }

    @DataProvider
    Object[][] integers() {
        return new Object[][] {
            { 42         , "i42e"         },
            { 8589934592L, "i8589934592e" }, // bytes in 8Gb
            { -13        , "i-13e"        },
            { 0          , "i0e"          }
        };
    }

    @Test(dataProvider = "integers")
    void encodeInteger(long integer, String encodedInteger) throws IOException {
        bencoder.encode(integer);

        assertEquals(output.toByteArray(), encodedInteger.getBytes(US_ASCII));
    }

    @DataProvider
    Object[][] strings() {
        return new Object[][] {
            { "hello world", "11:hello world" },
            { "watermill⌘" , "10:watermill⌘"  },
            { ""           , "0:"             }
        };
    }

    @Test(dataProvider = "strings")
    void encodeString(String string, String encodedString) throws IOException {
        bencoder.encode(string);

        assertEquals(output.toByteArray(), encodedString.getBytes(UTF_8));
    }

    @Test
    void encodeBytes() throws IOException {
        byte[] bytes = new byte[]{(byte) 0x65, (byte) 0x10, (byte) 0xf3, (byte) 0x29};
        bencoder.encode(bytes);

        byte[] encoded = output.toByteArray();
        assertEquals(Arrays.copyOfRange(encoded, 0, 2), "4:".getBytes(US_ASCII),
                "Wrong byte string length marker");
        assertEquals(Arrays.copyOfRange(encoded, 2, encoded.length), bytes,
                "Byte strings should not be changed during encoding");
    }

    @DataProvider
    @SuppressWarnings("PMD.AvoidUsingOctalValues")
    Object[][] lists() {
        return new Object[][] {
            { Collections.emptyList()                      , "le"                  },
            { Arrays.asList(new Object[]{47, "watermill⌘"}), "li47e10:watermill⌘e" },
            {
                Arrays.asList(
                        new HashMap<String, Object>() {{
                            put("list", Arrays.asList("hello", "world", new byte[]{064, 067}));
                            put("zero", 0);
                        }},
                        13
                ),
                "ld4:listl5:hello5:world2:47e4:zeroi0eei13ee"
            }
        };
    }

    @Test(dataProvider = "lists")
    void encodeList(List<?> list, String encodedList) throws IOException {
        bencoder.encode(list);

        assertEquals(output.toByteArray(), encodedList.getBytes(UTF_8));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    void encodeInvalidList() throws IOException {
        bencoder.encode(Arrays.asList(new Object[]{47, 47.9, "space"}));
    }

    @DataProvider
    Object[][] dictionaries() {
        return new Object[][] {
            { Collections.emptyMap(), "de" },
            {
                new HashMap<String, Object>() {{
                    put("life", 47);
                    put("grass", "green");
                }},
                "d5:grass5:green4:lifei47ee"
            },
            {
                // enforce order reverse to expected in result
                new LinkedHashMap<String, Object>() {{
                    put("end", 47);
                    put("start", 42);
                    put("anupper", -12);
                }},
                "d7:anupperi-12e3:endi47e5:starti42ee"
            },
            {
                new HashMap<String, Object>() {{
                    put("life", 47);
                    put("list", Arrays.asList("hello", "world", 0, -12));
                    put("inner", new HashMap<String, String>() {{
                        put("key", "value");
                    }});
                    put("sk❅", "grey");
                }},
                "d5:innerd3:key5:valuee4:lifei47e4:listl5:hello5:worldi0ei-12ee3:sk❅4:greye"
            }
        };
    }

    @Test(dataProvider = "dictionaries")
    void encodeDictionary(Map<String, ?> dictionary, String encodedDictionary) throws IOException {
        bencoder.encode(dictionary);

        assertEquals(output.toByteArray(), encodedDictionary.getBytes(UTF_8));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    void encodeInvalidDictionary() throws IOException {
        Map<String, Object> dictionary = new HashMap<>();
        dictionary.put("key", "value");
        dictionary.put("oops!", 47.0);
        bencoder.encode(dictionary);
    }

    @Test
    void chainedEncoding() throws IOException {
        bencoder
            .encode(5)
            .encode("ello")
            .encode(new byte[]{49, 50})
            .encode(Arrays.<Object>asList(4, 3))
            .encode(new HashMap<String, Object>(){{ put("k", "Ω"); }})
            .encode(9);
        assertEquals(output.toByteArray(), "i5e4:ello2:12li4ei3eed1:k1:Ωei9e".getBytes(UTF_8));
    }
}
