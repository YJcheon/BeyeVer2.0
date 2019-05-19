package com.Beye.capstone;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;


public final class CameraService extends Service {

    private static final String TAG = CameraService.class.getSimpleName();
    private boolean mStopThread;
    private Thread mThread;
    private VideoCapture mCamera;
    private int mCameraIndex = -1;

    public native void ConvertRGBtoGray(long matAddrInput, long matAddrResult);
    public native void Gaussian(long matAddrInput, long matAddrResult);
    public native void BinaryDilate(long matAddrInput, long matAddrResult);
    public native void BinaryEdge(long matAddrInput, long matAddrResult);
    public native void BlurImage(long matAddrInput, long matAddrResult);
    public native void ClosingFilter(long matAddrInput, long matAddrResult);
    public native void Watershed(long matAddrInput,long matAddrInput1, long matAddrResult);
    public native void Binary(long matAddrInput, long matAddrResult);
    public native void Eraseroad(long matAddrInput, long matAddrResult);
    public native void NormalEdge(long matAddrInput, long matAddrResult);
    public native boolean CalforCr(long matAddrInput);
    public native boolean Calfortick(long matAddrInput);
    public native void Calforob(long matAddrInput1, long matAddrInput2, long matAddrInput3, long matAddrResult);
    public native boolean Calforst(long matAddrInput);
    public native boolean Calfordown(long matAddrInput);
    public native boolean Calculateob(long matAddrInput);
    public int errorhandling=0;
    public int stairhandling=0;
    public int downhandling=0;
    public int noisehandiling =0;
    public boolean crossflag = false;
    public boolean tickflag = false;
    public boolean externflagcr = false;
    public boolean externflagbs = false;
    public boolean externflagtick = false;
    public void MatrixTime(int delayTime){
        long saveTime = System.currentTimeMillis();
        long currTime = 0;
        while( currTime - saveTime < delayTime){
            currTime = System.currentTimeMillis();
        }
    }
    private Mat matInput;
    private Mat matResult;
    private Mat origin;
    private Mat binary;
    private Mat edge;
    private Mat blur;
    private Mat blurforcheck;
    private Mat label;
    private Mat closing;
    private Mat calob;
    private Mat watershed;
    private Mat mRgba = new Mat(120, 160, CvType.CV_8UC4);
    private Mat mGray = new Mat(120, 160, CvType.CV_8UC4);

    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }
    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("", "OpenCV loaded successfully");

                    try {
                        if (!connectCamera(640, 480))
                            Log.e(TAG, "Could not connect camera");
                        else
                            Log.d(TAG, "Camera successfully connected");
                    } catch (Exception e) {
                        Log.e(TAG, "MyServer.connectCamera throws an exception: " + e.getMessage());
                    }

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_4, getApplicationContext(), mLoaderCallback))
            Log.i(TAG, "Loaded OpenCV");
        else
            Log.i(TAG, "Couldn't load OpenCV");
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        this.disconnectCamera();
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    private boolean connectCamera(int width, int height) {
        /* First step - initialize camera connection */
        if (!initializeCamera(width, height)) {
            Log.d(TAG, "initializeCamera failed");
            return false;
        } else {
            Log.d(TAG, "initializeCamera successfully");
            /* start update thread */
            mThread = new Thread(new CameraWorker());
            mThread.start();

            return true;
        }
    }

    private boolean initializeCamera(int width, int height) {
        synchronized (this) {
            if (mCameraIndex == -1)
                mCamera = new VideoCapture(Imgcodecs.IMREAD_COLOR);
            else
                mCamera = new VideoCapture(Imgcodecs.IMREAD_COLOR + mCameraIndex);

            if (mCamera == null)
                return false;

            if (mCamera.isOpened() == false)
                return false;

            /* Select the size that fits surface considering maximum size allowed */
            Size frameSize = new Size(width, height);
            mCamera.set(Imgcodecs.IMREAD_UNCHANGED, frameSize.width);
            mCamera.set(Imgcodecs.IMREAD_UNCHANGED, frameSize.height);
        }

        return true;
    }

    private void releaseCamera() {
        synchronized (this) {
            if (mCamera != null) {
                mCamera.release();
            }
        }
    }

    private void disconnectCamera() {
        // 1. Stop thread which updating the frames
        // 2. Stop camera and release it
        try {
            mStopThread = true;
            mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mThread =  null;
            mStopThread = false;
        }
        releaseCamera();
    }

    private class CameraWorker implements Runnable {
        public void run() {
            do {
                if (!mCamera.grab()) {
                    Log.e(TAG, "Camera frame grab failed");
                    break;
                }
                Log.e(TAG, "Camera frame grabbed");
                Log.d(this.getClass().getName(), "input oncameraFrame");
                mCamera.read(origin);
                origin.copyTo(mRgba);
                //if ( matResult != null ) matResult.release(); fix 2018. 8. 18

                if ( matResult == null )
                    matResult = new Mat(origin.rows(), origin.cols(), origin.type());
                mRgba.copyTo(origin);
                blur = new Mat(origin.rows(), origin.cols(), origin.type());
                edge = new Mat(origin.rows(), origin.cols(), origin.type());
                binary = new Mat(origin.rows(), origin.cols(), origin.type());
                label = new Mat(origin.rows(), origin.cols(), origin.type());
                closing = new Mat(origin.rows(), origin.cols(), origin.type());
                watershed = new Mat(origin.rows(), origin.cols(), origin.type());
                //blur for computer vision first.
                ConvertRGBtoGray(origin.getNativeObjAddr(), blur.getNativeObjAddr());
                Binary(blur.getNativeObjAddr(), binary.getNativeObjAddr());
                Gaussian(blur.getNativeObjAddr(), blur.getNativeObjAddr());
                blurforcheck = new Mat(blur.rows(), blur.cols(), blur.type());
                ClosingFilter(blur.getNativeObjAddr(), closing.getNativeObjAddr());
                BinaryDilate(blur.getNativeObjAddr(), blur.getNativeObjAddr());
                BinaryEdge(blur.getNativeObjAddr(), edge.getNativeObjAddr());
                Watershed(blurforcheck.getNativeObjAddr(),edge.getNativeObjAddr() , watershed.getNativeObjAddr());
                Calforob(binary.getNativeObjAddr(), edge.getNativeObjAddr(), origin.getNativeObjAddr(), matResult.getNativeObjAddr());
                tickflag = Calfortick(origin.getNativeObjAddr());
                //Eraseroad(matResult.getNativeObjAddr(), matResult.getNativeObjAddr());
                if(CalforCr(watershed.getNativeObjAddr())){
                    noisehandiling++;
                    if(noisehandiling > 7){
                        crossflag = true;
                    }
                }
                else if (noisehandiling > 7) {
                    noisehandiling = 7;
                }
                else if(noisehandiling>0){
                    noisehandiling--;
                }
                if(noisehandiling <5 && crossflag){
                    crossflag = false;
                }
                if(Calculateob(watershed.getNativeObjAddr())){
                    errorhandling++;
                }
                else if (errorhandling > 5) {
                    errorhandling = 5;
                }
                else {
                    errorhandling--;
                }
                if(Calforst(edge.getNativeObjAddr())){
                    stairhandling++;
                }
                else if (stairhandling > 7) {
                    stairhandling = 7;
                }
                else {
                    stairhandling--;
                }
                if(Calfordown(edge.getNativeObjAddr())){
                    downhandling++;
                }
                else if(downhandling>10){
                    downhandling=10;
                }
                else{
                    downhandling--;
                }
                if(stairhandling>3 && crossflag && !externflagcr &&!tickflag) {
                    Log.d(this.getClass().getName(), "opencvob");
                }
                else if(stairhandling>3 && crossflag && externflagcr &&!tickflag){
                    Log.d(this.getClass().getName(), "opencvcr");
                }
                else if(errorhandling>5 && !externflagbs &&!tickflag){
                    Log.d(this.getClass().getName(), "opencvup");
                }
                else if(errorhandling>5 && externflagbs &&!tickflag){
                    Log.d(this.getClass().getName(), "opencvbs");
                }
                else if(externflagtick && tickflag && errorhandling>5){
                    Log.d(this.getClass().getName(), "opencvtick");
                }
                else{
                    Log.d(this.getClass().getName(), "opencvr");
                }
                label = new Mat(matResult.rows(), matResult.cols(), origin.type());
            } while (!mStopThread);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not used
    }
}