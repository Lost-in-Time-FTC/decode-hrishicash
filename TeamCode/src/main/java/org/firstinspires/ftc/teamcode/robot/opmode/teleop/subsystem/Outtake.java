package org.firstinspires.ftc.teamcode.robot.opmode.teleop.subsystem;

import static androidx.core.math.MathUtils.clamp;

import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.robot.config.Hardware;
import org.firstinspires.ftc.teamcode.robot.config.PID;
import com.bylazar.configurables.annotations.Configurable;

@Configurable
public class Outtake {
    private Hardware hardware;
    private Telemetry telemetry;
    private Gamepad gamepad1;
    private double currentVoltage;
    private double pastPos;
    public static double turretKp = 0.007; // Slightly backed off from 0.0065 to stop oscillation
    public static double turretKi = 0.0; // Stronger integral to overcome friction stall
    public static double turretKd = 0.0; // Set back to a stable braking value
    public static double turretMinPower = 0.045; // Cut in half! 2 servos produce double the minimum power

    // Tracking variables for live movement detection
    private double lastX = 0, lastY = 0, lastH = 0;
    private long lastMoveTime = 0;

    // Flywheel PID constants
    double flywheelKp = 0.005;
    double flywheelKi = 0.0;
    double flywheelKd = 0.0;
    double flywheelKf = 0.00038; // Baseline power to maintain velocity

    private PID flywheelPID;

    double previousError = 0;
    ElapsedTime timer = new ElapsedTime();
    double LEFT_LIMIT  = Math.toRadians(340);
    double LEFT_LIMIT_DEG = 340;
    double RIGHT_LIMIT = Math.toRadians(-375);
    double RIGHT_LIMIT_DEG = -375;
    double TURRET_TO_SERVO_GEAR_RATIO = 166.0 / 43.0;
    public Outtake(Hardware hardware, Telemetry telemetry, Gamepad gamepad1) {
        this.hardware = hardware;
        this.telemetry = telemetry;
        this.gamepad1 = gamepad1;
        this.flywheelPID = new PID(flywheelKp, flywheelKi, flywheelKd, flywheelKf);

        // Disable internal PID in RTPAxon to let goal tracking take control
        this.hardware.outtakeRotatorRAxon.setRtp(false);
    }

    public void openGate() {
        hardware.outtakeGate.setPosition(-.25);
    }

    public void closeGate() {
        hardware.outtakeGate.setPosition(.5);
    }

    public void launch(int targetRPM) {
        double currentVelocity = hardware.outtakeL.getVelocity(); // Assuming this returns ticks/sec or similar
        // Convert target RPM to ticks/sec if Hardware.runOuttake logic is desired here
        // Or handle the conversion within the PID or Hardware class.
        // For now, let's stick to the requested structure:
        double power = flywheelPID.out(targetRPM, currentVelocity);
        power = Math.min(1.0, Math.max(-1.0, power)); // Clamp power between -1 and 1
        hardware.outtakeL.setPower(power);
        hardware.outtakeR.setPower(-power);
        telemetry.addData("Target RPM", targetRPM);
        telemetry.addData("Current Velocity", currentVelocity);
        telemetry.addData("Outtake Power", power);
    }

    public void launch() {
        hardware.runOuttake();
    }

    public void stop() {
        hardware.runOuttake(0);
    }

