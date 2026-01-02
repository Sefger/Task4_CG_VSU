package com.cgvsu;

import com.cgvsu.math.Matrix4x4;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.ModelProcessor;
import com.cgvsu.model.Polygon;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.*;
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
    private Model cameraMarkerMesh = null;

    private Timeline timeline;

    @FXML
    private void initialize() {
        // 1. Привязка размеров
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        // 2. Ресурсы
        this.cameraMarkerMesh = createCameraMarker();

        // 3. Сцена по умолчанию
        scene.addCamera(new Camera(
                new Vector3f(0, 5, 15),
                new Vector3f(0, 0, 0),
                60.0F, 1.0F, 0.1F, 1000.0F));

        scene.getLights().add(new DirectionalLight(new Vector3f(-1, -1, -1), 0.5f));

        // 4. Цикл отрисовки
        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();
            var gc = canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, width, height);

            Camera activeCamera = scene.getActiveCamera();
            if (activeCamera == null) return;
            activeCamera.setAspectRatio((float) (width / height));

            // 1. Отрисовка основной модели
            if (mesh != null) {
                RenderEngine.render(
                        gc, activeCamera, mesh,
                        (int) width, (int) height, Matrix4x4.identity(),
                        texture, scene.getLights(),
                        drawGridCheck.isSelected(),
                        useTextureCheck.isSelected(), useLightingCheck.isSelected()
                );
            }

            // 2. Отрисовка маркеров неактивных камер
            renderInactiveCameras(width, height);

            // 3. ВАЖНО: Отрисовка маркеров источников света
            renderLightMarkers(width, height);
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    private void renderInactiveCameras(double width, double height) {
        if (cameraMarkerMesh == null) return;

        Camera activeCamera = scene.getActiveCamera();
        for (int i = 0; i < scene.getCameras().size(); i++) {
            if (i == scene.getActiveCameraIndex()) continue;

            Camera cam = scene.getCameras().get(i);
            Matrix4x4 modelMatrix = GraphicConveyor.translation(
                    cam.getPosition().x, cam.getPosition().y, cam.getPosition().z);

            // Отрисовка пирамидки (без освещения и текстур, чтобы избежать ошибок индексов)
            RenderEngine.render(
                    canvas.getGraphicsContext2D(), activeCamera, cameraMarkerMesh,
                    (int) width, (int) height, modelMatrix,
                    null, null,
                    true, false, false
            );
        }
    }

    private Model createCameraMarker() {
        Model marker = new Model();
        marker.getVertices().add(new Vector3f(0, 0, 0));             // 0
        marker.getVertices().add(new Vector3f(-0.5f, -0.5f, 1.2f));  // 1
        marker.getVertices().add(new Vector3f(0.5f, -0.5f, 1.2f));   // 2
        marker.getVertices().add(new Vector3f(0.5f, 0.5f, 1.2f));    // 3
        marker.getVertices().add(new Vector3f(-0.5f, 0.5f, 1.2f));   // 4

        marker.getPolygons().add(createSimplePolygon(0, 1, 2));
        marker.getPolygons().add(createSimplePolygon(0, 2, 3));
        marker.getPolygons().add(createSimplePolygon(0, 3, 4));
        marker.getPolygons().add(createSimplePolygon(0, 4, 1));
        marker.getPolygons().add(createSimplePolygon(1, 4, 3));
        marker.getPolygons().add(createSimplePolygon(1, 3, 2));

        // КРИТИЧЕСКОЕ ИСПРАВЛЕНИЕ: рассчитываем нормали для маркера
        ModelProcessor.computeNormals(marker);
        return marker;
    }

    private Polygon createSimplePolygon(int... indices) {
        Polygon p = new Polygon();
        p.setVertexIndices(indices);
        return p;
    }

    // --- Файловые методы ---

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) return;

        try {
            String fileContent = Files.readString(file.toPath());
            mesh = ObjReader.read(fileContent);

            // Авто-триангуляция и расчет нормалей для корректного света
            ModelProcessor.triangulate(mesh);
            ModelProcessor.computeNormals(mesh);

        } catch (Exception e) {
            showError("Ошибка загрузки", e.getMessage());
        }
    }

    @FXML private void onTriangulateModelMenuItemClick() {
        if (mesh != null) ModelProcessor.triangulate(mesh);
    }

    @FXML private void onComputeNormalsMenuItemClick() {
        if (mesh != null) ModelProcessor.computeNormals(mesh);
    }

    @FXML private void onModelInfoMenuItemClick() {
        if (mesh == null) return;
        showInfo("Статистика", "Вершин: " + mesh.getVertices().size() + "\nПолигонов: " + mesh.getPolygons().size());
    }

    // --- Камеры и движение ---

    @FXML
    public void onAddCameraMenuItemClick() {
        Camera c = scene.getActiveCamera();
        scene.addCamera(new Camera(
                new Vector3f(c.getPosition().x + 3, c.getPosition().y, c.getPosition().z),
                new Vector3f(0, 0, 0), 60.0F, 1.0F, 0.1F, 1000.0F));
    }

    @FXML
    public void onNextCameraMenuItemClick() {
        if (!scene.getCameras().isEmpty()) {
            scene.setActiveCamera((scene.getActiveCameraIndex() + 1) % scene.getCameras().size());
        }
    }

    @FXML
    public void onDeleteCameraMenuItemClick() {
        if (scene.getCameras().size() > 1) {
            scene.removeCamera(scene.getActiveCameraIndex());
        }
    }
    @FXML
    private void onAddLightAtCameraClick() {
        Camera activeCamera = scene.getActiveCamera();
        if (activeCamera == null) return;

        // Берем текущую позицию камеры в мире
        Vector3f lightPos = new Vector3f(
                activeCamera.getPosition().x,
                activeCamera.getPosition().y,
                activeCamera.getPosition().z
        );

        // Добавляем лампочку точно в эту точку
        scene.getLights().add(new PointLight(lightPos, 0.8f));
    }

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
        Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.show();
    }
    @FXML
    private void onAddPointLightClick() {
        // Генерируем случайную позицию в районе центра сцены для наглядности
        float x = (float) (Math.random() * 20 - 10);
        float y = (float) (Math.random() * 10 + 2);
        float z = (float) (Math.random() * 20 - 10);

        // Добавляем новый точечный источник (белый свет)
        scene.getLights().add(new PointLight(new Vector3f(x, y, z), 0.8f));

        System.out.println("Added light at: " + x + ", " + y + ", " + z);
    }

    @FXML
    private void onClearLightsClick() {
        scene.getLights().clear();
        // Добавим хотя бы один слабый источник по умолчанию, чтобы сцена не была черной
        scene.getLights().add(new DirectionalLight(new Vector3f(-1, -1, -1), 0.3f));
    }
    private void renderLightMarkers(double width, double height) {
        Camera activeCamera = scene.getActiveCamera();
        if (activeCamera == null || scene.getLights().isEmpty()) return;

        var gc = canvas.getGraphicsContext2D();

        for (Light light : scene.getLights()) {
            if (light instanceof PointLight pl) {
                // Рисуем лампочки золотистым цветом
                gc.setStroke(javafx.scene.paint.Color.GOLD);

                // Матрица: Позиция света + уменьшение масштаба (чтобы отличить от камер)
                Matrix4x4 modelMatrix = Matrix4x4.multiply(
                        GraphicConveyor.translation(pl.getPosition().x, pl.getPosition().y, pl.getPosition().z),
                        GraphicConveyor.scale(0.5f, 0.5f, 0.5f)
                );

                RenderEngine.render(
                        gc, activeCamera, cameraMarkerMesh,
                        (int) width, (int) height, modelMatrix,
                        null, null, true, false, false
                );
                gc.setStroke(javafx.scene.paint.Color.BLACK); // Сброс цвета
            }
        }
    }
}