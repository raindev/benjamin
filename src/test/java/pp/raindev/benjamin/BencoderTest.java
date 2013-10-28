package pp.raindev.benjamin;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;

/**
 * Test for {@link Bencoder}
 */
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
        assertArrayEquals("Byte strings should not be changed during encoding",
                bytes, Arrays.copyOfRange(encoded, 2, encoded.length));
        assertArrayEquals("Wrong byte string length marker",
                "4:".getBytes(), Arrays.copyOfRange(encoded, 0, 2));
    }

    @Test
    public void encodeList() throws IOException {
        bencoder.encode(Arrays.asList(new Object[]{47, "watermill⌘"}));
        assertArrayEquals("List encoded not properly",
                "li47e10:watermill⌘e".getBytes(), output.toByteArray());
        System.out.println(Arrays.toString(output.toByteArray()));
    }

    @Test
    public void encodeDictionary() throws IOException {
        Map<String, Object> dictionary = new HashMap<>();
        dictionary.put("life", 47);
        dictionary.put("grass", "green");
        bencoder.encode(dictionary);
        //noinspection SpellCheckingInspection
        assertArrayEquals("Dictionary encoded not properly",
                "d4:lifei47e5:grass5:greene".getBytes(), output.toByteArray());
    }
}
