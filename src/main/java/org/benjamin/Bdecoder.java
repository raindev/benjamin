package org.benjamin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Bencode data decoder.
 */
public class Bdecoder {
    private final Charset charset;
    private final PushbackInputStream inputStream;

    public Bdecoder(final Charset charset, final String bencodedString) {
        this(charset,
                new ByteArrayInputStream(bencodedString.getBytes(charset)));
    }

    /**
     * Creates encoder reading from {@code inputStream} using {@code charset} to decode
     * character data.
     *
     * @param charset     charset used to decode {@code String}s
     * @param inputStream stream to decode data from
     */
    public Bdecoder(final Charset charset, final InputStream inputStream) {
        this.charset = charset;
        this.inputStream = new PushbackInputStream(inputStream);
    }

    /**
     * Decodes integer from the stream.
     *
     * @return decoded integer
     * @throws IOException if an I/O error occurs
     */
    public long readInt() throws IOException {
        ensureFirstChar('i');
        final StringBuilder number = readUntil('e');
        if (number.charAt(0) == '0' && number.length() != 1) {
            throw new IllegalStateException("Zero padded integers aren't allowed");
        }
        if (number.charAt(0) == '-' && number.charAt(1) == '0') {
            throw new IllegalStateException("Negative zero is not valid number");
        }
        return Long.parseLong(number.toString());
    }

    /**
     * Decodes string from the stream.
     *
     * @return decoded string
     * @throws IOException if an I/O error occurs
     */
    public String readString() throws IOException {
        return new String(readBytes(), charset);
    }

    /**
     * Reads encoded byte string from the stream.
     *
     * @return decoded {@code byte} array
     * @throws IOException if an I/O error occurs
     */
    public byte[] readBytes() throws IOException {
        int length;
        try {
            length = Integer.parseInt(readUntil(':').toString());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("String length specifier was expected", e);
        }
        final byte[] byteString = new byte[length];
        if (inputStream.read(byteString) != length) {
            throw streamEnded();
        }
        return byteString;
    }

    private StringBuilder readUntil(final char delimiter) throws IOException {
        final StringBuilder content = new StringBuilder();
        int chr;
        while ((chr = inputStream.read()) != delimiter) {
            if (chr == -1) {
                throw streamEnded();
            }
            content.append((char) chr);
        }
        return content;
    }

    /**
     * Decodes list from the stream.
     * All the strings within a list will be decoded as Strings of the specified encoding,
     * as opposed to raw byte arrays.
     *
     * @return list of decoded values
     * @throws IOException if an I/O error occurs
     */
    public List<?> readList() throws IOException {
        ensureFirstChar('l');
        int chr;
        final List<Object> list = new ArrayList<>();
        while ((chr = inputStream.read()) != 'e') {
            if (chr == -1) {
                throw streamEnded();
            }
            inputStream.unread(chr);
            list.add(readObject(chr));
        }
        return list;
    }

    /**
     * Decodes dictionary from the stream.
     * Accordingly to the Bencode specification, dictionary keys are sorted as raw strings.
     * All the strings values in a dictionary will be decoded as Strings of the specified
     * encoding, as opposed to raw byte arrays.
     *
     * @return dictionary of decoded values
     * @throws IOException if an I/O error occurs
     */
    public SortedMap<String, ?> readDictionary() throws IOException {
        ensureFirstChar('d');
        int chr;
        final SortedMap<String, Object> dictionary = new TreeMap<>();
        while ((chr = inputStream.read()) != 'e') {
            if (chr == -1) {
                throw streamEnded();
            }
            inputStream.unread(chr);
            final String string = readString();
            chr = inputStream.read();
            inputStream.unread(chr);
            dictionary.put(string, readObject(chr));
        }
        return dictionary;
    }

    private void ensureFirstChar(final char expected) throws IOException {
        int chr;
        if ((chr = inputStream.read()) != expected) {
            throw new IllegalStateException("Unexpected character occurred instead of '"
                + chr + "' or end of stream reached: " + (char) chr);
        }
    }

    private Object readObject(final int chr) throws IOException {
        switch (chr) {
            case 'i':
                return readInt();
            case 'l':
                return readList();
            case 'd':
                return readDictionary();
            default:
                return readString();
        }
    }

    private IllegalStateException streamEnded() {
        return new IllegalStateException("End of stream was reached prematurely");
    }
}
