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

    private final RevolverController controller;

    private boolean updatingProgrammatically = false; // for slot RadioButtons

    public RevolverUI(RevolverController pController) {
        controller = pController;
    }

    public DriverInput runUI(String pSelectedOpMode) {
        // Show the artifact selection combo box first.
        // 1. Create the ComboBox.
        ComboBox<String> artifactCombo = new ComboBox<>();
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
        controller.patternHBox.setSpacing(10);
        controller.patternHBox.getChildren().add(artifactCombo);

        Pair<ToggleGroup, HBox> slotGroupCenter;
        Pair<ToggleGroup, HBox> slotGroupLeft;
        Pair<ToggleGroup, HBox> slotGroupRight;

        Pair<ToggleGroup, HBox> colorGroupCenter;
        Pair<ToggleGroup, HBox> colorGroupLeft;
        Pair<ToggleGroup, HBox> colorGroupRight;

        // Variables to hold the UI responses.
        OpModeType opModeType;
        RevolverMotion.SearchOrder searchOrder;

        // If the driver has selected the radio button for "Auto top
        // shoot" then the UI for slot and color selection will be
        // laid out in the order CENTER, LEFT, RIGHT - but remember:
        // the UI is not active for Auto because the positions are
        // fixed. If the user selects "TeleOp bottom intake" then
        // the order will be LEFT, RIGHT, CENTER and the UI will be
        // active.
        Text uiInstructions;
        String opModeLabel = controller.opModeLabel.getText();
        if (pSelectedOpMode.equals("Auto top shoot")) {
            opModeType = OpModeType.AUTO;
            controller.opModeLabel.setText(opModeLabel + "Auto");

            // Use the default preload autoRevolverTracking.
            searchOrder = RevolverMotion.SearchOrder.IN_PLACE;

            uiInstructions = new Text("""
                    For Auto the user interface is not active.
                    Press Play to run the animation.""");

            controller.firstPositionLabel.setText(UIPositionLabel.CENTER.toString());
            controller.secondPositionLabel.setText(UIPositionLabel.LEFT.toString());
            controller.thirdPositionLabel.setText(UIPositionLabel.RIGHT.toString());

            // Slot selection.
            slotGroupCenter = Pair.create(uiSlotSelection(controller.firstSlotHBox), controller.firstSlotHBox);
            slotGroupLeft = Pair.create(uiSlotSelection(controller.secondSlotHBox), controller.secondSlotHBox);
            slotGroupRight = Pair.create(uiSlotSelection(controller.thirdSlotHBox), controller.thirdSlotHBox);

            // Color Selection.
            colorGroupCenter = Pair.create(uiColorSelection(controller.firstColorHBox), controller.firstColorHBox);
            colorGroupLeft = Pair.create(uiColorSelection(controller.secondColorHBox), controller.secondColorHBox);
            colorGroupRight = Pair.create(uiColorSelection(controller.thirdColorHBox), controller.thirdColorHBox);

            // No need to set listeners for Auto; slot selection is disabled.

            // Hide the TeleOp reset button.
            controller.resetTeleOpUIButton.setVisible(false); //

            // Since slot and color selections are fixed in our
            // standard setup for the Decode game, disable their
            // HBox containers.
            slotGroupCenter.second.setDisable(true);
            slotGroupLeft.second.setDisable(true);
            slotGroupRight.second.setDisable(true);

            colorGroupCenter.second.setDisable(true);
            colorGroupLeft.second.setDisable(true);
            colorGroupRight.second.setDisable(true);

        } else { // must be TeleOp
            opModeType = OpModeType.TELEOP;
            controller.opModeLabel.setText(opModeLabel + "TeleOp");

            searchOrder = RevolverMotion.SearchOrder.ON_TRANSITION;

            uiInstructions = new Text("""
                    For TeleOp the user interface is active.
                    Select a Revolver slot and a color for each position.
                    Press Play to run the animation.""");
            controller.firstPositionLabel.setText(UIPositionLabel.LEFT.toString());
            controller.secondPositionLabel.setText(UIPositionLabel.RIGHT.toString());
            controller.thirdPositionLabel.setText(UIPositionLabel.CENTER.toString());

            // Slot selection.
            slotGroupLeft = Pair.create(uiSlotSelection(controller.firstSlotHBox), controller.firstSlotHBox);
            slotGroupRight = Pair.create(uiSlotSelection(controller.secondSlotHBox), controller.secondSlotHBox);
            slotGroupCenter = Pair.create(uiSlotSelection(controller.thirdSlotHBox), controller.thirdSlotHBox);

            // Color Selection.
            colorGroupLeft = Pair.create(uiColorSelection(controller.firstColorHBox), controller.firstColorHBox);
            colorGroupRight = Pair.create(uiColorSelection(controller.secondColorHBox), controller.secondColorHBox);
            colorGroupCenter = Pair.create(uiColorSelection(controller.thirdColorHBox), controller.thirdColorHBox);

            // Set the listeners that ensure that a slot can only be selected once.
            setSlotListener(slotGroupCenter.first, slotGroupLeft.first, slotGroupRight.first);
            setSlotListener(slotGroupLeft.first, slotGroupCenter.first, slotGroupRight.first);
            setSlotListener(slotGroupRight.first, slotGroupCenter.first, slotGroupLeft.first);

            // If the driver hits the TeleOp reset button then re-enable the
            // RadioButtons for the slots.
            controller.resetTeleOpUIButton.setOnAction(e -> {
                slotGroupCenter.second.setDisable(false); // enable the enclosing HBox
                slotGroupLeft.second.setDisable(false);
                slotGroupRight.second.setDisable(false);

                // Enable all slot radio buttons.
                enableSlotRadioButtons(slotGroupCenter.first);
                enableSlotRadioButtons(slotGroupLeft.first);
                enableSlotRadioButtons(slotGroupRight.first);
            });
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

        //**TODO Access to the RevolverPane doesn't belong here; return uiInstructions.
        // Center the user inteface instructions in the Revolver Pane on the left.
        // Bind text X position: (PaneWidth / 2) - (TextWidth / 2)
        uiInstructions.setFont(Font.font("Arial", 16));
        uiInstructions.xProperty().bind(controller.revolverPane.widthProperty().divide(2).subtract(uiInstructions.getLayoutBounds().getWidth() / 2));

        // Bind text Y position: (PaneHeight / 2) + (TextHeight / 4) to adjust for baseline
        uiInstructions.yProperty().bind(controller.revolverPane.heightProperty().divide(2).add(uiInstructions.getLayoutBounds().getHeight() / 4));
        controller.revolverPane.getChildren().add(uiInstructions);

        // Now gather all of the driver input.
        return new DriverInput(opModeType, uiInstructions, searchOrder, patternColors,
                slotGroupCenter.first, slotGroupCenter.first, slotGroupRight.first,
                colorGroupCenter.first, colorGroupLeft.first, colorGroupRight.first);
    }

    // For a single RevolverTrackingPosition create a RadioButton for slot selection.
    private ToggleGroup uiSlotSelection(HBox pRowGridBox) {
        ToggleGroup slotGroup = new ToggleGroup();
        RadioButton rbSlot0 = new RadioButton(RevolverServo.RevolverSlot.SLOT_0.toString());
        rbSlot0.setMnemonicParsing(false); // show underscore
        rbSlot0.setFont(Font.font("Arial", 14));
        rbSlot0.setToggleGroup(slotGroup);

        RadioButton rbSlot1 = new RadioButton(RevolverServo.RevolverSlot.SLOT_1.toString());
        rbSlot1.setMnemonicParsing(false); // show underscore
        rbSlot1.setFont(Font.font("Arial", 14));
        rbSlot1.setToggleGroup(slotGroup);

        RadioButton rbSlot2 = new RadioButton(RevolverServo.RevolverSlot.SLOT_2.toString());
        rbSlot2.setMnemonicParsing(false); // show underscore
        rbSlot2.setFont(Font.font("Arial", 14));
        rbSlot2.setToggleGroup(slotGroup);

        // Layout side-by-side.
        pRowGridBox.setSpacing(15);
        pRowGridBox.setPadding(new Insets(0, 0, 0, 20)); // top, right, bottom, left
        pRowGridBox.getChildren().addAll(rbSlot0, rbSlot1, rbSlot2);

        return slotGroup;
    }

    private ToggleGroup uiColorSelection(HBox pColorGridBox) {

        // Create RadioButtons for artifact color selection.
        ToggleGroup colorGroup = new ToggleGroup();
        RadioButton rbGreen = new RadioButton("Green");
        // Style the text/radio color
        rbGreen.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        rbGreen.setStyle("-fx-text-fill: green;");
        rbGreen.setToggleGroup(colorGroup);

        RadioButton rbPurple = new RadioButton("Purple");
        rbPurple.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        rbPurple.setStyle("-fx-text-fill: purple;");
        rbPurple.setToggleGroup(colorGroup);

        RadioButton rbUnknown = new RadioButton("Unknown");
        rbUnknown.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        rbUnknown.setStyle("-fx-text-fill: red;");
        rbUnknown.setToggleGroup(colorGroup);

        RadioButton rbEmpty = new RadioButton("Empty");
        rbEmpty.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        rbEmpty.setStyle("-fx-text-fill: black;");
        rbEmpty.setToggleGroup(colorGroup);
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
            if (updatingProgrammatically) {
                return; // Ignore the event while the flag is active
            }

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

    private void enableSlotRadioButtons(ToggleGroup pSlotGroup) {
        updatingProgrammatically = true; // temporarily disable listener to change values programmatically

        for (Toggle toggle : pSlotGroup.getToggles()) {
            RadioButton oneButton = (RadioButton) toggle;
            oneButton.setDisable(false);
        }

        updatingProgrammatically = false;
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
