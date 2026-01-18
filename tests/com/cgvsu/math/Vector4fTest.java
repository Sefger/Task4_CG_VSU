package com.cgvsu.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Vector4fTest {

    @Test
    void testConstructor() {
        Vector4f v = new Vector4f(1.0f, 2.0f, 3.0f, 4.0f);
        assertEquals(1.0f, v.x);
        assertEquals(2.0f, v.y);
        assertEquals(3.0f, v.z);
        assertEquals(4.0f, v.w);
    }

    @Test
    void testGetXGetYGetZGetW() {
        Vector4f v = new Vector4f(3.0f, 4.0f, 5.0f, 6.0f);
        assertEquals(3.0f, v.getX());
        assertEquals(4.0f, v.getY());
        assertEquals(5.0f, v.getZ());
        assertEquals(6.0f, v.getW());
    }

    @Test
    void testEquals() {
        Vector4f v1 = new Vector4f(1.0f, 2.0f, 3.0f, 4.0f);
        Vector4f v2 = new Vector4f(1.0f, 2.0f, 3.0f, 4.0f);
        Vector4f v3 = new Vector4f(1.1f, 2.0f, 3.0f, 4.0f);
        assertTrue(v1.equals(v2));
        assertFalse(v1.equals(v3));
    }

    @Test
    void testAdd() {
        Vector4f v1 = new Vector4f(1.0f, 2.0f, 3.0f, 4.0f);
        Vector4f v2 = new Vector4f(5.0f, 6.0f, 7.0f, 8.0f);
        Vector4f result = v1.add(v2);
        assertEquals(6.0f, result.x);
        assertEquals(8.0f, result.y);
        assertEquals(10.0f, result.z);
        assertEquals(12.0f, result.w);
    }

    @Test
    void testSubtract() {
        Vector4f v1 = new Vector4f(5.0f, 7.0f, 9.0f, 11.0f);
        Vector4f v2 = new Vector4f(2.0f, 3.0f, 4.0f, 5.0f);
        Vector4f result = v1.subtract(v2);
        assertEquals(3.0f, result.x);
        assertEquals(4.0f, result.y);
        assertEquals(5.0f, result.z);
        assertEquals(6.0f, result.w);
    }

    @Test
    void testMultiply() {
        Vector4f v = new Vector4f(2.0f, 3.0f, 4.0f, 5.0f);
        Vector4f result = v.multiply(2.0f);
        assertEquals(4.0f, result.x);
        assertEquals(6.0f, result.y);
        assertEquals(8.0f, result.z);
        assertEquals(10.0f, result.w);
    }

    @Test
    void testDivide() {
        Vector4f v = new Vector4f(6.0f, 9.0f, 12.0f, 15.0f);
        Vector4f result = v.divide(3.0f);
        assertEquals(2.0f, result.x);
        assertEquals(3.0f, result.y);
        assertEquals(4.0f, result.z);
        assertEquals(5.0f, result.w);
    }

    @Test
    void testDivideByZero() {
        Vector4f v = new Vector4f(1.0f, 2.0f, 3.0f, 4.0f);
        assertThrows(IllegalArgumentException.class, () -> v.divide(0.0f));
    }

    @Test
    void testLength() {
        Vector4f v = new Vector4f(2.0f, 4.0f, 4.0f, 0.0f);
        assertEquals(6.0f, v.length(), 0.0001f);
    }

    @Test
    void testLengthFull() {
        Vector4f v = new Vector4f(1.0f, 2.0f, 2.0f, 4.0f);
        assertEquals(5.0f, v.length(), 0.0001f);
    }

    @Test
    void testNormalized() {
        Vector4f v = new Vector4f(2.0f, 4.0f, 4.0f, 0.0f);
        Vector4f normalized = v.normalized();
        assertEquals(2.0f/6.0f, normalized.x, 0.0001f);
        assertEquals(4.0f/6.0f, normalized.y, 0.0001f);
        assertEquals(4.0f/6.0f, normalized.z, 0.0001f);
        assertEquals(0.0f, normalized.w, 0.0001f);
        assertEquals(1.0f, normalized.length(), 0.0001f);
    }

    @Test
    void testNormalizedZeroVector() {
        Vector4f v = new Vector4f(0.0f, 0.0f, 0.0f, 0.0f);
        Vector4f normalized = v.normalized();
        assertEquals(0.0f, normalized.x);
        assertEquals(0.0f, normalized.y);
        assertEquals(0.0f, normalized.z);
        assertEquals(0.0f, normalized.w);
    }

    @Test
    void testDot() {
        Vector4f v1 = new Vector4f(1.0f, 2.0f, 3.0f, 4.0f);
        Vector4f v2 = new Vector4f(5.0f, 6.0f, 7.0f, 8.0f);
        float result = v1.dot(v2);
        assertEquals(70.0f, result);
    }
}