package org.firstinspires.ftc.teamcode.auto;

public class RevolverServo {

    private final static double VOLTS_PER_ROTATION = 3.3;

    // Enumeration for each labeled slot of the Revolver and the
    // voltage associated with each slot.
    public enum RevolverSlot {
        SLOT_0(0), SLOT_1(1), SLOT_2(2); // parameters: (numeric id for slot)

        private static final double slot0Top = 1.947;
        private static final double slotOffset = VOLTS_PER_ROTATION / 3;
        private static final double TopBottomOffset = VOLTS_PER_ROTATION / 2;

        private final double targetVoltageTop;
        private final double targetVoltageBottom;

        RevolverSlot(int pSlotId) {
            // top target
            double volt = slot0Top + slotOffset * pSlotId;
            targetVoltageTop = RevolverServo.getSafeVoltage(volt);

            // bottom target
            volt = slot0Top + TopBottomOffset + slotOffset * pSlotId;
            targetVoltageBottom = RevolverServo.getSafeVoltage(volt);
        }

        public double getTargetVoltageTop() {
            return targetVoltageTop;
        }

        public double getTargetVoltageBottom() {
            return targetVoltageBottom;
        }
    }

    private static double getSafeVoltage(double pVoltage) {
        double safeVolt = pVoltage % VOLTS_PER_ROTATION;
        if (safeVolt < 0) {
            safeVolt += VOLTS_PER_ROTATION;
        }
        return safeVolt;
    }

}
