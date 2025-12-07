package com.cgvsu.model;

import com.cgvsu.math.Vector3f;
import javafx.scene.image.PixelWriter;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ModelProcessor {

    /**
     * Триунуглирует модель
     */

    public static Model triangulate(Model model) {
        if (model == null) {
            return null;
        }
        Model triangulated = model.copy();

        for (Polygon polygon : model.polygons) {
            triangulatePolygon(polygon, triangulated);
        }
        return triangulated;
    }

    /**
     * Триангуляция веером
     */

    private static void triangulatePolygon(Polygon polygon, Model resultModel) {
        ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
        int vertexCount = vertexIndices.size();

        if (vertexCount < 3) {
            return;
        }
        if (vertexCount == 3) {
            resultModel.polygons.add(polygon);
            return;
        }

        ArrayList<Integer> textureIndices = polygon.getTextureVertexIndices();
        ArrayList<Integer> normalIndices = polygon.getNormalIndices();

        boolean hasTexture = !textureIndices.isEmpty() && textureIndices.size() == vertexCount;
        boolean hasNormals = !normalIndices.isEmpty() && normalIndices.size() == vertexCount;

        //Триангуляция веером

        for (int i = 1; i < vertexCount - 1; i++) {
            Polygon triangle = new Polygon();

            triangle.addVertexIndices(vertexIndices.get(0));
            triangle.addVertexIndices(vertexIndices.get(i));
            triangle.addVertexIndices(vertexIndices.get(i + 1));

            //todo: разбить на методы чтобы не проверять каждый цикл
            if (hasTexture) {
                triangle.addTextureVertexIndices(textureIndices.get(0));
                triangle.addTextureVertexIndices(textureIndices.get(i));
                triangle.addTextureVertexIndices(textureIndices.get(i + 1));
            }

            if (hasNormals) {
                triangle.addNormalIndices(textureIndices.get(0));
                triangle.addNormalIndices(textureIndices.get(i));
                triangle.addNormalIndices(textureIndices.get(i + 1));
            }

            resultModel.polygons.add(triangle);
        }
    }

    /**
     * Триангулирует модель с использованием алогритма ухоотсечения
     */
    public static Model triangulateWithEarClipping(Model model) {
        if (model == null) {
            return null;
        }

        Model triangulated = model.copy();
        Triangulator triangulator = new Triangulator(model);

        //todo: думаю здесь можно поменять
        for (Polygon polygon : model.polygons) {
            ArrayList<Polygon> triangles = triangulator.triangulatePolygon(polygon);
            triangulated.polygons.addAll(triangles);
        }

        return triangulated;
    }

    /**
     * Вычисляем нормали модели
     */
    public static void computeNormals(Model model) {
        if (model == null || model.vertices == null || model.polygons == null) {
            return;
        }

        // todo: нужно исправить очищение
        model.normals.clear();

        //инициализируем нормали
        for (int i = 0; i < model.vertices.size(); i++) {
            model.normals.add(new Vector3f(0, 0, 0));
        }

        //Вычисляем и аккумулируем нормали

        for (Polygon polygon : model.polygons) {
            Vector3f polygonNormal = computePolygonNormal(model.vertices, polygon);
            if (polygonNormal !=null){
                addPolygonNormalToVertices(model.normals, polygon, polygonNormal);
            }
        }

        // Нормализуем векторы
        normalizeAllVectors(model.normals);

        //Обновляем индексы
        updatePolygonNormalIndices(model.polygons);


    }

    /**
     * Вычисляет нормали для уже триангулированной модели (оптимизированная версия)
     */
    public static void computeNormalsForTriangulated(Model model) {
        if (model == null || model.vertices == null || model.polygons == null) {
            return;
        }

        // Проверяем, что модель триангулирована
        if (!isTriangulated(model)) {
            throw new IllegalArgumentException("Модель должна быть триангулирована");
        }

        model.normals.clear();

        // Инициализируем нормали нулевыми векторами
        for (int i = 0; i < model.vertices.size(); i++) {
            model.normals.add(new Vector3f(0, 0, 0));
        }

        // Для треугольников вычисляем нормали напрямую
        for (Polygon polygon : model.polygons) {
            if (polygon.getVertexIndices().size() != 3) {
                continue;
            }

            Vector3f v1 = model.vertices.get(polygon.getVertexIndices().get(0));
            Vector3f v2 = model.vertices.get(polygon.getVertexIndices().get(1));
            Vector3f v3 = model.vertices.get(polygon.getVertexIndices().get(2));

            Vector3f edge1 = new Vector3f(
                    v2.getX() - v1.getX(),
                    v2.getY() - v1.getY(),
                    v2.getZ() - v1.getZ()
            );

            Vector3f edge2 = new Vector3f(
                    v3.getX() - v1.getX(),
                    v3.getY() - v1.getY(),
                    v3.getZ() - v1.getZ()
            );

            Vector3f triangleNormal = new Vector3f(
                    edge1.getY() * edge2.getZ() - edge1.getZ() * edge2.getY(),
                    edge1.getZ() * edge2.getX() - edge1.getX() * edge2.getZ(),
                    edge1.getX() * edge2.getY() - edge1.getY() * edge2.getX()
            );

            // Добавляем нормаль ко всем вершинам треугольника
            for (Integer vertexIndex : polygon.getVertexIndices()) {
                Vector3f currentNormal = model.normals.get(vertexIndex);
                Vector3f newNormal = new Vector3f(
                        currentNormal.getX() + triangleNormal.getX(),
                        currentNormal.getY() + triangleNormal.getY(),
                        currentNormal.getZ() + triangleNormal.getZ()
                );
                model.normals.set(vertexIndex, newNormal);
            }
        }

        // Нормализуем векторы
        normalizeAllVectors(model.normals);

        // Обновляем индексы нормалей в полигонах
        updatePolygonNormalIndices(model.polygons);
    }

    private static Vector3f computePolygonNormal(ArrayList<Vector3f> vertices, Polygon polygon) {
        ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
        if (vertexIndices.size() < 3) {
            return null;
        }

        Vector3f sumNormal = new Vector3f(0, 0, 0);
        Vector3f v1 = vertices.get(0);

        for (int i = 1; i < vertexIndices.size() - 1; i++) {
            Vector3f v2 = vertices.get(vertexIndices.get(i));
            Vector3f v3 = vertices.get(vertexIndices.get(i + 1));

            Vector3f edge1 = new Vector3f(
                    v2.getX() - v1.getX(),
                    v2.getY() - v1.getY(),
                    v2.getZ() - v1.getZ()
            );

            Vector3f edge2 = new Vector3f(
                    v3.getX() - v1.getX(),
                    v3.getY() - v1.getY(),
                    v3.getZ() - v1.getZ()
            );

            Vector3f triangleNormal = new Vector3f(
                    edge1.getY() * edge2.getZ() - edge1.getZ() * edge2.getY(),
                    edge1.getZ() * edge2.getX() - edge1.getX() * edge2.getZ(),
                    edge1.getX() * edge2.getY() - edge1.getY() * edge2.getX()

            );

            sumNormal.setX(sumNormal.getX() + triangleNormal.getX());
            sumNormal.setY(sumNormal.getX() + triangleNormal.getX());
            sumNormal.setZ(sumNormal.getX() + triangleNormal.getX());
        }

        return sumNormal;
    }

    /**
     * Добавляет нормаль полигона к вершинам
     */
    private static void addPolygonNormalToVertices(
            ArrayList<Vector3f> normals,
            Polygon polygon,
            Vector3f polygonNormal
    ) {
        for (Integer vertexIndex : polygon.getVertexIndices()) {
            Vector3f currentNormal = normals.get(vertexIndex);

            Vector3f newNormal = new Vector3f(
                    currentNormal.getX() + polygonNormal.getX(),
                    currentNormal.getY() + polygonNormal.getY(),
                    currentNormal.getZ() + polygonNormal.getZ()
            );
            normals.set(vertexIndex, newNormal);
        }
    }

    /**
     * Нормализует все векторы
     */
    private static void normalizeAllVectors(ArrayList<Vector3f> vectors) {
        for (int i = 0; i < vectors.size(); i++) {
            Vector3f normal = vectors.get(i);
            float length = (float) Math.sqrt(
                    normal.getX() * normal.getX() +
                            normal.getY() * normal.getY() +
                            normal.getZ() * normal.getZ()
            );

            if (length != 0) {
                Vector3f normalizedNormal = new Vector3f(
                        normal.getX() / length,
                        normal.getY() / length,
                        normal.getZ() / length
                );
                vectors.set(i, normalizedNormal);
            }
        }
    }

    /**
     * Обновляет индексы нормалей в полигонах
     */
    private static void updatePolygonNormalIndices(ArrayList<Polygon> polygons) {
        for (Polygon polygon : polygons) {
            // так безопасней иначе была бы пляска с сылками
            ArrayList<Integer> normalIndices = new ArrayList<>(polygon.getVertexIndices());
            polygon.setNormalIndices(normalIndices);
        }
    }

    /**
     * Проверяет, нужно ли триангулировать модель
     */
    public static boolean needsTriangulation(Model model) {
        if (model == null || model.polygons == null) {
            return false;
        }

        for (Polygon polygon : model.polygons) {
            if (polygon.getTextureVertexIndices().size() > 3) {
                return true;
            }
        }

        return false;
    }

    /**
     * Проверяет, полностью ли триангулировать модель
     */
    public static boolean isTriangulated(Model model) {
        if (model == null || model.polygons == null) {
            return false;
        }

        for (Polygon polygon : model.polygons) {
            if (polygon.getVertexIndices().size() != 3) {
                return false;
            }
        }
        return true;
    }

    /**
     * Получить статистку по полигонам (отладка)
     */
    public static String getPolygonStatistics(Model model) {
        if (model == null || model.polygons == null) {
            return "No polygons";
        }

        int triangleCount = 0;
        int quadCount = 0;
        int ngonCount = 0;

        for (Polygon polygon : model.polygons) {
            int vertexCount = polygon.getVertexIndices().size();
            if (vertexCount == 3) {
                triangleCount++;
            } else if (vertexCount == 4) {
                quadCount++;
            } else {
                ngonCount++;
            }
        }

        return String.format("Triangles: %d, Quads: %d, N-gons: %d",
                triangleCount, quadCount, ngonCount);
    }

    private static boolean validateTriangulatedModel(Polygon polygon, Model model) {
        ArrayList<Integer> vertexIndices = polygon.getVertexIndices();

        // Проверка размера
        if (vertexIndices.size() != 3) {
            return false;
        }

        // Проверка индексов вершин
        for (int vertexIndex : vertexIndices) {
            if (vertexIndex < 0 || vertexIndex >= model.vertices.size()) {
                return false;
            }
        }

        // Проверка на уникальность вершин
        if (vertexIndices.get(0).equals(vertexIndices.get(1)) ||
                vertexIndices.get(1).equals(vertexIndices.get(2)) ||
                vertexIndices.get(0).equals(vertexIndices.get(2))) {
            return false;
        }

        // Проверка текстурных координат
        ArrayList<Integer> textureIndices = polygon.getTextureVertexIndices();
        if (!textureIndices.isEmpty()) {
            if (textureIndices.size() != 3) {
                return false;
            }
            for (int texIndex : textureIndices) {
                if (texIndex < 0 || texIndex >= model.textureVertices.size()) {
                    return false;
                }
            }
        }

        // Проверка нормалей
        ArrayList<Integer> normalIndices = polygon.getNormalIndices();
        if (!normalIndices.isEmpty()) {
            if (normalIndices.size() != 3) {
                return false;
            }
            for (int normalIndex : normalIndices) {
                if (normalIndex < 0 || normalIndex >= model.normals.size()) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Проверяет валидность треугольника
     */
    private static boolean validateTriangle(Polygon polygon, Model model) {
        ArrayList<Integer> vertexIndices = polygon.getVertexIndices();

        // Проверка размера
        if (vertexIndices.size() != 3) {
            return false;
        }

        // Проверка индексов вершин
        for (int vertexIndex : vertexIndices) {
            if (vertexIndex < 0 || vertexIndex >= model.vertices.size()) {
                return false;
            }
        }

        // Проверка на уникальность вершин
        if (vertexIndices.get(0).equals(vertexIndices.get(1)) ||
                vertexIndices.get(1).equals(vertexIndices.get(2)) ||
                vertexIndices.get(0).equals(vertexIndices.get(2))) {
            return false;
        }

        // Проверка текстурных координат
        ArrayList<Integer> textureIndices = polygon.getTextureVertexIndices();
        if (!textureIndices.isEmpty()) {
            if (textureIndices.size() != 3) {
                return false;
            }
            for (int texIndex : textureIndices) {
                if (texIndex < 0 || texIndex >= model.textureVertices.size()) {
                    return false;
                }
            }
        }

        // Проверка нормалей
        ArrayList<Integer> normalIndices = polygon.getNormalIndices();
        if (!normalIndices.isEmpty()) {
            if (normalIndices.size() != 3) {
                return false;
            }
            for (int normalIndex : normalIndices) {
                if (normalIndex < 0 || normalIndex >= model.normals.size()) {
                    return false;
                }
            }
        }

        return true;
    }
    /**
     * Проверяет валидность триангулированной модели
     */
    public static boolean validateTriangulatedModel(Model model) {
        if (!isTriangulated(model)) {
            return false;
        }

        for (Polygon polygon : model.polygons) {
            if (!validateTriangle(polygon, model)) {
                return false;
            }
        }

        return true;
    }

}
