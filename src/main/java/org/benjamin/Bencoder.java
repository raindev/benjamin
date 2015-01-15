package org.benjamin;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.nio.charset.StandardCharsets.*;
import static org.benjamin.Bencode.*;

/**
 * Bencode data encoder.
 */
public class Bencoder {

    /**
     * Used to encode character data.
     * Bencode markers and numbers are ASCII-encoded.
     */
    private final Charset charset;
    private final OutputStream outputStream;

    /**
     * Creates encoder writing to {@code outputStream} encoding {@code String}s using {@code charset}.
     *
     * @param charset      charset used to encode characters
     * @param outputStream stream to encode data to
     */
    public Bencoder(Charset charset, OutputStream outputStream) {
        this.charset = charset;
        this.outputStream = outputStream;
    }

    /**
     * Encodes integer value to Bencode.
     *
     * @param i integer number to encode
     * @throws IOException if an I/O error occurs
     * @return this Bencoder instance
     */
    public Bencoder encode(long i) throws IOException {
        outputStream.write(INTEGER_MARK);
        outputStream.write(toAsciiString(i));
        outputStream.write(END_MARK);
        return this;
    }

    /**
     * Encodes string value to Bencode using {@code charset}.
     *
     * @param s string to encode
     * @throws IOException if an I/O error occurs
     * @return this Bencoder instance
     */
    public Bencoder encode(String s) throws IOException {
        outputStream.write(toAsciiString(s.length()));
        outputStream.write(STRING_SPLIT);
        outputStream.write(s.getBytes(charset));
        return this;
    }

    /**
     * Encodes bytes as Bencode byte string.
     *
     * @param bytes bytes to encode
     * @throws IOException if an I/O error occurs
     * @return this Bencoder instance
     */
    public Bencoder encode(byte[] bytes) throws IOException {
        outputStream.write(toAsciiString(bytes.length));
        outputStream.write(STRING_SPLIT);
        outputStream.write(bytes);
        return this;
    }

    private byte[] toAsciiString(Number number) {
        return String.valueOf(number).getBytes(US_ASCII);
    }

    /**
     * Encodes list to Bencode. {@code list} should contain Bencode supported objects: {@code Integer}s,
     * {@code String}s, {@code byte} arrays, {@code Map} with {@code String} keys and another {@code List}s
     * containing elements which meet stated criteria.
     *
     * @param list list to encode
     * @throws IOException if an I/O error occurs
     * @return this Bencoder instance
     */
    public Bencoder encode(List<Object> list) throws IOException {
        outputStream.write(LIST_MARK);
        for (Object object : list) {
            encodeObject(object);
        }
        outputStream.write(END_MARK);
        return this;
    }

    /**
     * Encodes dictionary to Bencode. Map values should meet the same requirements as for lists.
     * See {@link #encode(java.util.List)}.
     *
     * @param dictionary dictionary to encode represented as {@code Map}
     * @throws IOException if an I/O error occurs
     * @return this Bencoder instance
     */
    public Bencoder encode(Map<String, Object> dictionary) throws IOException {
        outputStream.write(DICTIONARY_MARK);
        for (Map.Entry<String, Object> entry : new TreeMap<>(dictionary).entrySet()) {
            encode(entry.getKey());
            encodeObject(entry.getValue());
        }
        outputStream.write(END_MARK);
        return this;
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
