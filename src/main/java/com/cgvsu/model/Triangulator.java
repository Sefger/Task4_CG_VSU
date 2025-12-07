package com.cgvsu.model;


import com.cgvsu.math.Vector3f;

import java.util.ArrayList;

public class Triangulator {
    private Model model;

    public Triangulator(Model model) {
        this.model = model;
    }

    public TriangulateModel triangulate(Model model) {
        TriangulateModel triangulateModel = new TriangulateModel(model);
        triangulateModel.polygons.clear();

        for (Polygon orPolygon : model.polygons) {
            ArrayList<Polygon> triangles = triangulatePolygon(orPolygon);
            triangulateModel.polygons.addAll(triangles);
        }
        return triangulateModel;
    }

    private ArrayList<Polygon> triangulatePolygon(Polygon polygon) {
        ArrayList<Polygon> triangles = new ArrayList<>();
        ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
        ArrayList<Integer> textureIndices = polygon.getTextureVertexIndices();
        ArrayList<Integer> normalIndices = polygon.getNormalIndices();
        int vertexCount = vertexIndices.size();
        if (vertexCount < 3) {
            return triangles;
        }
        if (vertexCount == 3) {
            triangles.add(polygon);
            return triangles;
        }
        boolean hasTexture = !textureIndices.isEmpty() && textureIndices.size() == vertexCount;
        boolean hasNormals = !normalIndices.isEmpty() && normalIndices.size() == vertexCount;

        // Проверяем, выпуклый ли полигон
        if (isConvex(vertexIndices)) {
            // Триангуляция "веером" для выпуклых полигонов
            for (int i = 1; i < vertexCount - 1; i++) {
                Polygon triangle = new Polygon();
                ArrayList<Integer> triangleVertexIndices = new ArrayList<>();
                triangleVertexIndices.add(vertexIndices.get(0));
                triangleVertexIndices.add(vertexIndices.get(i));
                triangleVertexIndices.add(vertexIndices.get(i + 1));
                triangle.setVertexIndices(triangleVertexIndices);
                if (hasTexture) {
                    ArrayList<Integer> triangleTextureIndices = new ArrayList<>();
                    triangleTextureIndices.add(textureIndices.get(0));
                    triangleTextureIndices.add(textureIndices.get(i));
                    triangleTextureIndices.add(textureIndices.get(i + 1));
                    triangle.setTextureVertexIndices(triangleTextureIndices);
                }
                if (hasNormals) {
                    ArrayList<Integer> triangleNormalIndices = new ArrayList<>();
                    triangleNormalIndices.add(normalIndices.get(0));
                    triangleNormalIndices.add(normalIndices.get(i));
                    triangleNormalIndices.add(normalIndices.get(i + 1));
                    triangle.setNormalIndices(triangleNormalIndices);
                }
                triangles.add(triangle);
            }
        } else {
            // Алгоритм "ухоотсечения" для невыпуклых полигонов
            ArrayList<Integer> remainingVertices = new ArrayList<>(vertexIndices);
            ArrayList<Integer> remainingTextures = hasTexture ? new ArrayList<>(textureIndices) : null;
            ArrayList<Integer> remainingNormals = hasNormals ? new ArrayList<>(normalIndices) : null;

            while (remainingVertices.size() > 3) {
                boolean earFound = false;
                for (int i = 0; i < remainingVertices.size(); i++) {
                    int prev = (i - 1 + remainingVertices.size()) % remainingVertices.size();
                    int curr = i;
                    int next = (i + 1) % remainingVertices.size();
                    if (isEar(remainingVertices, prev, curr, next)) {
                        Polygon triangle = new Polygon();
                        ArrayList<Integer> triangleVertexIndices = new ArrayList<>();
                        triangleVertexIndices.add(remainingVertices.get(prev));
                        triangleVertexIndices.add(remainingVertices.get(curr));
                        triangleVertexIndices.add(remainingVertices.get(next));
                        triangle.setVertexIndices(triangleVertexIndices);

                        if (hasTexture) {
                            ArrayList<Integer> triangleTextureIndices = new ArrayList<>();
                            triangleTextureIndices.add(remainingTextures.get(prev));
                            triangleTextureIndices.add(remainingTextures.get(curr));
                            triangleTextureIndices.add(remainingTextures.get(next));
                            triangle.setTextureVertexIndices(triangleTextureIndices);
                        }
                        if (hasNormals) {
                            ArrayList<Integer> triangleNormalIndices = new ArrayList<>();
                            triangleNormalIndices.add(remainingNormals.get(prev));
                            triangleNormalIndices.add(remainingNormals.get(curr));
                            triangleNormalIndices.add(remainingNormals.get(next));
                            triangle.setNormalIndices(triangleNormalIndices);
                        }
                        triangles.add(triangle);

                        remainingVertices.remove(curr);
                        if (hasTexture) remainingTextures.remove(curr);
                        if (hasNormals) remainingNormals.remove(curr);
                        earFound = true;
                        break;
                    }
                }
                if (!earFound) {
                    // Если не нашли "ухо", полигон невыпуклый и возможно самопересекающийся
                    // Для простоты используем триангуляцию "веером" как резервный вариант
                    triangles.clear();
                    for (int i = 1; i < vertexCount - 1; i++) {
                        Polygon triangle = new Polygon();
                        ArrayList<Integer> triangleVertexIndices = new ArrayList<>();
                        triangleVertexIndices.add(vertexIndices.get(0));
                        triangleVertexIndices.add(vertexIndices.get(i));
                        triangleVertexIndices.add(vertexIndices.get(i + 1));
                        triangle.setVertexIndices(triangleVertexIndices);
                        if (hasTexture) {
                            ArrayList<Integer> triangleTextureIndices = new ArrayList<>();
                            triangleTextureIndices.add(textureIndices.get(0));
                            triangleTextureIndices.add(textureIndices.get(i));
                            triangleTextureIndices.add(textureIndices.get(i + 1));
                            triangle.setTextureVertexIndices(triangleTextureIndices);
                        }
                        if (hasNormals) {
                            ArrayList<Integer> triangleNormalIndices = new ArrayList<>();
                            triangleNormalIndices.add(normalIndices.get(0));
                            triangleNormalIndices.add(normalIndices.get(i));
                            triangleNormalIndices.add(normalIndices.get(i + 1));
                            triangle.setNormalIndices(triangleNormalIndices);
                        }
                        triangles.add(triangle);
                    }
                    break;
                }
            }

            // Добавляем последний треугольник
            if (remainingVertices.size() == 3) {
                Polygon triangle = new Polygon();
                triangle.setVertexIndices(remainingVertices);
                if (hasTexture) triangle.setTextureVertexIndices(remainingTextures);
                if (hasNormals) triangle.setNormalIndices(remainingNormals);
                triangles.add(triangle);
            }
        }
        return triangles;
    }

