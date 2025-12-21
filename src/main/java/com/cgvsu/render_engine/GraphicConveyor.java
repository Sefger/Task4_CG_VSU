package com.cgvsu.render_engine;

import com.cgvsu.math.Vector2f;
import com.cgvsu.model.Model;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javax.vecmath.*;
import java.util.List;

public class GraphicConveyor {

    private static final Vector3f tempNormal = new Vector3f();

    public static Matrix4f rotateScaleTranslate() {
        Matrix4f m = new Matrix4f();
        m.setIdentity();
        return m;
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0F, 1.0F, 0F));
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f zAxis = new Vector3f();
        zAxis.sub(eye, target);
        zAxis.normalize();
        Vector3f xAxis = new Vector3f();
        xAxis.cross(up, zAxis);
        xAxis.normalize();
        Vector3f yAxis = new Vector3f();
        yAxis.cross(zAxis, xAxis);
        yAxis.normalize();

        Matrix4f m = new Matrix4f();
        m.setIdentity();
        m.m00 = xAxis.x; m.m01 = xAxis.y; m.m02 = xAxis.z; m.m03 = -xAxis.dot(eye);
        m.m10 = yAxis.x; m.m11 = yAxis.y; m.m12 = yAxis.z; m.m13 = -yAxis.dot(eye);
        m.m20 = zAxis.x; m.m21 = zAxis.y; m.m22 = zAxis.z; m.m23 = -zAxis.dot(eye);
        return m;
    }

    public static Matrix4f perspective(float fov, float aspectRatio, float nearPlane, float farPlane) {
        Matrix4f result = new Matrix4f();
        float fovRadians = (float) Math.toRadians(fov);
        float tangent = (float) (Math.tan(fovRadians * 0.5F));
        result.m00 = 1.0F / (tangent * aspectRatio);
        result.m11 = 1.0F / tangent;
        result.m22 = -(farPlane + nearPlane) / (farPlane - nearPlane);
        result.m23 = -(2 * farPlane * nearPlane) / (farPlane - nearPlane);
        result.m32 = -1.0F;
        result.m33 = 0;
        return result;
    }

    public static void multiplyMatrix4ByVector3(final Matrix4f m, final Vector3f v, Vector3f res) {
        float x = m.m00 * v.x + m.m01 * v.y + m.m02 * v.z + m.m03;
        float y = m.m10 * v.x + m.m11 * v.y + m.m12 * v.z + m.m13;
        float z = m.m20 * v.x + m.m21 * v.y + m.m22 * v.z + m.m23;
        float w = m.m30 * v.x + m.m31 * v.y + m.m32 * v.z + m.m33;

        if (Math.abs(w) > 0.0001f) {
            res.x = x / w; res.y = y / w; res.z = z / w;
        } else {
            res.set(x, y, 0);
        }
    }

    public static void rasterizeTriangle(
            final PixelWriter pw, float[] zBuffer, int width, int height,
            Point2f p1, Point2f p2, Point2f p3,
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

        // Освещение в вершинах (используем геометрические индексы triV)
        float i1 = calculateVertexIntensity(triV[0], mesh, lightDir);
        float i2 = calculateVertexIntensity(triV[1], mesh, lightDir);
        float i3 = calculateVertexIntensity(triV[2], mesh, lightDir);

        // Текстурные координаты (используем текстурные индексы triT)
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
        List<com.cgvsu.math.Vector3f> normals = mesh.getNormals();
        if (normals.isEmpty() || vertexIdx >= normals.size()) return 0.6f;

        com.cgvsu.math.Vector3f n = normals.get(vertexIdx);
        tempNormal.set(n.x, n.y, n.z);
        tempNormal.normalize();
        return Math.max(0.2f, tempNormal.dot(lightDir));
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