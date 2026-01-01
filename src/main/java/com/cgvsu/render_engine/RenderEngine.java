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
            final Matrix4x4 modelMatrix, // Добавлено: матрица трансформации объекта в пространстве
            final Image texture,
            boolean drawGrid,
            boolean useTexture,
            boolean useLighting) {

        // 1. Инициализация Z-буфера
        if (zBuffer == null || width != lastW || height != lastH) {
            zBuffer = new float[width * height];
            lastW = width; lastH = height;
        }
        Arrays.fill(zBuffer, Float.POSITIVE_INFINITY);

        // 2. Подготовка матриц
        Matrix4x4 viewMatrix = camera.getViewMatrix();
        Matrix4x4 projectionMatrix = camera.getProjectionMatrix();

        // Важно: Порядок умножения матриц для получения MVP:
        // Вершина * Model * View * Projection
        Matrix4x4 mvp = projectionMatrix.multiply(viewMatrix).multiply(modelMatrix);

        // Направление света (от камеры к цели)
        Vector3f lightDir = camera.getPosition().subtract(camera.getTarget()).normalized();

        // 3. Проход по всем полигонам модели
        for (Polygon poly : mesh.getPolygons()) {
            int[] vIdx = poly.getVertexIndices();
            int[] tIdx = poly.getTextureVertexIndices();

            for (int i = 1; i < vIdx.length - 1; i++) {
                int[] triV = {vIdx[0], vIdx[i], vIdx[i + 1]};
                int[] triT = (tIdx != null && tIdx.length >= vIdx.length)
                        ? new int[]{tIdx[0], tIdx[i], tIdx[i + 1]}
                        : null;

                float[] sx = new float[3];
                float[] sy = new float[3];
                float[] sz = new float[3];
                boolean skipTriangle = false;

                for (int j = 0; j < 3; j++) {
                    Vector3f v = mesh.getVertices().get(triV[j]);

                    // Теперь вершина умножается на полную цепочку MVP
                    Vector3f transV = GraphicConveyor.multiplyMatrix4ByVector3(mvp, v);

                    if (transV.z < -1 || transV.z > 1) { skipTriangle = true; break; }

                    sx[j] = (transV.x + 1) * width * 0.5f;
                    sy[j] = (1 - transV.y) * height * 0.5f;
                    sz[j] = transV.z;
                }

                if (skipTriangle) continue;

                float area = (sx[1] - sx[0]) * (sy[2] - sy[0]) - (sy[1] - sy[0]) * (sx[2] - sx[0]);
                if (area > 0) continue;

                if (useTexture || useLighting || !drawGrid) {
                    GraphicConveyor.rasterizeTriangle(
                            gc.getPixelWriter(), zBuffer, width, height,
                            new Vector2f(sx[0], sy[0]), new Vector2f(sx[1], sy[1]), new Vector2f(sx[2], sy[2]),
                            sz[0], sz[1], sz[2],
                            triV, triT, mesh, lightDir,
                            useTexture ? texture : null,
                            useLighting
                    );
                }

                if (drawGrid) {
                    gc.setStroke(javafx.scene.paint.Color.BLACK);
                    gc.setLineWidth(0.5);
                    gc.strokePolygon(
                            new double[]{sx[0], sx[1], sx[2]},
                            new double[]{sy[0], sy[1], sy[2]},
                            3
                    );
                }
            }
        }
    }
}