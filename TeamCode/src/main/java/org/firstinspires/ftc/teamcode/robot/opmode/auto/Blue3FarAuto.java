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
import org.firstinspires.ftc.teamcode.robot.config.Config;

@Autonomous(name = "Blue 3 Far Auto", group = "Autonomous")
@Configurable
public class Blue3FarAuto extends OpMode {
    private Hardware hardware;
    private TelemetryManager panelsTelemetry; // Panels Telemetry instance
    private static final int START_SHOOT = 0;
    private static final int SHOOTING = 1;
    private static final int POST_SHOT_WAIT = 2;
    private static final int DONE = 8;
    private double waitStartTime = 0;
    private double waitDuration = 0;
    private int state;
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


        panelsTelemetry.debug("Status", "Initialized");
        panelsTelemetry.update(telemetry);
    }

    @Override
    public void loop() {
        // Log values to Panels and Driver Station
        panelsTelemetry.update(telemetry);
        switch (state) {
            case START_SHOOT:
                hardware.outtakeGate.setPosition(1);
                hardware.runOuttake(2200);
                hardware.intakeL.setPower(-.7);
                hardware.intakeR.setPower(-.7);

                startWait(2.0);
                state = SHOOTING;
                break;

            case SHOOTING:
                if (isWaitDone()) {
                    hardware.runOuttake(0);
                    hardware.intakeL.setPower(0);
                    hardware.intakeR.setPower(0);
                    hardware.outtakeGate.setPosition(-0.8);

                    startWait(1.0);
                    state = POST_SHOT_WAIT;
                }
                break;

            case POST_SHOT_WAIT:
                if (isWaitDone()) {
                    hardware.intakeL.setPower(-.7);
                    hardware.intakeR.setPower(-.7);
                    state = DONE;
                }
                break;

            case DONE:
                break;
        }
    }

    @Override
    public void start() {
        state = START_SHOOT;
    }

}
