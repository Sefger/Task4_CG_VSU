package com.cgvsu.math;

public class Matrix4x4 {
    private float[][] matrix;

    public Matrix4x4(float[][] values) {
        if (values.length != 4 || values[0].length != 4) {
            throw new IllegalArgumentException("Matrix must be 4x4");
        }
        this.matrix = values;
    }

    public float get(int row, int col) {
        return matrix[row][col];
    }

    public void set(int row, int col, float value) {
        matrix[row][col] = value;
    }

    public Vector4f multiply(Vector4f vector) {
        float[] result = new float[4];
        for (int i = 0; i < 4; i++) {
            result[i] = 0;
            for (int j = 0; j < 4; j++) {
                result[i] += matrix[i][j] * vector.get(j);
            }
        }
        return new Vector4f(result[0], result[1], result[2], result[3]);
    }

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

    // матрица масштабирования
    public static Matrix4x4 createScaleMatrix(float sx, float sy, float sz) {
        return new Matrix4x4(new float[][]{
                {sx, 0, 0, 0},
                {0, sy, 0, 0},
                {0, 0, sz, 0},
                {0, 0, 0, 1}
        });
    }

    // матрица поворота вокруг X
    public static Matrix4x4 createRotationXMatrix(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        return new Matrix4x4(new float[][]{
                {1, 0, 0, 0},
                {0, cos, sin, 0},
                {0, -sin, cos, 0},
                {0, 0, 0, 1}
        });
    }

    // матрица поворота вокруг Y
    public static Matrix4x4 createRotationYMatrix(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        return new Matrix4x4(new float[][]{
                {cos, 0, sin, 0},
                {0, 1, 0, 0},
                {-sin, 0, cos, 0},
                {0, 0, 0, 1}
        });
    }

    // поворот z
    public static Matrix4x4 createRotationZMatrix(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        return new Matrix4x4(new float[][]{
                {cos, sin, 0, 0},
                {-sin, cos, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        });
    }

    // матрица переноса
    public static Matrix4x4 createTranslationMatrix(float tx, float ty, float tz) {
        return new Matrix4x4(new float[][]{
                {1, 0, 0, tx},
                {0, 1, 0, ty},
                {0, 0, 1, tz},
                {0, 0, 0, 1}
        });
    }

}