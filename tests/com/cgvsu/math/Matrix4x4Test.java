package com.cgvsu.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Matrix4x4Test {

    @Test
    void testConstructorFromArray() {
        float[][] values = {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}, {13, 14, 15, 16}};
        Matrix4x4 matrix = new Matrix4x4(values);
        assertEquals(1, matrix.get(0, 0));
        assertEquals(6, matrix.get(1, 1));
        assertEquals(11, matrix.get(2, 2));
        assertEquals(16, matrix.get(3, 3));
    }

    @Test
    void testConstructorInvalidSize() {
        float[][] invalid = {{1, 2}, {3, 4}};
        assertThrows(IllegalArgumentException.class, () -> new Matrix4x4(invalid));
    }

    @Test
    void testConstructorWithValues() {
        Matrix4x4 matrix = new Matrix4x4(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        );
        assertEquals(1, matrix.get(0, 0));
        assertEquals(6, matrix.get(1, 1));
        assertEquals(11, matrix.get(2, 2));
        assertEquals(16, matrix.get(3, 3));
    }

    @Test
    void testIdentity() {
        Matrix4x4 identity = Matrix4x4.identity();
        assertEquals(1, identity.get(0, 0));
        assertEquals(1, identity.get(1, 1));
        assertEquals(1, identity.get(2, 2));
        assertEquals(1, identity.get(3, 3));
        assertEquals(0, identity.get(0, 1));
        assertEquals(0, identity.get(1, 0));
    }

    @Test
    void testZero() {
        Matrix4x4 zero = Matrix4x4.zero();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(0, zero.get(i, j));
            }
        }
    }

    @Test
    void testGetAndSet() {
        Matrix4x4 matrix = new Matrix4x4();
        matrix.set(2, 3, 7.5f);
        assertEquals(7.5f, matrix.get(2, 3));
    }

    @Test
    void testGetInvalidIndex() {
        Matrix4x4 matrix = new Matrix4x4();
        assertThrows(IllegalArgumentException.class, () -> matrix.get(4, 0));
        assertThrows(IllegalArgumentException.class, () -> matrix.get(0, 4));
    }

    @Test
    void testSetInvalidIndex() {
        Matrix4x4 matrix = new Matrix4x4();
        assertThrows(IllegalArgumentException.class, () -> matrix.set(4, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> matrix.set(0, 4, 1));
    }

    @Test
    void testEquals() {
        Matrix4x4 m1 = new Matrix4x4(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        );
        Matrix4x4 m2 = new Matrix4x4(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        );
        Matrix4x4 m3 = new Matrix4x4(
                16, 15, 14, 13,
                12, 11, 10, 9,
                8, 7, 6, 5,
                4, 3, 2, 1
        );
        assertTrue(m1.equals(m2));
        assertFalse(m1.equals(m3));
    }

    @Test
    void testAdd() {
        Matrix4x4 m1 = new Matrix4x4(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        );
        Matrix4x4 m2 = new Matrix4x4(
                16, 15, 14, 13,
                12, 11, 10, 9,
                8, 7, 6, 5,
                4, 3, 2, 1
        );
        Matrix4x4 result = m1.add(m2);
        assertEquals(17, result.get(0, 0));
        assertEquals(17, result.get(1, 1));
        assertEquals(17, result.get(2, 2));
        assertEquals(17, result.get(3, 3));
    }

    @Test
    void testSubtract() {
        Matrix4x4 m1 = new Matrix4x4(
                10, 10, 10, 10,
                10, 10, 10, 10,
                10, 10, 10, 10,
                10, 10, 10, 10
        );
        Matrix4x4 m2 = new Matrix4x4(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        );
        Matrix4x4 result = m1.subtract(m2);

        assertEquals(9, result.get(0, 0));
        assertEquals(8, result.get(0, 1));
        assertEquals(7, result.get(0, 2));
        assertEquals(6, result.get(0, 3));

        assertEquals(5, result.get(1, 0));
        assertEquals(4, result.get(1, 1));
        assertEquals(3, result.get(1, 2));
        assertEquals(2, result.get(1, 3));

        assertEquals(1, result.get(2, 0));
        assertEquals(0, result.get(2, 1));
        assertEquals(-1, result.get(2, 2));
        assertEquals(-2, result.get(2, 3));

        assertEquals(-3, result.get(3, 0));
        assertEquals(-4, result.get(3, 1));
        assertEquals(-5, result.get(3, 2));
        assertEquals(-6, result.get(3, 3));
    }

    @Test
    void testMultiplyVector() {
        Matrix4x4 matrix = new Matrix4x4(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        );
        Vector4f vector = new Vector4f(2, 3, 4, 5);
        Vector4f result = matrix.multiply(vector);
        assertEquals(40, result.x);
        assertEquals(96, result.y);
        assertEquals(152, result.z);
        assertEquals(208, result.w);
    }

    @Test
    void testMultiplyMatrix() {
        Matrix4x4 m1 = new Matrix4x4(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        );
        Matrix4x4 m2 = new Matrix4x4(
                16, 15, 14, 13,
                12, 11, 10, 9,
                8, 7, 6, 5,
                4, 3, 2, 1
        );
        Matrix4x4 result = m1.multiply(m2);
        assertEquals(80, result.get(0, 0));
        assertEquals(70, result.get(0, 1));
        assertEquals(240, result.get(1, 0));
        assertEquals(214, result.get(1, 1));
    }

    @Test
    void testTranspose() {
        Matrix4x4 matrix = new Matrix4x4(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        );
        Matrix4x4 transposed = matrix.transpose();
        assertEquals(1, transposed.get(0, 0));
        assertEquals(5, transposed.get(0, 1));
        assertEquals(9, transposed.get(0, 2));
        assertEquals(13, transposed.get(0, 3));
        assertEquals(2, transposed.get(1, 0));
        assertEquals(6, transposed.get(1, 1));
        assertEquals(10, transposed.get(1, 2));
        assertEquals(14, transposed.get(1, 3));
    }

    @Test
    void testToString() {
        Matrix4x4 matrix = new Matrix4x4(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        );
        String result = matrix.toString();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("1"));
        assertTrue(result.contains("6"));
        assertTrue(result.contains("11"));
        assertTrue(result.contains("16"));
    }
}