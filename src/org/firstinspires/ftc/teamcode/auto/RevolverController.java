package org.firstinspires.ftc.teamcode.auto;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class RevolverController {

    @FXML
    public Pane revolverPane; // on the left-hand side of the BorderPane
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
    GridPane uiGridPane;  // on the right-hand side of the BorderPane
    @FXML Label opModeLabel;
    @FXML
    public Label rowOneLabel;
    @FXML
    public Label rowTwoLabel;
    @FXML
    public Label rowThreeLabel;
}
