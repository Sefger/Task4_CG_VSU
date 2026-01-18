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

    public static void prepareZBuffer(int width, int height) {
        if (zBuffer == null || width != lastW || height != lastH) {
            zBuffer = new float[width * height];
            lastW = width;
            lastH = height;
        }
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
            boolean useLighting,
            int selectedPolyIdx,   // ID выбранного полигона (-1 если нет)
            int selectedVertexIdx  // ID выбранной вершины (-1 если нет)
    ) {
        // Z-буфер должен быть подготовлен в GuiController ДО вызова этого метода!

        Matrix4x4 viewMatrix = camera.getViewMatrix();
        Matrix4x4 projectionMatrix = camera.getProjectionMatrix();
        Matrix4x4 mvp = projectionMatrix.multiply(viewMatrix).multiply(modelMatrix);

        List<Polygon> polygons = mesh.getPolygons();
        for (int pIdx = 0; pIdx < polygons.size(); pIdx++) {
            Polygon poly = polygons.get(pIdx);
            int[] vIdx = poly.getVertexIndices();

            for (int i = 1; i < vIdx.length - 1; i++) {
                int[] triV = {vIdx[0], vIdx[i], vIdx[i + 1]};

                float[] sx = new float[3];
                float[] sy = new float[3];
                float[] sz = new float[3];
                float[] vIntensities = new float[3];
                boolean skipTriangle = false;

                // Сначала считаем экранные координаты, чтобы определить area
                for (int j = 0; j < 3; j++) {
                    Vector3f vertex = mesh.getVertices().get(triV[j]);
                    Vector3f transV = GraphicConveyor.multiplyMatrix4ByVector3(mvp, vertex);

                    if (transV.z < -1 || transV.z > 1) { skipTriangle = true; break; }

                    sx[j] = (transV.x + 1) * width * 0.5f;
                    sy[j] = (1 - transV.y) * height * 0.5f;
                    sz[j] = transV.z;
                }

                if (skipTriangle) continue;

                // Определяем ориентацию (лицо/изнанка)
                float area = (sx[1] - sx[0]) * (sy[2] - sy[0]) - (sy[1] - sy[0]) * (sx[2] - sx[0]);
                boolean isBackFace = (area > 0);

                // Рассчет освещения (с учетом инверсии для внутренних стенок)
                for (int j = 0; j < 3; j++) {
                    if (useLighting && lights != null && !mesh.getNormals().isEmpty()) {
                        Vector3f vertex = mesh.getVertices().get(triV[j]);
                        Vector3f worldPos = GraphicConveyor.multiplyMatrix4ByVector3(modelMatrix, vertex);
                        Vector3f normal = mesh.getNormals().get(triV[j]);

                        Vector3f worldNormal = GraphicConveyor.multiplyMatrix4ByVector3(modelMatrix, normal)
                                .subtract(GraphicConveyor.multiplyMatrix4ByVector3(modelMatrix, new Vector3f(0,0,0)))
                                .normalized();

                        // Если это изнанка, инвертируем нормаль, чтобы свет падал на внутреннюю стенку
                        if (isBackFace) {
                            worldNormal = new Vector3f(-worldNormal.x, -worldNormal.y, -worldNormal.z);
                        }

                        vIntensities[j] = GraphicConveyor.calculateTotalLighting(worldPos, worldNormal, lights);
                    } else {
                        vIntensities[j] = 1.0f;
                    }
                }

                // 1. Растеризация закрашенного треугольника
                // Теперь вызываем всегда, не пропуская area > 0
                if (useTexture || useLighting || !drawGrid) {
                    GraphicConveyor.rasterizeTriangle(
                            gc.getPixelWriter(), zBuffer, width, height,
                            new Vector2f(sx[0], sy[0]), new Vector2f(sx[1], sy[1]), new Vector2f(sx[2], sy[2]),
                            sz[0], sz[1], sz[2],
                            triV, poly.getTextureVertexIndices(), mesh, vIntensities,
                            useTexture ? texture : null, useLighting
                    );
                }

                // 2. Отрисовка сетки
                if (drawGrid || pIdx == selectedPolyIdx) {
                    if (pIdx == selectedPolyIdx) {
                        gc.setStroke(javafx.scene.paint.Color.RED);
                        gc.setLineWidth(2.0);
                    } else {
                        // Лицевая сетка - черная, внутренняя - серая
                        gc.setStroke(isBackFace ? javafx.scene.paint.Color.GRAY : javafx.scene.paint.Color.BLACK);
                        gc.setLineWidth(0.5);
                    }

                    gc.strokePolygon(
                            new double[]{sx[0], sx[1], sx[2]},
                            new double[]{sy[0], sy[1], sy[2]},
                            3
                    );
                }

                // 3. Выбранная вершина (с проверкой глубины, чтобы не "рентгенить")
                if (selectedVertexIdx != -1) {
                    for (int j = 0; j < 3; j++) {
                        if (triV[j] == selectedVertexIdx) {
                            int px = (int) sx[j];
                            int py = (int) sy[j];
                            if (px >= 0 && px < width && py >= 0 && py < height) {
                                if (sz[j] <= zBuffer[py * width + px] + 0.001f) {
                                    gc.setFill(javafx.scene.paint.Color.BLUE);
                                    gc.fillOval(sx[j] - 4, sy[j] - 4, 8, 8);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void renderAxes(
            final GraphicsContext gc,
            final Camera camera,
            final int width,
            final int height) {

        Matrix4x4 viewMatrix = camera.getViewMatrix();

        // Извлекаем компоненты вращения из матрицы вида
        float[][] rot = {
                {viewMatrix.get(0, 0), viewMatrix.get(0, 1), viewMatrix.get(0, 2)},
                {viewMatrix.get(1, 0), viewMatrix.get(1, 1), viewMatrix.get(1, 2)},
                {viewMatrix.get(2, 0), viewMatrix.get(2, 1), viewMatrix.get(2, 2)}
        };

        // Параметры отображения
        float margin = 60;      // Отступ от угла экрана
        float fixedSize = 35;   // Длина линий в пикселях
        float textOffset = 12;  // На сколько пикселей буква дальше конца линии

        float centerX = margin;
        float centerY = height - margin;

        gc.setLineWidth(2.5);
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 14));

        // Отрисовка осей с буквами
        drawFixedAxis(gc, centerX, centerY, rot[0][0], -rot[1][0], fixedSize, textOffset, "X", javafx.scene.paint.Color.RED);
        drawFixedAxis(gc, centerX, centerY, rot[0][1], -rot[1][1], fixedSize, textOffset, "Y", javafx.scene.paint.Color.GREEN);
        drawFixedAxis(gc, centerX, centerY, rot[0][2], -rot[1][2], fixedSize, textOffset, "Z", javafx.scene.paint.Color.BLUE);

        gc.setLineWidth(1.0);
    }

    private static void drawFixedAxis(
            GraphicsContext gc,
            float cx, float cy,
            float dirX, float dirY,
            float length,
            float textOffset,
            String label,
            javafx.scene.paint.Color color) {

        float currentProjLength = (float) Math.sqrt(dirX * dirX + dirY * dirY);

        if (currentProjLength > 0.0001f) {
            // Нормализованное направление на экране
            float nx = dirX / currentProjLength;
            float ny = dirY / currentProjLength;

            // Координаты конца линии
            float endX = cx + nx * length;
            float endY = cy + ny * length;

            // Рисуем линию
            gc.setStroke(color);
            gc.strokeLine(cx, cy, endX, endY);

            // Рисуем букву чуть дальше конца линии
            gc.setFill(color);
            // Небольшая корректировка, чтобы буква была по центру точки
            gc.fillText(label, endX + nx * textOffset - 5, endY + ny * textOffset + 5);
        }
    }

    public static float rayTriangleIntersection(
            Vector3f rayOrigin, Vector3f rayDir,
            Vector3f v0, Vector3f v1, Vector3f v2) {

        // Используем статические методы, чтобы не менять оригинальные вершины
        Vector3f edge1 = Vector3f.subtract(v1, v0);
        Vector3f edge2 = Vector3f.subtract(v2, v0);

        Vector3f pvec = rayDir.cross(edge2);
        float det = edge1.dot(pvec);

        // Если det близок к 0, луч параллелен плоскости треугольника
        if (Math.abs(det) < 0.000001f) return -1;

        float invDet = 1.0f / det;
        Vector3f tvec = Vector3f.subtract(rayOrigin, v0);

        float u = tvec.dot(pvec) * invDet;
        if (u < 0 || u > 1) return -1;

        Vector3f qvec = tvec.cross(edge1);
        float v = rayDir.dot(qvec) * invDet;
        if (v < 0 || u + v > 1) return -1;

        float t = edge2.dot(qvec) * invDet;

        return (t > 0) ? t : -1; // Возвращаем дистанцию только если треугольник впереди
    }

}