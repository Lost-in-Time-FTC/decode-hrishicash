package org.firstinspires.ftc.teamcode.robot.opmode.teleop;

import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.robot.config.Config;

@TeleOp(name = "Red Goal Tele", group = "Iterative OpMode")
public class RedGoalTele extends BlueGoalTele {
    @Override
    public Pose getStartingPose() {
        return Config.finalPoseRedGoalLaunchZoneAuto;
    }

    @Override
    public Pose getAllianceGoalPose() {
        return Config.redGoalPose;
    }
}
