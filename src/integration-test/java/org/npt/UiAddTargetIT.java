package org.npt;

import javafx.application.Platform;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.npt.services.DataService;
import org.npt.services.defaults.DefaultDataService;
import org.npt.uiservices.FrameService;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@ExtendWith({ApplicationExtension.class})
public class UiAddTargetIT {

    private final FrameService frameService = FrameService.getInstance();

    @Start
    public void start(Stage stage) throws Exception {
        DataService dataService = DefaultDataService.getInstance();
        dataService.run();
        frameService.runMainFrame(stage);
    }

    @Test
    public void testMenuButtonClickOnTargetMenu(FxRobot robot) throws InterruptedException {
        robot.clickOn("#actionMenu");
        robot.clickOn("#addTargetMenu");
        final Collection<String> stagesKeys = frameService.getStages().keySet();
        Assertions.assertThat(stagesKeys).contains("addTargetFrame");
        Stage stage = frameService.getStages().get("addTargetFrame").getStage();
        close(stage,robot);
    }

    @Test
    public void testClosingAddTargetStage(FxRobot robot) {
        robot.clickOn("#actionMenu");
        robot.clickOn("#addTargetMenu");
        Stage stage = frameService.getStages().get("addTargetFrame").getStage();
        close(stage,robot);
        final Collection<String> stagesKeys = frameService.getStages().keySet();
        Assertions.assertThat(stagesKeys).doesNotContain("addTargetFrame");
        robot.clickOn("#actionMenu");
        robot.clickOn("#addTargetMenu");
        Assertions.assertThat(stagesKeys).contains("addTargetFrame");
    }

    @Test
    public void testAddingTarget(FxRobot robot) {
        robot.clickOn("#actionMenu");
        robot.clickOn("#addTargetMenu");
        robot.clickOn("#deviceNameTextField");
        robot.write(UUID.randomUUID().toString());
        robot.clickOn("#ipTextField");
        robot.write("192.168.178.9");

        MenuButton menu = robot.lookup("#menuButton").queryAs(MenuButton.class);
        menu.getItems().stream()
                        .filter(menuItem -> menuItem.getText().equals("eth0"));


        robot.clickOn(menu);
        Stage stage = frameService.getStages().get("addTargetFrame").getStage();
        close(stage,robot);
    }

    private void close(Stage stage, FxRobot robot){
        robot.interact(() -> stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST)));
    }
}