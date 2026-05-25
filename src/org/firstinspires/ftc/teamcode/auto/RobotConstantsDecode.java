package org.firstinspires.ftc.teamcode.auto;

import java.util.*;

public class RobotConstantsDecode {

    public enum ArtifactColor {
        GREEN, PURPLE, UNKNOWN, NPOS
    }

    // Obelisk patterns indexed by AprilTag identifiers
    // Tag family: 36h11
    // Obelisk tag id 21: pattern GPP

    //!! For the Revolver simulation an enum is better ...
    public enum ObeliskPattern { GREEN_PURPLE_PURPLE, PURPLE_GREEN_PURPLE, PURPLE_PURPLE_GREEN }

    public static final Map<ObeliskPattern, List<ArtifactColor>> obeliskPatterns;

    static {
        obeliskPatterns = new HashMap<>();
        obeliskPatterns.put(ObeliskPattern.GREEN_PURPLE_PURPLE, new ArrayList<>(Arrays.asList(ArtifactColor.GREEN, ArtifactColor.PURPLE, ArtifactColor.PURPLE)));
        obeliskPatterns.put(ObeliskPattern.PURPLE_GREEN_PURPLE, new ArrayList<>(Arrays.asList(ArtifactColor.PURPLE, ArtifactColor.GREEN, ArtifactColor.PURPLE)));
        obeliskPatterns.put(ObeliskPattern.PURPLE_PURPLE_GREEN, new ArrayList<>(Arrays.asList(ArtifactColor.PURPLE, ArtifactColor.PURPLE, ArtifactColor.GREEN)));
    }

}