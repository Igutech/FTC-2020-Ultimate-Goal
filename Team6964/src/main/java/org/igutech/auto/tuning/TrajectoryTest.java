package org.igutech.auto.tuning;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.acmerobotics.roadrunner.trajectory.constraints.DriveConstraints;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.igutech.auto.roadrunner.SampleMecanumDrive;


/*
 * This is an example of a more complex path to really test the tuning.
 */
@Autonomous(group = "drive")
public class TrajectoryTest extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);
        Pose2d startPose = new Pose2d(-60, -35, Math.toRadians(0));
        drive.setPoseEstimate(startPose);
        waitForStart();

        if (isStopRequested()) return;

        Trajectory prepareToShoot = drive.trajectoryBuilder(startPose)
                .splineToConstantHeading(new Vector2d(-35.0, -35.0), Math.toRadians(0.0))
                .build();

        Trajectory intakeRingStack = drive.trajectoryBuilder(prepareToShoot.end(), new DriveConstraints(30.0, 30.0, 0.0, Math.toRadians(180), Math.toRadians(180), 0.0))
                .splineTo(new Vector2d(-25.0, -35.0), Math.toRadians(0.0))

                .build();

        Trajectory dropFirstWobbleGoal = drive.trajectoryBuilder(intakeRingStack.end())
                .splineTo(new Vector2d(10.0, -45.0), Math.toRadians(0.0))
                .build();
        Trajectory moveToShootRingStack = drive.trajectoryBuilder(dropFirstWobbleGoal.end(), true)
                .splineToConstantHeading(new Vector2d(-5.0, -40.0), Math.toRadians(0.0))
                .build();
        Trajectory grabSecondGoal = drive.trajectoryBuilder(moveToShootRingStack.end())
                .splineToConstantHeading(new Vector2d(-40.0, -40.0), Math.toRadians(0.0))
                .build();
        Trajectory dropSecondWobbleGoal = drive.trajectoryBuilder(grabSecondGoal.end())
                .splineTo(new Vector2d(10.0, -45.0), Math.toRadians(0.0))
                .build();

        drive.followTrajectory(prepareToShoot);
        drive.followTrajectory(intakeRingStack);
        drive.followTrajectory(dropFirstWobbleGoal);
        drive.followTrajectory(moveToShootRingStack);
        drive.followTrajectory(grabSecondGoal);
        drive.followTrajectory(dropSecondWobbleGoal);


    }
}
