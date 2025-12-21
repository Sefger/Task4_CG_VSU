package com.cgvsu;

import com.cgvsu.model.ModelProcessor;
import com.cgvsu.render_engine.RenderEngine;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.affinetransformation.*;

import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.Camera;

public class GuiController {

    final private float TRANSLATION = 0.5F;

    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    private Model mesh = null;
    Image texture = null;
    private Camera camera = new Camera(
            new Vector3f(0, 0, 15),     // Позиция: 15 единиц от центра
            new Vector3f(0, 0, 0),      // Смотрим в центр
            60.0F,                      // FOV 60 градусов (а не 1.0)
            1.0F,                       // Aspect (обновится автоматически)
            0.1F,                       // Near plane
            1000.0F);
    private Timeline timeline;

    @FXML
    private void initialize() {
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            camera.setAspectRatio((float) (width / height));

            if (mesh != null) {
                RenderEngine.render(canvas.getGraphicsContext2D(), camera, mesh, (int) width, (int) height, texture);
            }
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String fileContent = Files.readString(fileName);
            mesh = ObjReader.read(fileContent);
            // todo: обработка ошибок
        } catch (IOException exception) {
            showError("Ошибка зашрузки файла: ", exception.getMessage());
        } catch (Exception exception) {
            showError("Ошибка чтения модели: ", exception.getMessage());
        }
    }

    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, TRANSLATION));
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(-TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, TRANSLATION, 0));
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, -TRANSLATION, 0));
    }

    @FXML
    public void onTriangulateModelMenuItemClick() {
        if (mesh == null) {
            showWarning("Модель не загружена", "Сначала загрузите модель");
            return;
        }

        if (ModelProcessor.isTriangulated(mesh)) {
            showInfo("Модель уже триангулирована", "Модель уже состоит из треугольников\n");
            return;
        }
        try {
            //Model previousMesh = mesh;

            //Триунгируем
            mesh = ModelProcessor.triangulateWithEarClipping(mesh);

            showInfo("Модель триангулирована",
                    "Модель успешно триангулирована.\n");
            //может быть состоит здесь показать статистику, но зачем?
        } catch (Exception exception) {
            showError("Ошибка триангуляции", exception.getMessage());
        }
    }

    @FXML
    private void onComputeNormalsMenuItemClick() {
        if (mesh == null) {
            showWarning("Модель не загружена", "Сначала загрузите модель");
            return;
        }

        try {
            ModelProcessor.computeNormals(mesh);
            showInfo("Нормали вычислены", "Нормали модели успешно вычислены.\n");
        } catch (Exception exception) {
            showError("Ошибка вычисления нормалей", exception.getMessage());
        }
    }

    @FXML
    private void onModelInfoMenuItemClick() {
        showModelInfo();
    }

    @FXML
    private void onOpenTextureMenuItemClick(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
          new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.bmp")
        );
        fileChooser.setTitle("Load Texture");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file!=null){
            try{
                this.texture = new Image(file.toURI().toString());
            }
            catch (Exception e){
                showError("ОШибка загрузки текстуры", e.getMessage());
            }
        }
    }

    private void showModelInfo() {
        if (mesh == null) {
            showInfo("Информация о модели", "Модель не загружена");
            return;
        }

        StringBuilder info = new StringBuilder();
        info.append("Вершины: ").append(mesh.getVertices().size()).append("\n");
        info.append("Текстурные координаты: ").append(mesh.getTextureVertices().size()).append("\n");
        info.append("Нормали: ").append(mesh.getNormals().size()).append("\n");
        info.append("Полигоны: ").append(mesh.getPolygons().size()).append("\n");
        info.append("\n");
        info.append("Статистика полигонов:\n");
        info.append(ModelProcessor.getPolygonStatistics(mesh)).append("\n");
        info.append("\n");
        info.append("Триангулирована: ").append(ModelProcessor.isTriangulated(mesh) ? "Да" : "Нет").append("\n");
        info.append("Нуждается в триангуляции: ").append(ModelProcessor.needsTriangulation(mesh) ? "Да" : "Нет");

        showInfo("Информация о модели", info.toString());
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}