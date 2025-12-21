package com.cgvsu.math;

public class Vector4f {
    public Vector4f(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public boolean equals(Vector4f other) {
        final float eps = 1e-7f;
        return Math.abs(x - other.x) < eps &&
                Math.abs(y - other.y) < eps &&
                Math.abs(z - other.z) < eps &&
                Math.abs(w - other.w) < eps;
    }

    public float x, y, z, w;

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getW() {
        return w;
    }

    // сложение векторов
    public Vector4f add(Vector4f other) {
        return new Vector4f(x + other.x, y + other.y, z + other.z, w + other.w);
    }

    // вычитание векторов
    public Vector4f subtract(Vector4f other) {
        return new Vector4f(x - other.x, y - other.y, z - other.z, w - other.w);
    }

    // умножение на скаляр
    public Vector4f multiply(float scalar) {
        return new Vector4f(x * scalar, y * scalar, z * scalar, w * scalar);
    }

    // деление на скаляр
    public Vector4f divide(float scalar) {
        if (scalar == 0) {
            throw new IllegalArgumentException("Division by zero is not allowed");
        }
        return new Vector4f(x / scalar, y / scalar, z / scalar, w / scalar);
    }

    // вычисление длины вектора
    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z + w * w);
    }

    // нормализация вектора
    public Vector4f normalized() {
        float len = length();
        if (len == 0) {
            return new Vector4f(0, 0, 0, 0);
        }
        return new Vector4f(x / len, y / len, z / len, w / len);
    }

    // скалярное произведение векторов
    public float dot(Vector4f other) {
        return x * other.x + y * other.y + z * other.z + w * other.w;
    }
}