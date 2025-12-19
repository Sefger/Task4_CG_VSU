package com.cgvsu.model;

import com.cgvsu.math.Vector3f;
import java.util.ArrayList;

public class ModelProcessor {

    public static Model triangulate(Model model) {
        // Используем ваш Triangulator, он написан корректно
        return new Triangulator(model).triangulate(model);
    }

    public static void computeNormals(Model model) {
        if (model == null) return;

        model.getNormalsInternal().clear();

        // 1. Инициализируем нормали (0,0,0) для каждой вершины
        for (int i = 0; i < model.getVerticesInternal().size(); i++) {
            model.getNormalsInternal().add(new Vector3f(0, 0, 0));
        }

        // 2. Считаем сумму нормалей смежных граней (Smooth Shading)
        for (Polygon polygon : model.getPolygonsInternal()) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
            // Защита от битых полигонов
            if (vertexIndices.size() < 3) continue;

            Vector3f v1 = model.getVerticesInternal().get(vertexIndices.get(0));
            Vector3f v2 = model.getVerticesInternal().get(vertexIndices.get(1));
            Vector3f v3 = model.getVerticesInternal().get(vertexIndices.get(2));

            // Считаем векторное произведение (нормаль грани)
            Vector3f edge1 = new Vector3f(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z);
            Vector3f edge2 = new Vector3f(v3.x - v1.x, v3.y - v1.y, v3.z - v1.z);

            // Cross product
            Vector3f normal = new Vector3f(
                    edge1.y * edge2.z - edge1.z * edge2.y,
                    edge1.z * edge2.x - edge1.x * edge2.z,
                    edge1.x * edge2.y - edge1.y * edge2.x
            );

            // Добавляем эту нормаль ко всем вершинам этого полигона
            for (Integer vertexIndex : vertexIndices) {
                Vector3f current = model.getNormalsInternal().get(vertexIndex);
                current.x += normal.x;
                current.y += normal.y;
                current.z += normal.z;
            }

            // КРИТИЧЕСКИ ВАЖНО:
            // Даже если у полигона не было индексов нормалей, мы их создаем!
            // Теперь i-я вершина полигона всегда ссылается на i-ю нормаль в общем списке.
            ArrayList<Integer> newNormalIndices = new ArrayList<>(vertexIndices);
            polygon.setNormalIndices(newNormalIndices);
        }

        // 3. Нормализуем векторы (приводим длину к 1)
        for (Vector3f n : model.getNormalsInternal()) {
            float len = (float) Math.sqrt(n.x * n.x + n.y * n.y + n.z * n.z);
            if (len > 1e-6) {
                n.x /= len;
                n.y /= len;
                n.z /= len;
            }
        }
    }

    // Вспомогательные методы
    public static boolean isTriangulated(Model model) {
        if (model == null || model.getPolygons() == null) return false;
        for (Polygon p : model.getPolygons()) {
            if (p.getVertexIndices().size() != 3) return false;
        }
        return true;
    }

    public static boolean validateTriangulatedModel(Model model) {
        // Простая заглушка, так как основная логика теперь в computeNormals и Triangulator
        return isTriangulated(model);
    }

    public static boolean needsTriangulation(Model model) {
        return !isTriangulated(model);
    }

    public static String getPolygonStatistics(Model model) {
        return "Polygons: " + model.getPolygons().size();
    }
    public static Model triangulateWithEarClipping(Model model) {
        return triangulate(model);
    }
}