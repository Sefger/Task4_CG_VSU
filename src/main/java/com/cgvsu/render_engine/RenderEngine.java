package com.cgvsu.render_engine;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Matrix4x4;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.util.Arrays;
import java.util.List;

public class RenderEngine {
    private static float[] zBuffer;
    private static int lastW = -1, lastH = -1;

    // Метод для очистки буфера перед началом отрисовки ВСЕЙ сцены
    public static void prepareZBuffer(int width, int height) {
        if (zBuffer == null || width != lastW || height != lastH) {
            zBuffer = new float[width * height];
            lastW = width;
            lastH = height;
        }
        // Заполняем буфер "бесконечной" далью
        Arrays.fill(zBuffer, Float.POSITIVE_INFINITY);
    }

    public static void render(
            final GraphicsContext gc,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height,
            final Matrix4x4 modelMatrix,
            final Image texture,
            final List<Light> lights,
            boolean drawGrid,
            boolean useTexture,
            boolean useLighting) {

        // Проверка на случай, если забыли вызвать prepareZBuffer
        if (zBuffer == null || width != lastW || height != lastH) {
            prepareZBuffer(width, height);
        }

        Matrix4x4 viewMatrix = camera.getViewMatrix();
        Matrix4x4 projectionMatrix = camera.getProjectionMatrix();
        Matrix4x4 mvp = projectionMatrix.multiply(viewMatrix).multiply(modelMatrix);

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
                float[] vIntensities = new float[3];
                boolean skipTriangle = false;

                for (int j = 0; j < 3; j++) {
                    Vector3f vertex = mesh.getVertices().get(triV[j]);

                    if (useLighting && lights != null) {
                        Vector3f worldPos = GraphicConveyor.multiplyMatrix4ByVector3(modelMatrix, vertex);
                        Vector3f normal = mesh.getNormals().get(triV[j]);
                        Vector3f worldNormal = GraphicConveyor.multiplyMatrix4ByVector3(modelMatrix, normal)
                                .subtract(GraphicConveyor.multiplyMatrix4ByVector3(modelMatrix, new Vector3f(0,0,0)))
                                .normalized();

                        vIntensities[j] = GraphicConveyor.calculateTotalLighting(worldPos, worldNormal, lights);
                    } else {
                        vIntensities[j] = 1.0f;
                    }

                    Vector3f transV = GraphicConveyor.multiplyMatrix4ByVector3(mvp, vertex);

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
                            triV, triT, mesh, vIntensities,
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

    public static void renderAxes(
            final GraphicsContext gc,
            final Camera camera,
            final Matrix4x4 modelMatrix,
            final int width,
            final int height) {

        Matrix4x4 viewMatrix = camera.getViewMatrix();
        Matrix4x4 projectionMatrix = camera.getProjectionMatrix();
        Matrix4x4 mvp = projectionMatrix.multiply(viewMatrix).multiply(modelMatrix);

        Vector3f center = new Vector3f(0, 0, 0);
        Vector3f axisX = new Vector3f(5.0f, 0, 0);
        Vector3f axisY = new Vector3f(0, 5.0f, 0);
        Vector3f axisZ = new Vector3f(0, 0, 5.0f);

        Vector3f screenCenter = projectPoint(mvp, center, width, height);
        Vector3f screenX = projectPoint(mvp, axisX, width, height);
        Vector3f screenY = projectPoint(mvp, axisY, width, height);
        Vector3f screenZ = projectPoint(mvp, axisZ, width, height);

        if (screenCenter == null) return;

        if (screenX != null) {
            gc.setStroke(javafx.scene.paint.Color.RED);
            gc.strokeLine(screenCenter.x, screenCenter.y, screenX.x, screenX.y);
        }
        if (screenY != null) {
            gc.setStroke(javafx.scene.paint.Color.GREEN);
            gc.strokeLine(screenCenter.x, screenCenter.y, screenY.x, screenY.y);
        }
        if (screenZ != null) {
            gc.setStroke(javafx.scene.paint.Color.BLUE);
            gc.strokeLine(screenCenter.x, screenCenter.y, screenZ.x, screenZ.y);
        }
    }

    private static Vector3f projectPoint(Matrix4x4 mvp, Vector3f point, int width, int height) {
        Vector3f transV = GraphicConveyor.multiplyMatrix4ByVector3(mvp, point);
        if (transV.z < -1 || transV.z > 1) return null;
        float x = (transV.x + 1) * width * 0.5f;
        float y = (1 - transV.y) * height * 0.5f;
        return new Vector3f(x, y, transV.z);
    }
}