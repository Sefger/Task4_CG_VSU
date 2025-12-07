package com.cgvsu.model;

import java.util.ArrayList;

public class TriangulateModel extends Model {

    public TriangulateModel() {
        super();
    }

    /**
     * Создает триангулированную модель на основе существующей
     * Использует алгоритм "ухоотсечения" для лучшей триангуляции
     */
    public TriangulateModel(Model model) {
        this.vertices = new ArrayList<>(model.getVertices());
        this.textureVertices = new ArrayList<>(model.getTextureVertices());
        this.normals = new ArrayList<>(model.getNormals());

        // Используем Triangulator для качественной триангуляции
        Triangulator triangulator = new Triangulator(model);
        this.polygons = new ArrayList<>();

        for (Polygon polygon : model.getPolygons()) {
            ArrayList<Polygon> triangles = triangulator.triangulatePolygon(polygon);
            this.polygons.addAll(triangles);
        }
    }

    /**
     * Переопределяем метод для гарантии триангуляции
     */
    @Override
    public Model triangulate() {
        // Уже триангулирована, возвращаем себя
        return this;
    }

    /**
     * Проверяет, полностью ли триангулирована модель
     */
    public boolean isFullyTriangulated() {
        return ModelProcessor.isTriangulated(this);
    }

    /**
     * Подсчитывает количество треугольников
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

    /**
     * Проверка валидности триангулированной модели
     */
    @Override
    public boolean validateTriangulatedModel() {
        return ModelProcessor.validateTriangulatedModel(this);
    }
}