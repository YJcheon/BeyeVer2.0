package com.example.opencvwithcmake;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.os.Handler;
import java.lang.String;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class MainActivity extends AppCompatActivity
        implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "opencv";
    private CameraBridgeViewBase mOpenCvCameraView;
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
    private Mat mRgba = new Mat(240, 320, CvType.CV_8UC4);
    private Mat mGray = new Mat(240, 320, CvType.CV_8UC4);

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

    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }


    final Handler handler = new Handler(){
        public void handleMessage(Message msg){
            TextView textView1 = (TextView) findViewById(R.id.cv_check) ;
            super.handleMessage(msg);
            textView1.setText((String)msg.obj) ;
        }
    };
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //퍼미션 상태 확인
            if (!hasPermissions(PERMISSIONS)) {

                //퍼미션 허가 안되어있다면 사용자에게 요청
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }
        mOpenCvCameraView = (CameraBridgeViewBase)findViewById(R.id.activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(0); // front-camera(1),  back-camera(0)
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
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
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "onResum :: OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();

        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        origin = inputFrame.rgba();
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
            String upst = "상향계단";
            Message upmsg = handler.obtainMessage();
            upmsg.obj = (Object)upst;
            handler.sendMessage(upmsg);
        }
        else if(stairhandling>3 && crossflag && externflagcr &&!tickflag){
            String crst = "횡단보도";
            Message crmsg = handler.obtainMessage();
            crmsg.obj = (Object)crst;
            handler.sendMessage(crmsg);
        }
        else if(errorhandling>5 && !externflagbs &&!tickflag){
            String obst = "장애물";
            Message obmsg = handler.obtainMessage();
            obmsg.obj = (Object)obst;
            handler.sendMessage(obmsg);
        }
        else if(errorhandling>5 && externflagbs &&!tickflag){
            String bsst = "버스정류장";
            Message bsmsg = handler.obtainMessage();
            bsmsg.obj = (Object)bsst;
            handler.sendMessage(bsmsg);
        }
        else if(externflagtick && tickflag && errorhandling>5){
            String tist = "개찰구";
            Message timsg = handler.obtainMessage();
            timsg.obj = (Object)tist;
            handler.sendMessage(timsg);
        }
        else{
            String road = "도로";
            Message inmsg = handler.obtainMessage();
            inmsg.obj = (Object)road;
            handler.sendMessage(inmsg);
        }
        label = new Mat(matResult.rows(), matResult.cols(), origin.type());
        MatrixTime(50);

        return origin;
    }

    //여기서부턴 퍼미션 관련 메소드
    static final int PERMISSIONS_REQUEST_CODE = 1000;
    String[] PERMISSIONS  = {"android.permission.CAMERA"};


    private boolean hasPermissions(String[] permissions) {
        int result;

        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions){

            result = ContextCompat.checkSelfPermission(this, perms);

            if (result == PackageManager.PERMISSION_DENIED){
                //허가 안된 퍼미션 발견
                return false;
            }
        }

        //모든 퍼미션이 허가되었음
        return true;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){

            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraPermissionAccepted = grantResults[0]
                            == PackageManager.PERMISSION_GRANTED;

                    if (!cameraPermissionAccepted)
                        showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                }
                break;
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder( MainActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id){
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        builder.create().show();
    }


}