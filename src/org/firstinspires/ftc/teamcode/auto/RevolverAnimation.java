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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
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
import java.util.List;
import java.util.Map;

public class RevolverAnimation extends Application {
    private static final String TAG = RevolverAnimation.class.getSimpleName();

    private RevolverController controller;

    // Animation of rapid-fire shooting by pattern.
    // One of the main goals of this animation is to keep the class
    // RevolverMotion as close as possible to the one used with the
    // actual Revolver hardware. Methods that activate the hardware
    // are not called or commented out.
    // The animation also does not include the intake of artifacts.

    //**TODO Merge in the UI that will take the place of RevolverMotionTester.

    //**TODO ?Need replay button

    //**TODO Need labels for slots. The labels should remain horizontal even
    // as the revolver rotates.

    @Override
    public void start(final Stage pStage) throws IOException, InterruptedException {

        String logDirPath = WorkingDirectory.getWorkingDirectory() + RobotConstants.logDir;
        RobotLogCommon.OpenStatus openStatus = RobotLogCommon.initialize(RobotLogCommon.LogIdentifier.AUTO_LOG,
                logDirPath);
        System.out.println("Opened " + RobotLogCommon.LogIdentifier.AUTO_LOG + " with status " + openStatus);

        //**TODO Merge in components of the project DecodeRevolverUI.
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
            controller.rowOneLabel.setText("CENTER");
            leftRowIndex = 5;
            controller.rowTwoLabel.setText("LEFT");
            rightRowIndex = 8;
            controller.rowThreeLabel.setText("RIGHT");
        } else { // must be TeleOp
            leftRowIndex = 2;
            controller.rowOneLabel.setText("LEFT");
            rightRowIndex = 5;
            controller.rowTwoLabel.setText("RIGHT");
            centerRowIndex = 8;
            controller.rowThreeLabel.setText("CENTER");
        }

        //**TODO STOPPED HERE 5/25/26 16:25 ...

        // Note: for driver input we use the choices "AUTO" and "TELEOP"
        // but internally we use their RevolverMotion.SearchOrder
        // equivalents: IN_PLACE and ON_TRANSITION, respectively.
        RevolverMotion revolver = new RevolverMotion(driverInput.revolverTracking);

        // Show the initial orientation (top center shooting for Auto,
        // bottom center intake for TeleOp).
        initializeRevolverDisplay(driverInput);

        // Now show the rapid fire.
        // First rotate the Revolver into the correct shooting
        // position for the pattern entered by the user.
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

        rapidFire.play(); // rapid fire shows here.

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

    // Initialize the display from the user's input.
    // For Auto position artifacts at top center, lower left, lower right.
    // For TELEOP position artifacts at bottom center, upper left, upper right.
    private void initializeRevolverDisplay(RevolverMotionTester.DriverInput pDriverInput) {

        switch (pDriverInput.opModeType) {
            case RevolverMotionTester.OpModeType.AUTO: {
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

            case RevolverMotionTester.OpModeType.TELEOP: {
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

    private static class ArtifactDisplayPosition {
        public final Circle topArtifact;
        public final Circle bottomArtifact;

        public ArtifactDisplayPosition(Circle pTopArtifact, Circle pBottomArtifact) {
            topArtifact = pTopArtifact;
            bottomArtifact = pBottomArtifact;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