    // Проверка выпуклости полигона
    private boolean isConvex(ArrayList<Integer> vertices) {
        int n = vertices.size();
        if (n < 3) return false;

        boolean isConvex = true;
        int prevSign = 0;
        for (int i = 0; i < n; i++) {
            Vector3f a = getVertex(vertices.get(i));
            Vector3f b = getVertex(vertices.get((i + 1) % n));
            Vector3f c = getVertex(vertices.get((i + 2) % n));

            Vector3f ab = new Vector3f(b.getX() - a.getX(), b.getY() - a.getY(), b.getZ() - a.getZ());
            Vector3f bc = new Vector3f(c.getX() - b.getX(), c.getY() - b.getY(), c.getZ() - b.getZ());
            Vector3f cross = crossProduct(ab, bc);

            float z = cross.getZ(); // Для 2D-проекции на XY
            int sign = z > 0 ? 1 : -1;

            if (prevSign != 0 && sign != prevSign) {
                isConvex = false;
                break;
            }
            prevSign = sign;
        }
        return isConvex;
    }

    // Проверка, является ли вершина "ухом"
    private boolean isEar(ArrayList<Integer> vertices, int prev, int curr, int next) {
        Vector3f a = getVertex(vertices.get(prev));
        Vector3f b = getVertex(vertices.get(curr));
        Vector3f c = getVertex(vertices.get(next));

        // Проверяем, что треугольник не вырожденный
        Vector3f ab = new Vector3f(b.getX() - a.getX(), b.getY() - a.getY(), b.getZ() - a.getZ());
        Vector3f bc = new Vector3f(c.getX() - b.getX(), c.getY() - b.getY(), c.getZ() - b.getZ());
        Vector3f cross = crossProduct(ab, bc);
        float area = cross.getX() * cross.getX() + cross.getY() * cross.getY() + cross.getZ() * cross.getZ();
        if (area < 1e-7f) {
            return false;
        }

        // Проверяем, что внутри треугольника нет других вершин
        for (int i = 0; i < vertices.size(); i++) {
            if (i == prev || i == curr || i == next) {
                continue;
            }
            Vector3f p = getVertex(vertices.get(i));
            if (isPointInTriangle(a, b, c, p)) {
                return false;
            }
        }


        for (int i = 0; i < vertices.size(); i++) {
            int j = (i + 1) % vertices.size();
            if ((i == prev && j == curr) || (i == curr && j == next) || (i == next && j == prev)) {
                continue;
            }
            Vector3f p1 = getVertex(vertices.get(i));
            Vector3f p2 = getVertex(vertices.get(j));
            if (doSegmentsIntersect(a, b, p1, p2) || doSegmentsIntersect(b, c, p1, p2) || doSegmentsIntersect(c, a, p1, p2)) {
                return false;
            }
        }

        return true;
    }

