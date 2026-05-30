package org.firstinspires.ftc.teamcode.auto;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

public class RevolverController {

    @FXML
    public Pane revolverPane;
    @FXML
    public Group revolver;
    @FXML
    public Circle top_center;
    @FXML
    public Circle top_left;
    @FXML
    public Circle top_right;
    @FXML
    public Circle bottom_left;
    @FXML
    public Circle bottom_right;
    @FXML
    public Circle bottom_center;

    @FXML
    public GridPane uiGridPane;  // on the right-hand side of the BorderPane
    @FXML
    public HBox patternHBox;
    @FXML
    public Label opModeLabel;
    @FXML
    public Label firstPositionLabel;
    @FXML
    public HBox firstSlotHBox;
    @FXML
    public HBox firstColorHBox;
    @FXML
    public Label secondPositionLabel;
    public HBox secondSlotHBox;
    @FXML
    public HBox secondColorHBox;
    @FXML
    public Label thirdPositionLabel;
    public HBox thirdSlotHBox;
    @FXML
    public HBox thirdColorHBox;

    @FXML
    public HBox playResetHBox;
    @FXML
    public Button playButton;
    @FXML
    public Button resetTeleOpUIButton;
}
