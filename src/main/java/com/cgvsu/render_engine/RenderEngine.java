package com.cgvsu.render_engine;

import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;

import javax.vecmath.*;
import java.util.ArrayList;
import java.util.Arrays;

import static com.cgvsu.render_engine.GraphicConveyor.*;

public class RenderEngine {
    // ОШИБКА ИСПРАВЛЕНА: zBuffer должен быть массивом
    private static float[] zBuffer;
    private static int lastWidth = -1;
    private static int lastHeight = -1;

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height,
            final Image texture)
    {
        // ОПТИМИЗАЦИЯ: Пересоздаем zBuffer только при изменении размера окна
        if (zBuffer == null || width != lastWidth || height != lastHeight) {
            zBuffer = new float[width * height];
            lastWidth = width;
            lastHeight = height;
        }
        Arrays.fill(zBuffer, Float.POSITIVE_INFINITY);

        // 1. Подготовка матриц
        Matrix4f modelMatrix = rotateScaleTranslate();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelMatrix);
        modelViewProjectionMatrix.mul(viewMatrix);
        modelViewProjectionMatrix.mul(projectionMatrix);

        // 2. Подготовка освещения
        Vector3f lightDir = new Vector3f();
        lightDir.sub(camera.getPosition(), camera.getTarget());
        lightDir.normalize();

        PixelWriter pw = graphicsContext.getPixelWriter();

        // 3. Проход по полигонам
        for (Polygon polygon : mesh.getPolygons()) {
            if (polygon.getVertexIndices().size() < 3) continue;

            // ОПТИМИЗАЦИЯ: Избегаем создания лишних ArrayList внутри цикла
            // Для треугольника нам всегда нужно ровно 3 точки
            Point2f[] screenPoints = new Point2f[3];
            float[] zCoords = new float[3];

            for (int i = 0; i < 3; i++) {
                int vertexIdx = polygon.getVertexIndices().get(i);
                com.cgvsu.math.Vector3f v = mesh.getVertices().get(vertexIdx);

                // Трансформация и проекция
                Vector3f vertexVecmath = new Vector3f(v.x, v.y, v.z);
                Vector3f transformedV = multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertexVecmath);

                screenPoints[i] = vertexToPoint(transformedV, width, height);
                zCoords[i] = transformedV.z;
            }

            // 4. Растеризация
            rasterizeTriangle(
                    pw,
                    zBuffer,
                    width, height,
                    screenPoints[0], screenPoints[1], screenPoints[2],
                    zCoords[0], zCoords[1], zCoords[2],
                    polygon,
                    mesh,
                    texture,
                    lightDir
            );
        }
    }
}