package org.firstinspires.ftc.teamcode.robot.opmode.teleop.subsystem;

import static androidx.core.math.MathUtils.clamp;

import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.robot.config.Hardware;

public class Outtake {
    private Hardware hardware;
    private Telemetry telemetry;
    private Gamepad gamepad2;
    private double currentVoltage;
    private double pastPos;
    double turretKp = 0.008;
    double turretKd = 0.002;
    double turretKi = 0;

    double integral = 0;
    double previousError = 0;
    ElapsedTime timer = new ElapsedTime();
    double LEFT_LIMIT  = Math.toRadians(340);
    double LEFT_LIMIT_DEG = 340;
    double RIGHT_LIMIT = Math.toRadians(-375);
    double RIGHT_LIMIT_DEG = -375;
    double TURRET_TO_SERVO_GEAR_RATIO = 166.0 / 42.0;
    public Outtake(Hardware hardware, Telemetry telemetry, Gamepad gamepad2) {
        this.hardware = hardware;
        this.telemetry = telemetry;
        this.gamepad2 = gamepad2;
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

    public void track(Pose robotPose, Pose goalPose) {
        double xPos = robotPose.getX();
        double yPos = robotPose.getY();
        double robotHeadingRad = robotPose.getHeading(); // radians from Pedro

        // convert robot heading to degrees for scaling purposes, necessary due to mechanical quirks
        double robotHeadingDeg = Math.toDegrees(robotHeadingRad);

        // Turret heading from servo, continuous in degrees
        double outtakeHeadingDeg = hardware.outtakeRotatorRAxon.getTotalRotation();
        double turretHeadingDeg = outtakeHeadingDeg * 1/TURRET_TO_SERVO_GEAR_RATIO;

        // target position
        double targetAngleRad = Math.atan2(goalPose.getY() - yPos, goalPose.getX() - xPos);
        double targetAngleDeg = Math.toDegrees(targetAngleRad);

        // absolute turret angle in field coordinates (degrees)
        double absLauncherAngleDeg = robotHeadingDeg + turretHeadingDeg;

        // error in degrees, wrapped to [-180, 180] for shortest rotation
        double errorDeg = targetAngleDeg - absLauncherAngleDeg;
        errorDeg = ((errorDeg + 180) % 360 + 360) % 360 - 180; // wrap to [-180, 180]

        double deltaTime = Math.max(timer.seconds(), 0.001); // prevent div by zero
        double derivative = (errorDeg - previousError) / deltaTime;
        if (!((outtakeHeadingDeg >= LEFT_LIMIT_DEG && errorDeg > 0) || (outtakeHeadingDeg <= RIGHT_LIMIT_DEG && errorDeg < 0))) {
            integral += errorDeg * deltaTime;
        }

        double power = turretKp * errorDeg + turretKd * integral + turretKi * derivative;

        // limit rotation on turret
        if (outtakeHeadingDeg >= LEFT_LIMIT_DEG && power > 0) power = 0;
        if (outtakeHeadingDeg <= RIGHT_LIMIT_DEG && power < 0) power = 0;

        previousError = errorDeg;
        timer.reset();

        power = clamp(power, -1, 1);

        hardware.rotateOuttake(power);

        telemetry.addData("Outtake Power", power);
        telemetry.addData("Error (deg)", errorDeg);
        telemetry.addData("Turret Heading", outtakeHeadingDeg);
        telemetry.addData("Target Angle", targetAngleDeg);
    }

    public void run(Pose robotPose, Pose goalPose) {
        hardware.outtakeRotatorRAxon.update();

        // YAW
//        if (gamepad2.right_trigger>0.8) {
//            hardware.rotateOuttake(-0.8);
//        } else if (gamepad2.left_trigger>0.8) {
//            hardware.rotateOuttake(0.8);
//        } else {
//            hardware.rotateOuttake(0);
//        }

        track(robotPose, goalPose); // CANNOT HAVE THIS WHILE YAW IS UNCOMMENTED

        // PITCH (outtake hood)
        if(gamepad2.right_bumper) {
//            hardware.outtakeHood.setPower(0.8);
            hardware.outtakeHood.setPosition(hardware.outtakeHood.getPosition() - 0.1);
            //hardware.rotateOuttake(0.8);
            telemetry.addData("hood position: ", hardware.outtakeHoodEncoder.getVoltage());
        } else if (gamepad2.left_bumper) {
            hardware.outtakeHood.setPosition(hardware.outtakeHood.getPosition() + 0.1);
            //hardware.outtakeGate.setPosition(1);
            //hardware.rotateOuttake(-0.8);
            telemetry.addData("hood position: ", hardware.outtakeHoodEncoder.getVoltage());
        } else {
//            hardware.outtakeHood.setPosition(0);
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
