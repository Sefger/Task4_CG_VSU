package com.cgvsu.model;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;

import java.util.*;




import java.util.*;
public class Model {

    protected ArrayList<Vector3f> vertices = new ArrayList<>();
    protected ArrayList<Vector2f> textureVertices = new ArrayList<>();
    protected ArrayList<Vector3f> normals = new ArrayList<>();
    protected ArrayList<Polygon> polygons = new ArrayList<>();

    public void computeNormals() {
        normals.clear();
        for (int i = 0; i < vertices.size(); i++) {
            normals.add(new Vector3f(0, 0, 0));
        }

        for (Polygon polygon : polygons) {
            Vector3f polygonNormal = computePolygonNormal(polygon);
            if (polygonNormal != null) {
                addPolygonNormalToVertices(polygon, polygonNormal);
            }
        }
        normalizeAllNormals();
        updatePolygonNormalIndices();
    }

    private Vector3f computePolygonNormal(Polygon polygon) {
        ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
        if (vertexIndices.size() < 3) return null;

        Vector3f sumNormal = new Vector3f(0, 0, 0);
        Vector3f v1 = vertices.get(vertexIndices.get(0));

        for (int i = 1; i < vertexIndices.size() - 1; i++) {
            Vector3f triangleNormal = getVector3f(vertexIndices, i, v1);

            sumNormal.setX(sumNormal.getX() + triangleNormal.getX());
            sumNormal.setY(sumNormal.getY() + triangleNormal.getY());
            sumNormal.setZ(sumNormal.getZ() + triangleNormal.getZ());
        }

        return sumNormal;
    }

    private Vector3f getVector3f(ArrayList<Integer> vertexIndices, int i, Vector3f v1) {
        Vector3f v2 = vertices.get(vertexIndices.get(i));
        Vector3f v3 = vertices.get(vertexIndices.get(i + 1));

        Vector3f edge1 = new Vector3f(v2.getX() - v1.getX(), v2.getY() - v1.getY(), v2.getZ() - v1.getZ());
        Vector3f edge2 = new Vector3f(v3.getX() - v1.getX(), v3.getY() - v1.getY(), v3.getZ() - v1.getZ());

        return new Vector3f(
                edge1.getY() * edge2.getZ() - edge1.getZ() * edge2.getY(),
                edge1.getZ() * edge2.getX() - edge1.getX() * edge2.getZ(),
                edge1.getX() * edge2.getY() - edge1.getY() * edge2.getX()
        );

    }

    private void addPolygonNormalToVertices(Polygon polygon, Vector3f polygonNormal) {
        for (Integer vertexIndex : polygon.getVertexIndices()) {
            Vector3f currentNormal = normals.get(vertexIndex);

            Vector3f newNormal = new Vector3f(
                    currentNormal.getX() + polygonNormal.getX(),
                    currentNormal.getY() + polygonNormal.getY(),
                    currentNormal.getZ() + polygonNormal.getZ()
            );
            normals.set(vertexIndex, newNormal);
        }
    }

    private void normalizeAllNormals() {
        for (int i = 0; i < normals.size(); i++) {
            Vector3f normal = normals.get(i);
            float length = (float) Math.sqrt(normal.getX() * normal.getX() + normal.getY() * normal.getY() + normal.getZ() * normal.getZ());
            if (length != 0) {
                Vector3f normalizedNormal = new Vector3f(
                        normal.getX() / length,
                        normal.getY() / length,
                        normal.getZ() / length
                );
                normals.set(i, normalizedNormal);
            }
        }
    }

    private void updatePolygonNormalIndices() {
        for (Polygon polygon : polygons) {
            ArrayList<Integer> normalIndices = new ArrayList<>(polygon.getVertexIndices());
            polygon.setNormalIndices(normalIndices);
        }
    }

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
    public void addVertices(Vector3f v3){
        this.vertices.add(v3);
    }
    public void addTextureVertices(Vector2f v2){
        this.textureVertices.add(v2);
    }
    public void addNormal(Vector3f v3){
        this.normals.add(v3);
    }
    public void addPolygon(Polygon p){
        this.polygons.add(p);
    }
}
