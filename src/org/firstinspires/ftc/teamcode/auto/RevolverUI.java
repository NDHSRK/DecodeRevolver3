package org.firstinspires.ftc.teamcode.auto;

import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.firstinspires.ftc.ftcdevcommon.Pair;

import java.util.ArrayList;
import java.util.List;

public class RevolverUI {

    public enum OpModeType {AUTO, TELEOP}

    private enum UIPositionLabel {LEFT, CENTER, RIGHT}

    // Variables to hold the UI responses.
    private final ComboBox<String> artifactCombo = new ComboBox<>();
    private final OpModeType opModeType;
    private final RevolverMotion.SearchOrder searchOrder;
    private final Text uiInstructions;

    private final Pair<ToggleGroup, HBox> slotGroupCenter;
    private final Pair<ToggleGroup, HBox> slotGroupLeft;
    private final Pair<ToggleGroup, HBox> slotGroupRight;

    private final Pair<ToggleGroup, HBox> colorGroupCenter;
    private final Pair<ToggleGroup, HBox> colorGroupLeft;
    private final Pair<ToggleGroup, HBox> colorGroupRight;

    public RevolverUI(RevolverController pController, String pSelectedOpMode) {
        // Show the artifact selection combo box first.
        artifactCombo.setPrefWidth(Region.USE_COMPUTED_SIZE);

        // 2. Add three choices.
        artifactCombo.getItems().addAll(RobotConstantsDecode.ObeliskPattern.GREEN_PURPLE_PURPLE.toString(),
                RobotConstantsDecode.ObeliskPattern.PURPLE_GREEN_PURPLE.toString(),
                RobotConstantsDecode.ObeliskPattern.PURPLE_PURPLE_GREEN.toString());

        // 3. Set PURPLE_GREEN_PURPLE as the default.
        artifactCombo.setValue(RobotConstantsDecode.ObeliskPattern.PURPLE_GREEN_PURPLE.toString());

        // To get the correct spacing between the label "Artifact pattern"
        // and the ComboBox with the patterns I had to put both into an
        // HBox.
        pController.patternHBox.setSpacing(10);
        pController.patternHBox.getChildren().add(artifactCombo);

        // If the driver has selected the radio button for "Auto top
        // shoot" then the UI for slot and color selection will be
        // laid out in the order CENTER, LEFT, RIGHT - but remember:
        // the UI is not active for Auto because the positions are
        // fixed. If the user selects "TeleOp bottom intake" then
        // the order will be LEFT, RIGHT, CENTER and the UI will be
        // active.
        String opModeLabel = pController.opModeLabel.getText();
        if (pSelectedOpMode.equals("Auto top shoot")) {
            opModeType = OpModeType.AUTO;
            pController.opModeLabel.setText(opModeLabel + "Auto");

            // Use the default preload autoRevolverTracking.
            searchOrder = RevolverMotion.SearchOrder.IN_PLACE;

            uiInstructions = new Text("""
                    For Auto the user interface is not active.
                    Press Play to run the animation.""");

            pController.firstPositionLabel.setText(UIPositionLabel.CENTER.toString());
            pController.secondPositionLabel.setText(UIPositionLabel.LEFT.toString());
            pController.thirdPositionLabel.setText(UIPositionLabel.RIGHT.toString());

            // For Auto set the default slot and color selections according to the
            // 4348 convention for the Decode game. The driver may not change these:
            // CENTER, SLOT_0, GREEN
            // LEFT, SLOT_2, PURPLE
            // RIGHT, SLOT_1, PURPLE

            // Slot selection.
            slotGroupCenter = Pair.create(uiSlotSelection(pController.firstSlotHBox, RevolverServo.RevolverSlot.SLOT_0), pController.firstSlotHBox);
            slotGroupLeft = Pair.create(uiSlotSelection(pController.secondSlotHBox, RevolverServo.RevolverSlot.SLOT_2), pController.secondSlotHBox);
            slotGroupRight = Pair.create(uiSlotSelection(pController.thirdSlotHBox, RevolverServo.RevolverSlot.SLOT_1), pController.thirdSlotHBox);

            // Color Selection.
            colorGroupCenter = Pair.create(uiColorSelection(pController.firstColorHBox, RobotConstantsDecode.ArtifactColor.GREEN), pController.firstColorHBox);
            colorGroupLeft = Pair.create(uiColorSelection(pController.secondColorHBox, RobotConstantsDecode.ArtifactColor.PURPLE), pController.secondColorHBox);
            colorGroupRight = Pair.create(uiColorSelection(pController.thirdColorHBox, RobotConstantsDecode.ArtifactColor.PURPLE), pController.thirdColorHBox);

            // No need to set listeners for Auto; slot selection is disabled.
            // Ans, since slot and color selections are fixed in our standard
            // setup for the Decode game, disable their HBox containers.
            slotGroupCenter.second.setDisable(true);
            slotGroupLeft.second.setDisable(true);
            slotGroupRight.second.setDisable(true);

            colorGroupCenter.second.setDisable(true);
            colorGroupLeft.second.setDisable(true);
            colorGroupRight.second.setDisable(true);

        } else { // must be TeleOp
            opModeType = OpModeType.TELEOP;
            pController.opModeLabel.setText(opModeLabel + "TeleOp");

            searchOrder = RevolverMotion.SearchOrder.ON_TRANSITION;

            uiInstructions = new Text("""
                    For TeleOp the user interface is active.
                    Select a Revolver slot and a color for each position.
                    Press Play to run the animation.""");

            pController.firstPositionLabel.setText(UIPositionLabel.LEFT.toString());
            pController.secondPositionLabel.setText(UIPositionLabel.RIGHT.toString());
            pController.thirdPositionLabel.setText(UIPositionLabel.CENTER.toString());

            // For TeleOp set the default slot and color selections; the driver can
            // change these.
            // Slot selection.
            slotGroupLeft = Pair.create(uiSlotSelection(pController.firstSlotHBox, RevolverServo.RevolverSlot.SLOT_2), pController.firstSlotHBox);
            slotGroupRight = Pair.create(uiSlotSelection(pController.secondSlotHBox, RevolverServo.RevolverSlot.SLOT_1), pController.secondSlotHBox);
            slotGroupCenter = Pair.create(uiSlotSelection(pController.thirdSlotHBox, RevolverServo.RevolverSlot.SLOT_0), pController.thirdSlotHBox);

            // Color Selection; set all defaults to "Empty".
            colorGroupLeft = Pair.create(uiColorSelection(pController.firstColorHBox, RobotConstantsDecode.ArtifactColor.NPOS), pController.firstColorHBox);
            colorGroupRight = Pair.create(uiColorSelection(pController.secondColorHBox, RobotConstantsDecode.ArtifactColor.NPOS), pController.secondColorHBox);
            colorGroupCenter = Pair.create(uiColorSelection(pController.thirdColorHBox, RobotConstantsDecode.ArtifactColor.NPOS), pController.thirdColorHBox);

            // Set the listeners that ensure that a slot can only be selected once.
            setSlotListener(slotGroupCenter.first, slotGroupLeft.first, slotGroupRight.first);
            setSlotListener(slotGroupLeft.first, slotGroupCenter.first, slotGroupRight.first);
            setSlotListener(slotGroupRight.first, slotGroupCenter.first, slotGroupLeft.first);
        }

        // Center the user interface instructions in the Revolver Pane on the left.
        // Bind text X position: (PaneWidth / 2) - (TextWidth / 2)
        uiInstructions.setFont(Font.font("Arial", 16));
        uiInstructions.xProperty().bind(pController.revolverPane.widthProperty().divide(2).subtract(uiInstructions.getLayoutBounds().getWidth() / 2));

        // Bind text Y position: (PaneHeight / 2) + (TextHeight / 4) to adjust for baseline
        uiInstructions.yProperty().bind(pController.revolverPane.heightProperty().divide(2).add(uiInstructions.getLayoutBounds().getHeight() / 4));
        pController.revolverPane.getChildren().add(uiInstructions);
    }

