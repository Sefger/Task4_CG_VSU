package com.cgvsu;

import com.cgvsu.math.Matrix4x4;
import com.cgvsu.model.ModelProcessor;
import javafx.fxml.FXML;
import com.cgvsu.model.Model;

public class AffineTransformation {

    // матрица масштабирования
    public static Matrix4x4 scale(float sx, float sy, float sz) {
        return new Matrix4x4(
                sx, 0, 0, 0,
                0, sy, 0, 0,
                0, 0, sz, 0,
                0, 0, 0, 1
        );
    }

    // матрица поворота вокруг X
    public static Matrix4x4 rotationX(float angle) {
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
    public static Matrix4x4 rotationY(float angle) {
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
    public static Matrix4x4 rotationZ(float angle) {
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
    public static Matrix4x4 translation(float tx, float ty, float tz) {
        return new Matrix4x4(
                1, 0, 0, tx,
                0, 1, 0, ty,
                0, 0, 1, tz,
                0, 0, 0, 1
        );
    }

    public static Matrix4x4 combine(Matrix4x4... matrices) {
        if (matrices.length == 0) {
            return Matrix4x4.identity();
        }

        Matrix4x4 result = matrices[0];
        for (int i = 1; i < matrices.length; i++) {
            result = result.multiply(matrices[i]);
        }
        return result;
    }

    public static Matrix4x4 createModelMatrix(
            float tx, float ty, float tz,
            float rx, float ry, float rz,
            float sx, float sy, float sz) {
        Matrix4x4 scale = scale(sx, sy, sz);
        Matrix4x4 rotateX = rotationX(rx);
        Matrix4x4 rotateY = rotationX(ry);
        Matrix4x4 rotateZ = rotationX(rz);
        Matrix4x4 translate = translation(tx, ty, tz);

        return combine(translate, rotateZ, rotateY, rotateX, scale);
    }
    public static void transformate(Model model, float tx, float ty, float tz, float rx, float ry, float rz, float sx, float sy, float sz) {
        if (model == null) return;

        Matrix4x4 current = model.getModelMatrix();
        Matrix4x4 scale = scale(sx, sy, sz);
        Matrix4x4 rotationX = rotationX(rx);
        Matrix4x4 rotationY = rotationY(ry);
        Matrix4x4 rotationZ = rotationZ(rz);
        Matrix4x4 translation = translation(tx, ty, tz);

        Matrix4x4 transform = combine(translation, rotationZ, rotationY, rotationX, scale);

        model.setModelMatrix(transform.multiply(current));

        ModelProcessor.computeNormals(model);
    }
}