package org.igutech.auto.paths;


import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.arcrobotics.ftclib.vision.UGContourRingPipeline;

import org.igutech.auto.FullRedAuto;
import org.igutech.auto.statelib.State;
import org.jetbrains.annotations.Nullable;

public class PrepareToShootState extends State {

    private boolean done = false;
    private Trajectory prepareToShoot;
    private FullRedAuto redAutoInstance;

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
        } else {
            prepareToShoot = redAutoInstance.getDrive().trajectoryBuilder(start)
                    .addDisplacementMarker(() -> {
                        redAutoInstance.getHardware().getServos().get("wobbleGoalLift").setPosition(0.15);
                    })
                    .splineToConstantHeading(new Vector2d(-55.0, -20), Math.toRadians(0.0))
                    .splineToConstantHeading(new Vector2d(-10.0, -20.0), Math.toRadians(0.0))
                    .splineToConstantHeading(new Vector2d(-10.0, -45), Math.toRadians(0.0))
                    .addDisplacementMarker(() -> done = true)
                    .build();
        }
    }

    @Override
    public void onEntry(@Nullable State previousState) {
        redAutoInstance.getDrive().followTrajectoryAsync(prepareToShoot);
    }

    @Override
    public @Nullable State getNextState() {
        if (done) {
            System.out.println("Transitioning from move to shoot ring stack to shooting ring stack");
            return new ShootingPreloadRingsState(redAutoInstance, prepareToShoot.end());
        } else {
            return null;
        }
    }
}
