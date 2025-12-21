package com.cgvsu.math;

public class Matrix3x3 {
    private final float[][] matrix;

    public Matrix3x3() {
        this.matrix = new float[3][3];
    }

    public Matrix3x3(float[][] values) {
        if (values.length != 3 || values[0].length != 3) {
            throw new IllegalArgumentException("Matrix must be 3x3");
        }
        this.matrix = new float[3][3];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(values[i], 0, this.matrix[i], 0, 3);
        }
    }

    public Matrix3x3(float a11, float a12, float a13,
                     float a21, float a22, float a23,
                     float a31, float a32, float a33) {
        this.matrix = new float[][]{
                {a11, a12, a13},
                {a21, a22, a23},
                {a31, a32, a33}
        };
    }

    // единичная матрица
    public static Matrix3x3 identity() {
        return new Matrix3x3(
                1, 0, 0,
                0, 1, 0,
                0, 0, 1
        );
    }

    // нулевая матрица
    public static Matrix3x3 zero() {
        return new Matrix3x3(
                0, 0, 0,
                0, 0, 0,
                0, 0, 0
        );
    }
    public float get(int row, int col) {
        if (row < 0 || row >= 3 || col < 0 || col >= 3) {
            throw new IllegalArgumentException("Index out of bounds");
        }
        return matrix[row][col];
    }
    public void set(int row, int col, float value) {
        if (row < 0 || row >= 3 || col < 0 || col >= 3) {
            throw new IllegalArgumentException("Index out of bounds");
        }
        matrix[row][col] = value;
    }
    public boolean equals(Matrix3x3 other) {
        final float eps = 1e-7f;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (Math.abs(this.matrix[i][j] - other.matrix[i][j]) > eps) {
                    return false;
                }
            }
        }
        return true;
    }

    // сложение матриц
    public Matrix3x3 add(Matrix3x3 other) {
        float[][] result = new float[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i][j] = this.matrix[i][j] + other.matrix[i][j];
            }
        }
        return new Matrix3x3(result);
    }

    // вычитание матриц
    public Matrix3x3 subtract(Matrix3x3 other) {
        float[][] result = new float[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i][j] = this.matrix[i][j] - other.matrix[i][j];
            }
        }
        return new Matrix3x3(result);
    }

    // умножение на вектор-столбец
    public Vector3f multiply(Vector3f vector) {
        float x = matrix[0][0] * vector.x + matrix[0][1] * vector.y + matrix[0][2] * vector.z;
        float y = matrix[1][0] * vector.x + matrix[1][1] * vector.y + matrix[1][2] * vector.z;
        float z = matrix[2][0] * vector.x + matrix[2][1] * vector.y + matrix[2][2] * vector.z;
        return new Vector3f(x, y, z);
    }

    // перемножение матриц
    public Matrix3x3 multiply(Matrix3x3 other) {
        float[][] result = new float[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i][j] = 0;
                for (int k = 0; k < 3; k++) {
                    result[i][j] += this.matrix[i][k] * other.matrix[k][j];
                }
            }
        }
        return new Matrix3x3(result);
    }

    // транспонирование
    public Matrix3x3 transpose() {
        float[][] result = new float[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i][j] = this.matrix[j][i];
            }
        }
        return new Matrix3x3(result);
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append("[");
            for (int j = 0; j < 3; j++) {
                sb.append(String.format("%.4f", matrix[i][j]));
                if (j < 2) sb.append(", ");
            }
            sb.append("]\n");
        }
        return sb.toString();
    }
}