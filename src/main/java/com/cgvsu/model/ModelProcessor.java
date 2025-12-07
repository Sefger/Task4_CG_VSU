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
        }


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

            if (length!=0){
                Vector3f normalizedNormal = new Vector3f(
                        normal.getX()/length,
                        normal.getY()/length,
                        normal.getZ()/length
                );
                vectors.set(i, normalizedNormal);
            }
        }
    }

    /**
     *  Обновляет индексы нормалей в полигонах
     */
    private static void updatePolygonNormalIndices(ArrayList<Polygon> polygons){
        for (Polygon polygon: polygons){
            // так безопасней иначе была бы пляска с сылками
            ArrayList<Integer> normalIndices = new ArrayList<>(polygon.getVertexIndices());
            polygon.setNormalIndices(normalIndices);
        }
    }

    /**
     * Проверяет, нужно ли триангулировать модель
     */
    public static boolean needsTriangulation(Model model){
        if (model == null || model.polygons== null){
            return false;
        }

        for (Polygon polygon: model.polygons){
            if (polygon.getTextureVertexIndices().size() >3){
                return true;
            }
        }

        return false;
    }

    /**
     * Проверяет, полностью ли триангулировать модель
     */
    public static boolean isTriangulated(Model model){
        if (model == null || model.polygons == null){
            return false;
        }

        for (Polygon polygon: model.polygons){
            if (polygon.getVertexIndices().size() !=3){
                return false;
            }
        }
        return true;
    }

    /**
     * Получить статистку по полигонам (отладка)
     */
    public static String getPolygonStatistics(Model model){
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


}
