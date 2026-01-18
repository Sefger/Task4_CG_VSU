package com.cgvsu.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Vector2fTest {

    @Test
    void testConstructor() {
        Vector2f v = new Vector2f(1.5f, 2.5f);
        assertEquals(1.5f, v.x);
        assertEquals(2.5f, v.y);
    }

    @Test
    void testGetXGetY() {
        Vector2f v = new Vector2f(3.0f, 4.0f);
        assertEquals(3.0f, v.getX());
        assertEquals(4.0f, v.getY());
    }

    @Test
    void testEquals() {
        Vector2f v1 = new Vector2f(1.0f, 2.0f);
        Vector2f v2 = new Vector2f(1.0f, 2.0f);
        Vector2f v3 = new Vector2f(1.1f, 2.0f);
        assertTrue(v1.equals(v2));
        assertFalse(v1.equals(v3));
    }

    @Test
    void testAdd() {
        Vector2f v1 = new Vector2f(1.0f, 2.0f);
        Vector2f v2 = new Vector2f(3.0f, 4.0f);
        Vector2f result = v1.add(v2);
        assertEquals(4.0f, result.x);
        assertEquals(6.0f, result.y);
    }

    @Test
    void testSubtract() {
        Vector2f v1 = new Vector2f(5.0f, 7.0f);
        Vector2f v2 = new Vector2f(2.0f, 3.0f);
        Vector2f result = v1.subtract(v2);
        assertEquals(3.0f, result.x);
        assertEquals(4.0f, result.y);
    }

    @Test
    void testMultiply() {
        Vector2f v = new Vector2f(2.0f, 3.0f);
        Vector2f result = v.multiply(2.0f);
        assertEquals(4.0f, result.x);
        assertEquals(6.0f, result.y);
    }

    @Test
    void testDivide() {
        Vector2f v = new Vector2f(6.0f, 9.0f);
        Vector2f result = v.divide(3.0f);
        assertEquals(2.0f, result.x);
        assertEquals(3.0f, result.y);
    }

    @Test
    void testDivideByZero() {
        Vector2f v = new Vector2f(1.0f, 2.0f);
        assertThrows(IllegalArgumentException.class, () -> v.divide(0.0f));
    }

    @Test
    void testLength() {
        Vector2f v = new Vector2f(3.0f, 4.0f);
        assertEquals(5.0f, v.length(), 0.0001f);
    }

    @Test
    void testLengthZero() {
        Vector2f v = new Vector2f(0.0f, 0.0f);
        assertEquals(0.0f, v.length(), 0.0001f);
    }

    @Test
    void testNormalized() {
        Vector2f v = new Vector2f(3.0f, 4.0f);
        Vector2f normalized = v.normalized();
        assertEquals(0.6f, normalized.x, 0.0001f);
        assertEquals(0.8f, normalized.y, 0.0001f);
        assertEquals(1.0f, normalized.length(), 0.0001f);
    }

    @Test
    void testNormalizedZeroVector() {
        Vector2f v = new Vector2f(0.0f, 0.0f);
        Vector2f normalized = v.normalized();
        assertEquals(0.0f, normalized.x);
        assertEquals(0.0f, normalized.y);
    }

    @Test
    void testDot() {
        Vector2f v1 = new Vector2f(1.0f, 2.0f);
        Vector2f v2 = new Vector2f(3.0f, 4.0f);
        float result = v1.dot(v2);
        assertEquals(11.0f, result);
    }
}