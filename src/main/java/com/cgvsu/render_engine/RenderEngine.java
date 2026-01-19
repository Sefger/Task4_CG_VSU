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
            int selectedPolyIdx,
            int selectedVertexIdx
    ) {
        // Z-буфер должен быть подготовлен в GuiController ДО вызова этого метода!

        Matrix4x4 viewMatrix = camera.getViewMatrix();
        Matrix4x4 projectionMatrix = camera.getProjectionMatrix();
        Matrix4x4 mvp = projectionMatrix.multiply(viewMatrix).multiply(modelMatrix);

        List<Polygon> polygons = mesh.getPolygons();
        for (int pIdx = 0; pIdx < polygons.size(); pIdx++) {
            Polygon poly = polygons.get(pIdx);
            int[] vIdx = poly.getVertexIndices();

            // Пропускаем полигоны с менее чем 3 вершин
            if (vIdx.length < 3) {
                continue;
            }

            // 1. Подготовим экранные координаты для всех вершин полигона
            float[] sx = new float[vIdx.length];
            float[] sy = new float[vIdx.length];
            float[] sz = new float[vIdx.length];
            float[] vIntensities = new float[vIdx.length];
            boolean skipPolygon = false;

            for (int j = 0; j < vIdx.length; j++) {
                // Проверка индекса вершины
                if (vIdx[j] < 0 || vIdx[j] >= mesh.getVertices().size()) {
                    skipPolygon = true;
                    break;
                }

                Vector3f vertex = mesh.getVertices().get(vIdx[j]);
                Vector3f transV = GraphicConveyor.multiplyMatrix4ByVector3(mvp, vertex);

                if (transV.z < -1 || transV.z > 1) {
                    skipPolygon = true;
                    break;
                }

                sx[j] = (transV.x + 1) * width * 0.5f;
                sy[j] = (1 - transV.y) * height * 0.5f;
                sz[j] = transV.z;
            }

            if (skipPolygon) continue;

            // 2. Рассчет освещения для каждой вершины (если нужно)
            if (useLighting && lights != null) {
                // Получаем индексы нормалей ДЛЯ ЭТОГО ПОЛИГОНА
                int[] nIdx = poly.getNormalIndices();

                for (int j = 0; j < vIdx.length; j++) {
                    Vector3f vertex = mesh.getVertices().get(vIdx[j]);
                    Vector3f worldPos = GraphicConveyor.multiplyMatrix4ByVector3(modelMatrix, vertex);

                    Vector3f normal;

                    // ПРОВЕРКА: есть ли нормали у этой вершины в файле?
                    if (nIdx != null && nIdx.length == vIdx.length && nIdx[j] != -1) {
                        // Нормаль загружена из файла
                        int normalIdx = nIdx[j];
                        if (normalIdx >= 0 && normalIdx < mesh.getNormals().size()) {
                            // БЕРЕМ НОРМАЛЬ ИЗ ФАЙЛА
                            normal = mesh.getNormals().get(normalIdx);
                        } else {
                            // Если индекс нормали некорректен, используем нормаль по умолчанию
                            normal = calculatePolygonNormal(poly, mesh.getVertices());
                        }
                    } else {
                        // Если в полигоне нет нормалей, вычисляем нормаль полигона
                        normal = calculatePolygonNormal(poly, mesh.getVertices());
                    }

                    // Преобразуем нормаль в мировое пространство
                    // GraphicConveyor.calculateTotalLighting будет нормализовать нормаль внутри себя
                    vIntensities[j] = GraphicConveyor.calculateTotalLighting(worldPos, normal, lights);
                }
            } else {
                // Если освещение отключено, используем полную яркость
                Arrays.fill(vIntensities, 0, vIdx.length, 1.0f);
            }

            // 3. Триангуляция веером ТОЛЬКО для растеризации (заливки)
            // Это нужно потому что методы растеризации (rasterizeTriangle) обычно работают только с треугольниками
            for (int i = 1; i < vIdx.length - 1; i++) {
                // Индексы для текущего треугольника
                int idx0 = 0;          // Центральная вершина
                int idx1 = i;          // Текущая вершина
                int idx2 = i + 1;      // Следующая вершина

                // Подготовка данных для треугольника
                Vector2f[] screenCoords = new Vector2f[3];
                float[] depths = new float[3];
                int[] vertexIndices = new int[3];
                float[] intensities = new float[3];

                screenCoords[0] = new Vector2f(sx[idx0], sy[idx0]);
                screenCoords[1] = new Vector2f(sx[idx1], sy[idx1]);
                screenCoords[2] = new Vector2f(sx[idx2], sy[idx2]);

                depths[0] = sz[idx0];
                depths[1] = sz[idx1];
                depths[2] = sz[idx2];

                vertexIndices[0] = vIdx[idx0];
                vertexIndices[1] = vIdx[idx1];
                vertexIndices[2] = vIdx[idx2];

                intensities[0] = vIntensities[idx0];
                intensities[1] = vIntensities[idx1];
                intensities[2] = vIntensities[idx2];

                // Получаем текстурные координаты (если есть)
                int[] texIndices = null;
                if (useTexture) {
                    int[] allTexIndices = poly.getTextureVertexIndices();
                    if (allTexIndices != null && allTexIndices.length == vIdx.length) {
                        texIndices = new int[]{
                                allTexIndices[idx0],
                                allTexIndices[idx1],
                                allTexIndices[idx2]
                        };
                    }
                }

                // 4. Растеризация треугольника (для заливки)
                // ТОЛЬКО если нужно заливать (текстура, освещение или не рисуем сетку)
                if (useTexture || useLighting || !drawGrid) {
                    GraphicConveyor.rasterizeTriangle(
                            gc.getPixelWriter(), zBuffer, width, height,
                            screenCoords[0], screenCoords[1], screenCoords[2],
                            depths[0], depths[1], depths[2],
                            vertexIndices, texIndices, mesh, intensities,
                            useTexture ? texture : null, useLighting
                    );
                }

                // 5. НЕ рисуем грани треугольника здесь - рисуем ниже весь полигон целиком
            }

            // 6. Отрисовка сетки (грани полигона)
            if (drawGrid) {
                if (pIdx == selectedPolyIdx) {
                    gc.setStroke(javafx.scene.paint.Color.RED);
                    gc.setLineWidth(2.0);
                } else {
                    gc.setStroke(javafx.scene.paint.Color.BLACK);
                    gc.setLineWidth(0.5);
                }

                // Рисуем ВСЕ грани полигона (N-угольника)
                for (int i = 0; i < vIdx.length; i++) {
                    int next = (i + 1) % vIdx.length;
                    gc.strokeLine(sx[i], sy[i], sx[next], sy[next]);
                }
            }

            // 7. Отображение выбранной вершины (ЭТУ ЧАСТЬ Я ВЕРНУЛ)
            if (selectedVertexIdx != -1) {
                for (int j = 0; j < vIdx.length; j++) {
                    if (vIdx[j] == selectedVertexIdx) {
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

    // Метод для вычисления нормали полигона (используется, если в файле нет нормалей)
    private static Vector3f calculatePolygonNormal(Polygon poly, List<Vector3f> vertices) {
        int[] vIdx = poly.getVertexIndices();
        if (vIdx.length < 3) {
            return new Vector3f(0, 0, 1); // Нормаль по умолчанию
        }

        // Берем первые три вершины полигона
        Vector3f v0 = vertices.get(vIdx[0]);
        Vector3f v1 = vertices.get(vIdx[1]);
        Vector3f v2 = vertices.get(vIdx[2]);

        // Вычисляем нормаль через векторное произведение
        Vector3f edge1 = Vector3f.subtract(v1, v0);
        Vector3f edge2 = Vector3f.subtract(v2, v0);
        return edge1.cross(edge2);
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