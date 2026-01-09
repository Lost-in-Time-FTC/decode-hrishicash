
/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode.robot.opmode.teleop;


import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.teamcode.robot.config.Hardware;
import org.firstinspires.ftc.teamcode.robot.opmode.teleop.subsystem.Drive;
import org.firstinspires.ftc.teamcode.robot.opmode.teleop.subsystem.Intake;
import org.firstinspires.ftc.teamcode.robot.opmode.teleop.subsystem.Outtake;

/*
 * This OpMode illustrates the concept of driving a path based on time.
 * The code is structured as a LinearOpMode
 *
 * The code assumes that you do NOT have encoders on the wheels,
 *   otherwise you would use: RobotAutoDriveByEncoder;
 *
 *   The desired path in this example is:
 *   - Drive forward for 3 seconds
 *   - Spin right for 1.3 seconds
 *   - Drive Backward for 1 Second
 *
 *  The code is written in a simple form with no optimizations.
 *  However, there are several ways that this type of sequence could be streamlined,
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list
 */
//Robot: Auto Drive By Time
@Autonomous(name="LIT Rightside Shooting AUTO", group="Robot")
//@Disabled
public class PreliminaryAuto extends LinearOpMode {

    /* Declare OpMode members. */
    private DcMotor         fL   = null;
    private DcMotor         bL   = null;
    private DcMotor         fR  = null;
    private DcMotor         bR  = null;
    private Hardware hardware;
    private ElapsedTime     runtime = new ElapsedTime();


    static final double     FORWARD_SPEED = 0.6;
    static final double     TURN_SPEED    = 0.5;

    public void driveWithoutTurn(String direction) {
        // To drive forward, most robots need the motor on one side to be reversed, because the axles point in opposite directions.
        // When run, this OpMode should start both motors driving forward. So adjust these two lines based on your first test drive.
        // This should be what Tardis needs below
        // Below is auto-preset for forward, if command will adjust to direction
        fL.setDirection(DcMotor.Direction.REVERSE);
        bL.setDirection(DcMotor.Direction.REVERSE);
        fR.setDirection(DcMotor.Direction.FORWARD);
        bR.setDirection(DcMotor.Direction.FORWARD);
        if (direction == "back") {
            // flips all motor directions, is there an easier way to do this?
            fL.setDirection(DcMotor.Direction.FORWARD);
            bL.setDirection(DcMotor.Direction.FORWARD);
            fR.setDirection(DcMotor.Direction.REVERSE);
            bR.setDirection(DcMotor.Direction.REVERSE);
        } else if (direction == "left") {
            fL.setDirection(DcMotor.Direction.FORWARD);
            bR.setDirection(DcMotor.Direction.REVERSE);
        } else if (direction == "right") {
            fR.setDirection(DcMotor.Direction.REVERSE);
            bL.setDirection(DcMotor.Direction.FORWARD);
        }
        fL.setPower(FORWARD_SPEED);
        fR.setPower(FORWARD_SPEED);
        bL.setPower(FORWARD_SPEED);
        bR.setPower(FORWARD_SPEED);

        /* used if the above for loop doesn't work
        fL.setPower(FORWARD_SPEED);
        bL.setPower(FORWARD_SPEED);
        fR.setPower(FORWARD_SPEED);
        bR.setPower(FORWARD_SPEED);
         */
    }

    public void setTurnDirection(boolean direction) {
        // a false direction will turn left, a true direction turns right (according to the code rn)
        int k = -1;
        if (direction) {
            k = 1;
        }
        fL.setPower(k * TURN_SPEED);
        fR.setPower(k * TURN_SPEED);
        bL.setPower(k * TURN_SPEED);
        bR.setPower(k * TURN_SPEED);

    }
    @Override
    public void runOpMode() {

        hardware = new Hardware(hardwareMap);
        // Initialize the drive system variables.
        fL  = hardwareMap.get(DcMotor.class, "fL");
        fR = hardwareMap.get(DcMotor.class, "fR");
        bL  = hardwareMap.get(DcMotor.class, "bL");
        bR = hardwareMap.get(DcMotor.class, "bR");



        // Send telemetry message to signify robot waiting;
        telemetry.addData("Status", "Ready to run");    //
        telemetry.update();

        // Wait for the game to start (driver presses START)
        waitForStart();

        // Step through each leg of the path, ensuring that the OpMode has not been stopped along the way.

        // Step 1:  Drive forward for some seconds
        runtime.reset();
        while (opModeIsActive() && (runtime.seconds() < 0.5)) {
            hardware.moveBackward(0.75);
            telemetry.addData("Path", "Leg 1: %4.1f S Elapsed", runtime.seconds());
            telemetry.update();
        }
        runtime.reset();
        while (opModeIsActive() && (runtime.seconds() < 0.6)) {
            hardware.moveBackward(0.5);
            telemetry.addData("Path", "Leg 1: %4.1f S Elapsed", runtime.seconds());
            telemetry.update();
        }
        hardware.stopDrive();

        sleep(1000);


        // Step 2:  turn to ready up into the shooting shooter shooting TURN MAY NOT BE NEEDED+        runtime.reset();
        /*while (opModeIsActive() && (runtime.seconds() < 1)) {
            hardware.rotateRight(0.75);
            telemetry.addData("Path", "Leg 2: %4.1f S Elapsed", runtime.seconds());
            telemetry.update();
        }
        hardware.stopDrive();*/

        /*runtime.reset();
        while (opModeIsActive() && (runtime.seconds() < 1)) {
            hardware.rotateLeft(0.5);
            telemetry.addData("Path", "Leg 43: %4.1f S Elapsed", runtime.seconds());
            telemetry.update();
        }*/
        //hardware.stopDrive();

        //Step 3: you may fire when ready
        runtime.reset();
        /*while (opModeIsActive() && (runtime.seconds() < 1.5)) {
            hardware.runOuttake();
            telemetry.addData("Path", "Leg 3: %4.1f S Elapsed", runtime.seconds());
            telemetry.update();
        }
        runtime.reset();
        while (opModeIsActive() && (runtime.seconds() < 2)) {
            hardware.feedOuttake(-1);
            telemetry.addData("Path", "Leg 4: %4.1f S Elapsed", runtime.seconds());
            telemetry.update();
        }
        hardware.stopOuttake();
        hardware.feedOuttake(0);*/


        // Step 4:  Stop
        hardware.stopDrive();

        telemetry.addData("Path", "Complete");
        telemetry.update();
        sleep(1000);
    }
}