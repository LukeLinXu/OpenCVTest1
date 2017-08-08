package com.example.lukelin.opencvtest1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean              mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        Log.d(TAG, "onCameraViewStarted: "+width+","+height);
    }

    public void onCameraViewStopped() {
        Log.d(TAG, "onCameraViewStopped: ");
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.d(TAG, "onCameraFrame: "+inputFrame);
        int threshold = 50;
        Mat edges = new Mat();

        Imgproc.Canny(inputFrame.gray(), edges, threshold, threshold*3);
//        Imgproc.findContours(edges, contours, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        //convert the image to black and white does (8 bit)
        Imgproc.Canny(inputFrame.gray(), edges, 50, 50);

        //apply gaussian blur to smoothen lines of dots
        Imgproc.GaussianBlur(edges, edges, new Size(5, 5), 5);

        //find the contours
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(edges, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        double maxArea = -1;
        int maxAreaIdx = -1;
        MatOfPoint temp_contour = contours.get(0); //the largest is at the index 0 for starting point
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        Mat largest_contour = contours.get(0);
        List<MatOfPoint> largest_contours = new ArrayList<MatOfPoint>();
        for (int idx = 0; idx < contours.size(); idx++) {
            temp_contour = contours.get(idx);
            double contourarea = Imgproc.contourArea(temp_contour);
            //compare this contour to the previous largest contour found
            if (contourarea > maxArea) {
                //check if this contour is a square
                MatOfPoint2f new_mat = new MatOfPoint2f( temp_contour.toArray() );
                int contourSize = (int)temp_contour.total();
                Imgproc.approxPolyDP(new_mat, approxCurve, contourSize*0.05, true);
                if (approxCurve.total() == 4) {
                    maxArea = contourarea;
                    maxAreaIdx = idx;
                    approxCurve.convertTo(temp_contour, CvType.CV_32S);
                    largest_contours.add(temp_contour);
                    largest_contour = temp_contour;
                }
            }
        }
        MatOfPoint temp_largest = largest_contours.get(largest_contours.size()-1);
        largest_contours.clear();
        largest_contours.add(temp_largest);

        Imgproc.cvtColor(edges, edges, Imgproc.COLOR_BayerBG2RGB);
        edges = inputFrame.rgba();
        Imgproc.drawContours(edges, largest_contours, -1, new Scalar(0, 255, 0), 1);

        //create the new image here using the largest detected square

        return edges;
    }
//
//    private void ssss() {
//        MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
//        MatOfPoint2f approxCurve = new MatOfPoint2f();
//
//        for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
//            MatOfPoint contour = contours.get(idx);
//            Rect rect = Imgproc.boundingRect(contour);
//            double contourArea = Imgproc.contourArea(contour);
//            matOfPoint2f.fromList(contour.toList());
//            Imgproc.approxPolyDP(matOfPoint2f, approxCurve, Imgproc.arcLength(matOfPoint2f, true) * 0.02, true);
//            long total = approxCurve.total();
//            if (total == 3) { // is triangle
//                // do things for triangle
//            }
//            if (total >= 4 && total <= 6) {
//                List<Double> cos = new ArrayList<>();
//                Point[] points = approxCurve.toArray();
//                for (int j = 2; j < total + 1; j++) {
//                    cos.add(angle(points[(int) (j % total)], points[j - 2], points[j - 1]));
//                }
//                Collections.sort(cos);
//                Double minCos = cos.get(0);
//                Double maxCos = cos.get(cos.size() - 1);
//                boolean isRect = total == 4 && minCos >= -0.1 && maxCos <= 0.3;
//                boolean isPolygon = (total == 5 && minCos >= -0.34 && maxCos <= -0.27) || (total == 6 && minCos >= -0.55 && maxCos <= -0.45);
//                if (isRect) {
//                    double ratio = Math.abs(1 - (double) rect.width / rect.height);
//                    drawText(rect.tl(), ratio <= 0.02 ? "SQU" : "RECT");
//                }
//                if (isPolygon) {
//                    drawText(rect.tl(), "Polygon");
//                }
//            }
//        }
//    }

    private double angle(Point pt1, Point pt2, Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1*dx2 + dy1*dy2)/Math.sqrt((dx1*dx1 + dy1*dy1)*(dx2*dx2 + dy2*dy2) + 1e-10);
    }

//    private void drawText(Point ofs, String text) {
//        Imgproc.putText(colorImage, text, ofs, Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255,255,25);
//    }
}