package com.cgvsu.render_engine;

import com.cgvsu.model.Model;
import java.util.ArrayList;
import java.util.List;

public class Scene {
    private List<Model> models = new ArrayList<>();
    private List<Model> originalModels = new ArrayList<>();
    private int activeModelIndex = -1; // -1, если моделей нет
    private List<Camera> cameras = new ArrayList<>();
    private List<Light> lights = new ArrayList<>();
    private int activeCameraIndex = 0;

    public void addCamera(Camera camera) {
        this.cameras.add(camera);
    }
    public List<Model> getOriginalModels() { return originalModels;}

    public void removeCamera(int index) {
        // Проверка: нельзя удалить единственную камеру и индекс должен быть валидным
        if (cameras.size() <= 1 || index < 0 || index >= cameras.size()) {
            return;
        }

        cameras.remove(index);

        // Корректируем активный индекс после удаления
        if (activeCameraIndex >= index) {
            // Сдвигаем индекс назад, но не меньше 0
            activeCameraIndex = Math.max(0, activeCameraIndex - 1);
        }
    }

    public Camera getActiveCamera() {
        if (cameras.isEmpty()) {
            return null; // Или выбросить исключение, если камер нет
        }
        return cameras.get(activeCameraIndex);
    }

    public List<Camera> getCameras() {
        return cameras;
    }

    public int getActiveCameraIndex() {
        return activeCameraIndex;
    }

    public void setActiveCamera(int index) {
        // Проверка: индекс должен быть от 0 до size-1 ВКЛЮЧИТЕЛЬНО
        if (index >= 0 && index < cameras.size()) {
            activeCameraIndex = index;
        }
    }

    public List<Light> getLights() {
        return lights;
    }

    public void addModel(Model model) {
        models.add(model);
        originalModels.add(model);
        if (activeModelIndex == -1) activeModelIndex = 0;
    }

    public List<Model> getModels() {
        return models;
    }

    public Model getActiveModel() {
        if (activeModelIndex >= 0 && activeModelIndex < models.size()) {
            return models.get(activeModelIndex);
        }
        return null;
    }

    public void setActiveModelIndex(int index){
        if(index >= 0 && index < models.size()){
            this.activeModelIndex = index;
        }
    }

    public int getActiveModelIndex(){
        return activeModelIndex;
    }

    public void removeModel(int index) {
        if (index >= 0 && index < models.size()) {
            models.remove(index);
            originalModels.remove(index);

            // Корректируем индекс активной модели, чтобы он не указывал в пустоту
            if (models.isEmpty()) {
                activeModelIndex = -1;
            } else if (activeModelIndex >= index) {
                // Если удалили модель перед активной или саму активную,
                // сдвигаем индекс назад
                activeModelIndex = Math.max(0, activeModelIndex - 1);
            }
        }
    }

}