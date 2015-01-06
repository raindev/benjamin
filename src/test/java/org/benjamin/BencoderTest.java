package org.benjamin;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static org.testng.Assert.assertEquals;

/**
 * Test for {@link Bencoder}
 */
@SuppressWarnings("SpellCheckingInspection")
public class BencoderTest {

    private static final String charset = "utf-8";
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

    @Test
    public void encodeList() throws IOException {
        bencoder.encode(Arrays.asList(new Object[]{47, "watermill⌘"}));

        assertEquals(output.toByteArray(), "li47e10:watermill⌘e".getBytes(charset),
                "List encoded not properly");
    }

    @Test
    public void encodeDictionary() throws IOException {
        Map<String, Object> dictionary = new HashMap<>();
        dictionary.put("life", 47);
        dictionary.put("grass", "green");
        bencoder.encode(dictionary);

        assertEquals(output.toByteArray(), "d5:grass5:green4:lifei47ee".getBytes(),
                "Dictionary encoded not properly");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void encodeInvalidList() throws IOException {
        bencoder.encode(Arrays.asList(new Object[]{47, 47.9, "space"}));
    }

    @Test
    public void dictionaryOrder() throws IOException {
        Map<String, Object> dictionary = new LinkedHashMap<>();
        dictionary.put("end", 47);
        dictionary.put("start", 42);
        dictionary.put("anupper", -12);
        bencoder.encode(dictionary);

        assertEquals(output.toByteArray(), "d7:anupperi-12e3:endi47e5:starti42ee".getBytes(),
                "Reversed dictionary encoded not properly");
    }

    @Test
    public void encodeComplexDictionary() throws IOException {
        Map<String, Object> dictionary = new HashMap<>();
        dictionary.put("life", 47);
        dictionary.put("list", Arrays.asList("hello", "world", 0, -12));
        Map<String, Object> innerDictionary = new HashMap<>();
        innerDictionary.put("key", "value");
        dictionary.put("inner", innerDictionary);
        dictionary.put("sky", "grey");
        bencoder.encode(dictionary);

        assertEquals(
                output.toByteArray(),
                "d5:innerd3:key5:valuee4:lifei47e4:listl5:hello5:worldi0ei-12ee3:sky4:greye".getBytes(charset),
                "Dictionary-based tree is not encoded properly"
        );
    }

    @Test
    public void encodeComplexList() throws IOException {
        Map<String, Object> dictionary = new HashMap<>();
        //noinspection OctalInteger
        dictionary.put("list", Arrays.asList("hello", "world", new byte[]{064, 067}));
        dictionary.put("zero", 0);
        List<Object> list = Arrays.asList(dictionary, 13);
        bencoder.encode(list);

        assertEquals(output.toByteArray(), "ld4:listl5:hello5:world2:47e4:zeroi0eei13ee".getBytes(),
                "List-based tree is not encoded properly");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void encodeInvalidDictionary() throws IOException {
        Map<String, Object> dictionary = new HashMap<>();
        dictionary.put("key", "value");
        dictionary.put("oops!", 47.0);
        bencoder.encode(dictionary);
    }
}
