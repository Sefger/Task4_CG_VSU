package com.cgvsu.math;

public class Vector3f {
    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean equals(Vector3f other) {
        final float eps = 1e-7f;
        return Math.abs(x - other.x) < eps &&
                Math.abs(y - other.y) < eps &&
                Math.abs(z - other.z) < eps;
    }

    public float x, y, z;

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() { return z; }

    // сложение векторов
    public Vector3f add(Vector3f other) {
        return new Vector3f(x + other.x, y + other.y, z + other.z);
    }

    // вычитание векторов
    public Vector3f subtract(Vector3f other) {
        return new Vector3f(x - other.x, y - other.y, z - other.z);
    }

    // умножение на скаляр
    public Vector3f multiply(float scalar) {
        return new Vector3f(x * scalar, y * scalar, z * scalar);
    }

    // деление на скаляр
    public Vector3f divide(float scalar) {
        if (scalar == 0) {
            throw new IllegalArgumentException("Division by zero is not allowed");
        }
        return new Vector3f(x / scalar, y / scalar, z / scalar);
    }

    // длина вектора
    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    // нормализация вектора
    public Vector3f normalized() {
        float len = length();
        if (len == 0) {
            return new Vector3f(0, 0, 0);
        }
        return new Vector3f(x / len, y / len, z / len);
    }

    // скалярное произведение
    public float dot(Vector3f other) {
        return x * other.x + y * other.y + z * other.z;
    }

    // векторное произведение
    public Vector3f cross(Vector3f other) {
        return new Vector3f(
                y * other.z - z * other.y,
                z * other.x - x * other.z,
                x * other.y - y * other.x
        );
    }
}