package org.igutech.auto.paths;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;

import org.igutech.auto.roadrunner.SampleMecanumDrive;

import java.util.ArrayList;

public class RedA {
    public static ArrayList<Trajectory> createTrajectory(SampleMecanumDrive drive, Pose2d start) {
        ArrayList<Trajectory> trajectories = new ArrayList<>();
        Trajectory dropGoal = drive.trajectoryBuilder(start)
                .splineToConstantHeading(new Vector2d(0, -60), Math.toRadians(0.0))
                .splineToConstantHeading(new Vector2d(0, -35), Math.toRadians(0.0))
                .build();
        Trajectory getRings = drive.trajectoryBuilder(dropGoal.end())
                .splineToSplineHeading(new Pose2d(-5, -35, Math.toRadians(180)), Math.toRadians(0))
                .splineToSplineHeading(new Pose2d(-15, -35, Math.toRadians(180)), Math.toRadians(0))
                .build();
        Trajectory shootRing = drive.trajectoryBuilder(getRings.end())
                .splineToSplineHeading(new Pose2d(0, -35, Math.toRadians(0)), Math.toRadians(180))
                .build();

        Trajectory getSecondGoal = drive.trajectoryBuilder(shootRing.end())
                .splineToSplineHeading(new Pose2d(-30, -25, Math.toRadians(180)), Math.toRadians(0))
                .splineToSplineHeading(new Pose2d(-40, -25, Math.toRadians(180)), Math.toRadians(0))
                .build();
        Trajectory dropSecondGoal = drive.trajectoryBuilder(getSecondGoal.end())
                .splineToSplineHeading(new Pose2d(0, -60, Math.toRadians(0)), Math.toRadians(180))
                .build();

        trajectories.add(dropGoal);
        trajectories.add(getRings);
        trajectories.add(shootRing);
        trajectories.add(getSecondGoal);
        trajectories.add(dropSecondGoal);
        return trajectories;
    }
}
