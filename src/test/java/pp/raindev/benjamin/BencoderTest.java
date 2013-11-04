package pp.raindev.benjamin;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertArrayEquals;

/**
 * Test for {@link Bencoder}
 */
@SuppressWarnings("SpellCheckingInspection")
public class BencoderTest {

    private static final String charset = "utf-8";
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private Bencoder bencoder;

    @Before
    public void setUp() throws Exception {
        bencoder = new Bencoder(charset, output);
    }

    @Test
    public void encodeInteger() throws IOException {
        bencoder.encode(47);

        assertArrayEquals("Wrong integer encoding",
                "i47e".getBytes(), output.toByteArray());
    }

    @Test
    public void encodeNegativeInteger() throws IOException {
        bencoder.encode(-13);

        assertArrayEquals("Wrong negative integer encoding",
                "i-13e".getBytes(), output.toByteArray());
    }

    @Test
    public void encodeZero() throws IOException {
        bencoder.encode(0);

        assertArrayEquals("Wrong zero encoding",
                "i0e".getBytes(), output.toByteArray());
    }

    @Test
    public void encodeString() throws IOException {
        bencoder.encode("watermill⌘");

        assertArrayEquals("Wrong string encoding",
                "10:watermill⌘".getBytes(charset), output.toByteArray());
    }

    @Test
    public void encodeBytes() throws IOException {
        byte[] bytes = new byte[]{(byte) 0x65, (byte) 0x10, (byte) 0xf3, (byte) 0x29};
        bencoder.encode(bytes);
        byte[] encoded = output.toByteArray();

        assertArrayEquals("Wrong byte string length marker",
                "4:".getBytes(), Arrays.copyOfRange(encoded, 0, 2));
        assertArrayEquals("Byte strings should not be changed during encoding",
                bytes, Arrays.copyOfRange(encoded, 2, encoded.length));
    }

    @Test
    public void encodeList() throws IOException {
        bencoder.encode(Arrays.asList(new Object[]{47, "watermill⌘"}));

        assertArrayEquals("List encoded not properly",
                "li47e10:watermill⌘e".getBytes(charset), output.toByteArray());
    }

    @Test
    public void encodeDictionary() throws IOException {
        Map<String, Object> dictionary = new HashMap<>();
        dictionary.put("life", 47);
        dictionary.put("grass", "green");
        bencoder.encode(dictionary);

        assertArrayEquals("Dictionary encoded not properly",
                "d5:grass5:green4:lifei47ee".getBytes(), output.toByteArray());
    }

    @Test(expected = IllegalArgumentException.class)
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

        assertArrayEquals("Reversed dictionary encoded not properly",
                "d7:anupperi-12e3:endi47e5:starti42ee".getBytes(), output.toByteArray());
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

        assertArrayEquals("Dictionary-based tree is not encoded properly",
                "d5:innerd3:key5:valuee4:lifei47e4:listl5:hello5:worldi0ei-12ee3:sky4:greye".getBytes(charset),
                output.toByteArray());
    }

    @Test
    public void encodeComplexList() throws IOException {
        HashMap<Object, Object> dictionary = new HashMap<>();
        //noinspection OctalInteger
        dictionary.put("list", Arrays.asList("hello", "world", new byte[]{064, 067}));
        dictionary.put("zero", 0);
        List<Object> list = Arrays.asList(new Object[]{
                dictionary,
                13
        });
        bencoder.encode(list);

        assertArrayEquals("List-based tree is not encoded properly",
                "ld4:listl5:hello5:world2:47e4:zeroi0eei13ee".getBytes(),
                output.toByteArray());
    }
}
