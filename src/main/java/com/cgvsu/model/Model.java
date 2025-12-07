package com.cgvsu.model;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;

import java.util.*;

public class Model {

    public ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
    public ArrayList<Vector2f> textureVertices = new ArrayList<Vector2f>();
    public ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
    public ArrayList<Polygon> polygons = new ArrayList<Polygon>();

    public ArrayList<Vector3f> getVertices() {
        return new ArrayList<>(this.vertices);
    }

    public ArrayList<Vector2f> getTextureVertices() {
        return  new ArrayList<>(this.textureVertices);
    }

    public ArrayList<Vector3f> getNormals() {
        return new ArrayList<>(this.normals);
    }
    public ArrayList<Polygon> getPolygons(){
        return new ArrayList<>(this.polygons);
    }

}
