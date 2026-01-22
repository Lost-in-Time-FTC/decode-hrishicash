package org.firstinspires.ftc.teamcode.robot.opmode.teleop.subsystem;


import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.robot.config.Hardware;
import com.qualcomm.robotcore.util.ElapsedTime;

public class Outtake {
    private Hardware hardware;
    private Telemetry telemetry;
    private Gamepad gamepad2;
    private double currentVoltage;
    private double pastPos;

    public Outtake(Hardware hardware, Telemetry telemetry, Gamepad gamepad2) {
        this.hardware = hardware;
        this.telemetry = telemetry;
        this.gamepad2 = gamepad2;

    }

    public void run() {

        //basic user controlled stuff

        // PITCH
        if (gamepad2.right_trigger>0.8) {
            hardware.rotateOuttake(-0.8);
        } else if (gamepad2.left_trigger>0.8) {
            hardware.rotateOuttake(0.8);
        } else {
            hardware.rotateOuttake(0);
        }

        // YAW
        if(gamepad2.right_bumper) {
            hardware.outtakeLauncher.setPower(0.8);
            //hardware.outtakeGate.setPosition(-0.8);
            //hardware.rotateOuttake(0.8);
            telemetry.addData("hood position: ", hardware.outtakeLauncherEncoder.getVoltage());
        } else if (gamepad2.left_bumper) {
            hardware.outtakeLauncher.setPower(-0.8);
            //hardware.outtakeGate.setPosition(1);
            //hardware.rotateOuttake(-0.8);
            telemetry.addData("hood position: ", hardware.outtakeLauncherEncoder.getVoltage());
        }else {
            hardware.outtakeLauncher.setPower(0);
            //hardware.rotateOuttake(0);

        }

        // OPEN GATE
        if (gamepad2.dpad_down) {
            hardware.outtakeGate.setPosition(1);
        } else {hardware.outtakeGate.setPosition(-0.8);}

        // LAUNCH
        if (gamepad2.y) {
            //hardware.runOuttake(-1);
            hardware.runOuttake(2200);

        } else if (gamepad2.x) {
           hardware.runOuttake();
        }
        else {hardware.runOuttake(0);}

        /*
        //stand-ins until odo works
        double xPos = 0;
        double yPos = 0;
        double robotHeading = 0; //0 heading is towards main QR code; CCW is pos; in rad;
        double outtakeHeading = 0; //radians off of robot's relative heading

        double xGoal = 132; //in inches, might need to be mm
        double yGoal = 137; //in inches, might need to be mm

        double xDistance = xGoal - xPos;
        double yDistance = yGoal - yPos;

        double absTargetAngle = 90 - Math.atan(yDistance/xDistance);

        double absLauncherAngle = robotHeading + outtakeHeading;

        double finalAngle = absLauncherAngle - absTargetAngle; //should work, pretty much psuedo code
        //I don't know how the axon encoder stuff works sorry about that
        //need to get axon voltage, translate to radians for angle
        //PID necessary?
        */

    }

    /*public ElapsedTime run(ElapsedTime readyTime) {//altered with time
        //outtakeR is top
        //currentVoltage = hardware.myControlHubVoltageSensor.getVoltage();
        //int currentPos = hardware.outtakeL.getCurrentPosition();
        //telemetry.addData("encoder", currentPos);
        //telemetry.update();
        /*if (gamepad2.x) {
            hardware.outtakeL.setPower(1);
            hardware.outtakeR.setPower(0.8);
            //hardware.outtakeR.setPower(1);
        }
        else if (gamepad2.y) {
            hardware.outtakeL.setPower(0.8);
            hardware.outtakeR.setPower(1);
        }
        else {
            readyTime.reset();
            hardware.outtakeL.setPower(0);
            hardware.outtakeR.setPower(0);
        }

        if (gamepad2.right_trigger>0.8) {
            hardware.rotateOuttake(1); //I want this to rotate right, MUST TEST
        } else if (gamepad2.left_trigger>0.8) {
            hardware.rotateOuttake(-1);
        } else {
            hardware.rotateOuttake(0);
        }

        //time-based bar for  outtake gate,  removeifyoudot want
        if (((gamepad2.right_bumper||gamepad2.left_bumper) && hardware.outtakeL.getPower()<0.2) ||
                ((gamepad2.right_bumper||gamepad2.left_bumper) && hardware.outtakeL.getPower()>0.2 && readyTime.seconds()>1.5)) {//altered with time
            hardware.feedOuttake(-1);//inv for controller's sake
        }
        else if (gamepad2.left_trigger>0.8) {
            hardware.feedOuttake(1);
        }
        else {
            hardware.feedOuttake(0);
        }
        return readyTime;//altered with time
    }*/
}
