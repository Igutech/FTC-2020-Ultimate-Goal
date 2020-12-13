package org.igutech.auto.vision;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

import java.util.ArrayList;

public class RingDetectionPipeline extends OpenCvPipeline {


    @Override
    public Mat processFrame(Mat input) {
        Mat hsvMat = new Mat();
        Mat mask = new Mat();
        Mat bitwise = new Mat();
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        MatOfPoint2f approxCurve = new MatOfPoint2f();

        Imgproc.cvtColor(input, hsvMat, Imgproc.COLOR_RGB2HSV);
        Core.inRange(hsvMat, new Scalar(8, 108, 115), new Scalar(43, 253, 247), mask);
        Core.bitwise_and(hsvMat, hsvMat, bitwise, mask);

        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            double contourArea = Imgproc.contourArea(contours.get(contourIdx));
            if (contourArea > 1000) {
                Imgproc.drawContours(bitwise, contours, contourIdx, new Scalar(255, 0, 0), 3);
                MatOfPoint2f newPoint = new MatOfPoint2f(contours.get(contourIdx).toArray());
                double permi = Imgproc.arcLength(newPoint, true);
                Imgproc.approxPolyDP(newPoint, approxCurve, permi * 0.02, true);
                MatOfPoint points = new MatOfPoint(approxCurve.toArray());

                Rect rect = Imgproc.boundingRect(points);
                Imgproc.rectangle(input, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255), 3);
                Imgproc.putText(input,"Points: "+rect.width+" "+rect.height,new Point(rect.x + rect.width+ 20 ,rect.y + 20),Imgproc.FONT_HERSHEY_SIMPLEX, .7, new Scalar(0, 255, 0), 2);
            }

        }

        hsvMat.release();
        mask.release();
        bitwise.release();
        hierarchy.release();
        return input; // Return the input mat
    }
}
