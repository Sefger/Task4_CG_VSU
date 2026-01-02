package com.cgvsu.render_engine;

import com.cgvsu.model.Model;
import java.util.ArrayList;
import java.util.List;

public class Scene {
    private List<Model> models = new ArrayList<>();
    private List<Camera> cameras = new ArrayList<>();
    private List<Light> lights = new ArrayList<>();
    private int activeCameraIndex = 0;

    public void addCamera(Camera camera) {
        this.cameras.add(camera);
    }

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
}