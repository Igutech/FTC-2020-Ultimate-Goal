package org.igutech.utils.control;

import org.apache.commons.math3.util.FastMath;

/**
 * Created by Kevin on 7/21/2018.
 */

public class PIDFController implements BasicController {

    private long currentTimeMillis;
    private double kP, kI, kD, kF;
    private double iTerm = 0;
    private double prevError = 0;
    private double sp;

    public PIDFController(double kP, double kI, double kD, double kF) {
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
        this.kF = kF;
    }

    @Override
    public void init() {
        currentTimeMillis = System.currentTimeMillis();
    }

    @Override
    public double update(double pv) {
        if (FastMath.abs(error(pv)) <= 1) return 0;
        double timeOffset = System.currentTimeMillis() - currentTimeMillis;
        if (timeOffset < 1)
            timeOffset = 1;
        timeOffset = Math.max(timeOffset, 1);
        timeOffset /= 1000;
        //Log.d("PID_CONTROLLER", "\t" + timeOffset + "");
        double p = kP * error(pv);
        iTerm += error(pv) * timeOffset;
        double i = kI * iTerm;
        double d = kD * ((error(pv) - prevError) / timeOffset);
        double f = kF * sp;
        currentTimeMillis = System.currentTimeMillis();
        prevError = error(pv);
        return p + i + d + f;
    }

    public void reset(double pv) {
        prevError = error(pv);
        iTerm = 0;
        currentTimeMillis = System.currentTimeMillis();
    }

    @Override
    public void updateSetpoint(double sp) {
        this.sp = sp;
    }

    private double error(double pv) {
        return sp - pv;
    }

    public double getkP() {
        return kP;
    }

    public void setkP(double kP) {
        this.kP = kP;
    }

    public double getkI() {
        return kI;
    }

    public void setkI(double kI) {
        this.kI = kI;
    }

    public double getkD() {
        return kD;
    }

    public void setkD(double kD) {
        this.kD = kD;
    }

    public void setkF(double kF) {
        this.kF = kF;
    }

    public void setPIDFValues(double kp, double ki, double kd, double kf){
        kP = kp;
        kI = ki;
        kD = kd;
        kF = kf;
    }


}
