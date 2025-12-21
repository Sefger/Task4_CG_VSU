package com.cgvsu.render_engine;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Matrix4x4;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.util.Arrays;

public class RenderEngine {
    private static float[] zBuffer;
    private static int lastW = -1, lastH = -1;

    public static void render(
            final GraphicsContext gc,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height,
            final Image texture) {

        if (zBuffer == null || width != lastW || height != lastH) {
            zBuffer = new float[width * height];
            lastW = width; lastH = height;
        }
        Arrays.fill(zBuffer, Float.POSITIVE_INFINITY);

        Matrix4x4 viewMatrix = camera.getViewMatrix();
        Matrix4x4 projectionMatrix = camera.getProjectionMatrix();
        Matrix4x4 mvp = projectionMatrix.multiply(viewMatrix);

        Vector3f lightDir = camera.getPosition().subtract(camera.getTarget());
        lightDir = lightDir.normalized();

        Vector3f transV;
        float[] sx = new float[3], sy = new float[3], sz = new float[3];

        for (Polygon poly : mesh.getPolygons()) {
            int[] vIdx = poly.getVertexIndices();
            int[] tIdx = poly.getTextureVertexIndices();

            // Проходим по полигону, разбивая его на треугольники (Triangle Fan)
            for (int i = 1; i < vIdx.length - 1; i++) {
                int[] triV = {vIdx[0], vIdx[i], vIdx[i + 1]};

                // Извлекаем соответствующие текстурные индексы, если они есть
                int[] triT = null;
                if (tIdx != null && tIdx.length >= vIdx.length) {
                    triT = new int[]{tIdx[0], tIdx[i], tIdx[i + 1]};
                }

                boolean skipTriangle = false;
                for (int j = 0; j < 3; j++) {
                    Vector3f v = mesh.getVertices().get(triV[j]);
                    transV = GraphicConveyor.multiplyMatrix4ByVector3(mvp, v);

                    // Отсечение по ближней и дальней плоскости
                    if (transV.z < -1 || transV.z > 1) { skipTriangle = true; break; }

                    sx[j] = (transV.x + 1) * width * 0.5f;
                    sy[j] = (1 - transV.y) * height * 0.5f;
                    sz[j] = transV.z;
                }

                if (skipTriangle) continue;

                // Back-face culling (удаление невидимых граней)
                float area = (sx[1] - sx[0]) * (sy[2] - sy[0]) - (sy[1] - sy[0]) * (sx[2] - sx[0]);
                if (area > 0) continue;

                GraphicConveyor.rasterizeTriangle(
                        gc.getPixelWriter(), zBuffer, width, height,
                        new Vector2f(sx[0], sy[0]), new Vector2f(sx[1], sy[1]), new Vector2f(sx[2], sy[2]),
                        sz[0], sz[1], sz[2],
                        triV, triT, mesh, lightDir, texture
                );
            }
        }
    }
}