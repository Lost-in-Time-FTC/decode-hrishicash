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

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;
import org.firstinspires.ftc.teamcode.robot.config.Hardware;

@Autonomous(name = "Blue Auto", group = "Autonomous")
@Configurable // Panels
public class BlueAuto extends OpMode {
    private Hardware hardware;
    public Follower follower; // Pedro Pathing follower instance
    private TelemetryManager panelsTelemetry; // Panels Telemetry instance
    private int pathState; // Current autonomous path state (state machine)
    private Paths paths; // Paths defined in the Paths class

    @Override
    public void init() {
        hardware = new Hardware(hardwareMap);

        panelsTelemetry = PanelsTelemetry.INSTANCE.getTelemetry();

        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(new Pose(24.453, 126.792, Math.toRadians(135)));

        paths = new Paths(follower); // Build paths

        panelsTelemetry.debug("Status", "Initialized");
        panelsTelemetry.update(telemetry);
    }

    @Override
    public void loop() {
        follower.update(); // Update Pedro Pathing
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

    public void autonomousPathUpdate() {
        switch (pathState) {
            case 0:
                follower.followPath(paths.startToShootPath);
                setPathState(1);
                break;
            case 1:
                if (!follower.isBusy()) {
                    // run intake
                    hardware.intakeL.setPower(-1);
                    hardware.intakeR.setPower(-1);

                    follower.followPath(paths.pickupRightToGatePath);

                    // stop intake
                    hardware.intakeL.setPower(0);
                    hardware.intakeR.setPower(0);

                    setPathState(2);
                }
                break;
            case 2:
                if (!follower.isBusy()) {
                    follower.followPath(paths.pickupRightToShootPath);
                    // TODO: shoot
                    setPathState(3);
                }
                break;
            case 3:
                if (!follower.isBusy()) {
                    // run intake
                    hardware.intakeL.setPower(-1);
                    hardware.intakeR.setPower(-1);

                    follower.followPath(paths.pickupMiddlePath);

                    // stop intake
                    hardware.intakeL.setPower(0);
                    hardware.intakeR.setPower(0);

                    setPathState(4);
                }
                break;
            case 4:
                if (!follower.isBusy()) {
                    follower.followPath(paths.pickupMiddleToShootPath);
                    // TODO: shoot
                    setPathState(5);
                }
                break;
            case 5:
                if (!follower.isBusy()) {
                    // run intake
                    hardware.intakeL.setPower(-1);
                    hardware.intakeR.setPower(-1);

                    follower.followPath(paths.pickupLeftPath);

                    // stop intake
                    hardware.intakeL.setPower(0);
                    hardware.intakeR.setPower(0);

                    setPathState(6);
                }
                break;
            case 6:
                if (!follower.isBusy()) {
                    follower.followPath(paths.pickupLeftToShootPath);
                    // TODO: Shoot
                    hardware.runOuttake();
                    setPathState(7);
                }
                break;
            case 7:
                if (!follower.isBusy()) {
                    follower.followPath(paths.autoParkPath);
                    setPathState(8);
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
                                    new Pose(24.453, 126.792),

                                    new Pose(49.736, 84.094)
                            )
                    ).setLinearHeadingInterpolation(Math.toRadians(135), Math.toRadians(180))

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

                                    new Pose(49.660, 72.453)
                            )
                    ).setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))

                    .build();
        }
    }
}
