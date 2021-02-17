package org.igutech.auto.paths;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.acmerobotics.roadrunner.trajectory.constraints.DriveConstraints;

import org.igutech.auto.roadrunner.SampleMecanumDrive;
import org.igutech.config.Hardware;
import org.igutech.teleop.Modules.TimerService;
import org.igutech.utils.events.Callback;

public class RedC {
    public static void createTrajectory(SampleMecanumDrive drive, Pose2d start, Callback callback, Hardware hardware, TimerService timerService) {
        Trajectory prepareToShoot = drive.trajectoryBuilder(start)
                .addDisplacementMarker(() -> {
                    hardware.getServos().get("wobbleGoalLift").setPosition(0.15);
                })
                .splineToConstantHeading(new Vector2d(-55.0, -20), Math.toRadians(0.0))
                .splineToConstantHeading(new Vector2d(-10.0, -20.0), Math.toRadians(0.0))
                .splineToConstantHeading(new Vector2d(-7.0, -40), Math.toRadians(0.0))
                .addDisplacementMarker(callback::call)
                .build();

        Trajectory moveToRedC = drive.trajectoryBuilder(prepareToShoot.end())
                .lineToLinearHeading(new Pose2d(50.0, -54,Math.toRadians(90)))
                .addDisplacementMarker(callback::call)
                .build();

        Trajectory moveAwayFromRedC = drive.trajectoryBuilder(moveToRedC.end())
                .splineToConstantHeading(new Vector2d(20, -30), Math.toRadians(0.0))
                .addDisplacementMarker(callback::call)
                .build();

        Trajectory goToRingStack = drive.trajectoryBuilder(moveAwayFromRedC.end())
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
                .splineToConstantHeading(new Vector2d(-12.0, -38), Math.toRadians(180.0))
                .addDisplacementMarker(()->{
                        timerService.registerUniqueTimerEvent(1000,"Intake", callback::call);
                })
                .build();

        Trajectory moveToSecondWobbleGoal = drive.trajectoryBuilder(intakeRingStack.end())
                .splineToConstantHeading(new Vector2d(-14.0, -38), Math.toRadians(180.0))
                .addDisplacementMarker(()->{
                    timerService.registerUniqueTimerEvent(1000,"Intake", callback::call);
                })
                .build();

        Trajectory moveToSecondWobbleGoalContinued = drive.trajectoryBuilder(moveToSecondWobbleGoal.end())
                .splineToConstantHeading(new Vector2d(-16.0, -38), Math.toRadians(180.0))
                .addDisplacementMarker(()->{
                    timerService.registerUniqueTimerEvent(1000,"Intake", callback::call);
                })
                .build();

//        Trajectory moveToSecondWobbleGoalContinued2 = drive.trajectoryBuilder(moveToSecondWobbleGoalContinued.end())
//                .lineToConstantHeading(new Vector2d(-44.5, -38.5))
//                .addDisplacementMarker(callback::call)
//                .build();
//        trajectories.put(State.MOVE_TO_GRAB_SECOND_GOAL_CONTINUED2, moveToSecondWobbleGoalContinued2);
//
//        Trajectory moveToShootRingStack = drive.trajectoryBuilder(moveToSecondWobbleGoalContinued.end())
//                .splineToConstantHeading(new Vector2d(-7.0, -40.0), Math.toRadians(0.0))
//                .addDisplacementMarker(callback::call)
//                .build();
//        trajectories.put(State.MOVE_TO_SHOOT_RING_STACK, moveToShootRingStack);
//
//        Trajectory moveToRedASecondTime = drive.trajectoryBuilder(moveToShootRingStack.end())
//                .splineToConstantHeading(new Vector2d(36, -20), Math.toRadians(0.0))
//                .addDisplacementMarker(callback::call)
//                .build();
//        trajectories.put(State.MOVE_TO_DROP_SECOND_WOBBLE_GOAL, moveToRedASecondTime);
//
//        Trajectory park = drive.trajectoryBuilder(moveToRedASecondTime.end())
//                .lineToConstantHeading(new Vector2d(10 , -10))
//                .addDisplacementMarker(callback::call)
//                .build();
//        trajectories.put(State.PARK,park);

    }


}

