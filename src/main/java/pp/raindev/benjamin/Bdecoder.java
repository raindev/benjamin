package pp.raindev.benjamin;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bencode data decoder.
 */
public class Bdecoder {
    private static final char INTEGER_MARK = 'i';
    private static final char LIST_MARK = 'l';
    private static final char DICTIONARY_MARK = 'd';
    private static final char STRING_SPLIT = ':';
    private static final char END_MARK = 'e';

    private final String charset;
    private final PushbackInputStream inputStream;

    /**
     * Creates encoder reading from {@code inputStream} using {@code charset} to decode
     * character data.
     *
     * @param charset     charset used to decode {@code String}s
     * @param inputStream stream to decode data from
     */
    public Bdecoder(String charset, InputStream inputStream) {
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
        int chr;
        if ((chr = inputStream.read()) != INTEGER_MARK) {
            throw new IllegalStateException("Unexpected character occurred instead of 'i' or end of stream reached: "
                    + (char) chr);
        }
        StringBuilder number = new StringBuilder();
        while ((chr = inputStream.read()) != END_MARK) {
            if (chr == -1) {
                throw new IllegalStateException("End of stream was reached prematurely");
            }
            number.append((char) chr);
        }
        String numberString = number.toString();
        if (numberString.equals("-0")) {
            throw new IllegalArgumentException("Negative zero is not valid number");
        }
        return Long.valueOf(numberString);
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
        int chr;
        StringBuilder lengthString = new StringBuilder();
        while ((chr = inputStream.read()) != STRING_SPLIT) {
            if (chr == -1) {
                throw new IllegalStateException("End of stream was reached prematurely");
            }
            lengthString.append((char) chr);
        }
        int length;
        try {
            length = Integer.valueOf(lengthString.toString());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Length specifier was expected");
        }
        byte[] byteString = new byte[length];
        if (inputStream.read(byteString) != length) {
            throw new IllegalStateException("End of stream was reached prematurely during string reading");
        }
        return byteString;
    }

    /**
     * Decodes list from the stream.
     *
     * @return list of decoded values
     * @throws IOException if an I/O error occurs
     */
    public List<Object> readList() throws IOException {
        int chr;
        if ((chr = inputStream.read()) != LIST_MARK) {
            throw new IllegalStateException("Unexpected character occurred instead of 'l' or end of stream reached: "
                    + (char) chr);
        }
        List<Object> list = new ArrayList<>();
        while ((chr = inputStream.read()) != END_MARK) {
            if (chr == -1) {
                throw new IllegalStateException("End of stream was reached prematurely");
            }
            inputStream.unread(chr);
            list.add(readObject(chr));
        }
        return list;
    }

    /**
     * Decodes dictionary from the stream.
     *
     * @return dictionary of decoded values
     * @throws IOException if an I/O error occurs
     */
    public Map<String, Object> readDictionary() throws IOException {
        int chr;
        if ((chr = inputStream.read()) != DICTIONARY_MARK) {
            throw new IllegalStateException("Unexpected character occurred instead of 'd' or end of stream reached: "
                    + (char) chr);
        }
        Map<String, Object> dictionary = new HashMap<>();
        while ((chr = inputStream.read()) != END_MARK) {
            if (chr == -1) {
                throw new IllegalStateException("End of stream was reached prematurely");
            }
            inputStream.unread(chr);
            String string = readString();
            chr = inputStream.read();
            inputStream.unread(chr);
            dictionary.put(string, readObject(chr));
        }
        return dictionary;
    }

    private Object readObject(int chr) throws IOException {
        switch (chr) {
            case INTEGER_MARK:
                return readInt();
            case LIST_MARK:
                return readList();
            case DICTIONARY_MARK:
                return readDictionary();
            default:
                return readString();
        }
    }
}
