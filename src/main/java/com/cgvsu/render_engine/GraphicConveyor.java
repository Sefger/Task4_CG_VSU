package com.cgvsu.render_engine;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Matrix4x4;
import com.cgvsu.model.Model;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import java.util.List;

public class GraphicConveyor {

    public static Matrix4x4 rotateScaleTranslate() {
        return Matrix4x4.identity();
    }

    public static Matrix4x4 lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0F, 1.0F, 0F));
    }

    public static Matrix4x4 lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f zAxis = eye.subtract(target);
        zAxis = zAxis.normalized();

        Vector3f xAxis = up.cross(zAxis);
        xAxis = xAxis.normalized();

        Vector3f yAxis = zAxis.cross(xAxis);
        yAxis = yAxis.normalized();

        return new Matrix4x4(
                xAxis.x, xAxis.y, xAxis.z, -xAxis.dot(eye),
                yAxis.x, yAxis.y, yAxis.z, -yAxis.dot(eye),
                zAxis.x, zAxis.y, zAxis.z, -zAxis.dot(eye),
                0, 0, 0, 1
        );
    }

    public static Matrix4x4 perspective(float fov, float aspectRatio, float nearPlane, float farPlane) {
        float fovRadians = (float) Math.toRadians(fov);
        float tangent = (float) (Math.tan(fovRadians * 0.5F));

        return new Matrix4x4(
                1.0F / (tangent * aspectRatio), 0, 0, 0,
                0, 1.0F / tangent, 0, 0,
                0, 0, -(farPlane + nearPlane) / (farPlane - nearPlane), -(2 * farPlane * nearPlane) / (farPlane - nearPlane),
                0, 0, -1.0F, 0
        );
    }

    public static Vector3f multiplyMatrix4ByVector3(final Matrix4x4 m, final Vector3f v) {
        float x = m.get(0, 0) * v.x + m.get(0, 1) * v.y + m.get(0, 2) * v.z + m.get(0, 3);
        float y = m.get(1, 0) * v.x + m.get(1, 1) * v.y + m.get(1, 2) * v.z + m.get(1, 3);
        float z = m.get(2, 0) * v.x + m.get(2, 1) * v.y + m.get(2, 2) * v.z + m.get(2, 3);
        float w = m.get(3, 0) * v.x + m.get(3, 1) * v.y + m.get(3, 2) * v.z + m.get(3, 3);

        if (Math.abs(w) > 0.0001f) {
            return new Vector3f(x / w, y / w, z / w);
        } else {
            return new Vector3f(x, y, 0);
        }
    }

    public static void rasterizeTriangle(
            final PixelWriter pw, float[] zBuffer, int width, int height,
            Vector2f p1, Vector2f p2, Vector2f p3,
            float z1, float z2, float z3,
            int[] triV, int[] triT, Model mesh, Vector3f lightDir, Image texture) {

        int minX = (int) Math.max(0, Math.min(p1.x, Math.min(p2.x, p3.x)));
        int maxX = (int) Math.min(width - 1, Math.max(p1.x, Math.max(p2.x, p3.x)));
        int minY = (int) Math.max(0, Math.min(p1.y, Math.min(p2.y, p3.y)));
        int maxY = (int) Math.min(height - 1, Math.max(p1.y, Math.max(p2.y, p3.y)));

        if (minX > maxX || minY > maxY) return;

        float y2y3 = p2.y - p3.y;
        float x3x2 = p3.x - p2.x;
        float y3y1 = p3.y - p1.y;
        float x1x3 = p1.x - p3.x;
        float det = y2y3 * x1x3 + x3x2 * (p1.y - p3.y);
        if (Math.abs(det) < 0.000001f) return;
        float invDet = 1.0f / det;

        // Освещение в вершинах
        float i1 = calculateVertexIntensity(triV[0], mesh, lightDir);
        float i2 = calculateVertexIntensity(triV[1], mesh, lightDir);
        float i3 = calculateVertexIntensity(triV[2], mesh, lightDir);

        // Текстурные координаты
        Vector2f uv1 = getUV(triT != null ? triT[0] : -1, mesh);
        Vector2f uv2 = getUV(triT != null ? triT[1] : -1, mesh);
        Vector2f uv3 = getUV(triT != null ? triT[2] : -1, mesh);

        for (int y = minY; y <= maxY; y++) {
            float y_p3y = y - p3.y;
            int rowOffset = y * width;
            for (int x = minX; x <= maxX; x++) {
                float x_p3x = x - p3.x;
                float alpha = (y2y3 * x_p3x + x3x2 * y_p3y) * invDet;
                float beta = (y3y1 * x_p3x + x1x3 * y_p3y) * invDet;
                float gamma = 1.0f - alpha - beta;

                if (alpha >= 0 && beta >= 0 && gamma >= 0) {
                    float currentZ = alpha * z1 + beta * z2 + gamma * z3;
                    int idx = rowOffset + x;

                    if (currentZ < zBuffer[idx]) {
                        zBuffer[idx] = currentZ;

                        int color = 0x4287f5;
                        if (texture != null && triT != null) {
                            float u = alpha * uv1.x + beta * uv2.x + gamma * uv3.x;
                            float v = alpha * uv1.y + beta * uv2.y + gamma * uv3.y;
                            color = sampleTexture(texture, u, v);
                        }

                        float intensity = alpha * i1 + beta * i2 + gamma * i3;
                        pw.setArgb(x, y, applyIntensity(color, intensity));
                    }
                }
            }
        }
    }

    private static Vector2f getUV(int textureIdx, Model mesh) {
        List<Vector2f> uvs = mesh.getTextureVertices();
        if (textureIdx < 0 || uvs.isEmpty() || textureIdx >= uvs.size()) {
            return new Vector2f(0, 0);
        }
        return uvs.get(textureIdx);
    }

    private static int sampleTexture(Image tex, float u, float v) {
        int tw = (int) tex.getWidth();
        int th = (int) tex.getHeight();

        // Масштабируем UV к размерам изображения
        int tx = (int) (u * (tw - 1)) % tw;
        int ty = (int) ((1 - v) * (th - 1)) % th; // Инверсия V для формата OBJ

        if (tx < 0) tx += tw;
        if (ty < 0) ty += th;

        return tex.getPixelReader().getArgb(tx, ty);
    }

    private static float calculateVertexIntensity(int vertexIdx, Model mesh, Vector3f lightDir) {
        List<Vector3f> normals = mesh.getNormals();
        if (normals.isEmpty() || vertexIdx >= normals.size()) return 0.6f;

        Vector3f n = normals.get(vertexIdx);
        Vector3f normalized = n.normalized();
        float dot = normalized.dot(lightDir);
        return Math.max(0.2f, dot);
    }

    private static int applyIntensity(int color, float intensity) {
        int r = (int) (((color >> 16) & 0xFF) * intensity);
        int g = (int) (((color >> 8) & 0xFF) * intensity);
        int b = (int) ((color & 0xFF) * intensity);
        r = Math.min(255, Math.max(0, r));
        g = Math.min(255, Math.max(0, g));
        b = Math.min(255, Math.max(0, b));
        return (255 << 24) | (r << 16) | (g << 8) | b;
    }
}