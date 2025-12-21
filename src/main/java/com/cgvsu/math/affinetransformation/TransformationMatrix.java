package com.cgvsu.math.affinetransformation;

import com.cgvsu.math.Matrix4x4;

public class TransformationMatrix {

    // матрица масштабирования
    public static Matrix4x4 createScaleMatrix(float sx, float sy, float sz) {
        return new Matrix4x4(
                sx, 0, 0, 0,
                0, sy, 0, 0,
                0, 0, sz, 0,
                0, 0, 0, 1
        );
    }

    // матрица поворота вокруг X
    public static Matrix4x4 createRotationXMatrix(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        return new Matrix4x4(
                1, 0, 0, 0,
                0, cos, sin, 0,
                0, -sin, cos, 0,
                0, 0, 0, 1
        );
    }

    // матрица поворота вокруг Y
    public static Matrix4x4 createRotationYMatrix(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        return new Matrix4x4(
                cos, 0, sin, 0,
                0, 1, 0, 0,
                -sin, 0, cos, 0,
                0, 0, 0, 1
        );
    }

    // поворот z
    public static Matrix4x4 createRotationZMatrix(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        return new Matrix4x4(
                cos, sin, 0, 0,
                -sin, cos, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        );
    }

    // матрица переноса
    public static Matrix4x4 createTranslationMatrix(float tx, float ty, float tz) {
        return new Matrix4x4(
                1, 0, 0, tx,
                0, 1, 0, ty,
                0, 0, 1, tz,
                0, 0, 0, 1
        );
    }
}