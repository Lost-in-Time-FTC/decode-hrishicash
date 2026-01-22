package org.firstinspires.ftc.teamcode.robot.opmode.teleop.subsystem;

import static java.lang.Math.abs;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.robot.config.Hardware;

//intake now updated from previous robot
public class Intake {
    private Hardware hardware;
    private Telemetry telemetry;
    private Gamepad gamepad2;
    private boolean intakeToggle = false;
    private boolean intakingIn = true;

    public Intake(Hardware hardware, Telemetry telemetry, Gamepad gamepad2) {
        this.hardware = hardware;
        this.telemetry = telemetry;
        this.gamepad2 = gamepad2;
    }
    public void run() { //kushal wants just one controller
        if(gamepad2.a) {
            hardware.intakeL.setPower(-1);
            hardware.intakeR.setPower(-1);
        }
        else if(gamepad2.b) {
            hardware.intakeL.setPower(1);
            hardware.intakeR.setPower(1);
        }
        else {
            hardware.intakeL.setPower(0);
            hardware.intakeR.setPower(0);
        }
    }
/*
    public void run(Gamepad previousGamepad2, Gamepad currentGamepad2) {
        hardware.intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        try {
            previousGamepad2.copy(currentGamepad2);
            currentGamepad2.copy(gamepad2);
        } catch (Exception e) {}

        if (currentGamepad2.a && !previousGamepad2.a) {
            intakeToggle = !intakeToggle;
            intakingIn = true;

        }
        if (currentGamepad2.b && !previousGamepad2.b) {
            intakeToggle = !intakeToggle;
            intakingIn = false;
        }

        if (intakeToggle) {
            if(intakingIn) {
                hardware.intake.setPower(-.75);
            }
            else {
                hardware.intake.setPower(.75);
            }
        }
        else {
            hardware.intake.setPower(0);
        }

    }*/
}
