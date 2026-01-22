package org.firstinspires.ftc.teamcode.pedroPathing;

import com.pedropathing.control.FilteredPIDFCoefficients;
import com.pedropathing.control.PIDFCoefficients;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.ftc.FollowerBuilder;
import com.pedropathing.ftc.drivetrains.MecanumConstants;
import com.pedropathing.ftc.localization.Encoder;
import com.pedropathing.ftc.localization.constants.PinpointConstants;
import com.pedropathing.ftc.localization.constants.TwoWheelConstants;
import com.pedropathing.paths.PathConstraints;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class Constants {
    public static FollowerConstants followerConstants = new FollowerConstants()
            .mass(8.70)
            .forwardZeroPowerAcceleration(-67.088)
            //-49.2647 (ignored), -71.4159, -65.2534, -64.5947
            .lateralZeroPowerAcceleration(-81.65126667)
            //-80.7209  -79.4824  -84.7505
            .translationalPIDFCoefficients(new PIDFCoefficients(0.04,0,0.002,0.04))
            .headingPIDFCoefficients(new PIDFCoefficients(0.6,0,0.025,0.02))
            .drivePIDFCoefficients(new FilteredPIDFCoefficients(0.04,0,0.001,0.6,0.02))
            ;

    public static MecanumConstants driveConstants = new MecanumConstants()
        .maxPower(1)
        .rightFrontMotorName("fR")
        .rightRearMotorName("bR")
        .leftRearMotorName("bL")
        .leftFrontMotorName("fL")
        .leftFrontMotorDirection(DcMotorSimple.Direction.REVERSE)
        .leftRearMotorDirection(DcMotorSimple.Direction.REVERSE)
        .rightFrontMotorDirection(DcMotorSimple.Direction.FORWARD)
        .rightRearMotorDirection(DcMotorSimple.Direction.FORWARD)
        .xVelocity(74.40973333) // 70.671537 // 70.402644 // 70.659474 // avg: 70.577885
        //75.6847, 74.283, 73.2615
        .yVelocity(54.90163333)
        //52.7295, 56.3113, 55.6641
        ;

    public static PathConstraints pathConstraints = new PathConstraints(
            0.99,
            100,
            1.3,
            1
    );

    public static PinpointConstants localizerConstants = new PinpointConstants()
        .forwardPodY(-0.89217913386) // in inches
        .strafePodX(-3.104086614)    // please don't kill me thanks
            .distanceUnit(DistanceUnit.MM)
            .hardwareMapName("pinpoint")
            .customEncoderResolution(8192/(Math.PI * 35)); //trying this with quad resolution
            //.forwardEncoderDirection(GoBildaPinpointDriver.EncoderDirection.REVERSED)//; //finally works
            //.strafeEncoderDirection(GoBildaPinpointDriver.EncoderDirection.REVERSED);


    public static Follower createFollower(HardwareMap hardwareMap) {
        return new FollowerBuilder(followerConstants, hardwareMap)
                .pinpointLocalizer(localizerConstants)
                .pathConstraints(pathConstraints)
                .mecanumDrivetrain(driveConstants)
                .build();
    }
}
