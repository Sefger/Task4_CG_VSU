package com.cgvsu;

import com.cgvsu.math.Matrix4x4;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.ModelProcessor;
import com.cgvsu.model.Polygon;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.render_engine.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class GuiController {

    private final float TRANSLATION = 0.5F;

    @FXML
    AnchorPane anchorPane;
    @FXML
    private Canvas canvas;

    @FXML
    private CheckMenuItem drawGridCheck;
    @FXML
    private CheckMenuItem useTextureCheck;
    @FXML
    private CheckMenuItem useLightingCheck;

    @FXML
    private ListView<String> modelListView;
    @FXML
    private VBox transformPanel;

    private Scene scene = new Scene();
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

        // Слушатель выбора модели в списке
        modelListView.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            int index = newVal.intValue();
            scene.setActiveModelIndex(index);

            // 1. Очищаем поля трансформации при переключении,
            // чтобы случайно не применить старые значения к новому объекту
            clearTransformFields();

            // 2. (Опционально) Выводим в консоль для теста
            if (index != -1) {
                System.out.println("Выбрана модель: " + modelListView.getItems().get(index));
            }
        });

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();
            var gc = canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, width, height);

            Camera activeCamera = scene.getActiveCamera();
            if (activeCamera == null) return;
            activeCamera.setAspectRatio((float) (width / height));

            // 1. Отрисовка всех моделей
            for (int i = 0; i < scene.getModels().size(); i++) {
                Model m = scene.getModels().get(i);
                boolean isActive = (i == scene.getActiveModelIndex());

                Image textureToApply = isActive ? texture : null;

                RenderEngine.render(
                        gc, activeCamera, m,
                        (int) width, (int) height, m.getModelMatrix(),
                        textureToApply, scene.getLights(),
                        isActive || drawGridCheck.isSelected(), // Активная модель всегда подсвечена сеткой
                        useTextureCheck.isSelected(), useLightingCheck.isSelected()
                );
            }
            renderInactiveCameras(width, height);
            renderLightMarkers(width, height);
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    private void clearTransformFields() {
        translateX.setText("0");
        translateY.setText("0");
        translateZ.setText("0");
        rotateX.setText("0");
        rotateY.setText("0");
        rotateZ.setText("0");
        scaleX.setText("1");
        scaleY.setText("1");
        scaleZ.setText("1");
    }

    private void renderInactiveCameras(double width, double height) {
        if (cameraMarkerMesh == null) return;

        Camera activeCamera = scene.getActiveCamera();
        for (int i = 0; i < scene.getCameras().size(); i++) {
            if (i == scene.getActiveCameraIndex()) continue;

            Camera cam = scene.getCameras().get(i);
            Matrix4x4 modelMatrix = AffineTransformation.translation(
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
            Model newModel = ObjReader.read(fileContent);

            ModelProcessor.triangulate(newModel);
            ModelProcessor.computeNormals(newModel);

            // Смещение, чтобы модели не стояли в одной точке
            float offsetX = scene.getModels().size() * 5.0f;
            newModel.setModelMatrix(AffineTransformation.translation(offsetX, 0, 0));

            // 1. Добавляем модель в логику (в Scene)
            scene.addModel(newModel);

            // 2. КРИТИЧЕСКИЙ МОМЕНТ: Добавляем строку в ListView
            // Если этой строки нет, список останется пустым!
            modelListView.getItems().add("Model " + scene.getModels().size() + ": " + file.getName());

            // 3. Выделяем новую модель в списке
            modelListView.getSelectionModel().selectLast();

        } catch (Exception e) {
            showError("Ошибка загрузки", e.getMessage());
        }
    }

    @FXML
    private void onRemoveModelClick() {
        int index = scene.getActiveModelIndex();
        if (index != -1) {
            scene.removeModel(index);
            modelListView.getItems().remove(index);
        }
    }

    @FXML
    private void onSaveModelMenuItemClick() {
        Model activeModel = scene.getActiveModel();
        if (activeModel == null) {
            showError("Модель не загружена", "Сначала загрузите модель для сохранения");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Model As");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("OBJ Files", "*.obj"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        // Предлагаем имя по умолчанию
        fileChooser.setInitialFileName("model.obj");

        // Устанавливаем начальную директорию
        File initialDir = new File(System.getProperty("user.home") + File.separator + "Documents");
        if (initialDir.exists() && initialDir.isDirectory()) {
            fileChooser.setInitialDirectory(initialDir);
        }

        File file = fileChooser.showSaveDialog((Stage) canvas.getScene().getWindow());

        if (file != null) {
            try {
                // Убедимся, что файл имеет расширение .obj
                String filePath = file.getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".obj")) {
                    filePath += ".obj";
                    file = new File(filePath);
                }

                // Сохраняем модель с помощью ObjWriter
                ObjWriter.write(scene.getActiveModel(), filePath);

                showInfo("Сохранение завершено",
                        "Модель успешно сохранена в файл:\n" + filePath);

            } catch (IOException e) {
                showError("Ошибка сохранения",
                        "Не удалось сохранить файл:\n" + e.getMessage());
            } catch (Exception e) {
                showError("Ошибка данных",
                        "Ошибка при сохранении модели:\n" + e.getMessage());
            }
        }
    }

    @FXML
    private void onTriangulateModelMenuItemClick() {
        if (scene.getActiveModel() != null) ModelProcessor.triangulate(scene.getActiveModel());
    }

    @FXML
    private void onComputeNormalsMenuItemClick() {
        if (scene.getActiveModel() != null) ModelProcessor.computeNormals(scene.getActiveModel());
    }

    @FXML
    private void onModelInfoMenuItemClick() {
        if (scene.getActiveModel() == null) return;
        showInfo("Статистика", "Вершин: " + scene.getActiveModel().getVertices().size() + "\nПолигонов: " + scene.getActiveModel().getPolygons().size());
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

    @FXML
    public void handleCameraForward() {
        scene.getActiveCamera().movePosition(new Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    public void handleCameraBackward() {
        scene.getActiveCamera().movePosition(new Vector3f(0, 0, TRANSLATION));
    }

    @FXML
    public void handleCameraLeft() {
        scene.getActiveCamera().movePosition(new Vector3f(TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraRight() {
        scene.getActiveCamera().movePosition(new Vector3f(-TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraUp() {
        scene.getActiveCamera().movePosition(new Vector3f(0, TRANSLATION, 0));
    }

    @FXML
    public void handleCameraDown() {
        scene.getActiveCamera().movePosition(new Vector3f(0, -TRANSLATION, 0));
    }

    @FXML
    private void onOpenTextureMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file != null) texture = new Image(file.toURI().toString());
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.show();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.show();
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
                Matrix4x4 modelMatrix = AffineTransformation.combine(
                        AffineTransformation.translation(pl.getPosition().x, pl.getPosition().y, pl.getPosition().z),
                        AffineTransformation.scale(0.5f, 0.5f, 0.5f)
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

    @FXML
    private TextField translateX;
    @FXML
    private TextField translateY;
    @FXML
    private TextField translateZ;

    @FXML
    private TextField rotateX;
    @FXML
    private TextField rotateY;
    @FXML
    private TextField rotateZ;

    @FXML
    private TextField scaleX;
    @FXML
    private TextField scaleY;
    @FXML
    private TextField scaleZ;


    @FXML
    private void onApplyTranslation() {
        try {
            if (scene.getActiveModel() == null) return;
            float tx = Float.parseFloat(translateX.getText());
            float ty = Float.parseFloat(translateY.getText());
            float tz = Float.parseFloat(translateZ.getText());

            Matrix4x4 current = scene.getActiveModel().getModelMatrix();
            Matrix4x4 translation = AffineTransformation.translation(tx, ty, tz);
            scene.getActiveModel().setModelMatrix(translation.multiply(current));

            ModelProcessor.computeNormals(scene.getActiveModel());
        } catch (NumberFormatException e) {
            showError("Invalid input", "Please enter valid numbers for translation");
        }
    }

    @FXML
    private void onApplyRotation() {
        try {
            if (scene.getActiveModel() == null) return;

            float rx = Float.parseFloat(rotateX.getText());
            float ry = Float.parseFloat(rotateY.getText());
            float rz = Float.parseFloat(rotateZ.getText());

            Matrix4x4 current = scene.getActiveModel().getModelMatrix();
            Matrix4x4 rotationX = AffineTransformation.rotationX(rx);
            Matrix4x4 rotationY = AffineTransformation.rotationY(ry);
            Matrix4x4 rotationZ = AffineTransformation.rotationZ(rz);

            Matrix4x4 rotation = rotationZ.multiply(rotationY).multiply(rotationX);
            scene.getActiveModel().setModelMatrix(rotation.multiply(current));

            ModelProcessor.computeNormals(scene.getActiveModel());
        } catch (NumberFormatException e) {
            showError("Invalid input", "Please enter valid numbers for translation");
        }
    }

    @FXML
    private void onApplyScale() {
        try {
            if (scene.getActiveModel() == null) return;

            float sx = Float.parseFloat(scaleX.getText());
            float sy = Float.parseFloat(scaleY.getText());
            float sz = Float.parseFloat(scaleZ.getText());

            Matrix4x4 current = scene.getActiveModel().getModelMatrix();
            Matrix4x4 scale = AffineTransformation.scale(sx, sy, sz);
            scene.getActiveModel().setModelMatrix(scale.multiply(current));

            ModelProcessor.computeNormals(scene.getActiveModel());
        } catch (NumberFormatException e) {
            showError("Invalid input", "Please enter valid numbers for translation");
        }
    }

    @FXML
    public void onNextModelMenuItemClick() {
        int size = scene.getModels().size();
        if (size > 0) {
            int currentIndex = scene.getActiveModelIndex();
            int nextIndex = (currentIndex + 1) % size;

            // Выделяем в визуальном списке (это автоматически вызовет слушатель и сменит индекс в сцене)
            modelListView.getSelectionModel().select(nextIndex);
        }
    }

    @FXML
    public void onPrevModelMenuItemClick() {
        int size = scene.getModels().size();
        if (size > 0) {
            int currentIndex = scene.getActiveModelIndex();
            int prevIndex = (currentIndex - 1 + size) % size;

            // Выделяем в визуальном списке
            modelListView.getSelectionModel().select(prevIndex);
        }
    }

    @FXML
    private void onHideTransformPanel() {
        transformPanel.setVisible(false);
    }

    @FXML
    private void onShowTransformPanel() {
        transformPanel.setVisible(true);
    }

}