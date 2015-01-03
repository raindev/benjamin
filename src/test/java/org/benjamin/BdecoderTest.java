package org.benjamin;

import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

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

        assertEquals(bdecoder.readInt(), 47,
                "Integer is not decoded properly");
    }

    @Test
    public void decodeZero() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("i0e"));

        assertEquals(bdecoder.readInt(), 0,
                "Zero is not decode properly");
    }

    @Test
    public void decodeNegativeInteger() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("i-47e"));

        assertEquals(bdecoder.readInt(), -47,
                "Negative integer is not decoded properly");
    }

    @Test
    public void decodeLongInteger() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("i2438987776e"));

        assertEquals(bdecoder.readInt(), 2438987776L,
                "Numbers larger than 32 bits should be handled");
    }

    /**
     * Negative zero disallowed in Bencode.
     *
     * @throws IOException is an I/O error occurs
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void negativeZero() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("i-0e"));
        bdecoder.readInt();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void invalidIntegerPrefix() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("n-47e"));
        bdecoder.readInt();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void missingIntegerPostfix() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("i47"));
        bdecoder.readInt();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void emptyIntegerStream() throws IOException {
        bdecoder = new Bdecoder(charset, new ByteArrayInputStream(new byte[]{}));
        bdecoder.readInt();
    }

    @Test
    public void decodeString() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("7:smileΩ"));

        assertEquals(bdecoder.readString(), "smileΩ",
                "String is not decoded properly");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void tooShortString() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("6:four"));
        bdecoder.readString();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void missingStringSeparator() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("4four"));
        bdecoder.readString();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void missingStringLength() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream(":four"));
        bdecoder.readString();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void emptyStringStream() throws IOException {
        bdecoder = new Bdecoder(charset, new ByteArrayInputStream(new byte[]{}));
        bdecoder.readString();
    }

    @Test
    public void decodeBytes() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("4:2397"));

        //noinspection OctalInteger
        assertEquals(bdecoder.readBytes(), new byte[]{062, 063, 071, 067},
                "Byte string is not decoded properly");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void tooShortBytes() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("9:2532"));
        bdecoder.readBytes();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void missingBytesSeparator() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("43331"));
        bdecoder.readBytes();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void missingBytesLength() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream(":82382"));
        bdecoder.readBytes();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void emptyBytesStream() throws IOException {
        bdecoder = new Bdecoder(charset, new ByteArrayInputStream(new byte[]{}));
        bdecoder.readBytes();
    }

    @Test
    public void decodeList() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("l4:lanei47ee"));

        assertEquals(bdecoder.readList(), Arrays.asList(new Object[]{"lane", 47L}),
                "List decoded not properly");
    }

    @Test
    public void decodeComplexList() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("l2:coi47eli47ei42eed4:lifei42eee"));

        HashMap<String, Object> dictionary = new HashMap<>();
        dictionary.put("life", 42L);
        List<Object> list = Arrays.asList("co", 47L, Arrays.asList(47L, 42L), dictionary);

        assertEquals(bdecoder.readList(), list,
                "List-based tree is not decoded properly");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void listPrefixMissing() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("4:lanei47ee"));
        bdecoder.readList();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void listPostfixMissing() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("l4:lanei47e"));
        bdecoder.readList();
    }

    @Test(expectedExceptions = IllegalStateException.class)
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

        assertEquals(bdecoder.readDictionary(), dictionary,
                "Dictionary decoded not properly");
    }

    @Test
    public void decodeComplexDictionary() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("d4:listl2:co4:worke6:stringd3:key5:valueee"));

        Map<String, Object> dictionary = new HashMap<>();
        dictionary.put("list", Arrays.asList("co", "work"));
        Map<String, Object> innerDictionary = new HashMap<>();
        innerDictionary.put("key", "value");
        dictionary.put("string", innerDictionary);

        assertEquals(bdecoder.readDictionary(), dictionary,
                "Dictionary-based tree decoded not properly");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void dictionaryPrefixMissing() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("3:key5:valuee"));
        bdecoder.readDictionary();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void dictionaryPostfixMissing() throws IOException {
        bdecoder = new Bdecoder(charset, inputStream("d3:key5:value"));
        bdecoder.readDictionary();
    }

    @Test(expectedExceptions = IllegalStateException.class)
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
