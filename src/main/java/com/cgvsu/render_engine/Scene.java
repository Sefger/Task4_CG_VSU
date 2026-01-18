package com.cgvsu.render_engine;

import com.cgvsu.model.Model;
import com.cgvsu.model.ModelProcessor;
import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Scene {
    private List<Model> models = new ArrayList<>();
    private List<Model> originalModels = new ArrayList<>();
    private List<Image> textures = new ArrayList<>(); // Перенесли список текстур сюда

    private int activeModelIndex = -1;

    private List<Camera> cameras = new ArrayList<>();
    private int activeCameraIndex = 0;

    private List<Light> lights = new ArrayList<>();

    // --- Камеры ---

    public void addCamera(Camera camera) {
        this.cameras.add(camera);
    }

    public void removeCamera(int index) {
        if (cameras.size() <= 1 || index < 0 || index >= cameras.size()) {
            return;
        }
        cameras.remove(index);
        if (activeCameraIndex >= index) {
            activeCameraIndex = Math.max(0, activeCameraIndex - 1);
        }
    }

    public Camera getActiveCamera() {
        return cameras.isEmpty() ? null : cameras.get(activeCameraIndex);
    }

    public List<Camera> getCameras() { return cameras; }
    public int getActiveCameraIndex() { return activeCameraIndex; }
    public void setActiveCamera(int index) {
        if (index >= 0 && index < cameras.size()) activeCameraIndex = index;
    }

    // --- Свет ---

    public List<Light> getLights() { return lights; }

    // --- Модели и Текстуры ---

    public void addModel(Model model) {
        models.add(model);
        originalModels.add(model.copy()); // Создаем независимый клон для восстановления
        textures.add(null);
        if (activeModelIndex == -1) activeModelIndex = 0;
    }

    public void removeModel(int index) {
        if (index >= 0 && index < models.size()) {
            models.remove(index);
            originalModels.remove(index);
            textures.remove(index); // Синхронно удаляем текстуру

            if (models.isEmpty()) {
                activeModelIndex = -1;
            } else if (activeModelIndex >= index) {
                activeModelIndex = Math.max(0, activeModelIndex - 1);
            }
        }
    }

    public List<Model> getModels() { return models; }
    public List<Model> getOriginalModels() { return originalModels; }

    public List<Image> getTextures() { return textures; }


    // Метод для получения текстуры активной модели
    public Image getActiveTexture() {
        if (activeModelIndex >= 0 && activeModelIndex < textures.size()) {
            return textures.get(activeModelIndex);
        }
        return null;
    }

    // Метод для установки текстуры активной модели
    public void setActiveTexture(Image image) {
        if (activeModelIndex >= 0 && activeModelIndex < textures.size()) {
            textures.set(activeModelIndex, image);
        }
    }

    public void setActiveModelIndex(int index) {
        if (index >= -1 && index < models.size()) {
            this.activeModelIndex = index;
        }
    }

    public int getActiveModelIndex() { return activeModelIndex; }



    public void deletePolygonsInActiveModel(List<Integer> indices) {
        Model activeModel = getActiveModel();
        if (activeModel == null || indices.isEmpty()) return;

        // Сортируем по убыванию, чтобы индексы не "уплыли" в процессе удаления из ArrayList
        List<Integer> sortedIndices = indices.stream()
                .distinct()
                .sorted((a, b) -> b - a)
                .collect(Collectors.toList());

        for (int idx : sortedIndices) {
            activeModel.removePolygon(idx);
        }
    }

    public void deleteVerticesInActiveModel(List<Integer> indices) {
        Model activeModel = getActiveModel();
        if (activeModel == null || indices.isEmpty()) return;

        // Здесь сортировка КРИТИЧЕСКИ важна.
        // Если удалить вершину 0, то вершина 10 станет 9-й.
        // Если удалять с конца (с 10-й), то индекс 0 останется на месте.
        List<Integer> sortedIndices = indices.stream()
                .distinct()
                .sorted((a, b) -> b - a)
                .collect(Collectors.toList());

        for (int idx : sortedIndices) {
            activeModel.removeVertex(idx);
        }
    }

    public Model getActiveModel() {
        if (activeModelIndex < 0 || activeModelIndex >= models.size()) return null;
        return models.get(activeModelIndex);
    }

}
