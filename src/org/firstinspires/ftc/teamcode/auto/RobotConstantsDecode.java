package org.firstinspires.ftc.teamcode.auto;

import java.util.*;

public class RobotConstantsDecode {

    public enum ArtifactColor {
        GREEN, PURPLE, UNKNOWN, NPOS
    }

    // Obelisk patterns indexed by AprilTag identifiers
    // Tag family: 36h11
    // Obelisk tag id 21: pattern GPP
    public static final Integer obeliskAprilTagGPP = Integer.valueOf(21);

    // Obelisk tag id 22: pattern PGP
    public static final Integer obeliskAprilTagPGP = Integer.valueOf(22);

    // Obelisk tag id 23: pattern PPG
    public static final Integer obeliskAprilTagPPG = Integer.valueOf(23);

    public static final Map<Integer, List<ArtifactColor>> obeliskPatterns;

    static {
        obeliskPatterns = new HashMap<>();
        obeliskPatterns.put(obeliskAprilTagGPP, new ArrayList<>(Arrays.asList(ArtifactColor.GREEN, ArtifactColor.PURPLE, ArtifactColor.PURPLE)));
        obeliskPatterns.put(obeliskAprilTagPGP, new ArrayList<>(Arrays.asList(ArtifactColor.PURPLE, ArtifactColor.GREEN, ArtifactColor.PURPLE)));
        obeliskPatterns.put(obeliskAprilTagPPG, new ArrayList<>(Arrays.asList(ArtifactColor.PURPLE, ArtifactColor.PURPLE, ArtifactColor.GREEN)));
    }

}