package org.firstinspires.ftc.teamcode.robot.opmode.teleop.subsystem;

import static androidx.core.math.MathUtils.clamp;

import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.robot.config.Hardware;

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

    // adjust hood
    public void pitch() {

    }

    // rotate turret
    public void yaw() {

    }

    public void openGate() {
        hardware.outtakeGate.setPosition(1);
    }

    public void closeGate() {
        hardware.outtakeGate.setPosition(-0.8);
    }

    public void launch(int rpm) {
        hardware.runOuttake(rpm);
    }

    public void launch() {
        hardware.runOuttake();
    }

    public void stop() {
        hardware.runOuttake(0);
    }

    public void track(Pose pose) {
//        hardware.outtakeRotatorRAxon.setPidCoeffs(0.02, 0.0005, 0.0025);

        double xPos = pose.getX();
        double yPos = pose.getY();
        double robotHeading = pose.getHeading(); //0 heading is towards main QR code; CCW is pos; in rad;
        // radians from axon servo
//        double outtakeHeading = hardware.outtakeRotatorREncoder.getVoltage() / 3.3 * 2 * Math.PI - 0.45; // minus offset

        double outtakeHeading = Math.toRadians(hardware.outtakeRotatorRAxon.getTotalRotation());

        Pose blueGoalPose = new Pose(50, 84, Math.toRadians(135));

        double dX = blueGoalPose.getX() - xPos;
        double dY = blueGoalPose.getY() - yPos;

        double absTargetAngle = Math.atan2(dY, dX);

        double absLauncherAngle = robotHeading + outtakeHeading;

        double error = absTargetAngle - absLauncherAngle;
        error = Math.atan2(Math.sin(error), Math.cos(error)); // normalize to [-pi, pi]

        double kP = 3;
        double power = kP * error;
        if (Math.abs(error) < 0.01) power = 0;
        hardware.outtakeRotatorRAxon.setPower(power);
        hardware.outtakeRotatorL.setPower(power);
//        hardware.rotateOuttake(clamp(power, -1, 1));
        telemetry.addData("Is this running?", "PLEASE");
    }

    public void run(Pose pose) {
        hardware.outtakeRotatorRAxon.update();
        track(pose);

        // YAW
        if (gamepad2.right_trigger>0.8) {
            hardware.rotateOuttake(-0.8);
        } else if (gamepad2.left_trigger>0.8) {
            hardware.rotateOuttake(0.8);
        } else {
            hardware.rotateOuttake(0);
        }

        // PITCH (outtake hood)
        if(gamepad2.right_bumper) {
            hardware.outtakeHood.setPower(0.8);
            //hardware.outtakeGate.setPosition(-0.8);
            //hardware.rotateOuttake(0.8);
            telemetry.addData("hood position: ", hardware.outtakeHoodEncoder.getVoltage());
        } else if (gamepad2.left_bumper) {
            hardware.outtakeHood.setPower(-0.8);
            //hardware.outtakeGate.setPosition(1);
            //hardware.rotateOuttake(-0.8);
            telemetry.addData("hood position: ", hardware.outtakeHoodEncoder.getVoltage());
        }else {
            hardware.outtakeHood.setPower(0);
            //hardware.rotateOuttake(0);
        }

        // OPEN GATE
        if (gamepad2.dpad_down) {
            openGate();
        } else {
            closeGate();
        }

        // LAUNCH
        if (gamepad2.y) {
            launch(2200);
        } else if (gamepad2.x) {
           launch();
        } else {
            stop();
        }
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
