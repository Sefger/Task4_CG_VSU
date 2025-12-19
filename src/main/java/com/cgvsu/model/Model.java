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
        Model newModel = new Model();

        // Глубокое копирование вершин
        for (Vector3f v : this.vertices) {
            newModel.vertices.add(new Vector3f(v.x, v.y, v.z));
        }

        // Глубокое копирование текстурных вершин
        for (Vector2f vt : this.textureVertices) {
            newModel.textureVertices.add(new Vector2f(vt.x, vt.y));
        }

        // Глубокое копирование нормалей
        for (Vector3f n : this.normals) {
            newModel.normals.add(new Vector3f(n.x, n.y, n.z));
        }

        // Полигоны можно копировать поверхностно, так как они хранят Integer (immutable),
        // но сами объекты Polygon нужно создавать новые!
        for (Polygon p : this.polygons) {
            Polygon newP = new Polygon();
            newP.setVertexIndices(new ArrayList<>(p.getVertexIndices()));
            newP.setTextureVertexIndices(new ArrayList<>(p.getTextureVertexIndices()));
            newP.setNormalIndices(new ArrayList<>(p.getNormalIndices()));
            newModel.polygons.add(newP);
        }

        return newModel;
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

    // Геттеры и cеттеры в стиле ооп ломают мне ноутбук, так что сильно извиняюсь, но это капец
    public List<Vector3f> getVertices() {
        return Collections.unmodifiableList(vertices);
    }

    public List<Vector2f> getTextureVertices() {
        return Collections.unmodifiableList(textureVertices);
    }

    public List<Vector3f> getNormals() {
        return Collections.unmodifiableList(normals);
    }

    public List<Polygon> getPolygons() {
        return Collections.unmodifiableList(polygons);
    }

    public ArrayList<Vector3f> getVerticesInternal() {
        return vertices;
    }

    public ArrayList<Vector2f> getTextureVerticesInternal() {
        return textureVertices;
    }

    public ArrayList<Vector3f> getNormalsInternal() {
        return normals;
    }

    public ArrayList<Polygon> getPolygonsInternal() {
        return polygons;
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