    // Вспомогательный метод: получение вершины по индексу
    private Vector3f getVertex(int index) {
        return model.getVertices().get(index);
    }

    // Вспомогательный метод: векторное произведение
    private Vector3f crossProduct(Vector3f a, Vector3f b) {
        float x = a.getY() * b.getZ() - a.getZ() * b.getY();
        float y = a.getZ() * b.getX() - a.getX() * b.getZ();
        float z = a.getX() * b.getY() - a.getY() * b.getX();
        return new Vector3f(x, y, z);
    }

    // Вспомогательный метод: скалярное произведение
    private float dotProduct(Vector3f a, Vector3f b) {
        return a.getX() * b.getX() + a.getY() * b.getY() + a.getZ() * b.getZ();
    }

    // Вспомогательный метод: проверка, лежит ли точка внутри треугольника
    private boolean isPointInTriangle(Vector3f a, Vector3f b, Vector3f c, Vector3f p) {
        Vector3f v0 = new Vector3f(c.getX() - a.getX(), c.getY() - a.getY(), c.getZ() - a.getZ());
        Vector3f v1 = new Vector3f(b.getX() - a.getX(), b.getY() - a.getY(), b.getZ() - a.getZ());
        Vector3f v2 = new Vector3f(p.getX() - a.getX(), p.getY() - a.getY(), p.getZ() - a.getZ());

        float dot00 = dotProduct(v0, v0);
        float dot01 = dotProduct(v0, v1);
        float dot02 = dotProduct(v0, v2);
        float dot11 = dotProduct(v1, v1);
        float dot12 = dotProduct(v1, v2);

        float invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
        float u = (dot11 * dot02 - dot01 * dot12) * invDenom;
        float v = (dot00 * dot12 - dot01 * dot02) * invDenom;

        return (u >= 0) && (v >= 0) && (u + v < 1);
    }

    // Вспомогательный метод: проверка пересечения отрезков
    private boolean doSegmentsIntersect(Vector3f a1, Vector3f a2, Vector3f b1, Vector3f b2) {
        float x1 = a1.getX(), y1 = a1.getY();
        float x2 = a2.getX(), y2 = a2.getY();
        float x3 = b1.getX(), y3 = b1.getY();
        float x4 = b2.getX(), y4 = b2.getY();

        float d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (Math.abs(d) < 1e-7f) {
            return false;
        }

        float t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / d;
        float u = ((x1 - x3) * (y1 - y2) - (y1 - y3) * (x1 - x2)) / d;

        return t >= 0 && t <= 1 && u >= 0 && u <= 1;
    }

    public static boolean needsTriangulation(Model model) {
        for (Polygon polygon : model.polygons) {
            if (polygon.getVertexIndices().size() > 3) {
                return true;
            }
        }
        return false;
    }

    public static String getPolygonStatistics(Model model) {
        int triangleCount = 0;
        int quadCount = 0;
        int ngonCount = 0;
        for (Polygon polygon: model.polygons) {
            int vertexCount = polygon.getVertexIndices().size();
            if (vertexCount == 3) {
                triangleCount++;
            } else if (vertexCount == 4) {
                quadCount++;
            } else {
                ngonCount++;
            }
        }
        return String.format("Triangles: %d, Quads: %d, N-gons: %d", triangleCount, quadCount, ngonCount);
    }
}
