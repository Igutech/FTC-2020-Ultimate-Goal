package org.igutech.auto.paths;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.arcrobotics.ftclib.vision.UGContourRingPipeline;

import org.igutech.auto.FullRedAuto;
import org.igutech.auto.statelib.State;
import org.jetbrains.annotations.Nullable;


public class ShootingPreloadRingsState extends State {
    private FullRedAuto fullRedAuto;
    private boolean done = false;
    private Pose2d previous;
    public ShootingPreloadRingsState(FullRedAuto fullRedAuto, Pose2d previous) {
        this.fullRedAuto = fullRedAuto;
        this.previous = previous;
    }

    @Override
    public void onEntry(@Nullable State previousState) {
        fullRedAuto.setShooterEnabled(true);
        fullRedAuto.handleLift(1,true,()->{
            done = true;
        });
    }

    @Override
    public @Nullable State getNextState() {
        if(done){
            System.out.println("Transitioning from shooting ring stack to move to target zone");
            if(fullRedAuto.getHeight()== UGContourRingPipeline.Height.FOUR){
                fullRedAuto.getHardware().getServos().get("releaseLiftServo").setPosition(0.2);
                return new IntakeRingStack(fullRedAuto,previous);
            }else{
                return new MoveToTargetZoneFirstTime(fullRedAuto, previous);
            }
        }
        return null;
    }
}
