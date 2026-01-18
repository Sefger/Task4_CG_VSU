package com.cgvsu.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Matrix3x3Test {

    @Test
    void testConstructorFromArray() {
        float[][] values = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        Matrix3x3 matrix = new Matrix3x3(values);
        assertEquals(1, matrix.get(0, 0));
        assertEquals(5, matrix.get(1, 1));
        assertEquals(9, matrix.get(2, 2));
    }

    @Test
    void testConstructorInvalidSize() {
        float[][] invalid = {{1, 2}, {3, 4}};
        assertThrows(IllegalArgumentException.class, () -> new Matrix3x3(invalid));
    }

    @Test
    void testConstructorWithValues() {
        Matrix3x3 matrix = new Matrix3x3(1, 2, 3, 4, 5, 6, 7, 8, 9);
        assertEquals(1, matrix.get(0, 0));
        assertEquals(5, matrix.get(1, 1));
        assertEquals(9, matrix.get(2, 2));
    }

    @Test
    void testIdentity() {
        Matrix3x3 identity = Matrix3x3.identity();
        assertEquals(1, identity.get(0, 0));
        assertEquals(1, identity.get(1, 1));
        assertEquals(1, identity.get(2, 2));
        assertEquals(0, identity.get(0, 1));
        assertEquals(0, identity.get(1, 0));
    }

    @Test
    void testZero() {
        Matrix3x3 zero = Matrix3x3.zero();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(0, zero.get(i, j));
            }
        }
    }

    @Test
    void testGetAndSet() {
        Matrix3x3 matrix = new Matrix3x3();
        matrix.set(1, 1, 5.5f);
        assertEquals(5.5f, matrix.get(1, 1));
    }

    @Test
    void testGetInvalidIndex() {
        Matrix3x3 matrix = new Matrix3x3();
        assertThrows(IllegalArgumentException.class, () -> matrix.get(3, 0));
        assertThrows(IllegalArgumentException.class, () -> matrix.get(0, 3));
    }

    @Test
    void testSetInvalidIndex() {
        Matrix3x3 matrix = new Matrix3x3();
        assertThrows(IllegalArgumentException.class, () -> matrix.set(3, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> matrix.set(0, 3, 1));
    }

    @Test
    void testEquals() {
        Matrix3x3 m1 = new Matrix3x3(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Matrix3x3 m2 = new Matrix3x3(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Matrix3x3 m3 = new Matrix3x3(9, 8, 7, 6, 5, 4, 3, 2, 1);
        assertTrue(m1.equals(m2));
        assertFalse(m1.equals(m3));
    }

    @Test
    void testAdd() {
        Matrix3x3 m1 = new Matrix3x3(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Matrix3x3 m2 = new Matrix3x3(9, 8, 7, 6, 5, 4, 3, 2, 1);
        Matrix3x3 result = m1.add(m2);
        assertEquals(10, result.get(0, 0));
        assertEquals(10, result.get(1, 1));
        assertEquals(10, result.get(2, 2));
    }

    @Test
    void testSubtract() {
        Matrix3x3 m1 = new Matrix3x3(10, 10, 10, 10, 10, 10, 10, 10, 10);
        Matrix3x3 m2 = new Matrix3x3(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Matrix3x3 result = m1.subtract(m2);
        assertEquals(9, result.get(0, 0));
        assertEquals(8, result.get(0, 1));
        assertEquals(1, result.get(2, 2));
    }

    @Test
    void testMultiplyVector() {
        Matrix3x3 matrix = new Matrix3x3(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Vector3f vector = new Vector3f(2, 3, 4);
        Vector3f result = matrix.multiply(vector);
        assertEquals(20, result.x);
        assertEquals(47, result.y);
        assertEquals(74, result.z);
    }

    @Test
    void testMultiplyMatrix() {
        Matrix3x3 m1 = new Matrix3x3(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Matrix3x3 m2 = new Matrix3x3(9, 8, 7, 6, 5, 4, 3, 2, 1);
        Matrix3x3 result = m1.multiply(m2);
        assertEquals(30, result.get(0, 0));
        assertEquals(24, result.get(0, 1));
        assertEquals(84, result.get(1, 0));
        assertEquals(69, result.get(1, 1));
    }

    @Test
    void testTranspose() {
        Matrix3x3 matrix = new Matrix3x3(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Matrix3x3 transposed = matrix.transpose();
        assertEquals(1, transposed.get(0, 0));
        assertEquals(4, transposed.get(0, 1));
        assertEquals(7, transposed.get(0, 2));
        assertEquals(2, transposed.get(1, 0));
        assertEquals(5, transposed.get(1, 1));
        assertEquals(8, transposed.get(1, 2));
    }

    @Test
    void testToString() {
        Matrix3x3 matrix = new Matrix3x3(1, 2, 3, 4, 5, 6, 7, 8, 9);
        String result = matrix.toString();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("1"));
        assertTrue(result.contains("5"));
        assertTrue(result.contains("9"));
    }
}