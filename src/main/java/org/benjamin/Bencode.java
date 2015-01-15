package org.benjamin;

/**
 * Bencode constants holder.
 */
interface Bencode {
    static final char INTEGER_MARK = 'i';
    static final char LIST_MARK = 'l';
    static final char DICTIONARY_MARK = 'd';
    static final char STRING_SPLIT = ':';
    static final char END_MARK = 'e';
}
