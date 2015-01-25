package org.benjamin;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * Bencode data encoder.
 */
public class Bencoder {

    /**
     * Used to encode {@code String}s.
     *
     * Bencode markers and numbers are ASCII-encoded.
     */
    private final Charset charset;

    /**
     * A stream to write encoded data to.
     */
    private final OutputStream outputStream;

    /**
     * Creates encoder writing to {@code outputStream} encoding {@code String}s in {@code charset}.
     *
     * @param charset      charset used to encode characters
     * @param outputStream stream to encode data to
     */
    public Bencoder(final Charset charset, final OutputStream outputStream) {
        this.charset = charset;
        this.outputStream = outputStream;
    }

    /**
     * Encodes integer value to Bencode.
     *
     * @param integer integer number to encode
     * @throws IOException if an I/O error occurs
     * @return this Bencoder instance
     */
    public Bencoder encode(final long integer) throws IOException {
        outputStream.write('i');
        outputStream.write(toAsciiString(integer));
        outputStream.write('e');
        return this;
    }

    /**
     * Encodes string value to Bencode using {@code charset}.
     *
     * @param string string to encode
     * @throws IOException if an I/O error occurs
     * @return this Bencoder instance
     */
    public Bencoder encode(final String string) throws IOException {
        outputStream.write(toAsciiString(string.length()));
        outputStream.write(':');
        outputStream.write(string.getBytes(charset));
        return this;
    }

    /**
     * Encodes bytes as Bencode byte string.
     *
     * @param bytes bytes to encode
     * @throws IOException if an I/O error occurs
     * @return this Bencoder instance
     */
    public Bencoder encode(final byte[] bytes) throws IOException {
        outputStream.write(toAsciiString(bytes.length));
        outputStream.write(':');
        outputStream.write(bytes);
        return this;
    }

    private byte[] toAsciiString(final Number number) {
        return String.valueOf(number).getBytes(US_ASCII);
    }

    /**
     * Encodes a list to Bencode.
     *
     * The {@code list} could contain objects of types supported in Bencode: {@code Integer}s,
     * {@code String}s, {@code byte} arrays, {@code Map}s with {@code String} keys or another
     * {@code List}s meet the same criteria.
     *
     * @param list list to encode
     * @throws IOException if an I/O error occurs
     * @return this Bencoder instance
     */
    public Bencoder encode(final List<?> list) throws IOException {
        outputStream.write('l');
        for (final Object object : list) {
            encodeObject(object);
        }
        outputStream.write('e');
        return this;
    }

    /**
     * Encodes dictionary to Bencode.
     *
     * Map values should meet the same requirements as for lists.
     * See {@link #encode(java.util.List)}.
     *
     * @param dictionary dictionary to encode represented as {@code Map}
     * @throws IOException if an I/O error occurs
     * @return this Bencoder instance
     */
    public Bencoder encode(final Map<String, ?> dictionary) throws IOException {
        outputStream.write('d');
        for (final Map.Entry<String, Object> entry : new TreeMap<>(dictionary).entrySet()) {
            encode(entry.getKey());
            encodeObject(entry.getValue());
        }
        outputStream.write('e');
        return this;
    }

    /**
     * All black magic goes here.
     *
     * @param object object to encode in Bencode
     * @throws IOException if an I/O error occurs
     */
    @SuppressWarnings("unchecked")
    private void encodeObject(final Object object) throws IOException {
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
