package org.firstinspires.ftc.teamcode.auto;

import org.firstinspires.ftc.ftcdevcommon.AutonomousRobotException;
import org.firstinspires.ftc.ftcdevcommon.platform.intellij.RobotLogCommon;
import org.firstinspires.ftc.ftcdevcommon.platform.intellij.WorkingDirectory;

import java.util.*;

import static java.lang.Thread.sleep;

public class RevolverMotionTester {
    private static final String TAG = RevolverMotionTester.class.getSimpleName();

    public enum OpModeType {AUTO, TELEOP}

    private enum UserPosition {LEFT, CENTER, RIGHT}

    private static final String POSITION_PREFIX = "REAR_VIEW_";

    private Scanner scanner = new Scanner(System.in);

    private final OpModeType opModeType;
    private final RevolverMotion.SearchOrder searchOrder;

    // Starting contents of the revolver after the pre-loads have been placed.
    // The representation of the revolver is from the point of view of an observer
    // standing behind the robot.
    private final EnumMap<RevolverMotion.RevolverTrackingPosition, RevolverMotion.RevolverSlotInfo> autoRevolverTracking = new EnumMap<>(Map.of(
            RevolverMotion.RevolverTrackingPosition.REAR_VIEW_LEFT, new RevolverMotion.RevolverSlotInfo(RobotConstantsDecode.ArtifactColor.PURPLE, RevolverServo.RevolverSlot.SLOT_2),
            RevolverMotion.RevolverTrackingPosition.REAR_VIEW_CENTER, new RevolverMotion.RevolverSlotInfo(RobotConstantsDecode.ArtifactColor.GREEN, RevolverServo.RevolverSlot.SLOT_0),
            RevolverMotion.RevolverTrackingPosition.REAR_VIEW_RIGHT, new RevolverMotion.RevolverSlotInfo(RobotConstantsDecode.ArtifactColor.PURPLE, RevolverServo.RevolverSlot.SLOT_1)
    ));

    private final EnumMap<RevolverMotion.RevolverTrackingPosition, RevolverMotion.RevolverSlotInfo> teleopRevolverTracking = new EnumMap<>(RevolverMotion.RevolverTrackingPosition.class);

    private final List<RobotConstantsDecode.ArtifactColor> artifactPattern = new ArrayList<>();

    public RevolverMotionTester() throws InterruptedException {

        // Enter the OpModeType: AUTO or TELEOP
        System.out.print("Enter the OpMode type: AUTO or TELEOP ");
        String userOpModeType = scanner.next().toUpperCase();
        try {
            opModeType = OpModeType.valueOf(userOpModeType);
        } catch (IllegalArgumentException e) {
            throw new AutonomousRobotException(TAG, "Invalid OpMode type, try again.");
        }

        RobotLogCommon.d(TAG, "OpMode type " + opModeType);

        // OpModeType is for the user but internally we use SearchOrder.
        searchOrder = opModeType == OpModeType.AUTO ? RevolverMotion.SearchOrder.IN_PLACE : RevolverMotion.SearchOrder.ON_TRANSITION;

        if (opModeType == OpModeType.AUTO) {
            // Use the default preload autoRevolverTracking
        } else {
            // TeleOp - prompt for post-intake slots, positions, and colors. Use intake orientation.
            if (!createPostIntakeTracking(RevolverServo.RevolverSlot.SLOT_0))
                return;

            if (!createPostIntakeTracking(RevolverServo.RevolverSlot.SLOT_1))
                return;

            if (!createPostIntakeTracking(RevolverServo.RevolverSlot.SLOT_2))
                return;
        }

        // Simulate pattern selection.
        System.out.print("Enter the artifact pattern colors: GREEN, PURPLE, UNKNOWN, or NONE ");
        String firstUserColor = scanner.next().toUpperCase();
        if (firstUserColor.equals("NONE"))
            firstUserColor = "NPOS";

        String secondUserColor = scanner.next().toUpperCase();
        if (secondUserColor.equals("NONE"))
            secondUserColor = "NPOS";

        String thirdUserColor = scanner.next().toUpperCase();
        if (thirdUserColor.equals("NONE"))
            thirdUserColor = "NPOS";

        RobotConstantsDecode.ArtifactColor firstColor;
        try {
            firstColor = RobotConstantsDecode.ArtifactColor.valueOf(firstUserColor);
            artifactPattern.add(firstColor);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid pattern color, try again.");
            return;
        }

        RobotConstantsDecode.ArtifactColor secondColor;
        try {
            secondColor = RobotConstantsDecode.ArtifactColor.valueOf(secondUserColor);
            artifactPattern.add(secondColor);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid pattern color, try again.");
            return;
        }

        RobotConstantsDecode.ArtifactColor thirdColor;
        try {
            thirdColor = RobotConstantsDecode.ArtifactColor.valueOf(thirdUserColor);
            artifactPattern.add(thirdColor);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid pattern color, try again.");
            return;
        }

        RobotLogCommon.d(TAG, "Artifact pattern colors " + firstColor + ", " + secondColor + ", " + thirdColor);
    }

    public UserInput getUserInput() {
        EnumMap<RevolverMotion.RevolverTrackingPosition, RevolverMotion.RevolverSlotInfo> revolverTracking =
                opModeType == OpModeType.AUTO ? autoRevolverTracking : teleopRevolverTracking;
        return new UserInput(opModeType, searchOrder, revolverTracking, artifactPattern);
    }

    private boolean createPostIntakeTracking(RevolverServo.RevolverSlot pSlot) {
        RevolverMotion.RevolverTrackingPosition position;

        RobotConstantsDecode.ArtifactColor color;

        System.out.print("Enter the revolver position and color for " + pSlot + " ");
        String userPosition = scanner.next().toUpperCase();
        try {
            UserPosition.valueOf(userPosition);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid position for " + pSlot + ", try again.");
            return false;
        }

        position = RevolverMotion.RevolverTrackingPosition.valueOf(POSITION_PREFIX + userPosition);

        // Make sure the position is not already in the map.
        if (teleopRevolverTracking.containsKey(position)) {
            System.out.println("Duplicate position " + position + ", try again.");
            return false;
        }

        String userColor = scanner.next().toUpperCase();
        try {
            color = RobotConstantsDecode.ArtifactColor.valueOf(userColor);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid color for " + pSlot + ", try again.");
            return false;
        }

        RobotLogCommon.d(TAG, "Revolver contents: slot " + pSlot + ", position " + position + ", color " + color);

        teleopRevolverTracking.put(position, new RevolverMotion.RevolverSlotInfo(color, pSlot));
        return true;
    }

    public static class UserInput {
        public final OpModeType opModeType;
        public final RevolverMotion.SearchOrder searchOrder;
        public final EnumMap<RevolverMotion.RevolverTrackingPosition, RevolverMotion.RevolverSlotInfo> revolverTracking;
        public final List<RobotConstantsDecode.ArtifactColor> artifactPattern;

        UserInput(OpModeType pOpModeType, RevolverMotion.SearchOrder pSearchOrder,
                  EnumMap<RevolverMotion.RevolverTrackingPosition, RevolverMotion.RevolverSlotInfo> pRevolverTracking,
                  List<RobotConstantsDecode.ArtifactColor> pArtifactPattern) {
            opModeType = pOpModeType;
            searchOrder = pSearchOrder;
            revolverTracking = pRevolverTracking;
            artifactPattern = pArtifactPattern;
        }

    }

}

