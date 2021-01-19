package org.igutech.auto.paths;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.acmerobotics.roadrunner.trajectory.constraints.DriveConstraints;

import org.igutech.auto.FullRedAuto;
import org.igutech.auto.State;
import org.igutech.auto.roadrunner.SampleMecanumDrive;

import java.util.ArrayList;
import java.util.HashMap;

public class RedA {
    public static HashMap<String, Trajectory> createTrajectory(SampleMecanumDrive drive, Pose2d start) {
        HashMap<String, Trajectory> trajectories = new HashMap<>();
        Trajectory prepareToShoot = drive.trajectoryBuilder(start)
                .splineToConstantHeading(new Vector2d(-35.0, -35.0), Math.toRadians(0.0))
                .addDisplacementMarker(()->{
                    FullRedAuto.setState(State.SHOOTING);
                })
                .build();

        Trajectory intakeRingStack = drive.trajectoryBuilder(prepareToShoot.end(), new DriveConstraints(30.0, 30.0, 0.0, Math.toRadians(180), Math.toRadians(180), 0.0))
                .splineTo(new Vector2d(-25.0, -35.0), Math.toRadians(0.0))
                .build();

        Trajectory dropFirstWobbleGoal = drive.trajectoryBuilder(intakeRingStack.end())
                .splineTo(new Vector2d(10.0, -45.0),Math.toRadians(0.0))
                .build();
        Trajectory shootRingStack = drive.trajectoryBuilder(dropFirstWobbleGoal.end(),true)
                .splineToConstantHeading(new Vector2d(-5.0, -40.0),Math.toRadians(0.0))
                .build();


        trajectories.put("PrepareToShoot", prepareToShoot);
        trajectories.put("IntakeRingStack", intakeRingStack);
        trajectories.put("DropFirstWobbleGoal", dropFirstWobbleGoal);
        trajectories.put("ShootRingStack", shootRingStack);
        return trajectories;
    }


}
