package com.cgvsu.affinetransformation;

import com.cgvsu.math.Matrix4x4;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;
import com.cgvsu.model.Model;

import java.util.List;

public class AffineTransformation {

    //Масштабирование
    public static void scale(Model model, float sx, float sy, float sz) {
        Matrix4x4 scaleMatrix = TransformationMatrix.createScaleMatrix(sx, sy, sz);
        applyTransformation(model, scaleMatrix);
    }

    // Поворот вокруг x
    public static void rotateX(Model model, float angle) {
        float radians = (float) Math.toRadians(angle);
        Matrix4x4 rotationMatrix = TransformationMatrix.createRotationXMatrix(radians);
        applyTransformation(model, rotationMatrix);
    }

    //Поворот y
    public static void rotateY(Model model, float angle) {
        float radians = (float) Math.toRadians(angle);
        Matrix4x4 rotationMatrix = TransformationMatrix.createRotationYMatrix(radians);
        applyTransformation(model, rotationMatrix);
    }

    //Поворот z
    public static void rotateZ(Model model, float angle) {
        float radians = (float) Math.toRadians(angle);
        Matrix4x4 rotationMatrix = TransformationMatrix.createRotationZMatrix(radians);
        applyTransformation(model, rotationMatrix);
    }

    // перенос
    public static void translate(Model model, float tx, float ty, float tz) {
        Matrix4x4 translationMatrix = TransformationMatrix.createTranslationMatrix(tx, ty, tz);
        applyTransformation(model, translationMatrix);
    }

    //применение матрицы к модели
    private static void applyTransformation(Model model, Matrix4x4 transformationMatrix) {
        List<Vector3f> vertices = model.getVertices();

        for (int i = 0; i < vertices.size(); i++) {
            Vector3f vertex = vertices.get(i);
            Vector4f vertex4D = new Vector4f(vertex.x, vertex.y, vertex.z, 1);

            Vector4f transformed4D = transformationMatrix.multiply(vertex4D);

            Vector3f transformed3D;
            if (Math.abs(transformed4D.w) > 1e-7f) {
                transformed3D = new Vector3f(
                        transformed4D.x / transformed4D.w,
                        transformed4D.y / transformed4D.w,
                        transformed4D.z / transformed4D.w
                );
            } else {
                transformed3D = new Vector3f(transformed4D.x, transformed4D.y, transformed4D.z);
            }

            vertices.set(i, transformed3D);
        }
    }
}