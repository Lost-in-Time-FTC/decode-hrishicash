package org.firstinspires.ftc.teamcode.robot.config;

import com.pedropathing.geometry.Pose;

public class Config {
    // auto
    public static final Pose initialPoseBlueGoalLaunchZoneAuto = new Pose(24.453, 126.792, Math.toRadians(135));
    public static final Pose finalPoseBlueGoalLaunchZoneAuto = new Pose(49.660, 72.453, Math.toRadians(180));

    public static final Pose initialPoseRedGoalLaunchZoneAuto = new Pose(119.547, 126.792, Math.toRadians(45));

    public static final Pose finalPoseRedGoalLaunchZoneAuto = new Pose(94.340, 72.453, Math.toRadians(0));

    // non-auto starting pose
    public static final Pose initialPoseBlueHumanPlayerLaunchZone = new Pose(); // TODO: update pose
    public static final Pose initialPoseRedHumanPlayerLaunchZone = new Pose(); // TODO: update pose

    // goals
    public static final Pose blueGoalPose = new Pose(132, 12);
    public static final Pose redGoalPose = new Pose(132, 132);
}
