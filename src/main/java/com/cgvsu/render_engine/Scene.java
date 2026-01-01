package com.cgvsu.render_engine;

import com.cgvsu.model.Model;

import java.util.ArrayList;
import java.util.List;

public class Scene {
    private List<Model> models = new ArrayList<>();
    private List<Camera> cameras = new ArrayList<>();
    private int activeCameraIndex = 0;

    public void addCamera(Camera camera) {
        this.cameras.add(camera);
    }

    public void removeCamera(int index) {
        // не удаляем последнюю камеру
        if (cameras.size() > 1 && index < cameras.size() && index > 0) {
            cameras.remove(index);
        }
    }
    public Camera getActiveCamera(){
        return cameras.get(activeCameraIndex);
    }

    // надеюсь что никто не попытается удалять
    // WARNING!!!
    public List<Camera> getCameras(){
        return cameras;
    }

    public void setActiveCameraIndex(int index){
        if (index>0 && index<cameras.size()){
            activeCameraIndex = index;
        }
    }

    public int getActiveCameraIndex(){
        return activeCameraIndex;
    }

}
