package org.firstinspires.ftc.teamcode.robot.opmode.teleop;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;
import org.firstinspires.ftc.teamcode.robot.config.Hardware;
import org.firstinspires.ftc.teamcode.robot.opmode.teleop.subsystem.Drive;
import org.firstinspires.ftc.teamcode.robot.opmode.teleop.subsystem.Intake;
import org.firstinspires.ftc.teamcode.robot.opmode.teleop.subsystem.Outtake;

@TeleOp(name = "Tele", group = "Iterative OpMode")
public class Tele extends OpMode {
    private final ElapsedTime runtime = new ElapsedTime();
    private ElapsedTime outtakeTime = new ElapsedTime();//altered with time
    private Hardware hardware;
    private Drive drive;
    private Intake intake;
    private Outtake outtake;
    private Gamepad currentGamepad2;
    private Gamepad previousGamepad2;
    private Follower follower;
    private boolean alliance = false; // false = blue; true = red;

    @Override
    public void init() {
        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(new Pose(0, 0, 0));
        follower.update();

        currentGamepad2 = new Gamepad();
        previousGamepad2 = new Gamepad();

        hardware = new Hardware(hardwareMap);

        drive = new Drive(hardware, telemetry, gamepad1);
        intake = new Intake(hardware, telemetry, gamepad2);
        outtake = new Outtake(hardware, telemetry, gamepad2);

        if (gamepad1.right_bumper) {
            alliance = false;
        }

        if (gamepad1.left_bumper) {
            alliance = true;
        }

        telemetry.addData("Status", "Initialized");
        telemetry.addData("Alliance", (alliance) ? "Red" : "Blue");
        telemetry.addData("outtakeRotatorREncoderVoltage", hardware.outtakeRotatorRAxon.getTotalRotation());
        telemetry.update();
    }

    @Override
    public void start() {
        runtime.reset();
    }

    @Override
    public void loop() {
        follower.update();
        Pose pose = follower.getPose();

        drive.run();
        intake.run();
        outtake.run(pose);
        telemetry.addData("Pose:", pose.getAsVector());
        hardware.printEncoders(telemetry);
    }
}
