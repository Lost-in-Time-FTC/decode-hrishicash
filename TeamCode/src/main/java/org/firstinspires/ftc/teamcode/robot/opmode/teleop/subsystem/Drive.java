package org.firstinspires.ftc.teamcode.robot.opmode.teleop.subsystem;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.robot.config.Hardware;

public class Drive {
    private Hardware hardware;
    private Telemetry telemetry;
    private Gamepad gamepad1;

    public Drive(Hardware hardware, Telemetry telemetry, Gamepad gamepad1) {
        this.hardware = hardware;
        this.telemetry = telemetry;
        this.gamepad1 = gamepad1;
    }

    public void run() {
        // Mecanum
        double drive = -gamepad1.left_stick_y; // Inverted Y-axis
        double turn = gamepad1.right_stick_x; //added neg
        double strafe = gamepad1.left_stick_x; //added neg

        // Strafing
        double fL = Range.clip(drive + strafe + turn, -.85, .85);
        double fR = Range.clip(drive - strafe - turn, -.85, .85); //strafe had plus
        double bL = Range.clip(drive - strafe + turn, -.85, .85);
        double bR = Range.clip(drive + strafe - turn, -.85, .85); //strafe had minus

        double rapidMode = 0.9;
        double sniperMode = 0.5;

        // Sniper mode
        if (gamepad1.left_trigger > 0) {
            hardware.fL.setPower(fL * rapidMode * sniperMode);
            hardware.fR.setPower(fR * rapidMode * sniperMode);
            hardware.bL.setPower(bL * rapidMode * sniperMode);
            hardware.bR.setPower(bR * rapidMode * sniperMode);
        }
        // Brakes
        else if (gamepad1.right_trigger > 0) {
            hardware.fL.setPower(fL * 0);
            hardware.fR.setPower(fR * 0);
            hardware.bL.setPower(bL * 0);
            hardware.bR.setPower(bR * 0);

        }
        // Normal drive
        else {
            hardware.fL.setPower(fL * rapidMode);
            hardware.fR.setPower(fR * rapidMode);
            hardware.bL.setPower(bL * rapidMode);
            hardware.bR.setPower(bR * rapidMode);
        }
    }
}
