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
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Files;

public class GuiController {

    private final float TRANSLATION = 0.5F;
    private double mousePrevX, mousePrevY;
    private boolean isMousePressed = false;

    @FXML
    AnchorPane anchorPane;
    @FXML
    private Canvas canvas;
    @FXML
    private CheckMenuItem drawGridCheck, useTextureCheck, useLightingCheck;
    @FXML
    private ListView<String> modelListView;
    @FXML
    private VBox transformPanel;
    @FXML
    private TextField translateX, translateY, translateZ, rotateX, rotateY, rotateZ, scaleX, scaleY, scaleZ;
    @FXML
    private CheckBox randomTransformationCheck;

    private Scene scene = new Scene();
    private Model cameraMarkerMesh = null;
    private Timeline timeline;
    private boolean randomTransformation = false;

    @FXML
    private void initialize() {
        anchorPane.prefWidthProperty().addListener((ov, old, newVal) -> canvas.setWidth(newVal.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, old, newVal) -> canvas.setHeight(newVal.doubleValue()));

        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseReleased(e -> isMousePressed = false);
        canvas.setOnScroll(this::handleMouseScroll);

        this.cameraMarkerMesh = createCameraMarker();

        // Инициализация дефолтной камеры и света
        scene.addCamera(new Camera(new Vector3f(0, 5, 15), new Vector3f(0, 0, 0), 60.0F, 1.0F, 0.1F, 1000.0F));
        scene.getLights().add(new DirectionalLight(new Vector3f(-1, -1, -1), 0.5f));

        modelListView.getSelectionModel().selectedIndexProperty().addListener((obs, old, newVal) -> {
            int index = newVal.intValue();
            scene.setActiveModelIndex(index);
            if (index != -1) focusCameraOnActiveModel();
        });

        timeline = new Timeline(new KeyFrame(Duration.millis(15), event -> render()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void render() {
        if (randomTransformation && scene.getActiveModel() != null) AffineTransformation.randomTransformation(scene.getActiveModel());;

        double width = canvas.getWidth();
        double height = canvas.getHeight();
        var gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        Camera activeCamera = scene.getActiveCamera();
        if (activeCamera == null) return;
        activeCamera.setAspectRatio((float) (width / height));

        for (int i = 0; i < scene.getModels().size(); i++) {
            Model m = scene.getModels().get(i);
            RenderEngine.render(
                    gc, activeCamera, m,
                    (int) width, (int) height, m.getModelMatrix(),
                    scene.getTextures().get(i), scene.getLights(),
                    drawGridCheck.isSelected(),
                    useTextureCheck.isSelected(), useLightingCheck.isSelected()
            );

            // Если модель активна — рисуем поверх неё оси
            if (i == scene.getActiveModelIndex()) {
                RenderEngine.renderAxes(gc, activeCamera, m.getModelMatrix(), (int) width, (int) height);
            }
        }
        renderMarkers(width, height);
    }


    // --- Файловые операции (Модели и Текстуры) ---

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        File file = fc.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) return;
        try {
            Model m = ObjReader.read(Files.readString(file.toPath()));
            ModelProcessor.triangulate(m);
            ModelProcessor.computeNormals(m);
            m.setModelMatrix(AffineTransformation.translation(scene.getModels().size() * 5.0f, 0, 0));
            scene.addModel(m);
            modelListView.getItems().add("Model " + scene.getModels().size() + ": " + file.getName());
            modelListView.getSelectionModel().selectLast();
        } catch (Exception e) {
            showError("Error", e.getMessage());
        }
    }

    @FXML
    private void onOpenTextureMenuItemClick() {
        if (scene.getActiveModelIndex() == -1) {
            showError("Ошибка", "Выберите модель");
            return;
        }
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fc.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file != null) scene.setActiveTexture(new Image(file.toURI().toString()));
    }

    @FXML
    private void onRemoveModelClick() {
        int index = scene.getActiveModelIndex();
        if (index != -1) {
            scene.removeModel(index);
            modelListView.getItems().remove(index);
        }
    }

