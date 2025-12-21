package com.cgvsu.math;

public class Vector2f {
    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float x, y;

    public boolean equals(Vector2f other) {
        final float eps = 1e-7f;
        return Math.abs(x - other.x) < eps &&
                Math.abs(y - other.y) < eps;
    }
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    // сложение векторов
    public Vector2f add(Vector2f other) {
        return new Vector2f(this.x + other.x, this.y + other.y);
    }

    // вычитание векторов
    public Vector2f subtract(Vector2f other) {
        return new Vector2f(this.x - other.x, this.y - other.y);
    }

    // умножение на скаляр
    public Vector2f multiply(float scalar) {
        return new Vector2f(this.x * scalar, this.y * scalar);
    }

    // деление на скаляр
    public Vector2f divide(float scalar) {
        if (scalar == 0) {
            throw new IllegalArgumentException("Division by zero is not allowed");
        }
        return new Vector2f(this.x / scalar, this.y / scalar);
    }

    // длина вектора
    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    // нормализация
    public Vector2f normalized() {
        float len = length();
        if (len == 0) {
            return new Vector2f(0, 0);
        }
        return new Vector2f(x / len, y / len);
    }

    // скалярное произведение
    public float dot(Vector2f other) {
        return this.x * other.x + this.y * other.y;
    }
}