package org.firstinspires.ftc.teamcode.robot.opmode.auto;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;
import org.firstinspires.ftc.teamcode.robot.config.Config;
import org.firstinspires.ftc.teamcode.robot.config.Hardware;

@Autonomous(name = "Blue Far Auto", group = "Autonomous")
@Configurable
public class BlueFarAuto extends OpMode {
    private Hardware hardware;
    public Follower follower;
    private TelemetryManager panelsTelemetry;
    private Paths paths;

    private double waitStartTime = 0;
    private double waitDuration = 0;

    private int pathState;

    private static final int START_SHOOT = 0;
    private static final int SHOOTING = 1;
    private static final int POST_SHOT_WAIT = 2;
    private static final int DRIVE_TO_PICKUP = 3;
    private static final int WAIT_AFTER_PICKUP = 4;
    private static final int DRIVE_BACK = 5;
    private static final int START_SECOND_SHOT = 6;
    private static final int SECOND_SHOOTING = 7;
    private static final int DONE = 8;

    public void startWait(double seconds) {
        waitStartTime = getRuntime();
        waitDuration = seconds;
    }

    public boolean isWaitDone() {
        return getRuntime() - waitStartTime >= waitDuration;
    }

    @Override
    public void init() {
        hardware = new Hardware(hardwareMap);
        panelsTelemetry = PanelsTelemetry.INSTANCE.getTelemetry();

        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(Config.initialPoseBlueGoalLaunchZoneAuto);

        paths = new Paths(follower);

        panelsTelemetry.debug("Status", "Initialized");
        panelsTelemetry.update(telemetry);
    }

    @Override
    public void start() {
        pathState = START_SHOOT;
    }

    @Override
    public void loop() {
        follower.update();
        autonomousPathUpdate();

        panelsTelemetry.debug("State", pathState);
        panelsTelemetry.debug("X", follower.getPose().getX());
        panelsTelemetry.debug("Y", follower.getPose().getY());
        panelsTelemetry.debug("Heading", follower.getPose().getHeading());
        panelsTelemetry.update(telemetry);
    }

    public void autonomousPathUpdate() {
        switch (pathState) {

            case START_SHOOT:
                hardware.outtakeGate.setPosition(1);
                hardware.runOuttake(2200);
                hardware.intakeL.setPower(-.7);
                hardware.intakeR.setPower(-.7);

                startWait(2.0);
                pathState = SHOOTING;
                break;

            case SHOOTING:
                if (isWaitDone()) {
                    hardware.runOuttake(0);
                    hardware.intakeL.setPower(0);
                    hardware.intakeR.setPower(0);
                    hardware.outtakeGate.setPosition(-0.8);

                    startWait(1.0);
                    pathState = POST_SHOT_WAIT;
                }
                break;

            case POST_SHOT_WAIT:
                if (isWaitDone()) {
                    hardware.intakeL.setPower(-.7);
                    hardware.intakeR.setPower(-.7);

                    follower.followPath(paths.Path1);
                    pathState = DRIVE_TO_PICKUP;
                }
                break;

            case DRIVE_TO_PICKUP:
                if (!follower.isBusy()) {
                    startWait(2.0);
                    pathState = WAIT_AFTER_PICKUP;
                }
                break;

            case WAIT_AFTER_PICKUP:
                if (isWaitDone()) {
                    hardware.intakeL.setPower(0);
                    hardware.intakeR.setPower(0);

                    follower.followPath(paths.Path2);
                    pathState = DRIVE_BACK;
                }
                break;

            case DRIVE_BACK:
                if (!follower.isBusy()) {
                    pathState = START_SECOND_SHOT;
                }
                break;

            case START_SECOND_SHOT:
                hardware.outtakeGate.setPosition(1);
                hardware.runOuttake(2200);
                hardware.intakeL.setPower(-.7);
                hardware.intakeR.setPower(-.7);

                startWait(2.0);
                pathState = SECOND_SHOOTING;
                break;

            case SECOND_SHOOTING:
                if (isWaitDone()) {
                    hardware.runOuttake(0);
                    hardware.intakeL.setPower(0);
                    hardware.intakeR.setPower(0);
                    hardware.outtakeGate.setPosition(-0.8);

                    pathState = DONE;
                }
                break;

            case DONE:
                // Auto finished
                break;
        }
    }

    public static class Paths {
        public PathChain Path1;
        public PathChain Path2;

        public Paths(Follower follower) {
            Path1 = follower.pathBuilder()
                    .addPath(new BezierCurve(
                            new Pose(54.189, 8.000),
                            new Pose(61.887, 37.377),
                            new Pose(20.528, 35.396)
                    ))
                    .setLinearHeadingInterpolation(Math.toRadians(90), Math.toRadians(180))
                    .build();

            Path2 = follower.pathBuilder()
                    .addPath(new BezierCurve(
                            new Pose(20.528, 35.396),
                            new Pose(61.887, 37.377),
                            new Pose(54.189, 8.000)
                    ))
                    .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(90))
                    .build();
        }
    }
}
