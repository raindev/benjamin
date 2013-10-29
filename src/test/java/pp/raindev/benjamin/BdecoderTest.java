package pp.raindev.benjamin;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class BdecoderTest {

    private static final String charset = "utf-8";
    private InputStream input;
    private Bdecoder bdecoder;

    @Test
    public void decodeInteger() throws IOException {
        input = new ByteArrayInputStream("i47e".getBytes());
        bdecoder = new Bdecoder(charset, input);

        assertEquals("Integer is not decoded properly",
                47, bdecoder.readInt());
    }

    @Test
    public void decodeZero() throws IOException {
        input = new ByteArrayInputStream("i0e".getBytes());
        bdecoder = new Bdecoder(charset, input);

        assertEquals("Zero is not decode properly",
                0, bdecoder.readInt());
    }

    @Test
    public void decodeNegative() throws IOException {
         input = new ByteArrayInputStream("i-47e".getBytes());
        bdecoder = new Bdecoder(charset, input);

        assertEquals("Negative integer is not decoded properly",
                -47, bdecoder.readInt());
    }

    /**
     * Negative zero disallowed in Bencode.
     * @throws IOException is an I/O error occurs
     */
    @Test(expected = IllegalArgumentException.class)
    public void negativeZero() throws IOException {
        input = new ByteArrayInputStream("i-0e".getBytes());
        bdecoder = new Bdecoder(charset, input);
        bdecoder.readInt();
    }

    @Test(expected = IllegalStateException.class)
    public void invalidPrefix() throws IOException {
        input = new ByteArrayInputStream("n-47e".getBytes());
        bdecoder = new Bdecoder(charset, input);
        bdecoder.readInt();
    }

    @Test(expected = IllegalStateException.class)
    public void missingPostfix() throws IOException {
        input = new ByteArrayInputStream("i47".getBytes());
        bdecoder = new Bdecoder(charset, input);
        bdecoder.readInt();
    }

    @Test(expected = IllegalStateException.class)
    public void emptyStream() throws IOException {
        input = new ByteArrayInputStream(new byte[]{});
        bdecoder = new Bdecoder(charset, input);
        bdecoder.readInt();
    }

    @Test
    public void decodeString() throws IOException {
        input = new ByteArrayInputStream("7:smileΩ".getBytes());
        bdecoder = new Bdecoder(charset, input);

        assertEquals("String is not decoded properly",
                "smileΩ", bdecoder.readString());
    }

    @Test
    public void decodeBytes() throws IOException {
        input = new ByteArrayInputStream("4:2397".getBytes());
        bdecoder = new Bdecoder(charset, input);

        //noinspection OctalInteger
        assertArrayEquals("Byte string is not decoded properly",
                new byte[]{062, 063, 071, 067}, bdecoder.readBytes());
    }

    @Test
    public void decodeList() throws IOException {
        input = new ByteArrayInputStream("l4:lanei47ee".getBytes());
        bdecoder = new Bdecoder(charset, input);

        assertEquals("List decoded not properly",
                Arrays.asList(new Object[]{"lane", 47}), bdecoder.readList());
    }

    @Test
    public void decodeDictionary() throws IOException {
        input = new ByteArrayInputStream("d3:key5:value3:sun5:grass1:ni5ee".getBytes());
        bdecoder = new Bdecoder(charset, input);

        Map<String, Object> dictionary = new HashMap<>();
        dictionary.put("key", "value");
        dictionary.put("n", 5);
        dictionary.put("sun", "grass");
        assertEquals("Dictionary decoded not properly",
                dictionary, bdecoder.readDictionary());
    }
}
