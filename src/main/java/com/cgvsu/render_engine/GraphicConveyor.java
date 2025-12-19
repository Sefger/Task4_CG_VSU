package com.cgvsu.render_engine;

import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javax.vecmath.*;

public class GraphicConveyor {

    public static Matrix4f rotateScaleTranslate() {
        Matrix4f m = new Matrix4f();
        m.setIdentity();
        return m;
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0F, 1.0F, 0F));
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f resultZ = new Vector3f();
        resultZ.sub(target, eye);
        resultZ.normalize();

        Vector3f resultX = new Vector3f();
        resultX.cross(up, resultZ);
        resultX.normalize();

        Vector3f resultY = new Vector3f();
        resultY.cross(resultZ, resultX);

        Matrix4f m = new Matrix4f();
        m.m00 = resultX.x; m.m01 = resultY.x; m.m02 = resultZ.x; m.m03 = 0;
        m.m10 = resultX.y; m.m11 = resultY.y; m.m12 = resultZ.y; m.m13 = 0;
        m.m20 = resultX.z; m.m21 = resultY.z; m.m22 = resultZ.z; m.m23 = 0;
        m.m30 = -resultX.dot(eye);
        m.m31 = -resultY.dot(eye);
        m.m32 = -resultZ.dot(eye);
        m.m33 = 1;
        return m;
    }

    public static Matrix4f perspective(float fov, float aspectRatio, float nearPlane, float farPlane) {
        Matrix4f result = new Matrix4f();
        float tangent = (float) (Math.tan(fov * 0.5F));
        result.m00 = 1.0F / (tangent * aspectRatio);
        result.m11 = 1.0F / tangent;
        result.m22 = (farPlane + nearPlane) / (farPlane - nearPlane);
        result.m23 = 1.0F;
        result.m32 = 2 * (nearPlane * farPlane) / (nearPlane - farPlane);
        return result;
    }

    public static Vector3f multiplyMatrix4ByVector3(final Matrix4f matrix, final Vector3f vertex) {
        float x = (vertex.x * matrix.m00) + (vertex.y * matrix.m10) + (vertex.z * matrix.m20) + matrix.m30;
        float y = (vertex.x * matrix.m01) + (vertex.y * matrix.m11) + (vertex.z * matrix.m21) + matrix.m31;
        float z = (vertex.x * matrix.m02) + (vertex.y * matrix.m12) + (vertex.z * matrix.m22) + matrix.m32;
        float w = (vertex.x * matrix.m03) + (vertex.y * matrix.m13) + (vertex.z * matrix.m23) + matrix.m33;
        return new Vector3f(x / w, y / w, z / w);
    }

    public static Point2f vertexToPoint(final Vector3f vertex, final int width, final int height) {
        return new Point2f(vertex.x * width + width / 2.0F, -vertex.y * height + height / 2.0F);
    }

    // ИСПРАВЛЕННЫЙ МЕТОД
    public static float[] getBarycentric(float x, float y, Point2f p1, Point2f p2, Point2f p3) {
        float det = (p2.y - p3.y) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.y - p3.y);
        if (Math.abs(det) < 0.00001f) return new float[]{-1, -1, -1};

        float alpha = ((p2.y - p3.y) * (x - p3.x) + (p3.x - p2.x) * (y - p3.y)) / det;
        float beta = ((p3.y - p1.y) * (x - p3.x) + (p1.x - p3.x) * (y - p3.y)) / det;
        float gamma = 1.0f - alpha - beta;
        return new float[]{alpha, beta, gamma};
    }

    public static void rasterizeTriangle(
            final PixelWriter pw, float[] zBuffer, int width, int height,
            Point2f p1, Point2f p2, Point2f p3,
            float z1, float z2, float z3,
            Polygon poly, Model mesh, Image texture, Vector3f lightDir) {

        int minX = (int) Math.max(0, Math.min(p1.x, Math.min(p2.x, p3.x)));
        int maxX = (int) Math.min(width - 1, Math.max(p1.x, Math.max(p2.x, p3.x)));
        int minY = (int) Math.max(0, Math.min(p1.y, Math.min(p2.y, p3.y)));
        int maxY = (int) Math.min(height - 1, Math.max(p1.y, Math.max(p2.y, p3.y)));

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                float[] bary = getBarycentric(x, y, p1, p2, p3);
                if (bary[0] >= 0 && bary[1] >= 0 && bary[2] >= 0) {
                    float currentZ = bary[0] * z1 + bary[1] * z2 + bary[2] * z3;
                    if (currentZ < zBuffer[y * width + x]) {
                        zBuffer[y * width + x] = currentZ;
                        int color = calculatePixelColor(bary[0], bary[1], bary[2], poly, mesh, texture, lightDir);
                        pw.setArgb(x, y, color);
                    }
                }
            }
        }
    }

    public static int calculatePixelColor(
            float a, float b, float c,
            Polygon poly, Model mesh,
            Image texture, Vector3f lightDir) {

        int baseColor;

        // 1. Проверяем наличие текстуры и UV-координат
        if (texture != null && !poly.getTextureVertexIndices().isEmpty()) {
            var texVerts = mesh.getTextureVertices();
            var indices = poly.getTextureVertexIndices();

            float u = a * texVerts.get(indices.get(0)).x + b * texVerts.get(indices.get(1)).x + c * texVerts.get(indices.get(2)).x;
            float v = a * texVerts.get(indices.get(0)).y + b * texVerts.get(indices.get(1)).y + c * texVerts.get(indices.get(2)).y;

            int tx = (int) (u * (texture.getWidth() - 1));
            int ty = (int) ((1 - v) * (texture.getHeight() - 1));

            // Защита от выхода за границы
            tx = Math.max(0, Math.min((int)texture.getWidth() - 1, tx));
            ty = Math.max(0, Math.min((int)texture.getHeight() - 1, ty));

            baseColor = texture.getPixelReader().getArgb(tx, ty);
        } else {
            // ЦВЕТ ПО УМОЛЧАНИЮ (Синий: 0xFF0000FF в формате ARGB)
            baseColor = 0xFF4287f5; // Приятный небесно-синий
        }

        // 2. Освещение (Phong Shading с интерполяцией нормалей)
        float intensity = 0.5f; // Значение по умолчанию
        if (!poly.getNormalIndices().isEmpty()) {
            var normals = mesh.getNormals();
            var nIdx = poly.getNormalIndices();

            Vector3f n = new Vector3f();
            n.x = a * normals.get(nIdx.get(0)).x + b * normals.get(nIdx.get(1)).x + c * normals.get(nIdx.get(2)).x;
            n.y = a * normals.get(nIdx.get(0)).y + b * normals.get(nIdx.get(1)).y + c * normals.get(nIdx.get(2)).y;
            n.z = a * normals.get(nIdx.get(0)).z + b * normals.get(nIdx.get(1)).z + c * normals.get(nIdx.get(2)).z;
            n.normalize();

            // Привязка света к камере (lightDir уже нормализован в RenderEngine)
            intensity = Math.max(0.1f, n.dot(lightDir));
        }

        return applyLighting(baseColor, intensity);
    }
    private static int applyLighting(int argb, float intensity) {
        // Извлекаем каналы: Альфа, Красный, Зеленый, Синий
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;

        // Умножаем каналы на интенсивность (от 0.1 до 1.0)
        r = (int) (r * intensity);
        g = (int) (g * intensity);
        b = (int) (b * intensity);

        // Гарантируем, что значения остаются в диапазоне 0-255
        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));

        // Собираем всё обратно в одно 32-битное число
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}