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

public class RedB {
    public static Map<State, Trajectory> createTrajectory(SampleMecanumDrive drive, Pose2d start, Callback callback, Hardware hardware) {
        Map<State, Trajectory> trajectories = new EnumMap<State, Trajectory>(State.class);
        Trajectory prepareToShoot = drive.trajectoryBuilder(start, new DriveConstraints(20.0, 20.0, 0.0, Math.toRadians(180.0), Math.toRadians(180.0), 0.0))
                .addDisplacementMarker(() -> {
                    hardware.getServos().get("wobbleGoalLift").setPosition(0.15);
                })
                .splineToConstantHeading(new Vector2d(-55.0, -10.0), Math.toRadians(0.0))
                .splineToConstantHeading(new Vector2d(-5.0, -15.0), Math.toRadians(0.0))
                .addDisplacementMarker(callback::call)
                .build();
        trajectories.put(State.PREPARE_TO_SHOOT, prepareToShoot);

        Trajectory moveToRedB = drive.trajectoryBuilder(prepareToShoot.end())
                .splineToConstantHeading(new Vector2d(35.0, -20.0), Math.toRadians(0.0))
                .addDisplacementMarker(15, () -> {
                    hardware.getServos().get("wobbleGoalLift").setPosition(1);
                })
                .addDisplacementMarker(() -> {
                    hardware.getServos().get("wobbleGoalServo").setPosition(0.25);
                })
                .splineToConstantHeading(new Vector2d(15.0, -35.0), Math.toRadians(0.0))
                .addDisplacementMarker(callback::call)
                .build();
        trajectories.put(State.MOVE_TO_DROP_FIRST_WOBBLE_GOAL, moveToRedB);

        Trajectory goToRingStack = drive.trajectoryBuilder(moveToRedB.end())
                .splineToLinearHeading(new Pose2d(0.0, -35.0, Math.toRadians(180.0)), Math.toRadians(180.0))
                .addDisplacementMarker(callback::call)
                .build();
        trajectories.put(State.MOVE_TO_TO_RING_STACK, goToRingStack);

        Trajectory intakeRingStack = drive.trajectoryBuilder(goToRingStack.end(), new DriveConstraints(30.0, 30.0, 0.0, Math.toRadians(180), Math.toRadians(180), 0.0))
                .addDisplacementMarker(() -> {
                    hardware.getMotors().get("intake").setPower(-1);
                    hardware.getMotors().get("intake2").setPower(-1);
                })
                .splineToConstantHeading(new Vector2d(-25.0, -35.0), Math.toRadians(180.0))
                .addDisplacementMarker(callback::call)
                .build();
        trajectories.put(State.INTAKE_RING_STACK, intakeRingStack);

        Trajectory moveToSecondWobbleGoal = drive.trajectoryBuilder(intakeRingStack.end())
                .addDisplacementMarker(() -> {
                    hardware.getMotors().get("intake").setPower(0);
                    hardware.getMotors().get("intake2").setPower(0);
                })
                .splineToLinearHeading(new Pose2d(-35.0, -35.0, Math.toRadians(0.0)), Math.toRadians(0.0))
                .addDisplacementMarker(callback::call)
                .build();
        trajectories.put(State.MOVE_TO_GRAB_SECOND_GOAL, moveToSecondWobbleGoal);

        Trajectory moveToSecondWobbleGoalContinued = drive.trajectoryBuilder(moveToSecondWobbleGoal.end())
                .splineToConstantHeading(new Vector2d(-45.0, -40.0), Math.toRadians(0.0))
                .addDisplacementMarker(callback::call)
                .build();
        trajectories.put(State.MOVE_TO_GRAB_SECOND_GOAL_CONTINUED, moveToSecondWobbleGoalContinued);

        Trajectory moveToShootRingStack = drive.trajectoryBuilder(moveToSecondWobbleGoalContinued.end())
                .splineToConstantHeading(new Vector2d(0.0, -40.0), Math.toRadians(0.0))
                .addDisplacementMarker(callback::call)
                .build();
        trajectories.put(State.MOVE_TO_SHOOT_RING_STACK, moveToShootRingStack);

        return trajectories;
    }
}