    public void track(Pose robotPose, Pose goalPose) {
        // ALWAYS update the continuous encoder first, otherwise Auto mode gets stuck returning 0 and oscillating endlessly
        hardware.outtakeRotatorRAxon.update();

        double xPos = robotPose.getX();
        double yPos = robotPose.getY();
        double robotHeadingRad = robotPose.getHeading(); // radians from Pedro

        // convert robot heading to degrees for scaling purposes, necessary due to mechanical quirks
        double robotHeadingDeg = Math.toDegrees(robotHeadingRad);

        // Turret heading from servo, continuous in degrees
        double outtakeHeadingDeg = hardware.outtakeRotatorRAxon.getTotalRotation();

    // CALCULATE BOTH RATIOS TO FIND THE ERROR
        double turretHeadingIfEncoderOnServo = outtakeHeadingDeg * (43.0 / 166.0);
        double turretHeadingIfEncoderOnTurret = outtakeHeadingDeg; // 1:1

        // USERS: Swap this variable if your tracking is "Distance Inaccurate"
        double finalTurretHeading = turretHeadingIfEncoderOnServo;

        // --- PREDICTIVE AIMING (SHOOTING ON THE MOVE) ---
        double dtSec = Math.max(timer.seconds(), 0.001);
        
        // Calculate velocity (inches per second)
        double velX = 0, velY = 0;
        if (dtSec < 0.2) { // Only trust recent, valid dt to prevent jumping after stopping
            velX = (xPos - lastX) / dtSec;
            velY = (yPos - lastY) / dtSec;
        }

        // "Time of Flight" multiplier. The longer the distance, the more we need to lead the target.
        // E.g., at 60 inches, maybe it takes 0.5 seconds for ring to reach goal. 
        // We offset the goal by (RobotVelocity * TimeOfFlight)
        double distanceToGoal = Math.hypot(goalPose.getX() - xPos, goalPose.getY() - yPos);
        double estimatedTimeOfFlight = distanceToGoal * 0.015; // TUNABLE: increase if it shoots behind while moving
        
        // Calculate the "Offset Goal" by shifting it in the opposite direction of our travel
        // (If we are moving right, we must shoot right of the goal to compensate)
        double predictiveGoalX = goalPose.getX() - (velX * estimatedTimeOfFlight);
        double predictiveGoalY = goalPose.getY() - (velY * estimatedTimeOfFlight);

        // target position (NOW USING PREDICTIVE COORDS)
        double targetAngleRad = Math.atan2(predictiveGoalY - yPos, predictiveGoalX - xPos);
        double targetAngleDeg = Math.toDegrees(targetAngleRad);

        // absolute turret angle in field coordinates (degrees)
        double absLauncherAngleDeg = robotHeadingDeg + finalTurretHeading;

        // Find the target angle relative to the robot's heading
        double relativeTargetAngleDeg = targetAngleDeg - robotHeadingDeg;
        relativeTargetAngleDeg = ((relativeTargetAngleDeg + 180) % 360 + 360) % 360 - 180; // wrap to [-180, 180]
        
        // Software limits: restrict target turret angle to +/- 90 degrees on each side
        relativeTargetAngleDeg = Math.max(-90.0, Math.min(90.0, relativeTargetAngleDeg));

        // Error is the difference between the clamped relative target and the current turret heading
        double errorDeg = relativeTargetAngleDeg - finalTurretHeading;

        double deltaTime = Math.max(timer.seconds(), 0.001); // prevent div by zero
        double derivative = (errorDeg - previousError) / deltaTime;

        // PD formula
        double power = (turretKp * errorDeg) + (turretKd * derivative);

        // Friction Compensation
        if (Math.abs(errorDeg) > 0.4) { // 0.4 deg deadzone for stability
            power += Math.signum(errorDeg) * turretMinPower;
        } else {
            power = 0;
        }

        // --- MISSION CRITICAL DIAGNOSTICS (V12.0) ---
        long now = System.currentTimeMillis();
        boolean posMoving = (Math.abs(xPos - lastX) > 0.01 || Math.abs(yPos - lastY) > 0.01 || Math.abs(robotHeadingDeg - lastH) > 0.01);
        if (posMoving) lastMoveTime = now;
        boolean isOdomAlive = (now - lastMoveTime < 500); // Moved in the last 0.5 seconds

        double rawVolts = hardware.outtakeRotatorREncoder.getVoltage();
        boolean isEncoderAlive = (rawVolts > 0.1 && rawVolts < 3.2); // Typical Axon sensor range

        telemetry.addData("yPos", yPos);
        telemetry.addData("xPos", xPos);
        telemetry.addData("goalPose", goalPose);
        telemetry.addData("robotHeadingDeg", robotHeadingDeg);
        telemetry.addData("targetAngleDeg", targetAngleDeg);
        telemetry.addData("absLauncherAngleDeg", absLauncherAngleDeg);
        telemetry.addData("errorDeg", errorDeg);
        telemetry.addData("V12_6_TURRET_DEG", String.format("%.1f", finalTurretHeading));
        telemetry.addData("V12_7_PWR_SIGN", String.format("%.2f", power));

        lastX = xPos; lastY = yPos; lastH = robotHeadingDeg;
        pastPos = outtakeHeadingDeg;

        previousError = errorDeg;
        timer.reset();

        power = clamp(power, -1, 1);

        // --- HARD LIMIT PREVENTION ---
        // If the encoder thinks we are past 90 degrees, physically cut power in that direction
        if (finalTurretHeading > 90 && power > 0) {
            power = 0;
        } else if (finalTurretHeading < -90 && power < 0) {
            power = 0;
        }

        // --- POLARITY CORRECTION ---
        // If the turret moves AWAY from the goal, change the sign of 'power' below
        hardware.rotateOuttake(power);
    }

