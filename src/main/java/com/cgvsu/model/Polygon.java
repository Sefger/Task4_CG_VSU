package com.cgvsu.model;

import java.util.Arrays;

public class Polygon {

    // Используем примитивные массивы для экономии памяти в 4-6 раз
    private int[] vertexIndices;
    private int[] textureVertexIndices;
    private int[] normalIndices;

    public Polygon() {
        this.vertexIndices = new int[0];
        this.textureVertexIndices = new int[0];
        this.normalIndices = new int[0];
    }

    /**
     * Конструктор с инициализацией размера.
     * Позволяет избежать лишних переаллокаций памяти.
     */
    public Polygon(int size) {
        this.vertexIndices = new int[size];
        this.textureVertexIndices = new int[size];
        this.normalIndices = new int[size];
    }

    // --- Сеттеры с использованием примитивных массивов ---

    public void setVertexIndices(int[] vertexIndices) {
        assert vertexIndices.length >= 3;
        this.vertexIndices = vertexIndices;
    }

    public void setTextureVertexIndices(int[] textureVertexIndices) {
        this.textureVertexIndices = textureVertexIndices;
    }

    public void setNormalIndices(int[] normalIndices) {
        this.normalIndices = normalIndices;
    }

    // --- Геттеры ---

    public int[] getVertexIndices() {
        return vertexIndices;
    }

    public int[] getTextureVertexIndices() {
        return textureVertexIndices;
    }

    public int[] getNormalIndices() {
        return normalIndices;
    }

    /**
     * Возвращает количество вершин в полигоне (N)
     */
    public int getIndicesCount() {
        return vertexIndices.length;
    }

    /**
     * Быстрое глубокое копирование полигона через системное копирование массивов
     */
    public Polygon copy() {
        Polygon newPolygon = new Polygon(this.vertexIndices.length);

        System.arraycopy(this.vertexIndices, 0, newPolygon.vertexIndices, 0, this.vertexIndices.length);

        if (this.textureVertexIndices.length > 0) {
            newPolygon.textureVertexIndices = new int[this.textureVertexIndices.length];
            System.arraycopy(this.textureVertexIndices, 0, newPolygon.textureVertexIndices, 0, this.textureVertexIndices.length);
        }

        if (this.normalIndices.length > 0) {
            newPolygon.normalIndices = new int[this.normalIndices.length];
            System.arraycopy(this.normalIndices, 0, newPolygon.normalIndices, 0, this.normalIndices.length);
        }

        return newPolygon;
    }
    public void decrementVertexIndicesGreaterThan(int threshold) {
        for (int i = 0; i < vertexIndices.length; i++) {
            if (vertexIndices[i] > threshold) {
                vertexIndices[i] -= 1;
            }
        }
    }

    /**
     * Проверяет, содержит ли полигон конкретный индекс вершины.
     */
    public boolean containsVertexIndex(int index) {
        for (int vIdx : vertexIndices) {
            if (vIdx == index) return true;
        }
        return false;
    }

    /**
     * Метод для динамического добавления индекса (если понадобится при редактировании).
     * Создает новый массив на n+1 элементов.
     */
    public void addVertex(int index) {
        int[] newIndices = new int[vertexIndices.length + 1];
        System.arraycopy(vertexIndices, 0, newIndices, 0, vertexIndices.length);
        newIndices[vertexIndices.length] = index;
        vertexIndices = newIndices;
    }

}