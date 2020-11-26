package org.igutech.auto.tuning;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.kinematics.Kinematics;
import com.acmerobotics.roadrunner.profile.MotionProfile;
import com.acmerobotics.roadrunner.profile.MotionProfileGenerator;
import com.acmerobotics.roadrunner.profile.MotionState;
import com.acmerobotics.roadrunner.util.NanoClock;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;


import org.igutech.auto.roadrunner.DriveConstants;
import org.igutech.auto.roadrunner.SampleMecanumDrive;
import org.igutech.auto.util.LoggingUtil;

import static org.igutech.auto.roadrunner.DriveConstants.kA;
import static org.igutech.auto.roadrunner.DriveConstants.kStatic;
import static org.igutech.auto.roadrunner.DriveConstants.kV;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Objects;


/*
 * This routine is designed to tune the open-loop feedforward coefficients. Although it may seem unnecessary,
 * tuning these coefficients is just as important as the positional parameters. Like the other
 * manual tuning routines, this op mode relies heavily upon the dashboard. To access the dashboard,
 * connect your computer to the RC's WiFi network and navigate to https://192.168.49.1:8080/dash in
 * your browser. Once you've successfully connected, start the program, and your robot will begin
 * moving forward and backward according to a motion profile. Your job is to graph the velocity
 * errors over time and adjust the feedforward coefficients. Once you've found a satisfactory set
 * of gains, add them to your drive class.
 */
@Config
@Autonomous(group = "drive")
public class ManualFeedforwardTuner extends LinearOpMode {
    public static double DISTANCE = 72; // in

    private FtcDashboard dashboard = FtcDashboard.getInstance();

    private SampleMecanumDrive drive;
    PrintWriter pw;
    ArrayList<Double> time = new ArrayList<>();
    ArrayList<Double> leftEncoderVelo = new ArrayList<>();
    ArrayList<Double> leftEncoderTick = new ArrayList<>();
    ArrayList<Double> rightEncoderVelo = new ArrayList<>();
    ArrayList<Double> rightEncoderTick = new ArrayList<>();
    ArrayList<Double> strafeEncoderVelo = new ArrayList<>();
    ArrayList<Double> strafeEncoderTick = new ArrayList<>();
    long startTime;

    private static MotionProfile generateProfile(boolean movingForward) {
        MotionState start = new MotionState(movingForward ? 0 : DISTANCE, 0, 0, 0);
        MotionState goal = new MotionState(movingForward ? DISTANCE : 0, 0, 0, 0);
        return MotionProfileGenerator.generateSimpleMotionProfile(start, goal,
                DriveConstants.BASE_CONSTRAINTS.maxVel,
                DriveConstants.BASE_CONSTRAINTS.maxAccel,
                DriveConstants.BASE_CONSTRAINTS.maxJerk);
    }

    @Override
    public void runOpMode() {
        try {
            pw = new PrintWriter(LoggingUtil.getLogFile("info.csv"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        telemetry = new MultipleTelemetry(telemetry, dashboard.getTelemetry());

        drive = new SampleMecanumDrive(hardwareMap);

        NanoClock clock = NanoClock.system();

        telemetry.addLine("Ready!");
        telemetry.update();
        telemetry.clearAll();

        waitForStart();

        if (isStopRequested()) return;

        boolean movingForwards = true;
        MotionProfile activeProfile = generateProfile(true);
        double profileStart = clock.seconds();
        int i=0;
        startTime = System.currentTimeMillis();
        pw.println("time,leftEncoderTick,leftEncoderVelo,rightEncoderTick,rightEncoderVelo, strafeEncoderTick, starfeEncoderVelo");
        while (!isStopRequested()) {
            // calculate and set the motor power
            double profileTime = clock.seconds() - profileStart;
            time.add(profileTime);
            leftEncoderTick.add((double) drive.getWheelPositions().get(0));
            leftEncoderTick.add((double) drive.getWheelVelocities().get(0));

            rightEncoderTick.add(drive.getWheelPositions().get(1));
            rightEncoderVelo.add(drive.getWheelVelocities().get(1));
            strafeEncoderTick.add(drive.getWheelPositions().get(2));
            strafeEncoderVelo.add(drive.getWheelVelocities().get(2));
            //pw.println((time.get(i)) + "," + leftEncoderTick.get(i) + "," + leftEncoderVelo.get(i) + "," + rightEncoderTick.get(i) + "," +  rightEncoderVelo.get(i) + ","+ strafeEncoderTick.get(i) + "," +strafeEncoderVelo.get(i) + ",");
            pw.println(profileTime+","+drive.getWheelPositions().get(0) + "," + drive.getWheelVelocities().get(0) + "," + drive.getWheelPositions().get(1) + "," + drive.getWheelVelocities().get(1)  + ","+ drive.getWheelPositions().get(2) + ","+ drive.getWheelVelocities().get(2) + ",");

            if (profileTime > activeProfile.duration()) {
                // generate a new profile
                movingForwards = !movingForwards;
                activeProfile = generateProfile(movingForwards);
                profileStart = clock.seconds();
            }

            MotionState motionState = activeProfile.get(profileTime);
            double targetPower = Kinematics.calculateMotorFeedforward(motionState.getV(), motionState.getA(), kV, kA, kStatic);

            drive.setDrivePower(new Pose2d(targetPower, 0, 0));
            drive.updatePoseEstimate();

            // update telemetry
            telemetry.addData("targetVelocity", motionState.getV());

            Pose2d poseVelo = Objects.requireNonNull(drive.getPoseVelocity(), "poseVelocity() must not be null. Ensure that the getWheelVelocities() method has been overridden in your localizer.");
            double currentVelo = poseVelo.getX();
            telemetry.addData("left encoder tick", drive.getWheelPositions().get(0));
            telemetry.addData("right encoder tick", drive.getWheelPositions().get(1));
            telemetry.addData("strafe encoder tick", drive.getWheelPositions().get(2));
            telemetry.addData("poseVelocity", currentVelo);
            telemetry.addData("error", currentVelo - motionState.getV());

            telemetry.update();
            i=i+1;
        }
        pw.close();
    }
}
