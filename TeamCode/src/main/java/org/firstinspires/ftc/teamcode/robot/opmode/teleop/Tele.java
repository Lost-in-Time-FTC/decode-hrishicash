package org.firstinspires.ftc.teamcode.robot.opmode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;

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

    @Override
    public void init() {
        currentGamepad2 = new Gamepad();
        previousGamepad2 = new Gamepad();

        hardware = new Hardware(hardwareMap);

        drive = new Drive(hardware, telemetry, gamepad1);
        intake = new Intake(hardware, telemetry, gamepad2);
        outtake = new Outtake(hardware, telemetry, gamepad2);

        telemetry.addData("Status", "Initialized");
        telemetry.update();
    }

    @Override
    public void start() {
        runtime.reset();
    }

    @Override
    public void loop() {
        drive.run();
        //intake.run(previousGamepad2, currentGamepad2);
        intake.run();
        //outtakeTime = outtake.run(outtakeTime);//altered with time
        outtake.run();

        hardware.printEncoders(telemetry);
    }
}
