package org.igutech.utils;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorImpl;
import com.qualcomm.robotcore.hardware.DcMotorImplEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

public class CachingMotor {
    private DcMotor motor;
    private double prevPower=0.0;
    public CachingMotor(DcMotor motor){
        this.motor = motor;
    }

    public void setPower(double power){
        if(Math.abs(power-prevPower)>0.01){
            motor.setPower(power);
        }
        prevPower = power;
    }
}
