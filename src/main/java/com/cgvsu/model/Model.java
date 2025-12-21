package com.cgvsu.model;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;

import java.util.*;

public class Model {

    protected ArrayList<Vector3f> vertices = new ArrayList<>();
    protected ArrayList<Vector2f> textureVertices = new ArrayList<>();
    protected ArrayList<Vector3f> normals = new ArrayList<>();
    protected ArrayList<Polygon> polygons = new ArrayList<>();

    public Model() {
    }

    public Model(List<Vector3f> vert, List<Vector2f> textureVert, List<Vector3f> normals, List<Polygon> polygons) {
        this.vertices = new ArrayList<>(vert);
        this.textureVertices = new ArrayList<>(textureVert);
        this.normals = new ArrayList<>(normals);
        this.polygons = new ArrayList<>(polygons);
    }

    /**
     * Ультра-быстрое копирование модели.
     * Использует резервирование памяти (ensureCapacity) и быстрые методы копирования Polygon.
     */
    public Model copy() {
        Model newModel = new Model();

        // Резервируем память один раз, чтобы избежать лишних переаллокаций ArrayList
        newModel.vertices = new ArrayList<>(this.vertices.size());
        for (Vector3f v : this.vertices) {
            newModel.vertices.add(new Vector3f(v.x, v.y, v.z));
        }

        newModel.textureVertices = new ArrayList<>(this.textureVertices.size());
        for (Vector2f vt : this.textureVertices) {
            newModel.textureVertices.add(new Vector2f(vt.x, vt.y));
        }

        newModel.normals = new ArrayList<>(this.normals.size());
        for (Vector3f n : this.normals) {
            newModel.normals.add(new Vector3f(n.x, n.y, n.z));
        }

        // Копируем полигоны, используя метод copy(), который мы добавили в Polygon
        newModel.polygons = new ArrayList<>(this.polygons.size());
        for (Polygon p : this.polygons) {
            newModel.polygons.add(p.copy());
        }

        return newModel;
    }

    // --- Методы процессора (делегирование) ---

    public void computeNormals() {
        ModelProcessor.computeNormals(this);
    }

    public void triangulate() {
        ModelProcessor.triangulate(this);
    }

    public Model triangulateWithEarClipping() {
        return ModelProcessor.triangulateWithEarClipping(this);
    }

    public boolean needsTriangulation() {
        return ModelProcessor.needsTriangulation(this);
    }

    public boolean isTriangulated() {
        return ModelProcessor.isTriangulated(this);
    }

    public String getPolygonStatistics() {
        return ModelProcessor.getPolygonStatistics(this);
    }

    // --- Геттеры (Оптимизированы для скорости) ---

    // Возвращаем unmodifiable только если это критически важно для безопасности.
    // Если "ноутбук ломается", лучше возвращать список напрямую или использовать Internal методы.
    public List<Vector3f> getVertices() { return vertices; }
    public List<Vector2f> getTextureVertices() { return textureVertices; }
    public List<Vector3f> getNormals() { return normals; }
    public List<Polygon> getPolygons() { return polygons; }

    public ArrayList<Vector3f> getVerticesInternal() { return vertices; }
    public ArrayList<Vector2f> getTextureVerticesInternal() { return textureVertices; }
    public ArrayList<Vector3f> getNormalsInternal() { return normals; }
    public ArrayList<Polygon> getPolygonsInternal() { return polygons; }

    // --- Методы добавления ---

    public void addVertex(Vector3f v) { this.vertices.add(v); }
    public void addTextureVertex(Vector2f v) { this.textureVertices.add(v); }
    public void addNormal(Vector3f v) { this.normals.add(v); }
    public void addPolygon(Polygon p) { this.polygons.add(p); }
}