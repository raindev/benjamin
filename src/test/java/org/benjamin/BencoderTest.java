package org.benjamin;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.testng.Assert.assertEquals;

/**
 * Test for {@link Bencoder}
 */
@SuppressWarnings("SpellCheckingInspection")
public class BencoderTest {

    private static final Charset charset = StandardCharsets.UTF_8;
    private ByteArrayOutputStream output = new ByteArrayOutputStream();
    private Bencoder bencoder;

    @BeforeMethod
    public void setUp() throws Exception {
        output = new ByteArrayOutputStream();
        bencoder = new Bencoder(charset, output);
    }

    @DataProvider
    private Object[][] integers() {
        return new Object[][] {
            { 42         , "i42e"         },
            { 8589934592L, "i8589934592e" }, // bytes in 8Gb
            { -13        , "i-13e"        },
            { 0          , "i0e"          }
        };
    }

    @Test(dataProvider = "integers")
    public void encodeInteger(long integer, String encodedInteger) throws IOException {
        bencoder.encode(integer);

        assertEquals(output.toByteArray(), encodedInteger.getBytes(),
                "Wrong integer encoding");
    }

    @DataProvider
    private Object[][] strings() {
        return new Object[][] {
            { "hello world", "11:hello world" },
            { "watermill⌘" , "10:watermill⌘"  }
        };
    }

    @Test(dataProvider = "strings")
    public void encodeString(String string, String encodedString) throws IOException {
        bencoder.encode(string);

        assertEquals(output.toByteArray(), encodedString.getBytes(charset),
                "Wrong string encoding");
    }

    @Test
    public void encodeBytes() throws IOException {
        byte[] bytes = new byte[]{(byte) 0x65, (byte) 0x10, (byte) 0xf3, (byte) 0x29};
        bencoder.encode(bytes);
        byte[] encoded = output.toByteArray();

        assertEquals(Arrays.copyOfRange(encoded, 0, 2), "4:".getBytes(),
                "Wrong byte string length marker");
        assertEquals(Arrays.copyOfRange(encoded, 2, encoded.length), bytes,
                "Byte strings should not be changed during encoding");
    }

    @DataProvider
    private Object[][] lists() {
        return new Object[][] {
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
    public void encodeList(List<Object> list, String encodedList) throws IOException {
        bencoder.encode(list);

        assertEquals(output.toByteArray(), encodedList.getBytes(charset),
                "List encoded not properly");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void encodeInvalidList() throws IOException {
        bencoder.encode(Arrays.asList(new Object[]{47, 47.9, "space"}));
    }

    @DataProvider
    private Object[][] dictionaries() {
        return new Object[][] {
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
                    put("sky", "grey");
                }},
                "d5:innerd3:key5:valuee4:lifei47e4:listl5:hello5:worldi0ei-12ee3:sky4:greye"
            }
        };
    }

    @Test(dataProvider = "dictionaries")
    public void encodeDictionary(Map<String, Object> dictionary, String encodedDictionary) throws IOException {
        bencoder.encode(dictionary);

        assertEquals(output.toByteArray(), encodedDictionary.getBytes(),
                "Dictionary encoded not properly");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void encodeInvalidDictionary() throws IOException {
        Map<String, Object> dictionary = new HashMap<>();
        dictionary.put("key", "value");
        dictionary.put("oops!", 47.0);
        bencoder.encode(dictionary);
    }
}
