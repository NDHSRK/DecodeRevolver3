package org.firstinspires.ftc.teamcode.auto;

import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.firstinspires.ftc.ftcdevcommon.AutonomousRobotException;
import org.firstinspires.ftc.ftcdevcommon.Pair;
import org.firstinspires.ftc.ftcdevcommon.platform.intellij.RobotLogCommon;
import org.firstinspires.ftc.ftcdevcommon.platform.intellij.WorkingDirectory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// Animation of rapid-fire shooting by pattern.
// One of the main goals of this animation is to keep the class
// RevolverMotion as close as possible to the one used with the
// actual Revolver hardware. Methods that activate the hardware
// are not called or commented out.
// The animation also does not include the intake of artifacts.
public class RevolverAnimation extends Application {
    private static final String TAG = RevolverAnimation.class.getSimpleName();

    private RevolverController controller;

    private enum OpModeType {AUTO, TELEOP}

    private enum UIPositionLabel {LEFT, CENTER, RIGHT}

    private static final String POSITION_PREFIX = "REAR_VIEW_";

    // In the GridPane for the UI set the constant row
    // offsets from the revolver position.
    private static final int SLOT_ROW_OFFSET = 1;
    private static final int COLOR_ROW_OFFSET = 2;

    private DriverInput driverInput;

    // Starting contents of the revolver after the pre-loads have been placed.
    // The representation of the revolver is from the point of view of an observer
    // standing behind the robot.
    private final EnumMap<RevolverMotion.RevolverTrackingPosition, RevolverMotion.RevolverSlotInfo> autoRevolverTracking = new EnumMap<>(Map.of(
            RevolverMotion.RevolverTrackingPosition.REAR_VIEW_LEFT, new RevolverMotion.RevolverSlotInfo(RobotConstantsDecode.ArtifactColor.PURPLE, RevolverServo.RevolverSlot.SLOT_2),
            RevolverMotion.RevolverTrackingPosition.REAR_VIEW_CENTER, new RevolverMotion.RevolverSlotInfo(RobotConstantsDecode.ArtifactColor.GREEN, RevolverServo.RevolverSlot.SLOT_0),
            RevolverMotion.RevolverTrackingPosition.REAR_VIEW_RIGHT, new RevolverMotion.RevolverSlotInfo(RobotConstantsDecode.ArtifactColor.PURPLE, RevolverServo.RevolverSlot.SLOT_1)
    ));

    private final EnumMap<RevolverMotion.RevolverTrackingPosition, RevolverMotion.RevolverSlotInfo> teleopRevolverTracking = new EnumMap<>(RevolverMotion.RevolverTrackingPosition.class);

    //**TODO Need labels for slots. The labels should remain horizontal even
    // as the revolver rotates. But avoid clutter; slots are shown in the UI.

    @Override
    public void start(final Stage pStage) throws IOException {

        String logDirPath = WorkingDirectory.getWorkingDirectory() + RobotConstants.logDir;
        RobotLogCommon.OpenStatus openStatus = RobotLogCommon.initialize(RobotLogCommon.LogIdentifier.AUTO_LOG,
                logDirPath);
        System.out.println("Opened " + RobotLogCommon.LogIdentifier.AUTO_LOG + " with status " + openStatus);

        // Here's how this works:
        // First show a modal popup radio button for OpMode selection.
        // This must be done first because the driver's selection
        // determines the appearance of the options in the next screen.
        RadioButton selectedOpMode = showOpModePopup(pStage);

        // If the driver selects the radio button for "Auto top shoot"
        // then the UI for slot and color selection will be laid out
        // in the order CENTER, LEFT, RIGHT. If the user selects
        // "TeleOp bottom intake" then the order will be LEFT, RIGHT,
        // CENTER.

        // For the next 2 lines to work with later versions of JavaFX,
        // the fxml file must be under the same package as the current
        // class but in the resources folder. But in the current version
        // the file must be in the same package as the code.
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("Revolver.fxml"));
        BorderPane root = fxmlLoader.load();
        controller = fxmlLoader.getController();

        // Show the artifact selection combo box first.
        // 1. Create the ComboBox.
        ComboBox<String> artifactCombo = new ComboBox<>();
        artifactCombo.setPrefWidth(Region.USE_COMPUTED_SIZE);

        // 2. Add three choices
        artifactCombo.getItems().addAll(RobotConstantsDecode.ObeliskPattern.GREEN_PURPLE_PURPLE.toString(),
                RobotConstantsDecode.ObeliskPattern.PURPLE_GREEN_PURPLE.toString(),
                RobotConstantsDecode.ObeliskPattern.PURPLE_PURPLE_GREEN.toString());