    // --- Сохранение (те самые методы из ошибки) ---

    @FXML
    private void onSaveOriginalModelMenuItemClick() {
        if (scene.getActiveModelIndex() != -1) {
            onSaveModel(scene.getOriginalModels().get(scene.getActiveModelIndex()));
        }
    }

    @FXML
    private void onSaveTransformedModelMenuItemClick() {
        if (scene.getActiveModel() != null) {
            onSaveModel(scene.getActiveModel());
        }
    }

    private void onSaveModel(Model model) {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("model.obj");
        File file = fc.showSaveDialog((Stage) canvas.getScene().getWindow());
        if (file != null) {
            try {
                String path = file.getAbsolutePath();
                if (!path.toLowerCase().endsWith(".obj")) path += ".obj";
                ObjWriter.write(model, path);
                showInfo("Успех", "Модель сохранена");
            } catch (Exception e) {
                showError("Ошибка", e.getMessage());
            }
        }
    }

    // --- Управление панелью трансформаций ---

    @FXML
    private void onShowTransformPanel() {
        transformPanel.setVisible(true);
    }

    @FXML
    private void onHideTransformPanel() {
        transformPanel.setVisible(false);
    }

    @FXML
    private void onApplyTransformation() {
        try {
            if (scene.getActiveModel() == null) {
                showError("Ошибка", "Модель не загружена.");
                return;
            }
            AffineTransformation.transformation(scene.getActiveModel(),
                    Float.parseFloat(translateX.getText()), Float.parseFloat(translateY.getText()), Float.parseFloat(translateZ.getText()),
                    Float.parseFloat(rotateX.getText()), Float.parseFloat(rotateY.getText()), Float.parseFloat(rotateZ.getText()),
                    Float.parseFloat(scaleX.getText()), Float.parseFloat(scaleY.getText()), Float.parseFloat(scaleZ.getText()));
            translateX.setText("0");
            translateY.setText("0");
            translateZ.setText("0");
            scaleX.setText("1");
            scaleY.setText("1");
            scaleZ.setText("1");
            rotateX.setText("0");
            rotateY.setText("0");
            rotateZ.setText("0");
        } catch (Exception e) {
            showError("Ошибка", "Некорректные числа");
        }
    }

    @FXML
    private void onRandomTransformationCheckClicked() {

        if (scene.getActiveModel() == null) {
            showError("Ошибка", "Модель не загружена");
            randomTransformationCheck.setSelected(false);
            return;
        }
        randomTransformation = randomTransformationCheck.isSelected();
    }

    // --- Управление камерами ---

    @FXML
    public void onAddCameraMenuItemClick() {
        Camera c = scene.getActiveCamera();
        scene.addCamera(new Camera(new Vector3f(c.getPosition().x + 2, c.getPosition().y, c.getPosition().z), new Vector3f(0, 0, 0), 60, 1, 0.1f, 1000));
    }

    @FXML
    public void onNextCameraMenuItemClick() {
        if (!scene.getCameras().isEmpty())
            scene.setActiveCamera((scene.getActiveCameraIndex() + 1) % scene.getCameras().size());
    }

    @FXML
    public void onDeleteCameraMenuItemClick() {
        if (scene.getCameras().size() > 1) scene.removeCamera(scene.getActiveCameraIndex());
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

    // --- Работа с геометрией ---

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
        Model m = scene.getActiveModel();
        if (m != null) showInfo("Инфо", "Вершин: " + m.getVertices().size() + "\nПолигонов: " + m.getPolygons().size());
    }

    // --- Управление светом ---

    @FXML
    private void onClearLightsClick() {
        scene.getLights().clear();
        scene.getLights().add(new DirectionalLight(new Vector3f(-1, -1, -1), 0.5f));
    }

    @FXML
    private void onAddPointLightClick() {
        scene.getLights().add(new PointLight(new Vector3f(0, 10, 0), 0.8f));
    }

