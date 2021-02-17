package org.igutech.auto.paths;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.acmerobotics.roadrunner.trajectory.constraints.DriveConstraints;

import org.igutech.auto.roadrunner.SampleMecanumDrive;
import org.igutech.config.Hardware;
import org.igutech.utils.events.Callback;

public class RedB {
    public static void createTrajectory(SampleMecanumDrive drive, Pose2d start, Callback callback, Hardware hardware) {
        Trajectory prepareToShoot = drive.trajectoryBuilder(start)
                .addDisplacementMarker(() -> {
                    hardware.getServos().get("wobbleGoalLift").setPosition(0.15);
                })
                .splineToConstantHeading(new Vector2d(-55.0, -20), Math.toRadians(0.0))
                .splineToConstantHeading(new Vector2d(-10.0, -20.0), Math.toRadians(0.0))
                .splineToConstantHeading(new Vector2d(-7.0, -40), Math.toRadians(0.0))
                .addDisplacementMarker(callback::call)
                .build();

        Trajectory moveToRedB = drive.trajectoryBuilder(prepareToShoot.end())
                .splineToConstantHeading(new Vector2d(42.0, -28.0), Math.toRadians(0.0))
                .addDisplacementMarker(callback::call)
                .build();

        Trajectory moveAwayFromRedB = drive.trajectoryBuilder(moveToRedB.end())
                .splineToConstantHeading(new Vector2d(42.0, -20), Math.toRadians(0.0))
                .splineToConstantHeading(new Vector2d(20.0, -20), Math.toRadians(0.0))
                .addDisplacementMarker(callback::call)
                .build();

        Trajectory goToRingStack = drive.trajectoryBuilder(moveAwayFromRedB.end())
                .addDisplacementMarker(()->{
                    hardware.getServos().get("releaseLiftServo").setPosition(0.2);
                })
                .splineToLinearHeading(new Pose2d(0.0, -35.0, Math.toRadians(180.0)), Math.toRadians(180.0))
                .addDisplacementMarker(callback::call)
                .build();

        Trajectory intakeRingStack = drive.trajectoryBuilder(goToRingStack.end(), new DriveConstraints(30.0, 30.0, 0.0, Math.toRadians(180), Math.toRadians(180), 0.0))
                .addDisplacementMarker(() -> {
            hardware.getMotors().get("intake").setPower(-1);
            hardware.getMotors().get("intake2").setPower(-1);
        })
                .splineToConstantHeading(new Vector2d(-25.0, -35), Math.toRadians(180.0))
                .addDisplacementMarker(callback::call)
                .build();

        Trajectory moveToSecondWobbleGoal = drive.trajectoryBuilder(intakeRingStack.end())
                .addDisplacementMarker(() -> {
                    hardware.getMotors().get("intake").setPower(0);
                    hardware.getMotors().get("intake2").setPower(0);
                })
                .splineToLinearHeading(new Pose2d(-35.0, -30.0, Math.toRadians(0.0)), Math.toRadians(0.0))
                .addDisplacementMarker(callback::call)
                .build();

        Trajectory moveToSecondWobbleGoalContinued = drive.trajectoryBuilder(moveToSecondWobbleGoal.end())
                .lineToConstantHeading(new Vector2d(-44.5, -30.0))
                .addDisplacementMarker(callback::call)
                .build();

        Trajectory moveToSecondWobbleGoalContinued2 = drive.trajectoryBuilder(moveToSecondWobbleGoalContinued.end())
                .lineToConstantHeading(new Vector2d(-44.5, -38.5))
                .addDisplacementMarker(callback::call)
                .build();

        Trajectory moveToShootRingStack = drive.trajectoryBuilder(moveToSecondWobbleGoalContinued2.end())
                .splineToConstantHeading(new Vector2d(-7.0, -40.0), Math.toRadians(0.0))
                .addDisplacementMarker(callback::call)
                .build();

        Trajectory moveToRedASecondTime = drive.trajectoryBuilder(moveToShootRingStack.end())
                .splineToConstantHeading(new Vector2d(36, -20), Math.toRadians(0.0))
                .addDisplacementMarker(callback::call)
                .build();

        Trajectory park = drive.trajectoryBuilder(moveToRedASecondTime.end())
                .lineToConstantHeading(new Vector2d(10 , -10))
                .addDisplacementMarker(callback::call)
                .build();

    }


}
