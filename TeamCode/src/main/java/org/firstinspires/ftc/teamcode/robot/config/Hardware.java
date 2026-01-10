package org.firstinspires.ftc.teamcode.robot.config;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.VoltageSensor;//funny alex addition

import java.util.Arrays;
import java.util.List;

public class Hardware {
    public GoBildaPinpointDriver pinpoint; //they told me to (the robot error did)
    public DcMotor fL;
    public DcMotor fR;
    public DcMotor bL;
    public DcMotor bR;
    public List<DcMotor> driveMotors;
    //public DcMotor outtakeL;
    //public DcMotor outtakeR;
    //public List<DcMotor> outtakeMotors;
    //public CRServo outtakeGate;
    public CRServo rotatorR;
    public CRServo rotatorL;
    public DcMotor intakeL;

    public  DcMotor intakeR;

    public Hardware(HardwareMap hardwareMap) {
        fR = hardwareMap.get(DcMotor.class, "fR");
        fL = hardwareMap.get(DcMotor.class, "fL");
        bR = hardwareMap.get(DcMotor.class, "bR");
        bL = hardwareMap.get(DcMotor.class, "bL");
        driveMotors = Arrays.asList(fR, fL, bR, bL);

        pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");

        //outtakeL = hardwareMap.get(DcMotor.class, "outtakeL");
        //outtakeL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        //outtakeL.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        //outtakeR = hardwareMap.get(DcMotor.class, "outtakeR");
        //outtakeR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        //outtakeR.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        //outtakeMotors = Arrays.asList(outtakeL, outtakeR);
        intakeL = hardwareMap.get(DcMotor.class, "intakeL");
        intakeR = hardwareMap.get(DcMotor.class, "intakeR");
        //outtakeGate = hardwareMap.get(CRServo.class, "outtakeGate");

        // Most robots need the motor on one side to be reversed to drive forward
        // Reverse the motor that runs backwards when connected directly to the battery
        bL.setDirection(DcMotor.Direction.FORWARD);
        bR.setDirection(DcMotor.Direction.REVERSE);
        fL.setDirection(DcMotor.Direction.FORWARD);
        fR.setDirection(DcMotor.Direction.REVERSE);

        //outtakeL.setDirection(DcMotor.Direction.REVERSE);
        //outtakeR.setDirection(DcMotor.Direction.REVERSE);
        intakeL.setDirection(DcMotor.Direction.FORWARD);
        intakeR.setDirection(DcMotor.Direction.REVERSE);

    }

    public void moveForward(double power) {
        bL.setPower(power);
        bR.setPower(power);
        fL.setPower(power);
        fR.setPower(power);
    }

    public void moveBackward(double power) {
        bL.setPower(-power);
        bR.setPower(-power);
        fL.setPower(-power);
        fR.setPower(-power);
    }

    public void strafeLeft(double power) {
        bL.setPower(-power);
        bR.setPower(power);
        fL.setPower(-power);
        fR.setPower(power);
    }

    public void strafeRight(double power) {
        bL.setPower(power);
        bR.setPower(-power);
        fL.setPower(power);
        fR.setPower(-power);
    }

    public final void rotateLeft(double power) {
        bL.setPower(power);
        bR.setPower(power);
        fL.setPower(-power);
        fR.setPower(-power);
    }

    public final void rotateRight(double power) {
        fR.setPower(-power);
        bR.setPower(-power);
        fL.setPower(power);
        bL.setPower(power);
    }

    public final void feedOuttake(double power) {
        //outtakeGate.setPower(power);
    }
    //NEGATIVE IS INTAKE

    public final void rotateOuttake(double power){
        rotatorL.setPower(power);
        rotatorR.setPower(power);
        //MUST TEST - I don't know which direction is which
    }

    /*public final void runOuttake() {
        outtakeL.setPower(0.8);
        outtakeR.setPower(1);
    }

    public final void stopOuttake() {
        outtakeL.setPower(0);
        outtakeR.setPower(0);
    }*/

    public final void stopDrive() {
        fR.setPower(0);
        bR.setPower(0);
        fL.setPower(0);
        bL.setPower(0);
    }
}