    @FXML
    private void onAddLightAtCameraClick() {
        if (scene.getActiveCamera() != null)
            scene.getLights().add(new PointLight(scene.getActiveCamera().getPosition(), 0.8f));
    }

    // --- Навигация моделей ---

    @FXML
    public void onNextModelMenuItemClick() {
        if (!scene.getModels().isEmpty())
            modelListView.getSelectionModel().select((scene.getActiveModelIndex() + 1) % scene.getModels().size());
    }

    @FXML
    public void onPrevModelMenuItemClick() {
        if (!scene.getModels().isEmpty())
            modelListView.getSelectionModel().select((scene.getActiveModelIndex() - 1 + scene.getModels().size()) % scene.getModels().size());
    }

    // --- Вспомогательные методы ---

    private void focusCameraOnActiveModel() {
        Model active = scene.getActiveModel();
        if (active != null && scene.getActiveCamera() != null) {
            Matrix4x4 m = active.getModelMatrix();
            scene.getActiveCamera().setTarget(new Vector3f(m.get(0, 3), m.get(1, 3), m.get(2, 3)));
        }
    }

    private void handleMousePressed(MouseEvent e) {
        mousePrevX = e.getX();
        mousePrevY = e.getY();
        isMousePressed = true;
        canvas.requestFocus();
    }

    private void handleMouseDragged(MouseEvent e) {
        if (!isMousePressed || scene.getActiveCamera() == null) return;
        float dx = (float) (e.getX() - mousePrevX);
        float dy = (float) (e.getY() - mousePrevY);
        if (e.getButton() == MouseButton.PRIMARY) {
            scene.getActiveCamera().rotateAroundPoint(scene.getActiveCamera().getTarget(), dx, dy);
        } else if (e.getButton() == MouseButton.SECONDARY) {
            scene.getActiveCamera().move(new Vector3f(dx * 0.01f, -dy * 0.01f, 0));
        }
        mousePrevX = e.getX();
        mousePrevY = e.getY();
    }

    private void handleMouseScroll(ScrollEvent e) {
        if (scene.getActiveCamera() != null) scene.getActiveCamera().zoom((float) e.getDeltaY());
    }

    private void renderMarkers(double w, double h) {
        Camera active = scene.getActiveCamera();
        var gc = canvas.getGraphicsContext2D();
        for (int i = 0; i < scene.getCameras().size(); i++) {
            if (i == scene.getActiveCameraIndex()) continue;
            Vector3f p = scene.getCameras().get(i).getPosition();
            RenderEngine.render(gc, active, cameraMarkerMesh, (int) w, (int) h, AffineTransformation.translation(p.x, p.y, p.z), null, null, true, false, false);
        }
    }

    private Model createCameraMarker() {
        Model m = new Model();
        m.getVertices().add(new Vector3f(0, 0, 0));
        m.getVertices().add(new Vector3f(-0.5f, -0.5f, 1.2f));
        m.getVertices().add(new Vector3f(0.5f, -0.5f, 1.2f));
        m.getVertices().add(new Vector3f(0.5f, 0.5f, 1.2f));
        m.getVertices().add(new Vector3f(-0.5f, 0.5f, 1.2f));
        m.getPolygons().add(createPoly(0, 1, 2));
        m.getPolygons().add(createPoly(0, 2, 3));
        m.getPolygons().add(createPoly(0, 3, 4));
        m.getPolygons().add(createPoly(0, 4, 1));
        ModelProcessor.computeNormals(m);
        return m;
    }

    private Polygon createPoly(int... i) {
        Polygon p = new Polygon();
        p.setVertexIndices(i);
        return p;
    }

    private void showError(String t, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(t);
        a.setHeaderText(null);
        a.setContentText(m);
        a.show();
    }

    private void showInfo(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t);
        a.setHeaderText(null);
        a.setContentText(m);
        a.show();
    }
}