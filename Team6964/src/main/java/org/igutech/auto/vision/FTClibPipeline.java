package org.igutech.auto.vision;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

@Config
public class FTClibPipeline extends OpenCvPipeline {

    private Mat matYCrCb = new Mat();
    private Mat matCbBottom = new Mat();
    private Mat matCbTop = new Mat();
    private Mat topBlock = new Mat();
    private Mat bottomBlock = new Mat();
    private double topAverage;
    private double bottomAverage;
    public static double widthPercentage, heightPercentage;
    public static int rectangleWidth, rectangleHeight, rectangleDistance;

    @Override
    public Mat processFrame(Mat input) {
        Imgproc.cvtColor(input, matYCrCb, Imgproc.COLOR_RGB2YCrCb);

        int topOne =                     (int) (matYCrCb.width() * widthPercentage);
        int topTwo =                (int) (matYCrCb.width() * widthPercentage) + rectangleWidth;
        int topThree=                (int) (matYCrCb.height() * heightPercentage);
        int topFour=                (int) (matYCrCb.height() * heightPercentage) + rectangleHeight;

        int[] topRect = {
                topOne,topTwo,topThree,topFour
        };

        int bottomOne =                 (int) (matYCrCb.width() * widthPercentage);
        int bottomTwo =                 (int) (matYCrCb.width() * widthPercentage) + rectangleWidth;
        int bottomThree =                 (int) (matYCrCb.height() * heightPercentage) + rectangleDistance;
        int bottomFour =                 (int) (matYCrCb.height() * heightPercentage) + (rectangleDistance + rectangleHeight);

        int[] bottomRect = {
                bottomOne,bottomTwo,bottomThree,bottomFour
        };

        drawRectOnToMat(input, topRect, new Scalar(255, 0, 0));
        drawRectOnToMat(input, bottomRect, new Scalar(0, 255, 0));

        bottomBlock = matYCrCb.submat(bottomRect[1], bottomRect[3], bottomRect[0], bottomRect[2]);
        topBlock = matYCrCb.submat(topRect[1], topRect[3], topRect[0], topRect[2]);

        Core.extractChannel(bottomBlock, matCbBottom, 2);
        Core.extractChannel(topBlock, matCbTop, 2);

        Scalar bottomMean = Core.mean(matCbBottom);
        Scalar topMean = Core.mean(matCbTop);

        bottomAverage = bottomMean.val[0];
        topAverage = topMean.val[0];

        return input;
    }

    private void drawRectOnToMat(Mat mat, int[] rect, Scalar color) {
        Imgproc.rectangle(
                mat,
                new Point(
                        rect[0],
                        rect[1]),

                new Point(
                        rect[2],
                        rect[3]),
                color, 1);
    }

    public double getTopAverage() {
        return topAverage;
    }

    public double getBottomAverage() {
        return bottomAverage;
    }
}
