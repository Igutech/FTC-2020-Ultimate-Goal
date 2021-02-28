package org.igutech.auto.paths;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.arcrobotics.ftclib.vision.UGContourRingPipeline;

import org.igutech.auto.FullRedAuto;
import dev.raneri.statelib.State;

import org.jetbrains.annotations.Nullable;

public class PrepareToShootState extends State {

    private boolean done = false;
    private Trajectory prepareToShoot;
    private FullRedAuto redAutoInstance;
    private Trajectory prepareToShoot2;

    public PrepareToShootState(FullRedAuto redAutoInstance, Pose2d start) {
        this.redAutoInstance = redAutoInstance;
        if (redAutoInstance.getHeight() == UGContourRingPipeline.Height.ZERO) {
            prepareToShoot = redAutoInstance.getDrive().trajectoryBuilder(start)
                    .addDisplacementMarker(() -> {
                        redAutoInstance.getHardware().getServos().get("wobbleGoalLift").setPosition(0.15);
                    })
                    .splineToConstantHeading(new Vector2d(-60.0, -25), Math.toRadians(0.0))
                    .splineToConstantHeading(new Vector2d(-7.0, -40), Math.toRadians(0.0))
                    .addDisplacementMarker(() -> done = true)
                    .build();
        } else if (redAutoInstance.getHeight() == UGContourRingPipeline.Height.ONE) {
            prepareToShoot = redAutoInstance.getDrive().trajectoryBuilder(start)
                    .addDisplacementMarker(() -> {
                        redAutoInstance.getHardware().getServos().get("wobbleGoalLift").setPosition(0.15);
                    })
                    .splineToConstantHeading(new Vector2d(-55.0, -20), Math.toRadians(0.0))
                    .splineToConstantHeading(new Vector2d(-10.0, -20.0), Math.toRadians(0.0))
                    .splineToConstantHeading(new Vector2d(-10.0, -45), Math.toRadians(0.0))
                    .addDisplacementMarker(() -> done = true)
                    .build();
        } else {
            prepareToShoot = redAutoInstance.getDrive().trajectoryBuilder(start)
                    .addDisplacementMarker(() -> {
                        redAutoInstance.getHardware().getServos().get("wobbleGoalLift").setPosition(0.15);
                    })
                    .splineToConstantHeading(new Vector2d(-60.0, -30.0), Math.toRadians(0.0))
                    .splineToConstantHeading(new Vector2d(-45.0, -30.0), Math.toRadians(0.0))
                    .addDisplacementMarker(() -> redAutoInstance.getDrive().followTrajectoryAsync(prepareToShoot2))
                    .build();
            prepareToShoot2 = redAutoInstance.getDrive().trajectoryBuilder(prepareToShoot.end())
                    .lineToLinearHeading(new Pose2d(-37, -38, Math.toRadians(-4)))
                    .addDisplacementMarker(() -> {
                        done = true;
                        System.out.println("Prepare to shoot callback ended");
                    })
                    .build();

        }
    }

    @Override
    public void onEntry(@Nullable State previousState) {
        redAutoInstance.getDrive().followTrajectoryAsync(prepareToShoot);
    }

    @Override
    public @Nullable
    State getNextState() {
        if (done) {
            if (redAutoInstance.getHeight() == UGContourRingPipeline.Height.FOUR) {
                return new ShootingPreloadRingsState(redAutoInstance, prepareToShoot2.end());
            }
            return new ShootingPreloadRingsState(redAutoInstance, prepareToShoot.end());
        } else {
            return null;
        }
    }
}