    public void run(Pose robotPose, Pose goalPose) {
        hardware.outtakeRotatorRAxon.update();

        // YAW
//        if (gamepad1.right_trigger>0.8) {
//            hardware.rotateOuttake(-0.8);
//        } else if (gamepad1.left_trigger>0.8) {
//            hardware.rotateOuttake(0.8);
//        } else {
//            hardware.rotateOuttake(0);
//      }

        // --- V12 MANUAL POLARITY TEST ---
        if (gamepad1.dpad_left) {
            hardware.rotateOuttake(0.4); // Should turn LEFT (CCW)
            telemetry.addData("V12_TEST", "Pulsing LEFT (CCW)");
        } else if (gamepad1.dpad_right) {
            hardware.rotateOuttake(-0.4); // Should turn RIGHT (CW)
            telemetry.addData("V12_TEST", "Pulsing RIGHT (CW)");
        } else if (gamepad1.right_trigger > 0.5) {
            hardware.rotateOuttake(0.4);
            telemetry.addData("!! MANUAL OVERRIDE !!", "Rotating at 0.4 power");
        } else {
            track(robotPose, goalPose);
        }

        // PITCH (outtake hood)
        if(gamepad1.right_bumper) {
//            hardware.outtakeHood.setPower(0.8);
            hardware.outtakeHood.setPosition(hardware.outtakeHood.getPosition() - 0.1);
            //hardware.rotateOuttake(0.8);
            telemetry.addData("hood position: ", hardware.outtakeHoodEncoder.getVoltage());
        } else if (gamepad1.left_bumper) {
            hardware.outtakeHood.setPosition(hardware.outtakeHood.getPosition() + 0.1);
            //hardware.outtakeGate.setPosition(1);
            //hardware.rotateOuttake(-0.8);
            telemetry.addData("hood position: ", hardware.outtakeHoodEncoder.getVoltage());
        } else {
//            hardware.outtakeHood.setPosition(0);
            //hardware.rotateOuttake(0);
        }

        // OPEN GATE
        //if (gamepad1.dpad_down) {
        //openGate();
        // } else {
        //     closeGate();
        // }
        // MANUAL TURRET RESET
        // Press B to re-center the turret's encoder software if it gets lost
        if (gamepad1.b) {
            hardware.outtakeRotatorRAxon.forceResetTotalRotation();
            previousError = 0;
        }
        // LAUNCH AND AUTO-FEED
        double distance = Math.hypot(goalPose.getX() - robotPose.getX(), goalPose.getY() - robotPose.getY());
        int dynamicRPM = getTargetRPM(distance);

        if (gamepad1.y) {
            launch(dynamicRPM);
            double currentVel = Math.abs(hardware.outtakeL.getVelocity());
            // Wait until the shooter is close to the target velocity (200 threshold accommodates speed drop)
            if (currentVel > dynamicRPM - 500) {
                openGate();
                hardware.intakeL.setPower(-1);
                hardware.intakeR.setPower(-1);
            } else {
                closeGate();
                hardware.intakeL.setPower(0);
                hardware.intakeR.setPower(0);
            }
        } else if (gamepad1.x) {
            launch();
            double currentVel = Math.abs(hardware.outtakeL.getVelocity());
            // Wait until the shooter is close to max speed (~2300 ticks/sec)
            if (currentVel > 2100) {
                openGate();
                hardware.intakeL.setPower(-1);
                hardware.intakeR.setPower(-1);
            } else {
                closeGate();
                hardware.intakeL.setPower(0);
                hardware.intakeR.setPower(0);
            }
        } else {
            stop();
            closeGate();
        }

        telemetry.addData("Distance to Goal", distance);
        telemetry.addData("Dynamic Target RPM", dynamicRPM);
    }

    public int getTargetRPM(double distance) {
        // Linear interpolation or a lookup table can be used here.
        // Formula: RPM = (slope * distance) + intercept
        // This needs to be tuned based on your physical testing!
        double slope = 18; // Example: increase 10 RPM per inch
        double intercept = 300.0; // Example: 1200 RPM at 0 distance

        return (int) (slope * distance + intercept);
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
