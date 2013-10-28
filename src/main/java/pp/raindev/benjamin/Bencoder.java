package pp.raindev.benjamin;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Bencode data encoder.
 */
public class Bencoder {

    /**
     * Creates encoder writing to {@code outputStream} encoding {@code String}s using {@code charset}.
     *
     * @param charset      charset used to encode characters
     * @param outputStream stream to encode data to
     */
    public Bencoder(String charset, OutputStream outputStream) {
    }

    /**
     * Encodes integer value to Bencode.
     *
     * @param i integer to encode
     */
    public void encode(int i) {
    }

    /**
     * Encodes string value to Bencode using {@code charset}.
     *
     * @param s string to encode
     */
    public void encode(String s) {
    }

    /**
     * Encodes bytes as Bencode byte string.
     *
     * @param bytes bytes to encode
     */
    public void encode(byte[] bytes) {
    }

    /**
     * Encodes list to Bencode. {@code list} should contain Bencode supported objects: {@code Integer}s,
     * {@code String}s, {@code byte} arrays, {@code Map} with {@code String} keys and another {@code List}s
     * containing elements which meet stated criteria.
     *
     * @param list list to encode
     */
    public void encode(List<Object> list) {
    }

    /**
     * Encodes dictionary to Bencode. Map values should meet the same requirements as for lists.
     * See {@link #encode(java.util.List)}.
     *
     * @param dictionary dictionary to encode represented as {@code Map}
     */
    public void encode(Map<String, Object> dictionary) {
    }
}
