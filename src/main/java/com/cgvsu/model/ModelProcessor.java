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

            float ax = v2.x - v1.x; float ay = v2.y - v1.y; float az = v2.z - v1.z;
            float bx = v3.x - v1.x; float by = v3.y - v1.y; float bz = v3.z - v1.z;

            float nx = ay * bz - az * by;
            float ny = az * bx - ax * bz;
            float nz = ax * by - ay * bx;

            for (int idx : vIdx) {
                Vector3f n = normals.get(idx);
                n.x += nx; n.y += ny; n.z += nz;
            }
            poly.setNormalIndices(vIdx.clone());
        }

        for (Vector3f n : normals) {
            float len = (float) Math.sqrt(n.x * n.x + n.y * n.y + n.z * n.z);
            if (len > 1e-6f) {
                n.x /= len; n.y /= len; n.z /= len;
            }
        }
    }

    public static String getPolygonStatistics(Model model) {
        if (model == null) return "Model is empty";
        return "Polygons: " + model.getPolygons().size();
    }
    public static boolean needsTriangulation(Model model) {
        return !isTriangulated(model);
    }
}