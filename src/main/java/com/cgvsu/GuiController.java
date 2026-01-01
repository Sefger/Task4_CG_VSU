package com.cgvsu;

import com.cgvsu.math.Matrix4x4;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.ModelProcessor;
import com.cgvsu.model.Polygon;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.GraphicConveyor;
import com.cgvsu.render_engine.RenderEngine;
import com.cgvsu.render_engine.Scene;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Files;

public class GuiController {

    private final float TRANSLATION = 0.5F;

    @FXML AnchorPane anchorPane;
    @FXML private Canvas canvas;

    @FXML private CheckMenuItem drawGridCheck;
    @FXML private CheckMenuItem useTextureCheck;
    @FXML private CheckMenuItem useLightingCheck;

    private Scene scene = new Scene();
    private Model mesh = null;
    private Image texture = null;
    private Model cameraMarkerMesh = null; // Модель-маркер для неактивных камер

    private Timeline timeline;

    @FXML
    private void initialize() {
        // Автоматическое изменение размера холста
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        // 1. Создаем геометрию маркера камеры
        this.cameraMarkerMesh = createCameraMarker();

        // 2. Инициализируем камеру по умолчанию
        scene.addCamera(new Camera(
                new Vector3f(0, 0, 15),
                new Vector3f(0, 0, 0),
                60.0F, 1.0F, 0.1F, 1000.0F));

        // 3. Цикл рендеринга
        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();
            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);

            Camera activeCamera = scene.getActiveCamera();
            activeCamera.setAspectRatio((float) (width / height));

            // Отрисовка основной модели
            if (mesh != null) {
                RenderEngine.render(
                        canvas.getGraphicsContext2D(), activeCamera, mesh,
                        (int) width, (int) height, Matrix4x4.identity(),
                        texture, drawGridCheck.isSelected(),
                        useTextureCheck.isSelected(), useLightingCheck.isSelected()
                );
            }

            // Отрисовка маркеров других камер на сцене
            renderInactiveCameras(width, height);
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    /**
     * Генерирует модель пирамиды.
     * Вершина пирамиды находится в (0,0,0) — это точка расположения камеры.
     */
    private Model createCameraMarker() {
        Model marker = new Model();
        // Вершины
        marker.getVertices().add(new Vector3f(0, 0, 0));             // 0: Позиция глаза
        marker.getVertices().add(new Vector3f(-0.4f, -0.4f, 1.0f));  // 1: Лево-низ
        marker.getVertices().add(new Vector3f(0.4f, -0.4f, 1.0f));   // 2: Право-низ
        marker.getVertices().add(new Vector3f(0.4f, 0.4f, 1.0f));    // 3: Право-верх
        marker.getVertices().add(new Vector3f(-0.4f, 0.4f, 1.0f));   // 4: Лево-верх

        // Полигоны (используем ваш обновленный класс Polygon с int[])
        marker.getPolygons().add(createSimplePolygon(0, 1, 2));
        marker.getPolygons().add(createSimplePolygon(0, 2, 3));
        marker.getPolygons().add(createSimplePolygon(0, 3, 4));
        marker.getPolygons().add(createSimplePolygon(0, 4, 1));
        // Основание (два треугольника)
        marker.getPolygons().add(createSimplePolygon(1, 4, 3));
        marker.getPolygons().add(createSimplePolygon(1, 3, 2));

        return marker;
    }

    private Polygon createSimplePolygon(int... indices) {
        Polygon p = new Polygon();
        p.setVertexIndices(indices); // Передаем примитивный массив напрямую
        return p;
    }

    private void renderInactiveCameras(double width, double height) {
        if (cameraMarkerMesh == null) return;

        Camera activeCamera = scene.getActiveCamera();
        for (int i = 0; i < scene.getCameras().size(); i++) {
            if (i == scene.getActiveCameraIndex()) continue;

            Camera cam = scene.getCameras().get(i);

            // Трансформация: перемещаем пирамидку в координаты неактивной камеры
            Matrix4x4 modelMatrix = GraphicConveyor.translation(
                    cam.getPosition().x, cam.getPosition().y, cam.getPosition().z);

            RenderEngine.render(
                    canvas.getGraphicsContext2D(), activeCamera, cameraMarkerMesh,
                    (int) width, (int) height, modelMatrix,
                    null, true, false, false // Рисуем только сетку (grid)
            );
        }
    }

    // --- Файловые операции ---

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) return;

        try {
            String fileContent = Files.readString(file.toPath());
            mesh = ObjReader.read(fileContent);
        } catch (Exception e) {
            showError("Ошибка", "Ошибка при загрузке: " + e.getMessage());
        }
    }

    @FXML private void onTriangulateModelMenuItemClick() {
        if (mesh != null) mesh = ModelProcessor.triangulateWithEarClipping(mesh);
    }

    @FXML private void onComputeNormalsMenuItemClick() {
        if (mesh != null) ModelProcessor.computeNormals(mesh);
    }

    @FXML private void onModelInfoMenuItemClick() {
        if (mesh == null) return;
        showInfo("Модель", "Вершин: " + mesh.getVertices().size() + "\nПолигонов: " + mesh.getPolygons().size());
    }

    // --- Управление сценой и камерами ---

    @FXML
    public void onAddCameraMenuItemClick() {
        Camera current = scene.getActiveCamera();
        // Создаем новую камеру со смещением по оси X, чтобы увидеть маркер старой
        scene.addCamera(new Camera(
                new Vector3f(current.getPosition().x + 5, 2, 10),
                new Vector3f(0, 0, 0),
                60.0F, 1.0F, 0.1F, 1000.0F));
    }

    @FXML
    public void onNextCameraMenuItemClick() {
        if (!scene.getCameras().isEmpty()) {
            int next = (scene.getActiveCameraIndex() + 1) % scene.getCameras().size();
            scene.setActiveCamera(next);
        }
    }

    @FXML
    public void onDeleteCameraMenuItemClick() {
        scene.removeCamera(scene.getActiveCameraIndex());
    }

    // Управление движением
    @FXML public void handleCameraForward() { scene.getActiveCamera().movePosition(new Vector3f(0, 0, -TRANSLATION)); }
    @FXML public void handleCameraBackward() { scene.getActiveCamera().movePosition(new Vector3f(0, 0, TRANSLATION)); }
    @FXML public void handleCameraLeft() { scene.getActiveCamera().movePosition(new Vector3f(TRANSLATION, 0, 0)); }
    @FXML public void handleCameraRight() { scene.getActiveCamera().movePosition(new Vector3f(-TRANSLATION, 0, 0)); }
    @FXML public void handleCameraUp() { scene.getActiveCamera().movePosition(new Vector3f(0, TRANSLATION, 0)); }
    @FXML public void handleCameraDown() { scene.getActiveCamera().movePosition(new Vector3f(0, -TRANSLATION, 0)); }

    @FXML
    private void onOpenTextureMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file != null) texture = new Image(file.toURI().toString());
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.show();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle(title); a.setContentText(msg); a.show();
    }
}