    // It only makes sense to call this after the Play button has been pressed.
    public DriverInput getDriverInput() {
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

        // Now gather all of the driver input.
        return new DriverInput(opModeType, uiInstructions, searchOrder, patternColors,
                slotGroupCenter.first, slotGroupLeft.first, slotGroupRight.first,
                colorGroupCenter.first, colorGroupLeft.first, colorGroupRight.first);
    }

    // For a single RevolverTrackingPosition create a RadioButton for slot selection.
    private ToggleGroup uiSlotSelection(HBox pRowGridBox, RevolverServo.RevolverSlot pDefaultSlot) {
        ToggleGroup slotGroup = new ToggleGroup();
        RadioButton rbSlot0 = new RadioButton(RevolverServo.RevolverSlot.SLOT_0.toString());
        rbSlot0.setMnemonicParsing(false); // show underscore
        rbSlot0.setFont(Font.font("Arial", 14));
        rbSlot0.setToggleGroup(slotGroup);
        if (pDefaultSlot == RevolverServo.RevolverSlot.SLOT_0)
            rbSlot0.setSelected(true);

        RadioButton rbSlot1 = new RadioButton(RevolverServo.RevolverSlot.SLOT_1.toString());
        rbSlot1.setMnemonicParsing(false); // show underscore
        rbSlot1.setFont(Font.font("Arial", 14));
        rbSlot1.setToggleGroup(slotGroup);
        if (pDefaultSlot == RevolverServo.RevolverSlot.SLOT_1)
            rbSlot1.setSelected(true);

        RadioButton rbSlot2 = new RadioButton(RevolverServo.RevolverSlot.SLOT_2.toString());
        rbSlot2.setMnemonicParsing(false); // show underscore
        rbSlot2.setFont(Font.font("Arial", 14));
        rbSlot2.setToggleGroup(slotGroup);
        if (pDefaultSlot == RevolverServo.RevolverSlot.SLOT_2)
            rbSlot2.setSelected(true);

        // Layout side-by-side.
        pRowGridBox.setSpacing(15);
        pRowGridBox.setPadding(new Insets(0, 0, 0, 20)); // top, right, bottom, left
        pRowGridBox.getChildren().addAll(rbSlot0, rbSlot1, rbSlot2);

        return slotGroup;
    }

