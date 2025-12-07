package com.cgvsu.model;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;

import java.util.*;



public class Model {

    protected ArrayList<Vector3f> vertices = new ArrayList<>();
    protected ArrayList<Vector2f> textureVertices = new ArrayList<>();
    protected ArrayList<Vector3f> normals = new ArrayList<>();
    protected ArrayList<Polygon> polygons = new ArrayList<>();

    // Не радактировать!!!
    public Model(List<Vector3f> vert, List<Vector2f> textureVert, List<Vector3f> normals, List<Polygon> polygons) {
        this.vertices = new ArrayList<>(vert);
        this.textureVertices = new ArrayList<>(textureVert);
        this.normals = new ArrayList<>(normals);
        this.polygons = new ArrayList<>(polygons);

    }

    // Можем оставить так как оно есть
    // Сделано для того, чтобы обеспечить сохранность данных
    public Model copy() {
        return new Model(this.vertices, this.textureVertices, this.normals, this.polygons);
    }

    public Model() {
    }

    /**
     * Вычисляет нормали для модели
     */
    public void computeNormals() {
        ModelProcessor.computeNormals(this);
    }

    /**
     * Триангулирует модель (простая триангуляция веером)
     * @return новая триангулированная модель
     */
    public Model triangulate() {
        return ModelProcessor.triangulate(this);
    }

    /**
     * Триангулирует модель с использованием алгоритма "ухоотсечения"
     * @return новая триангулированная модель
     */
    public Model triangulateWithEarClipping() {
        return ModelProcessor.triangulateWithEarClipping(this);
    }

    /**
     * Проверяет, нужно ли триангулировать модель
     */
    public boolean needsTriangulation() {
        return ModelProcessor.needsTriangulation(this);
    }

    /**
     * Проверяет, полностью ли триангулирована модель
     */
    public boolean isTriangulated() {
        return ModelProcessor.isTriangulated(this);
    }

    /**
     * Получает статистику по полигонам
     */
    public String getPolygonStatistics() {
        return ModelProcessor.getPolygonStatistics(this);
    }

    /**
     * Проверяет валидность триангулированной модели
     */
    public boolean validateTriangulatedModel() {
        return ModelProcessor.validateTriangulatedModel(this);
    }

    // Геттеры и сеттеры остаются без изменений
    public ArrayList<Polygon> getPolygons() {
        return new ArrayList<>(polygons);
    }

    public ArrayList<Vector2f> getTextureVertices() {
        return new ArrayList<>(textureVertices);
    }

    public ArrayList<Vector3f> getNormals() {
        return new ArrayList<>(normals);
    }

    public ArrayList<Vector3f> getVertices() {
        return new ArrayList<>(vertices);
    }

    public void addVertices(Vector3f v3) {
        this.vertices.add(v3);
    }

    public void addTextureVertices(Vector2f v2) {
        this.textureVertices.add(v2);
    }

    public void addNormal(Vector3f v3) {
        this.normals.add(v3);
    }

    public void addPolygon(Polygon p) {
        this.polygons.add(p);
    }
}
