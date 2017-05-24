package io.mrarm.chatlib.dto;

import java.util.Iterator;

public class NickPrefixList implements Iterable<Character> {

    private String prefix;

    public NickPrefixList(String prefix) {
        this.prefix = prefix;
    }

    public int length() {
        return prefix == null ? 0 : prefix.length();
    }

    public char get(int i) {
        return prefix.charAt(i);
    }

    @Override
    public String toString() {
        return prefix;
    }

    @Override
    public Iterator<Character> iterator() {
        return new Iterator<Character>() {

            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < length();
            }

            @Override
            public Character next() {
                return get(i++);
            }

        };
    }

}
