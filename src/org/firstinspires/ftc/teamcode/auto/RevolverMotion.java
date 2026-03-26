package org.firstinspires.ftc.teamcode.auto;


import org.firstinspires.ftc.ftcdevcommon.AutonomousRobotException;
import org.firstinspires.ftc.ftcdevcommon.Pair;
import org.firstinspires.ftc.ftcdevcommon.platform.intellij.RobotLogCommon;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RevolverMotion {

    private static final String TAG = RevolverMotion.class.getSimpleName();

    public enum RevolverTrackingPosition {
        REAR_VIEW_LEFT, REAR_VIEW_CENTER, REAR_VIEW_RIGHT
    }

    public enum RevolverOrientation {
        BOTTOM_CENTER_INTAKE, TOP_CENTER_SHOOT, NPOS
    }

    // The representation of the revolver is from the point of view of an observer
    // standing behind the robot.
    private final EnumMap<RevolverTrackingPosition, RevolverSlotInfo> revolverTracking;

    private final List<RobotConstantsDecode.ArtifactColor> artifactPattern = new ArrayList<>();

    private RevolverOrientation revolverOrientation = RevolverOrientation.NPOS;

    public static final int MAX_ARTIFACTS_IN_REVOLVER = 3;
    private int artifactsInRevolver = 0;

    // Specify the order of search for a position in the revolver.
    // Use ON_TRANSITION_SEARCH_ORDER when switching from intake
    // to shooting and vice-versa. Use IN_PLACE_SEARCH_ORDER when
    // the revolver is already at the bottom (for intake) or the
    // top (for shooting).
    public static final List<RevolverTrackingPosition> ON_TRANSITION_SEARCH_ORDER = new ArrayList<>(Arrays.asList(
            RevolverTrackingPosition.REAR_VIEW_LEFT, RevolverTrackingPosition.REAR_VIEW_RIGHT, RevolverTrackingPosition.REAR_VIEW_CENTER));

    public static final List<RevolverTrackingPosition> IN_PLACE_SEARCH_ORDER = new ArrayList<>(Arrays.asList(
            RevolverTrackingPosition.REAR_VIEW_CENTER, RevolverTrackingPosition.REAR_VIEW_LEFT, RevolverTrackingPosition.REAR_VIEW_RIGHT));

    public enum SearchOrder {IN_PLACE, ON_TRANSITION}

    private final EnumMap<SearchOrder, List<RevolverTrackingPosition>> searchOrderMap = new EnumMap<>(SearchOrder.class);

    public RevolverMotion(EnumMap<RevolverTrackingPosition, RevolverSlotInfo> pInitialRevolverTracking) {
        revolverTracking = new EnumMap<>(pInitialRevolverTracking);
        searchOrderMap.put(SearchOrder.IN_PLACE, IN_PLACE_SEARCH_ORDER);
        searchOrderMap.put(SearchOrder.ON_TRANSITION, ON_TRANSITION_SEARCH_ORDER);

        // Autonomous initializes the revolver with 3 pre-loads
        // but TeleOp assumes that the Revolver is empty at the
        // end of Auto. Set artifactsInRevolver according to
        // these assumptions.
        for (RevolverSlotInfo info : revolverTracking.values())
            if (info.color != RobotConstantsDecode.ArtifactColor.NPOS)
                artifactsInRevolver++;
    }

    // Position the revolver for the first shot by finding the artifact
    // that matches the first position in the pattern or, if no pattern
    // has been set, any artifact, and then moving the artifact to the
    // top of the revolver.

    // From Auto called with the obelisk pattern when the Revolver
    // hardware has already been set to the shooting position: use
    // pSearchOrder = IN_PLACE. Also called from Auto after the intake
    // of artifacts is complete: use pSearchOrder = ON_TRANSITION.

    // From DecodTeleOp called when intake is complete with a full
    // revolver: use pSearchOrder = ON_TRANSITION. Also called from
    // DecodeTeleOp when the aim button is pressed and the revolver
    // is not full: use pSearchOrder = ON_TRANSITION on the first
    // call after intake has been interrupted and pSearchOrder =
    // IN_PLACE if the aim button is released and pressed again.

    //!! For the rapid fire animation return information about the
    // artifact that should be rotated to the top shooting position.
    public Pair<RevolverTrackingPosition, RevolverSlotInfo> setRevolverToShootingOrientation(List<RobotConstantsDecode.ArtifactColor> pPattern,
                                                 SearchOrder pSearchOrder) {
        if (artifactsInRevolver == 0)
            throw new AutonomousRobotException(TAG, "No artifacts in the revolver: cannot set the revolver to its shooting position");

        revolverOrientation = RevolverOrientation.TOP_CENTER_SHOOT;
        RobotLogCommon.d(TAG, "Changing revolver orientation to top center shooting");

        List<Pair<RevolverTrackingPosition, RevolverSlotInfo>> revolverModel = new ArrayList<>();
        List<List<Pair<RevolverTrackingPosition, RevolverSlotInfo>>> revolverCombos = new ArrayList<>();

        // Reformat the EnumMap of RevolverTrackingPositions into a List
        // whose elements will be incorporated into a two-dimensional
        // List of possible artifact combinations.
        if (pSearchOrder == SearchOrder.IN_PLACE) { // already in the shooting position
            revolverModel.add(Pair.create(RevolverTrackingPosition.REAR_VIEW_CENTER, revolverTracking.get(RevolverTrackingPosition.REAR_VIEW_CENTER)));
            revolverModel.add(Pair.create(RevolverTrackingPosition.REAR_VIEW_LEFT, revolverTracking.get(RevolverTrackingPosition.REAR_VIEW_LEFT)));
            revolverModel.add(Pair.create(RevolverTrackingPosition.REAR_VIEW_RIGHT, revolverTracking.get(RevolverTrackingPosition.REAR_VIEW_RIGHT)));
        } else { // in the intake position
            revolverModel.add(Pair.create(RevolverTrackingPosition.REAR_VIEW_LEFT, revolverTracking.get(RevolverTrackingPosition.REAR_VIEW_LEFT)));
            revolverModel.add(Pair.create(RevolverTrackingPosition.REAR_VIEW_CENTER, revolverTracking.get(RevolverTrackingPosition.REAR_VIEW_CENTER)));
            revolverModel.add(Pair.create(RevolverTrackingPosition.REAR_VIEW_RIGHT, revolverTracking.get(RevolverTrackingPosition.REAR_VIEW_RIGHT)));
        }

        // Log the contents of the reformatted revolver model.
        RobotLogCommon.d(TAG, "Revolver model for rapid fire shooting");
        for (Pair<RevolverTrackingPosition, RevolverSlotInfo> onePosition : revolverModel) {
            RobotLogCommon.d(TAG, "Revolver position " + onePosition.toString() +
                    ", color " + onePosition.second.color + ", slot " + onePosition.second.revolverSlot.name());
        }

        // Initialize the combinations with empty entries; they will all be replaced.
        for (int i = 0; i < 3; i++) {
            List<Pair<RevolverTrackingPosition, RevolverSlotInfo>> empty = new ArrayList<>();
            empty.add(Pair.create(RevolverTrackingPosition.REAR_VIEW_CENTER, null));
            empty.add(Pair.create(RevolverTrackingPosition.REAR_VIEW_CENTER, null));
            empty.add(Pair.create(RevolverTrackingPosition.REAR_VIEW_CENTER, null));
            revolverCombos.add(empty);
        }

        // Now create the two-dimensional List of possible artifact combinations.
        for (int comboIndex = 0; comboIndex < 3; comboIndex++) {
            for (int i = 0; i < 3; i++) {
                revolverCombos.get(comboIndex).set(i, revolverModel.get((comboIndex + i) % 3));
            }
        }

        // Log all combos after initialization.
        RobotLogCommon.d(TAG, "Revolver combos after initialization");
        Pair<RevolverTrackingPosition, RevolverSlotInfo> oneCombo;
        for (int comboIndex = 0; comboIndex < 3; comboIndex++) {
            for (int colorIndex = 0; colorIndex < 3; colorIndex++) {
                oneCombo = revolverCombos.get(comboIndex).get(colorIndex);
                RobotLogCommon.d(TAG, "One combo at index " + comboIndex + ", color index " + colorIndex +
                        ", position " + oneCombo.first + ", color " + oneCombo.second.color);
            }
        }

        // To make life easier, fill out the pattern with NPOS (empty)
        // if the pattern is not full.
        List<RobotConstantsDecode.ArtifactColor> localPattern = new ArrayList<>(pPattern);
        for (int i = localPattern.size(); i < 3; i++)
            localPattern.add(RobotConstantsDecode.ArtifactColor.NPOS);
        RobotLogCommon.d(TAG, "Local pattern after padding with NPOS " + localPattern);

        // Move all NPOS (empty) values to the back of each combo.
        for (int comboIndex = 0; comboIndex < 3; comboIndex++) {
            List<Pair<RevolverTrackingPosition, RevolverSlotInfo>> temp = new ArrayList<>();
            List<Pair<RevolverTrackingPosition, RevolverSlotInfo>> empties = new ArrayList<>();

            // Compress non-empty values into a temporary list.
            for (int colorIndex = 0; colorIndex < 3; colorIndex++) {
                if (revolverCombos.get(comboIndex).get(colorIndex).second.color != RobotConstantsDecode.ArtifactColor.NPOS) {
                    temp.add(revolverCombos.get(comboIndex).get(colorIndex));
                } else
                    empties.add(revolverCombos.get(comboIndex).get(colorIndex));
            }

            // Add the empties to the end of the list.
            temp.addAll(empties);
            revolverCombos.set(comboIndex, temp);
        }

        // Log all combos after NPOS shuffling.
        RobotLogCommon.d(TAG, "Revolver combos after NPOS shuffling");
        Pair<RevolverTrackingPosition, RevolverSlotInfo> oneComboPostShuffling;
        for (int comboIndex = 0; comboIndex < 3; comboIndex++) {
            for (int colorIndex = 0; colorIndex < 3; colorIndex++) {
                oneComboPostShuffling = revolverCombos.get(comboIndex).get(colorIndex);
                RobotLogCommon.d(TAG, "One combo at index " + comboIndex + ", color index " + colorIndex +
                        ", position " + oneComboPostShuffling.first + ", color " + oneComboPostShuffling.second.color);
            }
        }

        // Assign scores based on number of matches to the pattern
        double[] revolverScores = {0, 0, 0};
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                // If the combo starts with NPOS, there will be another equivalent combo with the
                // same ordering of non-NPOS elements that we would rather use instead
                if ((j == 0) && (revolverCombos.get(i).get(j).second.color == RobotConstantsDecode.ArtifactColor.NPOS)) {
                    break;
                }

                if (revolverCombos.get(i).get(j).second.color == RobotConstantsDecode.ArtifactColor.NPOS) {
                    continue;
                } else if (revolverCombos.get(i).get(j).second.color == RobotConstantsDecode.ArtifactColor.UNKNOWN) {
                    if (localPattern.get(j) == RobotConstantsDecode.ArtifactColor.PURPLE) {
                        revolverScores[i] += 2.0 / 3.0;
                    } else if (localPattern.get(j) == RobotConstantsDecode.ArtifactColor.GREEN) {
                        revolverScores[i] += 1.0 / 3.0;
                    }
                } else if (revolverCombos.get(i).get(j).second.color == localPattern.get(j)) {
                    revolverScores[i]++;
                }
            }
        }

        // Subtract additional points for adjacency of nonempty elements to break ties
        // ex. we will choose PEP over PPE because there is less adjacency of nonempty elements in PEP
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2; j++) {
                if (revolverCombos.get(i).get(j).second.color != RobotConstantsDecode.ArtifactColor.NPOS &&
                        revolverCombos.get(i).get(j + 1).second.color != RobotConstantsDecode.ArtifactColor.NPOS) {
                    revolverScores[i] -= 1e-6;
                }
            }
        }

        // Log all scores.
        RobotLogCommon.d(TAG, "Scores: 0 " + revolverScores[0] +
                ", 1 " + revolverScores[1] + ", 2 " + revolverScores[2]);

        // Determine best starting slot, i.e. the slot that should
        // be moved to the top of the revolver.
        int indexToBestCombination = 0;
        for (int i = 1; i < 3; i++) {
            if (revolverScores[i] > revolverScores[indexToBestCombination]) {
                indexToBestCombination = i;
            }
        }

        // Log the final selection.
        RobotLogCommon.d(TAG, "Index to best combination " + indexToBestCombination);
        RobotLogCommon.d(TAG, "Best combination:");
        List<Pair<RevolverTrackingPosition, RevolverSlotInfo>> bestCombination = revolverCombos.get(indexToBestCombination);
        for (Pair<RevolverTrackingPosition, RevolverSlotInfo> onePosition : bestCombination) {
            RobotLogCommon.d(TAG, "Revolver position " + onePosition.toString() +
                    ", color " + onePosition.second.color + ", slot " + onePosition.second.revolverSlot);
        }

        // The first entry in the combination is the one that
        // should be moved to the top of the revolver.
        Pair<RevolverTrackingPosition, RevolverSlotInfo> newTop = bestCombination.get(0);

        // Rotate the selected artifact into the shooting position.
        rotateToShootingPosition(newTop.first, pSearchOrder);

        return newTop;
    }

    // RevolverMotion is in charge of the number of artifacts
    // in the Revolver but other classes need to know.
    public int getArtifactCount() {
        return artifactsInRevolver;
    }


    // On the first call waits for the pattern to be set. On subsequent
    // calls returns the pattern.
    public List<RobotConstantsDecode.ArtifactColor> getCurrentPattern() {
        return artifactPattern;
    }

    //!! Not needed in the rapid fire animation.
    // In preparation for intake find an open slot in the revolver
    // and rotate it to bottom center.
    private void rotateToOpenSlotForIntake(SearchOrder pSearchOrder) {
        // ...
        //robot.revolverServo.rotateToTarget(linear, targetVoltage,
        //        RevolverServo.RotationType.INTAKE, RevolverServo.RevolverPIDValues.DEFAULT);
    }

    public void rotateToShootingPosition(RevolverTrackingPosition pFromRevolverPosition, SearchOrder pSearchOrder) {
        RevolverSlotInfo slotToRotate = revolverTracking.get(pFromRevolverPosition);
        double targetVoltage = slotToRotate.revolverSlot.getTargetVoltageTop();
        RobotLogCommon.d(TAG, "Rotate " + slotToRotate.revolverSlot.name() + " at " + pFromRevolverPosition + " to top center with voltage " + targetVoltage);

        rotateToCenter(pFromRevolverPosition, pSearchOrder); // rotate the software model
        printTrackingPositions();

        // Actually rotate here using the targetVoltage. Use the default PID
        // for rotating the revolver into position for the first shot.
        //!! Not needed in the rapid fire animation.
        //robot.revolverServo.rotateToTarget(linear, targetVoltage,
        //        RevolverServo.RotationType.SHOOT, RevolverServo.RevolverPIDValues.DEFAULT);

        // Now the artifact to shoot is at the top center.
        RevolverSlotInfo topCenterSlot = revolverTracking.get(RevolverTrackingPosition.REAR_VIEW_CENTER);
        RobotLogCommon.d(TAG, "Artifact with color " + topCenterSlot.color + " in slot " + topCenterSlot.revolverSlot.name() + " at top center");
    }

    //!! Not needed in the rapid fire animation.
    // Assumes that the shutter and kicker are in position for rapid fire.
    // Assumes that the revolver is in the shooting position.
    public void rotateForRapidFire() {
        // For each position in the Revolver we need to record
        // information about the slots that are not empty so that we
        // can later turn off their LEDs.
        // Map the filtered entries to their values
        List<RevolverSlotInfo> slotsWithAnArtifact = revolverTracking.values().stream()
                // Filter out slots with no artifact.
                .filter(revolverSlotInfo -> revolverSlotInfo.color != RobotConstantsDecode.ArtifactColor.NPOS)
                // Collect the results into a List
                .collect(Collectors.toList());

        // Get the slot at the REAR_VIEW_RIGHT position the revolver.
        // This position will end up at the top after rapid fire.
        RevolverSlotInfo lastSlotToShoot = revolverTracking.get(RevolverTrackingPosition.REAR_VIEW_RIGHT);
        double targetVoltage = lastSlotToShoot.revolverSlot.getTargetVoltageTop();
        RobotLogCommon.d(TAG, "Last slot to shoot in rapid fire " + lastSlotToShoot.revolverSlot.name());
        RobotLogCommon.d(TAG, "Voltage from " + lastSlotToShoot.revolverSlot.name() + " to top " + lastSlotToShoot.revolverSlot.getTargetVoltageTop());

        //!! Not needed in the rapid fire animation.
        // Execute the rapid fire.
        //robot.revolverServo.quickFireRotate(linear, targetVoltage, pRapidFireFar, pRapidFireFarHoodIncrement);

        // Assume that all artifacts were shot.
        artifactsInRevolver = 0;

        // Turn off the LEDs for the shots fired.
        for (RevolverSlotInfo oneSlot : slotsWithAnArtifact) {
          oneSlot.color = RobotConstantsDecode.ArtifactColor.NPOS; // artifact has been shot
        }

        // We need to rotate the software model to match the
        // hardware revolver. For a rotation to the ending
        // position after rapid fire the movement is from RIGHT
        // to CENTER.
        rotateToCenter(RevolverTrackingPosition.REAR_VIEW_RIGHT, SearchOrder.IN_PLACE); // rotate the software model
        RobotLogCommon.d(TAG, "Positions after rapid fire");
        printTrackingPositions();
    }

    // Record the results of a single shot.
    public void registerShotFired() {

        --artifactsInRevolver;

        // Set LED of the registered slot OFF.
        RevolverSlotInfo topCenterSlot = revolverTracking.get(RevolverTrackingPosition.REAR_VIEW_CENTER);

        RobotLogCommon.d(TAG, "Shot artifact with color " + topCenterSlot.color + " from slot " + topCenterSlot.revolverSlot.name() + " at top center");
        RobotLogCommon.d(TAG, "Artifacts remaining in revolver " + artifactsInRevolver);

        topCenterSlot.color = RobotConstantsDecode.ArtifactColor.NPOS; // artifact has been shot
        printTrackingPositions();
    }

    // Find a tracking position that matches the criteria specified
    // in the Predicate pColorPredicate by searching the three
    // possible positions in the specified order.
    public RevolverTrackingPosition getTrackingPosition(List<RevolverTrackingPosition> pSearchOrder, Predicate<RevolverSlotInfo> pColorPredicate) {
        Optional<RevolverTrackingPosition> firstMatchingPosition = pSearchOrder.stream()
                .filter(position -> pColorPredicate.test(revolverTracking.get(position)))
                .findFirst();
        return firstMatchingPosition.orElse(null);
    }

    // Rotate a selected position in the revolver to the center.
    // This rotation is for the software model of the revolver
    // only. The caller is responsible for calling the hardware
    // API to move the actual revolver.
    private void rotateToCenter(RevolverTrackingPosition pRotateFrom, SearchOrder pSearchOrder) {
        RevolverSlotInfo selectedSlot = revolverTracking.get(pRotateFrom);
        RevolverSlotInfo hold;

        switch (pSearchOrder) {
            case IN_PLACE: {
                RobotLogCommon.d(TAG, "In-place rotation of slot " + selectedSlot.revolverSlot.name() + " at tracking position " + pRotateFrom + " with color " + selectedSlot.color + " to the center");

                switch (pRotateFrom) {
                    case REAR_VIEW_LEFT: {
                        // Copy RIGHT to hold; move CENTER to RIGHT, move LEFT to CENTER, move hold to LEFT
                        hold = revolverTracking.get(RevolverTrackingPosition.REAR_VIEW_RIGHT);
                        RevolverSlotInfo center = revolverTracking.get(RevolverTrackingPosition.REAR_VIEW_CENTER);
                        revolverTracking.put(RevolverTrackingPosition.REAR_VIEW_RIGHT, center);
                        RevolverSlotInfo left = revolverTracking.get(RevolverTrackingPosition.REAR_VIEW_LEFT);
                        revolverTracking.put(RevolverTrackingPosition.REAR_VIEW_CENTER, left);
                        revolverTracking.put(RevolverTrackingPosition.REAR_VIEW_LEFT, hold);
                        break;
                    }
                    case REAR_VIEW_CENTER: {
                        // Do nothing here, already positioned at CENTER.
                        break;
                    }
                    case REAR_VIEW_RIGHT: {
                        // Copy LEFT to hold; move CENTER to LEFT, move RIGHT to CENTER, move hold to RIGHT
                        hold = revolverTracking.get(RevolverTrackingPosition.REAR_VIEW_LEFT);
                        RevolverSlotInfo center = revolverTracking.get(RevolverTrackingPosition.REAR_VIEW_CENTER);
                        revolverTracking.put(RevolverTrackingPosition.REAR_VIEW_LEFT, center);
                        RevolverSlotInfo right = revolverTracking.get(RevolverTrackingPosition.REAR_VIEW_RIGHT);
                        revolverTracking.put(RevolverTrackingPosition.REAR_VIEW_CENTER, right);
                        revolverTracking.put(RevolverTrackingPosition.REAR_VIEW_RIGHT, hold);
                        break;
                    }
                    default: {
                        throw new AutonomousRobotException(TAG, "Rotate: no such revolver slot " + pRotateFrom);
                    }
                }
                break;
            }
            case ON_TRANSITION: {
                RobotLogCommon.d(TAG, "Transitional rotation of slot " + selectedSlot.revolverSlot.name() + " at tracking position " + pRotateFrom + " with color " + selectedSlot.color + " to the center");

                switch (pRotateFrom) {
                    case REAR_VIEW_LEFT: {
                        // Copy CENTER to hold; move LEFT to CENTER, no change to RIGHT, move hold to LEFT
                        hold = revolverTracking.get(RevolverTrackingPosition.REAR_VIEW_CENTER);
                        RevolverSlotInfo left = revolverTracking.get(RevolverTrackingPosition.REAR_VIEW_LEFT);
                        revolverTracking.put(RevolverTrackingPosition.REAR_VIEW_CENTER, left);
                        revolverTracking.put(RevolverTrackingPosition.REAR_VIEW_LEFT, hold);
                        break;
                    }
                    case REAR_VIEW_CENTER: {
                        // We're changing the orientation of the Revolver from top center
                        // shoot to bottom center intake, or vice-versa.
                        // CENTER stays the same; copy RIGHT to hold; move LEFT to RIGHT; move HOLD to LEFT
                        hold = revolverTracking.get(RevolverTrackingPosition.REAR_VIEW_RIGHT);
                        RevolverSlotInfo left = revolverTracking.get(RevolverTrackingPosition.REAR_VIEW_LEFT);
                        revolverTracking.put(RevolverTrackingPosition.REAR_VIEW_RIGHT, left);
                        revolverTracking.put(RevolverTrackingPosition.REAR_VIEW_LEFT, hold);
                        break;
                    }
                    case REAR_VIEW_RIGHT: {
                        // Copy CENTER to hold; move RIGHT to CENTER, no change to LEFT, move hold to RIGHT
                        hold = revolverTracking.get(RevolverTrackingPosition.REAR_VIEW_CENTER);
                        RevolverSlotInfo right = revolverTracking.get(RevolverTrackingPosition.REAR_VIEW_RIGHT);
                        revolverTracking.put(RevolverTrackingPosition.REAR_VIEW_CENTER, right);
                        revolverTracking.put(RevolverTrackingPosition.REAR_VIEW_RIGHT, hold);
                        break;
                    }
                    default: {
                        throw new AutonomousRobotException(TAG, "Rotate to center: no such revolver slot " + pRotateFrom);
                    }
                }
                break;
            }
            default:
                throw new AutonomousRobotException(TAG, "Rotate to center: no such search order " + pSearchOrder);
        }
    }

    public void printTrackingPositions() {
        for (Map.Entry<RevolverTrackingPosition, RevolverSlotInfo> entry : revolverTracking.entrySet()) {
            RevolverSlotInfo RevolverSlotInfo = revolverTracking.get(entry.getKey());
            RobotLogCommon.d(TAG, "Revolver slot " + RevolverSlotInfo.revolverSlot.name() + " ");
            RobotLogCommon.d(TAG, "Tracking position " + entry.getKey() + ": " + entry.getValue().color + " ");
        }
    }

    public static class RevolverSlotInfo {
        public RobotConstantsDecode.ArtifactColor color; // mutable
        public final RevolverServo.RevolverSlot revolverSlot;

        public RevolverSlotInfo(RobotConstantsDecode.ArtifactColor pColor, RevolverServo.RevolverSlot pSlot) {
            color = pColor;
            revolverSlot = pSlot;
        }
    }

}
