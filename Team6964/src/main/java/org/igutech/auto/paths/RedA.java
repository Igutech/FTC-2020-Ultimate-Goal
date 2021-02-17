package org.igutech.auto.paths;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;

import org.igutech.auto.roadrunner.SampleMecanumDrive;
import org.igutech.config.Hardware;
import org.igutech.utils.events.Callback;

public class RedA {
    public static void createTrajectory(SampleMecanumDrive drive, Pose2d start, Callback callback, Hardware hardware) {
        Trajectory prepareToShoot = drive.trajectoryBuilder(start)
                .addDisplacementMarker(() -> {
                    hardware.getServos().get("wobbleGoalLift").setPosition(0.15);
                })
//                .splineToConstantHeading(new Vector2d(-55.0, -20), Math.toRadians(0.0))
//                .splineToConstantHeading(new Vector2d(-15.0, -20.0), Math.toRadians(0.0))
                .splineToConstantHeading(new Vector2d(-7.0, -40), Math.toRadians(0.0))
                .addDisplacementMarker(callback::call)
                .build();


        Trajectory moveToRedA = drive.trajectoryBuilder(prepareToShoot.end())
                .splineToConstantHeading(new Vector2d(12.0, -50), Math.toRadians(0.0))
                .addDisplacementMarker(callback::call)
                .build();

        Trajectory moveAwayFromRedA = drive.trajectoryBuilder(moveToRedA.end())
                .splineToConstantHeading(new Vector2d(12, -35), Math.toRadians(0.0))
                .addDisplacementMarker(callback::call)
                .build();

        Trajectory moveToSecondWobbleGoal = drive.trajectoryBuilder(moveAwayFromRedA.end())
                .lineToConstantHeading(new Vector2d(-44.5, -30.0))
                .addDisplacementMarker(callback::call)
                .build();

        Trajectory moveToSecondWobbleGoalContinued = drive.trajectoryBuilder(moveToSecondWobbleGoal.end())
                .lineToConstantHeading(new Vector2d(-44.5, -38.5))
                .addDisplacementMarker(callback::call)
                .build();

        Trajectory moveToRedBSecondTime = drive.trajectoryBuilder(moveToSecondWobbleGoalContinued.end())
                .splineToConstantHeading(new Vector2d(20, -45), Math.toRadians(0.0))
                .addDisplacementMarker(callback::call)
                .build();

        Trajectory park = drive.trajectoryBuilder(moveToRedBSecondTime.end())
                .splineToConstantHeading(new Vector2d(18, -35), Math.toRadians(0.0))
                .addDisplacementMarker(callback::call)
                .build();
    }


}
