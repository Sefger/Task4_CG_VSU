package com.cgvsu.model;

import java.util.ArrayList;

public class TriangulateModel extends  Model {
    public TriangulateModel() {
        //СУПЕР!!!
        super();
    }

    //на всякий случай вдруг кто захочет
    public TriangulateModel(Model model) {
        this.vertices = new ArrayList<>(model.getVertices());
        this.textureVertices = new ArrayList<>(model.getTextureVertices());
        this.normals = new ArrayList<>(model.getNormals());
        this.polygons = new ArrayList<>(model.getPolygons());
    }

    //кол-во треуг в модели
    /*
    Хотя наверно это плохая идея, ведь класс уже сразу должен протриангулировать, надо будет уточнить!!!
     */
    public int getTriangleCount() {
        int count = 0;
        for (Polygon polygon : polygons) {
            if (polygon.getVertexIndices().size() == 3) {
                count++;
            }
        }
        return count;
    }

    public boolean isFullyTriangulated() {
        //можно было бы использовать getTriangleCount, но это долго
        for (Polygon polygon : polygons) {
            if (polygon.getVertexIndices().size() != 3) {
                return false;
            }
        }
        return true;
    }

    //Провервка валидности триангулированной модели

    public boolean isValid() {
        for (Polygon polygon : polygons) {
            // Проверка что полигон - треугольник
            if (polygon.getVertexIndices().size() != 3) {
                return false;
            }

            int vertexCount = polygon.getVertexIndices().size();

            // Проверка индексов вершин
            for (int vertexIndex : polygon.getVertexIndices()) {
                if (vertexIndex < 0 || vertexIndex >= vertices.size()) {
                    return false;
                }
            }

            // Проверка текстурных координат
            ArrayList<Integer> textureIndices = polygon.getTextureVertexIndices();
            if (!textureIndices.isEmpty()) {
                // Проверка согласованности размера
                if (textureIndices.size() != vertexCount) {
                    return false;
                }
                // Проверка валидности индексов
                for (int texIndex : textureIndices) {
                    if (texIndex < 0 || texIndex >= textureVertices.size()) {
                        return false;
                    }
                }
            }

            // Проверка нормалей
            ArrayList<Integer> normalIndices = polygon.getNormalIndices();
            if (!normalIndices.isEmpty()) {
                // Проверка согласованности размера
                if (normalIndices.size() != vertexCount) {
                    return false;
                }
                // Проверка валидности индексов
                for (int normalIndex : normalIndices) {
                    if (normalIndex < 0 || normalIndex >= normals.size()) {
                        return false;
                    }
                }
            }

            // Дополнительная проверка: все три вершины должны быть разными
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
            if (vertexIndices.get(0).equals(vertexIndices.get(1)) ||
                    vertexIndices.get(1).equals(vertexIndices.get(2)) ||
                    vertexIndices.get(0).equals(vertexIndices.get(2))) {
                return false;
            }
        }
        return true;
    }
}