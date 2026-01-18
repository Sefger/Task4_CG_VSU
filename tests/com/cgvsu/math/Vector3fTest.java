package com.cgvsu.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Vector3fTest {

    @Test
    void testConstructor() {
        Vector3f v = new Vector3f(1.0f, 2.0f, 3.0f);
        assertEquals(1.0f, v.x);
        assertEquals(2.0f, v.y);
        assertEquals(3.0f, v.z);
    }

    @Test
    void testGetXGetYGetZ() {
        Vector3f v = new Vector3f(3.0f, 4.0f, 5.0f);
        assertEquals(3.0f, v.getX());
        assertEquals(4.0f, v.getY());
        assertEquals(5.0f, v.getZ());
    }

    @Test
    void testEquals() {
        Vector3f v1 = new Vector3f(1.0f, 2.0f, 3.0f);
        Vector3f v2 = new Vector3f(1.0f, 2.0f, 3.0f);
        Vector3f v3 = new Vector3f(1.1f, 2.0f, 3.0f);
        assertTrue(v1.equals(v2));
        assertFalse(v1.equals(v3));
    }

    @Test
    void testAdd() {
        Vector3f v1 = new Vector3f(1.0f, 2.0f, 3.0f);
        Vector3f v2 = new Vector3f(4.0f, 5.0f, 6.0f);
        v1.add(v2);
        assertEquals(5.0f, v1.x);
        assertEquals(7.0f, v1.y);
        assertEquals(9.0f, v1.z);
    }

    @Test
    void testSubtract() {
        Vector3f v1 = new Vector3f(5.0f, 7.0f, 9.0f);
        Vector3f v2 = new Vector3f(2.0f, 3.0f, 4.0f);
        Vector3f result = v1.subtract(v2);
        assertEquals(3.0f, result.x);
        assertEquals(4.0f, result.y);
        assertEquals(5.0f, result.z);
    }

    @Test
    void testMultiply() {
        Vector3f v = new Vector3f(2.0f, 3.0f, 4.0f);
        Vector3f result = v.multiply(2.0f);
        assertEquals(4.0f, result.x);
        assertEquals(6.0f, result.y);
        assertEquals(8.0f, result.z);
    }

    @Test
    void testDivide() {
        Vector3f v = new Vector3f(6.0f, 9.0f, 12.0f);
        Vector3f result = v.divide(3.0f);
        assertEquals(2.0f, result.x);
        assertEquals(3.0f, result.y);
        assertEquals(4.0f, result.z);
    }

    @Test
    void testDivideByZero() {
        Vector3f v = new Vector3f(1.0f, 2.0f, 3.0f);
        assertThrows(IllegalArgumentException.class, () -> v.divide(0.0f));
    }

    @Test
    void testLength() {
        Vector3f v = new Vector3f(3.0f, 4.0f, 0.0f);
        assertEquals(5.0f, v.length(), 0.0001f);
    }

    @Test
    void testLengthFull() {
        Vector3f v = new Vector3f(1.0f, 2.0f, 2.0f);
        assertEquals(3.0f, v.length(), 0.0001f);
    }

    @Test
    void testNormalized() {
        Vector3f v = new Vector3f(3.0f, 4.0f, 0.0f);
        Vector3f normalized = v.normalized();
        assertEquals(0.6f, normalized.x, 0.0001f);
        assertEquals(0.8f, normalized.y, 0.0001f);
        assertEquals(0.0f, normalized.z, 0.0001f);
        assertEquals(1.0f, normalized.length(), 0.0001f);
    }

    @Test
    void testNormalizedZeroVector() {
        Vector3f v = new Vector3f(0.0f, 0.0f, 0.0f);
        Vector3f normalized = v.normalized();
        assertEquals(0.0f, normalized.x);
        assertEquals(0.0f, normalized.y);
        assertEquals(0.0f, normalized.z);
    }

    @Test
    void testDot() {
        Vector3f v1 = new Vector3f(1.0f, 2.0f, 3.0f);
        Vector3f v2 = new Vector3f(4.0f, 5.0f, 6.0f);
        float result = v1.dot(v2);
        assertEquals(32.0f, result);
    }

    @Test
    void testCross() {
        Vector3f v1 = new Vector3f(1.0f, 0.0f, 0.0f);
        Vector3f v2 = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f result = v1.cross(v2);
        assertEquals(0.0f, result.x);
        assertEquals(0.0f, result.y);
        assertEquals(1.0f, result.z);
    }

    @Test
    void testSet() {
        Vector3f v = new Vector3f(0.0f, 0.0f, 0.0f);
        v.set(7.0f, 8.0f, 9.0f);
        assertEquals(7.0f, v.x);
        assertEquals(8.0f, v.y);
        assertEquals(9.0f, v.z);
    }
}