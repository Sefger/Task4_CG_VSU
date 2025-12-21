package com.cgvsu.math;

public class Matrix4x4 {
    private final float[][] matrix;

    public Matrix4x4() {
        this.matrix = new float[4][4];
    }

    public Matrix4x4(float[][] values) {
        if (values.length != 4 || values[0].length != 4) {
            throw new IllegalArgumentException("Matrix must be 4x4");
        }
        this.matrix = new float[4][4];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(values[i], 0, this.matrix[i], 0, 4);
        }
    }

    public Matrix4x4(float a11, float a12, float a13, float a14,
                     float a21, float a22, float a23, float a24,
                     float a31, float a32, float a33, float a34,
                     float a41, float a42, float a43, float a44) {
        this.matrix = new float[][]{
                {a11, a12, a13, a14},
                {a21, a22, a23, a24},
                {a31, a32, a33, a34},
                {a41, a42, a43, a44}
        };
    }
    public static Matrix4x4 identity() {
        return new Matrix4x4(
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        );
    }
    public static Matrix4x4 zero() {
        return new Matrix4x4(
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0
        );
    }
    public float get(int row, int col) {
        if (row < 0 || row >= 4 || col < 0 || col >= 4) {
            throw new IllegalArgumentException("Index out of bounds");
        }
        return matrix[row][col];
    }
    public void set(int row, int col, float value) {
        if (row < 0 || row >= 4 || col < 0 || col >= 4) {
            throw new IllegalArgumentException("Index out of bounds");
        }
        matrix[row][col] = value;
    }
    public boolean equals(Matrix4x4 other) {
        final float eps = 1e-7f;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (Math.abs(this.matrix[i][j] - other.matrix[i][j]) > eps) {
                    return false;
                }
            }
        }
        return true;
    }

    // сложение матриц
    public Matrix4x4 add(Matrix4x4 other) {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = this.matrix[i][j] + other.matrix[i][j];
            }
        }
        return new Matrix4x4(result);
    }

    // вычитание матриц
    public Matrix4x4 subtract(Matrix4x4 other) {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = this.matrix[i][j] - other.matrix[i][j];
            }
        }
        return new Matrix4x4(result);
    }

    // умножение на вектор-столбец
    public Vector4f multiply(Vector4f vector) {
        float x = matrix[0][0] * vector.x + matrix[0][1] * vector.y +
                matrix[0][2] * vector.z + matrix[0][3] * vector.w;
        float y = matrix[1][0] * vector.x + matrix[1][1] * vector.y +
                matrix[1][2] * vector.z + matrix[1][3] * vector.w;
        float z = matrix[2][0] * vector.x + matrix[2][1] * vector.y +
                matrix[2][2] * vector.z + matrix[2][3] * vector.w;
        float w = matrix[3][0] * vector.x + matrix[3][1] * vector.y +
                matrix[3][2] * vector.z + matrix[3][3] * vector.w;
        return new Vector4f(x, y, z, w);
    }

    // перемножение матриц
    public Matrix4x4 multiply(Matrix4x4 other) {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = 0;
                for (int k = 0; k < 4; k++) {
                    result[i][j] += this.matrix[i][k] * other.matrix[k][j];
                }
            }
        }
        return new Matrix4x4(result);
    }

    // транспонирование матрицы
    public Matrix4x4 transpose() {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = this.matrix[j][i];
            }
        }
        return new Matrix4x4(result);
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append("[");
            for (int j = 0; j < 4; j++) {
                sb.append(String.format("%.4f", matrix[i][j]));
                if (j < 3) sb.append(", ");
            }
            sb.append("]\n");
        }
        return sb.toString();
    }
}