        // 3. Set PURPLE_GREEN_PURPLE as the default
        artifactCombo.setValue(RobotConstantsDecode.ObeliskPattern.PURPLE_GREEN_PURPLE.toString());

        // Add to the GridPane at the correct row index for the selected OpMode.
        // gridPane.add(child, columnIndex, rowIndex, columnSpan, rowSpan);
        controller.uiGridPane.add(artifactCombo, 1, 0, GridPane.REMAINING, 1); // column 1, row 0

        int centerRowIndex;
        int leftRowIndex;
        int rightRowIndex;
        if (selectedOpMode.getText().equals("Auto top shoot")) {
            centerRowIndex = 2;
            controller.rowOneLabel.setText(UIPositionLabel.CENTER.toString());
            leftRowIndex = 5;
            controller.rowTwoLabel.setText(UIPositionLabel.LEFT.toString());
            rightRowIndex = 8;
            controller.rowThreeLabel.setText(UIPositionLabel.RIGHT.toString());
        } else { // must be TeleOp
            leftRowIndex = 2;
            controller.rowOneLabel.setText(UIPositionLabel.LEFT.toString());
            rightRowIndex = 5;
            controller.rowTwoLabel.setText(UIPositionLabel.RIGHT.toString());
            centerRowIndex = 8;
            controller.rowThreeLabel.setText(UIPositionLabel.CENTER.toString());
        }

        //**TODO make RevolverMotion.RevolverTrackingPosition a parameter
        // and set the default RadioButton selection to the Auto setup.
        //**TODO Same for color ...
        // For each RevolverTrackingPosition create RadioButtons for the slots
        // and for color selection.
        Pair<ToggleGroup, HBox> slotGroupCenter = uiSlotSelection(controller.uiGridPane, centerRowIndex + SLOT_ROW_OFFSET);
        Pair<ToggleGroup, HBox> slotGroupLeft = uiSlotSelection(controller.uiGridPane, leftRowIndex + SLOT_ROW_OFFSET);
        Pair<ToggleGroup, HBox> slotGroupRight = uiSlotSelection(controller.uiGridPane, rightRowIndex + SLOT_ROW_OFFSET);

        // Set listeners.
        setSlotListener(slotGroupCenter.first, slotGroupLeft.first, slotGroupRight.first);
        setSlotListener(slotGroupLeft.first, slotGroupCenter.first, slotGroupRight.first);
        setSlotListener(slotGroupRight.first, slotGroupCenter.first, slotGroupLeft.first);

        // Color Selection.
        Pair<ToggleGroup, HBox> colorGroupCenter = uiColorSelection(controller.uiGridPane, centerRowIndex + COLOR_ROW_OFFSET);
        Pair<ToggleGroup, HBox> colorGroupLeft = uiColorSelection(controller.uiGridPane, leftRowIndex + COLOR_ROW_OFFSET);
        Pair<ToggleGroup, HBox> colorGroupRight = uiColorSelection(controller.uiGridPane, rightRowIndex + COLOR_ROW_OFFSET);

        // Gather all the UI responses and instantiate the DriverInput class.
        OpModeType opModeType = selectedOpMode.getText().equals("Auto top shoot") ? OpModeType.AUTO : OpModeType.TELEOP;
        RevolverMotion.SearchOrder searchOrder;
        EnumMap<RevolverMotion.RevolverTrackingPosition, RevolverMotion.RevolverSlotInfo> revolverTracking;

        // Configure the UI.
        if (opModeType == OpModeType.AUTO) {
            // Use the default preload autoRevolverTracking
            searchOrder = RevolverMotion.SearchOrder.IN_PLACE;
            revolverTracking = autoRevolverTracking;

            // Since slot and color selections are fixed in our
            // standard setup for the Decode game, disable their
            // HBox containers.
            //slotGroupCenter.selectToggle(option1);
            slotGroupCenter.second.setDisable(true);
            slotGroupLeft.second.setDisable(true);
            slotGroupRight.second.setDisable(true);

            colorGroupCenter.second.setDisable(true);
            colorGroupLeft.second.setDisable(true);
            colorGroupRight.second.setDisable(true);
        } else { // TeleOp
            searchOrder = RevolverMotion.SearchOrder.ON_TRANSITION;
            revolverTracking = teleopRevolverTracking;
        }

