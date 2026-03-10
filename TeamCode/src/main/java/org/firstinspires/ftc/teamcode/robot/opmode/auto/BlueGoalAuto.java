package org.firstinspires.ftc.teamcode.robot.opmode.auto;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;
import org.firstinspires.ftc.teamcode.robot.config.Hardware;
import org.firstinspires.ftc.teamcode.robot.config.Config;
import org.firstinspires.ftc.teamcode.robot.opmode.teleop.subsystem.Outtake;

@Autonomous(name = "Blue Goal Auto", group = "Autonomous")
@Configurable // Panels
public class BlueGoalAuto extends OpMode {
    private Hardware hardware;
    public Follower follower; // Pedro Pathing follower instance
    private TelemetryManager panelsTelemetry; // Panels Telemetry instance
    private int pathState; // Current autonomous path state (state machine)
    private Paths paths; // Paths defined in the Paths class
    private Outtake outtake;
    private int shootState = 0;
    private ElapsedTime outtakeTimer = new ElapsedTime();

    @Override
    public void init() {
        hardware = new Hardware(hardwareMap);

        panelsTelemetry = PanelsTelemetry.INSTANCE.getTelemetry();

        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(Config.initialPoseBlueGoalLaunchZoneAuto);
        
        // Slow down the robot for testing
        follower.setMaxPower(0.5); // 50% power max

        paths = new Paths(follower); // Build paths

        // Use the OpMode's default gamepad1 (even if physical one isn't connected yet)
        outtake = new Outtake(hardware, telemetry, gamepad1);
        
        panelsTelemetry.debug("Status", "Initialized");
        panelsTelemetry.update(telemetry);
    }

    @Override
    public void loop() {
        follower.update(); // Update Pedro Pathing
        
        // Continuously track target while following path or standing still
        outtake.track(follower.getPose(), Config.blueGoalPose);
        
        autonomousPathUpdate(); // Update autonomous state machine

        // Log values to Panels and Driver Station
        panelsTelemetry.debug("Path State", pathState);
        panelsTelemetry.debug("X", follower.getPose().getX());
        panelsTelemetry.debug("Y", follower.getPose().getY());
        panelsTelemetry.debug("Heading", follower.getPose().getHeading());
        panelsTelemetry.update(telemetry);
    }

    @Override
    public void start() {
        setPathState(0); // Initialize path state
    }

    public void setPathState(int pState) {
        pathState = pState;
    }

    public boolean executeAutoShoot() {
        double distance = Math.hypot(Config.blueGoalPose.getX() - follower.getPose().getX(), Config.blueGoalPose.getY() - follower.getPose().getY());
        int dynamicRPM = outtake.getTargetRPM(distance);

        if (shootState == 0) {
            outtake.launch(dynamicRPM);
            double currentVel = Math.abs(hardware.outtakeL.getVelocity());
            if (currentVel > dynamicRPM - 500) {
                outtake.openGate();
                hardware.intakeL.setPower(-1);
                hardware.intakeR.setPower(-1);
                
                outtakeTimer.reset();
                shootState = 1;
            } else {
                outtake.closeGate();
                hardware.intakeL.setPower(0);
                hardware.intakeR.setPower(0);
            }
            return false;
        } else if (shootState == 1) {
            outtake.launch(dynamicRPM);
            hardware.intakeL.setPower(-1);
            hardware.intakeR.setPower(-1);
            
            if (outtakeTimer.seconds() > 0.5) {
                outtake.stop();
                outtake.closeGate();
                hardware.intakeL.setPower(0);
                hardware.intakeR.setPower(0);
                shootState = 0;
                return true;
            }
            return false;
        }
        return false;
    }

