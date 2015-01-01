package org.benjamin;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Test for {@code Bdecoder}.
 */
@SuppressWarnings("SpellCheckingInspection")
public class BdecoderTest {

    private static final String charset = "utf-8";
    private Bdecoder bdecoder;

    @Test
    public void decodeInteger() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("i47e"));

        assertEquals("Integer is not decoded properly",
                47, bdecoder.readInt());
    }

    @Test
    public void decodeZero() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("i0e"));

        assertEquals("Zero is not decode properly",
                0, bdecoder.readInt());
    }

    @Test
    public void decodeNegativeInteger() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("i-47e"));

        assertEquals("Negative integer is not decoded properly",
                -47, bdecoder.readInt());
    }

    @Test
    public void decodeLongInteger() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("i2438987776e"));

        assertEquals("Numbers larger than 32 bits should be handled",
                2438987776L, bdecoder.readInt());
    }

    /**
     * Negative zero disallowed in Bencode.
     *
     * @throws IOException is an I/O error occurs
     */
    @Test(expected = IllegalArgumentException.class)
    public void negativeZero() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("i-0e"));
        bdecoder.readInt();
    }

    @Test(expected = IllegalStateException.class)
    public void invalidIntegerPrefix() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("n-47e"));
        bdecoder.readInt();
    }

    @Test(expected = IllegalStateException.class)
    public void missingIntegerPostfix() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("i47"));
        bdecoder.readInt();
    }

    @Test(expected = IllegalStateException.class)
    public void emptyIntegerStream() throws IOException {
        bdecoder = new Bdecoder(charset, new ByteArrayInputStream(new byte[]{}));
        bdecoder.readInt();
    }

    @Test
    public void decodeString() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("7:smileΩ"));

        assertEquals("String is not decoded properly",
                "smileΩ", bdecoder.readString());
    }

    @Test(expected = IllegalStateException.class)
    public void tooShortString() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("6:four"));
        bdecoder.readString();
    }

    @Test(expected = IllegalStateException.class)
    public void missingStringSeparator() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("4four"));
        bdecoder.readString();
    }

    @Test(expected = IllegalStateException.class)
    public void missingStringLength() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream(":four"));
        bdecoder.readString();
    }

    @Test(expected = IllegalStateException.class)
    public void emptyStringStream() throws IOException {
        bdecoder = new Bdecoder(charset, new ByteArrayInputStream(new byte[]{}));
        bdecoder.readString();
    }

    @Test
    public void decodeBytes() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("4:2397"));

        //noinspection OctalInteger
        assertArrayEquals("Byte string is not decoded properly",
                new byte[]{062, 063, 071, 067}, bdecoder.readBytes());
    }

    @Test(expected = IllegalStateException.class)
    public void tooShortBytes() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("9:2532"));
        bdecoder.readBytes();
    }

    @Test(expected = IllegalStateException.class)
    public void missingBytesSeparator() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("43331"));
        bdecoder.readBytes();
    }

    @Test(expected = IllegalStateException.class)
    public void missingBytesLength() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream(":82382"));
        bdecoder.readBytes();
    }

    @Test(expected = IllegalStateException.class)
    public void emptyBytesStream() throws IOException {
        bdecoder = new Bdecoder(charset, new ByteArrayInputStream(new byte[]{}));
        bdecoder.readBytes();
    }

    @Test
    public void decodeList() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("l4:lanei47ee"));

        assertEquals("List decoded not properly",
                Arrays.asList(new Object[]{"lane", 47L}), bdecoder.readList());
    }

    @Test
    public void decodeComplexList() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("l2:coi47eli47ei42eed4:lifei42eee"));

        HashMap<String, Object> dictionary = new HashMap<>();
        dictionary.put("life", 42L);
        List<Object> list = Arrays.asList("co", 47L, Arrays.asList(47L, 42L), dictionary);

        assertEquals("List-based tree is not decoded properly",
                list, bdecoder.readList());
    }

    @Test(expected = IllegalStateException.class)
    public void listPrefixMissing() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("4:lanei47ee"));
        bdecoder.readList();
    }

    @Test(expected = IllegalStateException.class)
    public void listPostfixMissing() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("l4:lanei47e"));
        bdecoder.readList();
    }

    @Test(expected = IllegalStateException.class)
    public void listEmptyStream() throws IOException {
        bdecoder = new Bdecoder(charset, new ByteArrayInputStream(new byte[]{}));
        bdecoder.readList();
    }

    @Test
    public void decodeDictionary() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("d3:key5:value3:sun5:grass1:ni5ee"));

        Map<String, Object> dictionary = new HashMap<>();
        dictionary.put("key", "value");
        dictionary.put("n", 5L);
        dictionary.put("sun", "grass");

        assertEquals("Dictionary decoded not properly",
                dictionary, bdecoder.readDictionary());
    }

    @Test
    public void decodeComplexDictionary() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("d4:listl2:co4:worke6:stringd3:key5:valueee"));

        Map<String, Object> dictionary = new HashMap<>();
        dictionary.put("list", Arrays.asList("co", "work"));
        Map<String, Object> innerDictionary = new HashMap<>();
        innerDictionary.put("key", "value");
        dictionary.put("string", innerDictionary);

        assertEquals("Dictionary-based tree decoded not properly",
                dictionary, bdecoder.readDictionary());
    }

    @Test(expected = IllegalStateException.class)
    public void dictionaryPrefixMissing() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("3:key5:valuee"));
        bdecoder.readDictionary();
    }

    @Test(expected = IllegalStateException.class)
    public void dictionaryPostfixMissing() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("d3:key5:value"));
        bdecoder.readDictionary();
    }

    @Test(expected = IllegalStateException.class)
    public void dictionaryEmptyStream() throws IOException {
        bdecoder = new Bdecoder(charset, new ByteArrayInputStream(new byte[]{}));
        bdecoder.readDictionary();
    }

    private InputStream inputStream(String s) {
        try {
            return new ByteArrayInputStream(s.getBytes(charset));
        } catch (UnsupportedEncodingException e) {
            //Should not happen
            return null;
        }
    }
}