        // From the ComboBox selection for the artifact pattern
        // create a list of colors.
        RobotConstantsDecode.ObeliskPattern pattern = RobotConstantsDecode.ObeliskPattern.valueOf(artifactCombo.getSelectionModel().getSelectedItem());
        List<RobotConstantsDecode.ArtifactColor> patternColors = new ArrayList<>();
        switch (pattern) {
            case GREEN_PURPLE_PURPLE -> {
                patternColors.add(RobotConstantsDecode.ArtifactColor.GREEN);
                patternColors.add(RobotConstantsDecode.ArtifactColor.PURPLE);
                patternColors.add(RobotConstantsDecode.ArtifactColor.PURPLE);
            }
            case PURPLE_GREEN_PURPLE -> {
                patternColors.add(RobotConstantsDecode.ArtifactColor.PURPLE);
                patternColors.add(RobotConstantsDecode.ArtifactColor.GREEN);
                patternColors.add(RobotConstantsDecode.ArtifactColor.PURPLE);
            }
            case PURPLE_PURPLE_GREEN -> {
                patternColors.add(RobotConstantsDecode.ArtifactColor.PURPLE);
                patternColors.add(RobotConstantsDecode.ArtifactColor.PURPLE);
                patternColors.add(RobotConstantsDecode.ArtifactColor.GREEN);
            }
        }

        driverInput = new DriverInput(opModeType, searchOrder, revolverTracking, patternColors);

        // Get the final slot and color selections when the driver hits the Play button.
        // Add to the 1st column (index 0) of the 12th row (index 11).
        Button playButton = new Button("Play");
        playButton.setStyle("-fx-font-weight: bold;");
        controller.uiGridPane.add(playButton, 0, 11);
        playButton.setOnAction(e -> {
            // Don't enable the Play button until all the UI selections have been made.
            playButton.setDisable(true);

            // If the driver hits the Play button but the (TeleOp)
            // configuration is not complete, put out an alert and return.
            if (opModeType == OpModeType.TELEOP) {
                String centerSlot = getSelectedRadioButton(slotGroupCenter.first);
                String centerColor = getSelectedRadioButton(colorGroupCenter.first);
                if (centerSlot != null && centerColor != null)
                    createPostIntakeTracking(UIPositionLabel.CENTER.toString(), centerSlot, centerColor);
                else {
                    alertSlotSelectionMissing(UIPositionLabel.CENTER.toString());
                    playButton.setDisable(false);
                    return;
                }

                String leftSlot = getSelectedRadioButton(slotGroupLeft.first);
                String leftColor = getSelectedRadioButton(colorGroupLeft.first);
                if (leftSlot != null && leftColor != null)
                    createPostIntakeTracking(UIPositionLabel.LEFT.toString(), leftSlot, leftColor);
                else {
                    alertSlotSelectionMissing(UIPositionLabel.LEFT.toString());
                    playButton.setDisable(false);
                    return;
                }

                String rightSlot = getSelectedRadioButton(slotGroupRight.first);
                String rightColor = getSelectedRadioButton(colorGroupRight.first);
                if (rightSlot != null && rightColor != null)
                    createPostIntakeTracking(UIPositionLabel.RIGHT.toString(), rightSlot, rightColor);
                else {
                    alertSlotSelectionMissing(UIPositionLabel.RIGHT.toString());
                    playButton.setDisable(false);
                    return;
                }
            }

            // Show the initial orientation (top center shooting for Auto,
            // bottom center intake for TeleOp).
            initializeRevolverDisplay(driverInput);

            rapidFire(); // run the simulation

            //**TODO For now Close the main window; in the real application
            // you may want to support re-configuring and re-running.
            //playButton.setDisable(false);
            //((Stage) playButton.getScene().getWindow()).close();
        });

        pStage.setTitle("FTC Decode: Team 4348 Revolver");
        Scene rootScene = new Scene(root);
        pStage.setScene(rootScene);

        // Set the event handler for the close request
        pStage.setOnCloseRequest(event -> RobotLogCommon.closeLog());

