package com.shikhir.hash;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class MurmurHashTest {

    @Test
    public void hash32ShouldBeDeterministicAcrossOverloads() {
        String text = "hello hash";
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

        assertEquals(MurmurHash.hash32(text), MurmurHash.hash32(bytes, bytes.length));
        assertEquals(MurmurHash.hash32(text, 0, text.length()), MurmurHash.hash32(text));
    }

    @Test
    public void hash64ShouldBeDeterministicAcrossOverloads() {
        String text = "hello hash";
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

        assertEquals(MurmurHash.hash64(text), MurmurHash.hash64(bytes, bytes.length));
        assertEquals(MurmurHash.hash64(text, 0, text.length()), MurmurHash.hash64(text));
    }

    @Test
    public void hashShouldChangeForDifferentInput() {
        assertNotEquals(MurmurHash.hash32("one"), MurmurHash.hash32("two"));
        assertNotEquals(MurmurHash.hash64("one"), MurmurHash.hash64("two"));
    }
}
