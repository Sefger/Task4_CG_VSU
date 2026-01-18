package com.cgvsu.model;

import com.cgvsu.math.Vector3f;
import java.util.ArrayList;

public class ModelProcessor {

    // Добавляем этот метод, который потерял компилятор
    public static boolean isTriangulated(Model model) {
        if (model == null) return false;
        for (Polygon polygon : model.getPolygons()) {
            if (polygon.getVertexIndices().length != 3) {
                return false;
            }
        }
        return true;
    }

    public static void triangulate(Model model) {
        if (model == null || isTriangulated(model)) return;

        ArrayList<Polygon> oldPolygons = new ArrayList<>(model.getPolygons());
        ArrayList<Polygon> newPolygons = new ArrayList<>();

        for (Polygon poly : oldPolygons) {
            int[] vIdx = poly.getVertexIndices();
            int n = vIdx.length;
            if (n < 3) continue;

            int[] tIdx = poly.getTextureVertexIndices();
            int[] nIdx = poly.getNormalIndices();

            // Веерная триангуляция (Triangle Fan)
            for (int i = 1; i < n - 1; i++) {
                Polygon triangle = new Polygon(3);

                triangle.setVertexIndices(new int[]{vIdx[0], vIdx[i], vIdx[i + 1]});

                // Перенос текстурных индексов
                if (tIdx != null && tIdx.length == n) {
                    triangle.setTextureVertexIndices(new int[]{tIdx[0], tIdx[i], tIdx[i + 1]});
                }
                // Перенос индексов нормалей
                if (nIdx != null && nIdx.length == n) {
                    triangle.setNormalIndices(new int[]{nIdx[0], nIdx[i], nIdx[i + 1]});
                }
                newPolygons.add(triangle);
            }
        }
        model.getPolygons().clear();
        model.getPolygons().addAll(newPolygons);
    }

    // Вспомогательный метод для GuiController
    public static Model triangulateWithEarClipping(Model model) {
        triangulate(model);
        return model;
    }

    public static void computeNormals(Model model) {
        if (model == null) return;
        ArrayList<Vector3f> vertices = (ArrayList<Vector3f>) model.getVertices();
        ArrayList<Vector3f> normals = (ArrayList<Vector3f>) model.getNormals();

        normals.clear();
        for (int i = 0; i < vertices.size(); i++) {
            normals.add(new Vector3f(0, 0, 0));
        }

        for (Polygon poly : model.getPolygons()) {
            int[] vIdx = poly.getVertexIndices();
            if (vIdx.length < 3) continue;

            Vector3f v1 = vertices.get(vIdx[0]);
            Vector3f v2 = vertices.get(vIdx[1]);
            Vector3f v3 = vertices.get(vIdx[2]);

            Vector3f e1 = v2.subtract(v1);
            Vector3f e2 = v3.subtract(v1);

            Vector3f normal = e1.cross(e2);

            for (int idx : vIdx) {
                Vector3f n = normals.get(idx);
                n.add(normal);
            }
            poly.setNormalIndices(vIdx.clone());
        }

        for (Vector3f n : normals) {
            Vector3f normalized = n.normalized();
            n.x = normalized.x;
            n.y = normalized.y;
            n.z = normalized.z;
        }
    }

    public static String getPolygonStatistics(Model model) {
        if (model == null) return "Model is empty";
        return "Polygons: " + model.getPolygons().size();
    }
    public static boolean needsTriangulation(Model model) {
        return !isTriangulated(model);
    }

    // Метод для удаления вершин и пересчета индексов
    public static void deleteVertices(Model model, java.util.List<Integer> indicesToDelete) {
        if (model == null || indicesToDelete == null || indicesToDelete.isEmpty()) return;

        // 1. Используем Set для быстрого поиска
        java.util.Set<Integer> toDeleteSet = new java.util.HashSet<>(indicesToDelete);

        // 2. Удаляем все полигоны, которые содержат хотя бы одну удаляемую вершину
        model.getPolygons().removeIf(poly -> {
            for (int vIdx : poly.getVertexIndices()) {
                if (toDeleteSet.contains(vIdx)) return true;
            }
            return false;
        });

        // 3. Создаем карту смещения индексов
        // Новое положение вершины = старое положение - количество удаленных вершин перед ней
        int[] indexMap = new int[model.getVertices().size()];
        java.util.List<com.cgvsu.math.Vector3f> newVertices = new java.util.ArrayList<>();

        for (int i = 0; i < model.getVertices().size(); i++) {
            if (toDeleteSet.contains(i)) {
                indexMap[i] = -1; // Метка удаления
            } else {
                indexMap[i] = newVertices.size();
                newVertices.add(model.getVertices().get(i));
            }
        }

        // 4. Обновляем список вершин в модели
        model.setVertices(newVertices);

        // 5. Обновляем индексы в оставшихся полигонах
        for (com.cgvsu.model.Polygon poly : model.getPolygons()) {
            int[] vIndices = poly.getVertexIndices();
            for (int i = 0; i < vIndices.length; i++) {
                vIndices[i] = indexMap[vIndices[i]];
            }
            poly.setVertexIndices(vIndices);
        }
    }

    // Метод для удаления полигонов по индексам
    public static void deletePolygons(Model model, java.util.List<Integer> indicesToDelete) {
        if (model == null || indicesToDelete == null) return;
        java.util.List<Integer> sorted = new java.util.ArrayList<>(indicesToDelete);
        sorted.sort(java.util.Collections.reverseOrder());
        System.out.println("Delete polygon");
        for (int idx : sorted) {
            if (idx >= 0 && idx < model.getPolygons().size()) {
                model.getPolygons().remove(idx);
            }
        }
    }
}