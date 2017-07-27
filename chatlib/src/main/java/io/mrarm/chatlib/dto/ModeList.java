package io.mrarm.chatlib.dto;

import java.util.Iterator;

public class ModeList implements Iterable<Character> {

    private String prefix;

    public ModeList(String prefix) {
        this.prefix = prefix;
    }

    public int length() {
        return prefix == null ? 0 : prefix.length();
    }

    public char get(int i) {
        return prefix.charAt(i);
    }

    public int find(char c) {
        return prefix.indexOf(c);
    }

    public boolean contains(char c) {
        return find(c) != -1;
    }

    @Override
    public String toString() {
        return prefix;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != getClass())
            return false;
        return prefix.equals(((ModeList) o).prefix);
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