    private ToggleGroup uiColorSelection(HBox pColorGridBox, RobotConstantsDecode.ArtifactColor pDefaultColor) {

        // Create RadioButtons for artifact color selection.
        ToggleGroup colorGroup = new ToggleGroup();
        RadioButton rbGreen = new RadioButton("Green");
        // Style the text/radio color
        rbGreen.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        rbGreen.setStyle("-fx-text-fill: green;");
        rbGreen.setToggleGroup(colorGroup);
        if (pDefaultColor == RobotConstantsDecode.ArtifactColor.GREEN)
            rbGreen.setSelected(true);

        RadioButton rbPurple = new RadioButton("Purple");
        rbPurple.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        rbPurple.setStyle("-fx-text-fill: purple;");
        rbPurple.setToggleGroup(colorGroup);
        if (pDefaultColor == RobotConstantsDecode.ArtifactColor.PURPLE)
            rbPurple.setSelected(true);

        RadioButton rbUnknown = new RadioButton("Unknown");
        rbUnknown.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        rbUnknown.setStyle("-fx-text-fill: red;");
        rbUnknown.setToggleGroup(colorGroup);
        if (pDefaultColor == RobotConstantsDecode.ArtifactColor.UNKNOWN)
            rbUnknown.setSelected(true);

        RadioButton rbEmpty = new RadioButton("Empty");
        rbEmpty.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        rbEmpty.setStyle("-fx-text-fill: black;");
        rbEmpty.setToggleGroup(colorGroup);
        if (pDefaultColor == RobotConstantsDecode.ArtifactColor.NPOS)
            rbEmpty.setSelected(true);

        // Layout side-by-side
        pColorGridBox.setSpacing(15);
        pColorGridBox.setPadding(new Insets(0, 0, 0, 20)); // top, right, bottom, left
        pColorGridBox.getChildren().addAll(rbGreen, rbPurple, rbUnknown, rbEmpty);

        return colorGroup;
    }