    public void autonomousPathUpdate() {
        switch (pathState) {
            case 0:
                follower.followPath(paths.startToShootPath);
                setPathState(1);
                break;
            case 1:
                if (!follower.isBusy()) {
                    if (executeAutoShoot()) {
                        follower.followPath(paths.pickupRightToGatePath);
                        setPathState(2);
                    }
                }
                break;
            case 2:
                if (!follower.isBusy()) {
                    follower.followPath(paths.pickupRightToShootPath);
                    setPathState(3);
                }
                break;
            case 3:
                if (!follower.isBusy()) {
                    if (executeAutoShoot()) {
                        follower.followPath(paths.pickupMiddlePath);
                        setPathState(4);
                    }
                }
                break;
            case 4:
                if (!follower.isBusy()) {
                    follower.followPath(paths.pickupMiddleToShootPath);
                    setPathState(5);
                }
                break;
            case 5:
                if (!follower.isBusy()) {
                    if (executeAutoShoot()) {
                        follower.followPath(paths.pickupLeftPath);
                        setPathState(6);
                    }
                }
                break;
            case 6:
                if (!follower.isBusy()) {
                    follower.followPath(paths.pickupLeftToShootPath);
                    setPathState(7);
                }
                break;
            case 7:
                if (!follower.isBusy()) {
                    if (executeAutoShoot()) {
                        follower.followPath(paths.autoParkPath);
                        setPathState(8);
                    }
                }
                break;
            case 8:
                if (!follower.isBusy()) {
                    setPathState(-1); // End of autonomous paths
                }
                break;
        }
    }

    public static class Paths {
        public PathChain startToShootPath;
        public PathChain pickupRightToGatePath;
        public PathChain pickupRightToShootPath;
        public PathChain pickupMiddlePath;
        public PathChain pickupMiddleToShootPath;
        public PathChain pickupLeftPath;
        public PathChain pickupLeftToShootPath;
        public PathChain autoParkPath;

        public Paths(Follower follower) {
            startToShootPath = follower.pathBuilder().addPath(
                            new BezierLine(
                                    Config.initialPoseBlueGoalLaunchZoneAuto,

                                    new Pose(49.736, 84.094)
                            )
                    ).setLinearHeadingInterpolation(Config.initialPoseBlueGoalLaunchZoneAuto.getHeading(), Math.toRadians(180))

                    .build();

            pickupRightToGatePath = follower.pathBuilder().addPath(
                            new BezierCurve(
                                    new Pose(49.736, 84.094),
                                    new Pose(36.736, 82.915),
                                    new Pose(24.491, 87.651),
                                    new Pose(25.868, 77.217),
                                    new Pose(16.302, 74.415)
                            )
                    ).setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))

                    .build();

            pickupRightToShootPath = follower.pathBuilder().addPath(
                            new BezierLine(
                                    new Pose(16.302, 74.415),
                                    new Pose(49.642, 84.038)
                            )
                    ).setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))

                    .build();

            pickupMiddlePath = follower.pathBuilder().addPath(
                            new BezierCurve(
                                    new Pose(49.642, 84.038),
                                    new Pose(51.575, 56.660),
                                    new Pose(23.094, 59.925)
                            )
                    ).setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))

                    .build();

            pickupMiddleToShootPath = follower.pathBuilder().addPath(
                            new BezierLine(
                                    new Pose(23.094, 59.925),
                                    new Pose(49.660, 84.226)
                            )
                    ).setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))

                    .build();

            pickupLeftPath = follower.pathBuilder().addPath(
                            new BezierCurve(
                                    new Pose(49.660, 84.226),
                                    new Pose(55.321, 27.226),
                                    new Pose(24.302, 35.472)
                            )
                    ).setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))

                    .build();

            pickupLeftToShootPath = follower.pathBuilder().addPath(
                            new BezierLine(
                                    new Pose(24.302, 35.472),
                                    new Pose(49.660, 83.925)
                            )
                    ).setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))

                    .build();

            autoParkPath = follower.pathBuilder().addPath(
                            new BezierLine(
                                    new Pose(49.660, 83.925),
                                    Config.finalPoseBlueGoalLaunchZoneAuto
                            )
                    ).setLinearHeadingInterpolation(Math.toRadians(180), Config.finalPoseBlueGoalLaunchZoneAuto.getHeading())

                    .build();
        }
    }
}
