package org.benjamin;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Bencode data encoder.
 */
public class Bencoder {

	private static final byte[] INTEGER_MARK = "i".getBytes();
	private static final byte[] LIST_MARK = "l".getBytes();
	private static final byte[] DICTIONARY_MARK = "d".getBytes();
	private static final byte[] STRING_SPLIT = ":".getBytes();
	private static final byte[] END_MARK = "e".getBytes();

    /**
     * Used to encode character data.
     */
    private final Charset charset;
    private final OutputStream outputStream;

    /**
     * Creates encoder writing to {@code outputStream} encoding {@code String}s using {@code charset}.
     *
     * @param charset      charset used to encode characters
     * @param outputStream stream to encode data to
     */
    public Bencoder(String charset, OutputStream outputStream) {
        this.charset = Charset.forName(charset);
        this.outputStream = outputStream;
    }

    /**
     * Encodes integer value to Bencode.
     *
     * @param i integer to encode
     * @throws IOException if an I/O error occurs
     */
    public void encode(int i) throws IOException {
		outputStream.write(INTEGER_MARK);
        outputStream.write(Integer.toString(i).getBytes());
		outputStream.write(END_MARK);
    }

    /**
     * Encodes string value to Bencode using {@code charset}.
     *
     * @param s string to encode
     * @throws IOException if an I/O error occurs
     */
    public void encode(String s) throws IOException {
		outputStream.write(Integer.toString(s.length()).getBytes());
		outputStream.write(STRING_SPLIT);
        outputStream.write(s.getBytes(charset));
    }

    /**
     * Encodes bytes as Bencode byte string.
     *
     * @param bytes bytes to encode
     * @throws IOException if an I/O error occurs
     */
    public void encode(byte[] bytes) throws IOException {
        outputStream.write(Integer.toString(bytes.length).getBytes());
		outputStream.write(STRING_SPLIT);
        outputStream.write(bytes);
    }

    /**
     * Encodes list to Bencode. {@code list} should contain Bencode supported objects: {@code Integer}s,
     * {@code String}s, {@code byte} arrays, {@code Map} with {@code String} keys and another {@code List}s
     * containing elements which meet stated criteria.
     *
     * @param list list to encode
     * @throws IOException if an I/O error occurs
     */
    public void encode(List<Object> list) throws IOException {
        outputStream.write(LIST_MARK);
        for (Object object : list) {
            encodeObject(object);
        }
        outputStream.write(END_MARK);
    }

    /**
     * Encodes dictionary to Bencode. Map values should meet the same requirements as for lists.
     * See {@link #encode(java.util.List)}.
     *
     * @param dictionary dictionary to encode represented as {@code Map}
     * @throws IOException if an I/O error occurs
     */
    public void encode(Map<String, Object> dictionary) throws IOException {
        outputStream.write(DICTIONARY_MARK);
        for (Map.Entry<String, Object> entry : new TreeMap<>(dictionary).entrySet()) {
            encode(entry.getKey());
            encodeObject(entry.getValue());
        }
        outputStream.write(END_MARK);
    }

    /**
     * All black magic goes here.
     *
     * @param object object to encode in Bencode
     * @throws IOException if an I/O error occurs
     */
    @SuppressWarnings("unchecked")
    private void encodeObject(Object object) throws IOException {
        if (object instanceof Integer || object instanceof Long || object instanceof Byte) {
            encode((int) object);
        } else if (object instanceof String) {
            encode((String) object);
        } else if (object.getClass().equals(byte[].class)) {
            encode((byte[]) object);
        } else if (object instanceof List) {
            encode((List<Object>) object);
        } else if (object instanceof Map) {
            encode((Map<String, Object>) object);
        } else {
            throw new IllegalArgumentException("Bencode unsupported type found in the arguments: " + object);
        }
    }
}