        pStage.show();
    }

    private RadioButton showOpModePopup(Stage pOwner) {
        Stage popupStage = new Stage();
        // Blocks interaction with the main window until this is closed
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initOwner(pOwner);

        // Radio button for the initial orientation of the Revolver:
        // Auto -> top center shoot; TeleOp -> bottom center intake.
        ToggleGroup opModeGroup = new ToggleGroup();
        RadioButton rbAuto = new RadioButton("Auto top shoot");
        rbAuto.setSelected(true);
        rbAuto.setToggleGroup(opModeGroup);

        RadioButton rbTeleOp = new RadioButton("TeleOp bottom intake");
        rbTeleOp.setToggleGroup(opModeGroup);

        // Lay out side-by-side.
        HBox hboxOpMode = new HBox(15); // 15px spacing
        // Indent by 10 pixels on the left
        hboxOpMode.setPadding(new Insets(0, 0, 0, 10));
        hboxOpMode.getChildren().addAll(rbAuto, rbTeleOp);

        Button doneButton = new Button("Done");
        doneButton.setOnAction(e -> {
            // Close the popup window
            ((Stage) doneButton.getScene().getWindow()).close();
        });

        VBox vLayout = new VBox(10); // 10 is the spacing between elements
        vLayout.getChildren().addAll(hboxOpMode, doneButton);
        vLayout.setAlignment(Pos.CENTER);

        Scene popupScene = new Scene(vLayout, 275, 100);
        popupStage.setScene(popupScene);
        popupStage.setTitle("Select an OpMode");
        popupStage.showAndWait();

        return (RadioButton) opModeGroup.getSelectedToggle();
    }

    // For a single RevolverTrackingPosition create a RadioButton for slot selection.
    private Pair<ToggleGroup, HBox> uiSlotSelection(GridPane pRoot, int pUIRowIndex) {
        ToggleGroup slotGroup = new ToggleGroup();
        RadioButton rbSlot0 = new RadioButton(RevolverServo.RevolverSlot.SLOT_0.toString());
        rbSlot0.setMnemonicParsing(false); // show underscore
        rbSlot0.setToggleGroup(slotGroup);

        RadioButton rbSlot1 = new RadioButton(RevolverServo.RevolverSlot.SLOT_1.toString());
        rbSlot1.setMnemonicParsing(false); // show underscore
        rbSlot1.setToggleGroup(slotGroup);

        RadioButton rbSlot2 = new RadioButton(RevolverServo.RevolverSlot.SLOT_2.toString());
        rbSlot2.setMnemonicParsing(false); // show underscore
        rbSlot2.setToggleGroup(slotGroup);

        // Layout side-by-side.
        HBox hboxSlots = new HBox(15); // 15px spacing
        hboxSlots.setPadding(new Insets(0, 0, 0, 20)); // top, right, bottom, left
        hboxSlots.getChildren().addAll(rbSlot0, rbSlot1, rbSlot2);

        // Add to the GridPane at the correct row index for the selected OpMode.
        // gridPane.add(child, columnIndex, rowIndex, columnSpan, rowSpan);
        pRoot.add(hboxSlots, 0, pUIRowIndex, GridPane.REMAINING, 1);

        return Pair.create(slotGroup, hboxSlots);
    }

    private Pair<ToggleGroup, HBox> uiColorSelection(GridPane pRoot, int pUIRowIndex) {

        // Create RadioButtons for artifact color selection.
        ToggleGroup colorGroup = new ToggleGroup();
        RadioButton rbGreen = new RadioButton("Green");
        // Style the text/radio color
        rbGreen.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        rbGreen.setToggleGroup(colorGroup);

        RadioButton rbPurple = new RadioButton("Purple");
        rbPurple.setStyle("-fx-text-fill: purple; -fx-font-weight: bold;");
        rbPurple.setToggleGroup(colorGroup);

        RadioButton rbUnknown = new RadioButton("Unknown");
        rbUnknown.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        rbUnknown.setToggleGroup(colorGroup);

        RadioButton rbEmpty = new RadioButton("Empty");
        rbEmpty.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
        rbEmpty.setToggleGroup(colorGroup);
        rbEmpty.setSelected(true);

        // Layout side-by-side
        HBox hboxColors = new HBox(15); // 15px spacing
        hboxColors.setPadding(new Insets(0, 0, 0, 20)); // top, right, bottom, left
        hboxColors.getChildren().addAll(rbGreen, rbPurple, rbUnknown, rbEmpty);

        // Add to the GridPane at the correct row index for the selected OpMode.
        // gridPane.add(child, columnIndex, rowIndex, columnSpan, rowSpan);
        pRoot.add(hboxColors, 0, pUIRowIndex, GridPane.REMAINING, 1);

        return Pair.create(colorGroup, hboxColors);
    }

    // Add a ChangeListener to the ToggleGroup of a set of 3 slots for a single RevolverTrackingPosition
    // (REAR_VIEW_LEFT, REAR_VIEW_CENTER, or REAR_VIEW_RIGHT). When the driver selects a slot, disable
    // the same slot for the other 2 RevolverTrackingPosition. The goal is that when the driver is ready
    // to press Play, each unique slot will be assigned to a unique RevolverTrackingPosition.
    private void setSlotListener(ToggleGroup pSlotGroup, ToggleGroup pOtherSlotGroup1, ToggleGroup pOtherSlotGroup2) {
        pSlotGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            //!! These tests must be done in this order - oldToggle first.
            if (oldToggle != null) { // if toggling from on to off ...
                RadioButton slotButton = (RadioButton) oldToggle;
                String slotString = slotButton.getText();
                setOtherSlotTogglesOn(slotString, pOtherSlotGroup1, pOtherSlotGroup2);
            }

            //!! No else here.
            if (newToggle != null) { // if toggling from off to on ...
                RadioButton slotButton = (RadioButton) newToggle;
                String slotString = slotButton.getText();
                if (newToggle.isSelected())
                    setOtherSlotTogglesOff(slotString, pOtherSlotGroup1, pOtherSlotGroup2);
            }
        });
    }

    private void setOtherSlotTogglesOff(String pSlotText, ToggleGroup pOtherSlotGroup1, ToggleGroup pOtherSlotGroup2) {
        // Disable the same button in pOtherSlotGroup1.
        for (Toggle toggle : pOtherSlotGroup1.getToggles()) {
            RadioButton selected = (RadioButton) toggle;
            if (selected.getText().equals(pSlotText))
                selected.setDisable(true);
        }

        // Do the same for pOtherSlotGroup2.
        for (Toggle toggle : pOtherSlotGroup2.getToggles()) {
            RadioButton selected = (RadioButton) toggle;
            if (selected.getText().equals(pSlotText))
                selected.setDisable(true);
        }
    }

    private void setOtherSlotTogglesOn(String pSlotText, ToggleGroup pOtherSlotGroup1, ToggleGroup pOtherSlotGroup2) {
        // Enable the same button in pOtherSlotGroup1.
        for (Toggle toggle : pOtherSlotGroup1.getToggles()) {
            RadioButton selected = (RadioButton) toggle;
            if (selected.getText().equals(pSlotText) && selected.isDisabled())
                selected.setDisable(false);
        }

        // Do the same for pOtherSlotGroup2.
        for (Toggle toggle : pOtherSlotGroup2.getToggles()) {
            RadioButton selected = (RadioButton) toggle;
            if (selected.getText().equals(pSlotText) && selected.isDisabled())
                selected.setDisable(false);
        }
    }

    private String getSelectedRadioButton(ToggleGroup pToggleGroup) {
        Toggle selectedToggle = pToggleGroup.getSelectedToggle();
        if (selectedToggle != null) {
            // Cast to RadioButton to get its text
            RadioButton selectedBtn = (RadioButton) selectedToggle;
            System.out.println("Selected radio button: " + selectedBtn.getText());
            return selectedBtn.getText();
        }

        System.out.println("No radio button selected");
        return null;
    }

    private void alertSlotSelectionMissing(String pPosition) {
        // 1. Create the alert with the ERROR type
        Alert alert = new Alert(Alert.AlertType.ERROR);

        // 2. Set the window title and message content
        alert.setTitle("Slot selection error");
        alert.setHeaderText("Cannot run the simulation");
        alert.setContentText("No slot selection for position " + pPosition);

        // 3. Show the dialog
        alert.showAndWait();
    }

    private void createPostIntakeTracking(String pUIPositionLabel, String pSlot, String pColor) {
        RevolverMotion.RevolverTrackingPosition position = RevolverMotion.RevolverTrackingPosition.valueOf(POSITION_PREFIX + pUIPositionLabel);
        RevolverServo.RevolverSlot slot = RevolverServo.RevolverSlot.valueOf(pSlot.toUpperCase());
        String enumColor = pColor.toUpperCase();
        RobotConstantsDecode.ArtifactColor color =
                enumColor.equals("EMPTY") ? RobotConstantsDecode.ArtifactColor.NPOS :
                        RobotConstantsDecode.ArtifactColor.valueOf(pColor.toUpperCase());

        teleopRevolverTracking.put(position, new RevolverMotion.RevolverSlotInfo(color, slot));
        RobotLogCommon.d(TAG, "Revolver contents: position " + position + ", slot " + pSlot + ", color " + pColor);
    }

    // Initialize the display from the user's input.
    // For Auto position artifacts at top center, lower left, lower right.
    // For TELEOP position artifacts at bottom center, upper left, upper right.
    private void initializeRevolverDisplay(DriverInput pDriverInput) {

        switch (pDriverInput.opModeType) {
            case AUTO: {
                Image revolverImage = new Image("file:Files/images/revolver outline 600x600 shoot.png");
                ImageView revolverImageView = new ImageView(revolverImage); // create the ImageView container

                // Add the ImageView to the revolver's Group.
                controller.revolver.getChildren().add(revolverImageView);

                for (Map.Entry<RevolverMotion.RevolverTrackingPosition, RevolverMotion.RevolverSlotInfo> entry : pDriverInput.revolverTracking.entrySet()) {
                    RevolverMotion.RevolverTrackingPosition key = entry.getKey();
                    RevolverMotion.RevolverSlotInfo value = entry.getValue();

                    switch (key) {
                        case REAR_VIEW_CENTER: {
                            formatArtifactForDisplay(controller.top_center, value.color);
                            break;
                        }
                        case REAR_VIEW_LEFT: {
                            formatArtifactForDisplay(controller.bottom_left, value.color);
                            break;
                        }
                        case REAR_VIEW_RIGHT: {
                            formatArtifactForDisplay(controller.bottom_right, value.color);
                            break;
                        }
                        default:
                            throw new AutonomousRobotException(TAG, "Unsupported Revolver position " + key);
                    }
                }
                break;
            }

            case TELEOP: {
                Image revolverImage = new Image("file:Files/images/revolver outline 600x600 intake.png");
                ImageView revolverImageView = new ImageView(revolverImage); // create the ImageView container

                // Add the ImageView to the revolver's Group.
                controller.revolver.getChildren().add(revolverImageView);
                for (Map.Entry<RevolverMotion.RevolverTrackingPosition, RevolverMotion.RevolverSlotInfo> entry : pDriverInput.revolverTracking.entrySet()) {
                    RevolverMotion.RevolverTrackingPosition key = entry.getKey();
                    RevolverMotion.RevolverSlotInfo value = entry.getValue();

                    switch (key) {
                        case REAR_VIEW_CENTER: {
                            formatArtifactForDisplay(controller.bottom_center, value.color);
                            break;
                        }
                        case REAR_VIEW_LEFT: {
                            formatArtifactForDisplay(controller.top_left, value.color);
                            break;
                        }
                        case REAR_VIEW_RIGHT: {
                            formatArtifactForDisplay(controller.top_right, value.color);
                            break;
                        }
                        default:
                            throw new AutonomousRobotException(TAG, "Unsupported Revolver position " + key);
                    }
                }
                break;
            }

            default:
                throw new AutonomousRobotException(TAG, "Unsupported OpModeType " + pDriverInput.opModeType);
        }
    }

    // Draw an artifact with the user's selected color.
    private void formatArtifactForDisplay(Circle pCircle, RobotConstantsDecode.ArtifactColor pColor) {

        switch (pColor) {
            case GREEN -> {
                Image greenArtifact = new Image("file:Files/images/Artifact green 200x200.png");
                pCircle.setFill(new ImagePattern(greenArtifact));
            }

            case PURPLE -> {
                Image purpleArtifact = new Image("file:Files/images/Artifact purple 200x200.png");
                pCircle.setFill(new ImagePattern(purpleArtifact));
            }

            //**TODO rotating text "Unknown"
            case UNKNOWN -> {
                pCircle.setFill(Color.LIGHTGRAY);
                pCircle.setStroke(Color.RED);
                pCircle.setStrokeWidth(10.0);
            }

            //**TODO rotating text "Empty"
            case NPOS -> {
                pCircle.setFill(Color.LIGHTGRAY);
                pCircle.setStroke(Color.BLACK);
                pCircle.setStrokeWidth(10.0);
            }
            default -> throw new AutonomousRobotException(TAG, "Invalid artifact color " + pColor);
        }

        pCircle.setVisible(true);
    }

    // Configure the amimation for rapid fire.
    private void rapidFire() {
        // First rotate the Revolver into the correct shooting
        // position for the pattern entered by the user.

        // Note: for driver input we use the choices "AUTO" and "TELEOP"
        // but internally we use their RevolverMotion.SearchOrder
        // equivalents: IN_PLACE and ON_TRANSITION, respectively.
        RevolverMotion revolver = new RevolverMotion(driverInput.revolverTracking);

        Pair<RevolverMotion.RevolverTrackingPosition, RevolverMotion.RevolverSlotInfo> firstShot = revolver.setRevolverToShootingOrientation(driverInput.artifactPattern,
                driverInput.searchOrder);

        // We need to get the shot order of the artifacts in
        // terms of the JavaFX representation of the Revolver.
        // For example, say that the JavaFX Revolver is in the
        // shooting orientation and the artifacts are in the
        // following positions: green at fx:id="top_center"; purple
        // at fx:id="bottom_left"; purple at fx:id="bottom_right".
        // If our rapid-fire algorithm determines that the purple
        // artifact at fx:id="bottom_left" is the first to shoot,
        // we'll rotate clockwise so that fx:id="bottom_left" is
        // at the top, fx:id="top_center" is at the lower right,
        // and fx:id="bottom_right" is at the lower left. Note
        // that the fx:id(s) of the artifacts no longer coincide
        // with the user's view of the Revolver. But this is how
        // JavaFX works - so we'll have to create a mapping so
        // that we can tell which JavaFX artifact (a Circle with
        // a certain fx:id) is at which position in the user's
        // view of the Revolver.

        // Following the example above: after shooting
        // fx:id="bottom_left" from the top center position, we'd
        // rotate the Revolver so that fx:id="bottom_right" is at
        // the top center and fx:id="top_center" is at the bottom
        // left. We'd fire fx:id="bottom_left" from the top center
        // and rotate again so that fx:id="top_center" is actually
        // at the top center for the final shot.

        // In summary, we need to know the fx:id of the artifact
        // rotated into the top shooting position so that we can
        // animate the shooting motion.

        // We need to know the shot order in terms of fx:id(s).
        // Auto: CW bottom_left -> viewer's center: bottom_left, bottom_right, top_center
        // Auto: top_center -> viewer's center (no movement): top_center, bottom_left, bottom_right
        // Auto: CCW bottom_right -> viewer's center: bottom_right, top_center, bottom_left

        // TeleOp: CW top_left -> viewer's center: top_left, top_right, bottom_center
        // TeleOp: bottom_center -> viewer's center: bottom_center, top_left, top_right
        // TeleOp: CCW top_right -> viewer's center: top_right, bottom_center, top_left

        // Set the amount and direction of the JavFX rotation to the top.
        // Set the shot order.
        //**TODO Need position and slot info for logging List<Pair<RevolverMotion.RevolverTrackingPosition, Circle>>
        double rotationToShootingPosition;
        List<Circle> fxShotOrder = new ArrayList<>();

        switch (driverInput.searchOrder) {
            case IN_PLACE: {
                RobotLogCommon.d(TAG, "Auto: in-place rotation of slot " + firstShot.second.revolverSlot.name() + " at tracking position " + firstShot.first + " with color " + firstShot.second.color + " to the center");

                switch (firstShot.first) {
                    case RevolverMotion.RevolverTrackingPosition.REAR_VIEW_LEFT: {
                        rotationToShootingPosition = 120.0;
                        fxShotOrder.add(controller.bottom_left);
                        fxShotOrder.add(controller.bottom_right);
                        fxShotOrder.add(controller.top_center);
                        break;
                    }
                    case RevolverMotion.RevolverTrackingPosition.REAR_VIEW_CENTER: {
                        // Do nothing here, already positioned at CENTER.
                        rotationToShootingPosition = 0.0;
                        fxShotOrder.add(controller.top_center);
                        fxShotOrder.add(controller.bottom_left);
                        fxShotOrder.add(controller.bottom_right);
                        break;
                    }
                    case RevolverMotion.RevolverTrackingPosition.REAR_VIEW_RIGHT: {
                        rotationToShootingPosition = -120.0;
                        fxShotOrder.add(controller.bottom_right);
                        fxShotOrder.add(controller.top_center);
                        fxShotOrder.add(controller.bottom_left);
                        break;
                    }
                    default: {
                        throw new AutonomousRobotException(TAG, "No such revolver slot " + firstShot.first);
                    }
                }
                break;
            }
            case ON_TRANSITION: {
                RobotLogCommon.d(TAG, "TeleOp: transitional rotation of slot " + firstShot.second.revolverSlot.name() + " at tracking position " + firstShot.first + " with color " + firstShot.second.color + " to the center");

                switch (firstShot.first) {
                    case RevolverMotion.RevolverTrackingPosition.REAR_VIEW_LEFT: {
                        rotationToShootingPosition = 60.0;
                        fxShotOrder.add(controller.top_left);
                        fxShotOrder.add(controller.bottom_center);
                        fxShotOrder.add(controller.top_right);
                        break;
                    }
                    case RevolverMotion.RevolverTrackingPosition.REAR_VIEW_CENTER: {
                        rotationToShootingPosition = 180.0;
                        fxShotOrder.add(controller.bottom_center);
                        fxShotOrder.add(controller.top_right);
                        fxShotOrder.add(controller.top_left);

                        break;
                    }
                    case RevolverMotion.RevolverTrackingPosition.REAR_VIEW_RIGHT: {
                        rotationToShootingPosition = -60.0;
                        fxShotOrder.add(controller.top_right);
                        fxShotOrder.add(controller.top_left);
                        fxShotOrder.add(controller.bottom_center);
                        break;
                    }
                    default: {
                        throw new AutonomousRobotException(TAG, "No such revolver position " + firstShot.first);
                    }
                }
                break;
            }
            default:
                throw new AutonomousRobotException(TAG, "No such search order " + driverInput.searchOrder);
        }

        // According to the shot order set above, rotate all three
        // artifacts to top center and shoot. Note that the only
        // way to make this work was to add each rotation and
        // shot as a SequentialTransition to an enclosing
        // SequentialTransition and then play.
        SequentialTransition rapidFire = new SequentialTransition();
        for (Circle oneArtifact : fxShotOrder) {
            RobotLogCommon.d(TAG, "Rotate artifact with fx:id " + oneArtifact.getId() + " " + rotationToShootingPosition + " degrees to top center");
            //**TODO include " from position " + pos " with slot id " and color " + color.

            rapidFire.getChildren().add(rotateAndShoot(oneArtifact, rotationToShootingPosition));
            rotationToShootingPosition = 120.0; // rotate 120 degrees CW for the second and third shots
        }

        rapidFire.play();
    }

    private SequentialTransition rotateAndShoot(Circle pArtifact, double pRotation) {
        // Rotate the Group with 3 positions.
        RotateTransition rotate1 = new RotateTransition(Duration.millis(2000));
        rotate1.setByAngle(pRotation); // Rotate relative to current position
        rotate1.setOnFinished(event -> {
            // A time-consuming lesson. The artifact (circle) at the upper left
            // in the TeleOp (intake) orientation is a member of the revolver
            // Group and rotates 60 degrees to the top shooting position when
            // the Group rotates. I thought that at that point I could just
            // decrease the Y-position to a point off the Pane to simulate a
            // shot. But not so: "In JavaFX, rotating a Group containing a
            // Circle applies a visual transformation, but it does not modify
            // the underlying coordinates (centerX, centerY) of the child nodes.
            // The circle moves visually, but its local coordinate space remains
            // unchanged relative to its parent group." I tried setting the
            // center X and Y directly, changing the layout X and Y, and others
            // but nothing worked. The only method that did work is the one
            // below.
        });

        // Pause Transition (wait for 1 second) then simulate a shot by
        // moving the artifact at top center off the top of the scene.
        PauseTransition pt = new PauseTransition(Duration.millis(1000));
        pt.setOnFinished(event -> {
            // Remove the node from the Group.
            controller.revolver.getChildren().remove(pArtifact);

            // Add the artifact to the Group's parent so that it shows.
            controller.revolverPane.getChildren().add(pArtifact);

            // Set the X and Y coordinates to the top center shooting position.
            pArtifact.setCenterX(controller.top_center.getCenterX());
            pArtifact.setCenterY(controller.top_center.getCenterY());

            // And move it vertically off the screen.
            TranslateTransition ttoff = new TranslateTransition(Duration.millis(1000), pArtifact);
            ttoff.setByY(-250); // move the node up by a specified amount
            ttoff.play(); // Start the animation

            ttoff.setOnFinished(offEvent -> {
                // We're done with the artifact we've just shot so remove it from
                // the Pane.
                controller.revolverPane.getChildren().remove(pArtifact);
            });
        });

        // Create the SequentialTransition and add the animations.
        // The animations will run in the order they are listed in the constructor
        return new SequentialTransition(controller.revolver, rotate1, pt);
    }

    private static class DriverInput {
        public final OpModeType opModeType;
        public final RevolverMotion.SearchOrder searchOrder;
        public final EnumMap<RevolverMotion.RevolverTrackingPosition, RevolverMotion.RevolverSlotInfo> revolverTracking;
        public final List<RobotConstantsDecode.ArtifactColor> artifactPattern;

        DriverInput(OpModeType pOpModeType, RevolverMotion.SearchOrder pSearchOrder,
                    EnumMap<RevolverMotion.RevolverTrackingPosition, RevolverMotion.RevolverSlotInfo> pRevolverTracking,
                    List<RobotConstantsDecode.ArtifactColor> pArtifactPattern) {
            opModeType = pOpModeType;
            searchOrder = pSearchOrder;
            revolverTracking = pRevolverTracking;
            artifactPattern = pArtifactPattern;
        }

    }

    public static void main(String[] args) {
        launch(args);
    }

}
