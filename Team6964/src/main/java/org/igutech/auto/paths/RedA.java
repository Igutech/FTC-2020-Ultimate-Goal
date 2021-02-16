package org.igutech.auto.paths;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.acmerobotics.roadrunner.trajectory.constraints.DriveConstraints;

import org.igutech.auto.State;
import org.igutech.auto.roadrunner.SampleMecanumDrive;
import org.igutech.config.Hardware;
import org.igutech.utils.events.Callback;

import java.util.EnumMap;
import java.util.Map;

public class RedA {
    public static Map<State, Trajectory> createTrajectory(SampleMecanumDrive drive, Pose2d start, Callback callback, Hardware hardware) {
        Map<State, Trajectory> trajectories = new EnumMap<State, Trajectory>(State.class);
        Trajectory prepareToShoot = drive.trajectoryBuilder(start)
                .addDisplacementMarker(() -> {
                    hardware.getServos().get("wobbleGoalLift").setPosition(0.15);
                })
//                .splineToConstantHeading(new Vector2d(-55.0, -20), Math.toRadians(0.0))
//                .splineToConstantHeading(new Vector2d(-15.0, -20.0), Math.toRadians(0.0))
                .splineToConstantHeading(new Vector2d(-7.0, -40), Math.toRadians(0.0))
                .addDisplacementMarker(callback::call)
                .build();
        trajectories.put(State.PREPARE_TO_SHOOT, prepareToShoot);

        Trajectory moveToRedA = drive.trajectoryBuilder(prepareToShoot.end())
                .splineToConstantHeading(new Vector2d(12.0, -50), Math.toRadians(0.0))
                .addDisplacementMarker(callback::call)
                .build();
        trajectories.put(State.MOVE_TO_DROP_FIRST_WOBBLE_GOAL, moveToRedA);

        Trajectory moveAwayFromRedA = drive.trajectoryBuilder(moveToRedA.end())
                .splineToConstantHeading(new Vector2d(12, -35), Math.toRadians(0.0))
                .addDisplacementMarker(callback::call)
                .build();
        trajectories.put(State.DROP_FIRST_WOBBLE_GOAL,moveAwayFromRedA);

        Trajectory moveToSecondWobbleGoal = drive.trajectoryBuilder(moveAwayFromRedA.end())
                .lineToConstantHeading(new Vector2d(-44.5, -30.0))
                .addDisplacementMarker(callback::call)
                .build();
        trajectories.put(State.MOVE_TO_GRAB_SECOND_GOAL, moveToSecondWobbleGoal);

        Trajectory moveToSecondWobbleGoalContinued = drive.trajectoryBuilder(moveToSecondWobbleGoal.end())
                .lineToConstantHeading(new Vector2d(-44.5, -38.5))
                .addDisplacementMarker(callback::call)
                .build();
        trajectories.put(State.MOVE_TO_GRAB_SECOND_GOAL_CONTINUED, moveToSecondWobbleGoalContinued);

        Trajectory moveToRedBSecondTime = drive.trajectoryBuilder(moveToSecondWobbleGoalContinued.end())
                .splineToConstantHeading(new Vector2d(20, -45), Math.toRadians(0.0))
                .addDisplacementMarker(callback::call)
                .build();
        trajectories.put(State.MOVE_TO_DROP_SECOND_WOBBLE_GOAL, moveToRedBSecondTime);

        Trajectory park = drive.trajectoryBuilder(moveToRedBSecondTime.end())
                .splineToConstantHeading(new Vector2d(18, -35), Math.toRadians(0.0))
                .addDisplacementMarker(callback::call)
                .build();
        trajectories.put(State.PARK,park);
        return trajectories;
    }


}
