package org.npt.controllers;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import kotlin.Pair;
import lombok.*;
import org.npt.models.ui.Frame;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FrameService {

    private static FrameService frameService = null;

    private final Map<String, StageMap> stages = new HashMap<>();

    @SneakyThrows
    public void runMainFrame(Stage primaryStage) {
        Frame mainFrame = Frame.createMainFrame();
        primaryStage.setTitle(mainFrame.getTitle());
        FXMLLoader loader = new FXMLLoader(readFileResource(mainFrame.getFxmlLocation()));
        DataInjector controllerInstance = (DataInjector) mainFrame.getControllerClass()
                .getDeclaredConstructor()
                .newInstance();
        controllerInstance.setArgs(mainFrame.getArgs());
        loader.setController(controllerInstance);
        Parent root = loader.load();
        Scene scene = new Scene(root, mainFrame.getSize().width(), mainFrame.getSize().height());
        primaryStage.setScene(scene);
        StageMap stageMap = new StageMap(primaryStage);
        stageMap.getSceneInfos().push(SceneInfo.builder()
                .scene(scene)
                .frame(mainFrame)
                .build());
        stages.put(mainFrame.getKey(), stageMap);
        primaryStage.show();
    }

    public void createNewStage(Frame frame, boolean resizable) {
        Stage popupStage = new Stage();
        popupStage.setMaximized(resizable);
        popupStage.initModality(Modality.WINDOW_MODAL);
        startStage(popupStage, frame);
    }

    public void stopStage(String key) {
        Platform.runLater(() -> {
            stages.get(key).stage.close();
            stages.remove(key);
        });
    }

    @SneakyThrows
    public Parent createNewScene(Frame frame, String key) {
        StageMap stageMap = stages.get(key);
        URL fxmlResource = readFileResource(frame.getFxmlLocation());
        FXMLLoader loader = new FXMLLoader(fxmlResource);
        DataInjector controllerInstance = (DataInjector) frame.getControllerClass()
                .getDeclaredConstructor()
                .newInstance();
        controllerInstance.setArgs(frame.getArgs());
        loader.setController(controllerInstance);
        Parent root = loader.load();
        Scene scene = new Scene(root, frame.getSize().width(), frame.getSize().height());
        stageMap.getStage().setScene(scene);
        SceneInfo sceneInfo = new SceneInfo(scene, frame, controllerInstance);
        stageMap.getSceneInfos().push(sceneInfo);
        return root;
    }

    @SneakyThrows
    public void removeCurrentScene(String key) {
        StageMap stageMap = stages.get(key);
        stageMap.getSceneInfos().pop();
        Scene scene = stageMap.getSceneInfos().getLast().scene();
        stageMap.getStage().setScene(scene);
    }

    @SneakyThrows
    public Pair<Parent, DataInjector> createNewPane(Frame frame) {
        URL fxmlResource = readFileResource(frame.getFxmlLocation());
        FXMLLoader loader = new FXMLLoader(fxmlResource);
        DataInjector controllerInstance = (DataInjector) frame.getControllerClass()
                .getDeclaredConstructor()
                .newInstance();
        controllerInstance.setArgs(frame.getArgs());
        loader.setController(controllerInstance);
        return new Pair<>(loader.load(), controllerInstance);
    }

    @SneakyThrows
    private void startStage(Stage stage, Frame frame) {
        stage.setTitle(frame.getTitle());
        URL fxmlResource = readFileResource(frame.getFxmlLocation());
        FXMLLoader loader = new FXMLLoader(fxmlResource);
        DataInjector controllerInstance = (DataInjector) frame.getControllerClass()
                .getDeclaredConstructor()
                .newInstance();
        controllerInstance.setArgs(frame.getArgs());
        loader.setController(controllerInstance);
        Parent root = loader.load();
        double prefWidth = frame.getSize().width();
        double prefHeight = frame.getSize().height();
        Scene scene = new Scene(root, prefWidth, prefHeight);
        stage.setScene(scene);
        stage.show();
        double decorationWidth = stage.getWidth() - scene.getWidth();
        double decorationHeight = stage.getHeight() - scene.getHeight();
        stage.setWidth(prefWidth + decorationWidth);
        stage.setHeight(prefHeight + decorationHeight);
        StageMap stageMap = new StageMap(stage);
        stageMap.getSceneInfos().push(SceneInfo.builder()
                .scene(scene)
                .frame(frame)
                .controller(controllerInstance)
                .build());
        stages.put(frame.getKey(), stageMap);
    }


    private URL readFileResource(String resourcePath) {
        return FrameService.class.getClassLoader().getResource(resourcePath);
    }

    public static FrameService getInstance() {
        if (frameService == null)
            frameService = new FrameService();
        return frameService;
    }

    @Getter
    public static class StageMap {

        private final Stage stage;
        private final Stack<SceneInfo> sceneInfos = new Stack<>();

        public StageMap(Stage stage) {
            this.stage = stage;
        }
    }

    @Builder
    public record SceneInfo(Scene scene, Frame frame, DataInjector controller) {

    }

}