package com.cgvsu.model;

import com.cgvsu.math.Vector3f;
import java.util.ArrayList;
import java.util.List;

public class ModelProcessor {

    // Основной метод триангуляции (Веерная)
    public static void triangulate(Model model) {
        if (model == null) return;

        ArrayList<Polygon> newPolygons = new ArrayList<>();

        for (Polygon polygon : model.getPolygonsInternal()) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
            int n = vertexIndices.size();

            if (n < 3) continue;

            // Если уже треугольник, просто оставляем
            if (n == 3) {
                newPolygons.add(polygon);
                continue;
            }

            // Разбиваем N-угольник на треугольники (веером)
            for (int i = 1; i < n - 1; i++) {
                Polygon triangle = new Polygon();

                // Вершины
                triangle.getVertexIndices().add(vertexIndices.get(0));
                triangle.getVertexIndices().add(vertexIndices.get(i));
                triangle.getVertexIndices().add(vertexIndices.get(i + 1));

                // Текстуры (если есть)
                if (polygon.getTextureVertexIndices().size() == n) {
                    triangle.getTextureVertexIndices().add(polygon.getTextureVertexIndices().get(0));
                    triangle.getTextureVertexIndices().add(polygon.getTextureVertexIndices().get(i));
                    triangle.getTextureVertexIndices().add(polygon.getTextureVertexIndices().get(i + 1));
                }

                // Нормали (если есть)
                if (polygon.getNormalIndices().size() == n) {
                    triangle.getNormalIndices().add(polygon.getNormalIndices().get(0));
                    triangle.getNormalIndices().add(polygon.getNormalIndices().get(i));
                    triangle.getNormalIndices().add(polygon.getNormalIndices().get(i + 1));
                }

                newPolygons.add(triangle);
            }
        }

        // Заменяем старые полигоны новыми треугольниками
        model.getPolygonsInternal().clear();
        model.getPolygonsInternal().addAll(newPolygons);
    }

    // Совместимость с GuiController
    public static Model triangulateWithEarClipping(Model model) {
        triangulate(model);
        return model;
    }

    // Тот самый метод расчета нормалей, который починит освещение
    public static void computeNormals(Model model) {
        if (model == null) return;

        model.getNormalsInternal().clear();
        int vertexCount = model.getVerticesInternal().size();

        // 1. Инициализация
        for (int i = 0; i < vertexCount; i++) {
            model.getNormalsInternal().add(new Vector3f(0, 0, 0));
        }

        // 2. Расчет нормалей граней
        for (Polygon polygon : model.getPolygonsInternal()) {
            ArrayList<Integer> vIdx = polygon.getVertexIndices();
            if (vIdx.size() < 3) continue;

            Vector3f v1 = model.getVerticesInternal().get(vIdx.get(0));
            Vector3f v2 = model.getVerticesInternal().get(vIdx.get(1));
            Vector3f v3 = model.getVerticesInternal().get(vIdx.get(2));

            // Векторы сторон
            Vector3f edge1 = new Vector3f(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z);
            Vector3f edge2 = new Vector3f(v3.x - v1.x, v3.y - v1.y, v3.z - v1.z);

            // Векторное произведение (Cross Product)
            Vector3f faceNormal = new Vector3f(
                    edge1.y * edge2.z - edge1.z * edge2.y,
                    edge1.z * edge2.x - edge1.x * edge2.z,
                    edge1.x * edge2.y - edge1.y * edge2.x
            );

            // Накопление нормалей в вершинах (для сглаживания)
            for (Integer index : vIdx) {
                Vector3f n = model.getNormalsInternal().get(index);
                n.x += faceNormal.x;
                n.y += faceNormal.y;
                n.z += faceNormal.z;
            }

            // Связываем индексы нормалей с индексами вершин
            polygon.getNormalIndices().clear();
            polygon.getNormalIndices().addAll(vIdx);
        }

        // 3. Нормализация
        for (Vector3f n : model.getNormalsInternal()) {
            float length = (float) Math.sqrt(n.x * n.x + n.y * n.y + n.z * n.z);
            if (length > 1e-6f) {
                n.x /= length; n.y /= length; n.z /= length;
            }
        }
    }

    // --- Вспомогательные методы ---

    public static boolean isTriangulated(Model model) {
        if (model == null) return false;
        for (Polygon p : model.getPolygons()) {
            if (p.getVertexIndices().size() != 3) return false;
        }
        return true;
    }

    public static boolean needsTriangulation(Model model) {
        return !isTriangulated(model);
    }

    public static String getPolygonStatistics(Model model) {
        if (model == null) return "No model";
        return "Total polygons: " + model.getPolygons().size();
    }

    public static boolean validateTriangulatedModel(Model model) {
        return isTriangulated(model);
    }
}