    // Add a ChangeListener to the ToggleGroup of a set of 3 slots for a single RevolverTrackingPosition
    // (REAR_VIEW_LEFT, REAR_VIEW_CENTER, or REAR_VIEW_RIGHT). When the driver selects a slot, disable
    // the same slot for the other 2 RevolverTrackingPosition. The goal is that when the driver is ready
    // to press Play, each unique slot will be assigned to a unique RevolverTrackingPosition.
    private void setSlotListener(ToggleGroup pSlotGroup, ToggleGroup pOtherSlotGroup1, ToggleGroup pOtherSlotGroup2) {
        pSlotGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            // When the driver selects a radio button for a slot,
            // deselect the button for the same slot in the other
            // two rows.
            if (newToggle != null) { // if toggling from off to on ...
                RadioButton slotButton = (RadioButton) newToggle;
                String slotString = slotButton.getText();
                if (newToggle.isSelected())
                    deselectOtherSlotToggles(slotString, pOtherSlotGroup1, pOtherSlotGroup2);
            }
        });
    }

    private void deselectOtherSlotToggles(String pSlotText, ToggleGroup pOtherSlotGroup1, ToggleGroup pOtherSlotGroup2) {
        // Disable the same button in pOtherSlotGroup1.
        for (Toggle toggle : pOtherSlotGroup1.getToggles()) {
            RadioButton selected = (RadioButton) toggle;
            if (selected.getText().equals(pSlotText))
                selected.setSelected(false);
        }

        // Do the same for pOtherSlotGroup2.
        for (Toggle toggle : pOtherSlotGroup2.getToggles()) {
            RadioButton selected = (RadioButton) toggle;
            if (selected.getText().equals(pSlotText))
                selected.setSelected(false);
        }
    }

    public static class DriverInput {
        public final OpModeType opModeType;
        public final Text uiInstructions;
        public final RevolverMotion.SearchOrder searchOrder;
        public final List<RobotConstantsDecode.ArtifactColor> artifactPattern;
        public final ToggleGroup slotToggleCenter;
        public final ToggleGroup slotToggleLeft;
        public final ToggleGroup slotToggleRight;
        public final ToggleGroup colorToggleCenter;
        public final ToggleGroup colorToggleLeft;
        public final ToggleGroup colorToggleRight;

        DriverInput(OpModeType pOpModeType, Text pUiInstructions, RevolverMotion.SearchOrder pSearchOrder,
                    List<RobotConstantsDecode.ArtifactColor> pArtifactPattern,
                    ToggleGroup pSlotToggleCenter, ToggleGroup pSlotToggleLeft, ToggleGroup pSlotToggleRight,
                    ToggleGroup pColorToggleCenter, ToggleGroup pColorToggleLeft, ToggleGroup pColorToggleRight) {
            opModeType = pOpModeType;
            uiInstructions = pUiInstructions;
            searchOrder = pSearchOrder;
            artifactPattern = pArtifactPattern;
            slotToggleCenter = pSlotToggleCenter;
            slotToggleLeft = pSlotToggleLeft;
            slotToggleRight = pSlotToggleRight;
            colorToggleCenter = pColorToggleCenter;
            colorToggleLeft = pColorToggleLeft;
            colorToggleRight = pColorToggleRight;
        }

    }
}
