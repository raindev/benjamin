package pp.raindev.benjamin;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

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
    public void encodeInteger() throws UnsupportedEncodingException {
        bencoder.encode(47);
        assertArrayEquals("Wrong integer encoding",
                "i47e".getBytes(charset), output.toByteArray());
    }

    @Test
    public void encodeString() throws UnsupportedEncodingException {
        bencoder.encode("watermill⌘");
        assertArrayEquals("Wrong string encoding",
                "10:watermill⌘".getBytes(charset), output.toByteArray());
    }

    @Test
    public void encodeBytes() {
        byte[] bytes = new byte[]{(byte) 0x65, (byte) 0x10, (byte) 0xf3, (byte) 0x29};
        bencoder.encode(bytes);
        assertArrayEquals("Byte strings should not be changed during encoding",
                bytes, output.toByteArray());
    }

    @Test
    public void encodeList() throws UnsupportedEncodingException {
        bencoder.encode(Arrays.asList(new Object[]{47, "watermill⌘"}));
        assertEquals("List encoded not properly",
                "li47e10:watermill⌘e".getBytes(charset), output.toByteArray());
    }

    @Test
    public void encodeDictionary() {
        Map<String, Object> dictionary = new HashMap<>();
        bencoder.encode(dictionary);
        //noinspection SpellCheckingInspection
        assertEquals("Dictionary encoded not properly",
                "d4:lifei47e5:grass5:greene");
    }
}
