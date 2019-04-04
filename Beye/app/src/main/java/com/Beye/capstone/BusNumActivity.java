package com.Beye.capstone;

import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.Beye.capstone.camera.CameraSource;
import com.Beye.capstone.camera.CameraSourcePreview;
import com.Beye.capstone.others.GraphicOverlay;
import com.Beye.capstone.text_detection.TextRecognitionProcessor;
import com.google.firebase.FirebaseApp;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Locale;

import static android.media.audiofx.AudioEffect.ERROR;

public class BusNumActivity extends AppCompatActivity {

    //region ----- Instance Variables -----
    GestureDetector mDetector;
    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    public static String busnumber;
    private static String TAG = BusNumActivity.class.getSimpleName().toString().trim();
    public static TextToSpeech tts;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_num);
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR) {
                    // 언어를 선택한다.
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });
        Intent intent = getIntent();
        busnumber = intent.getExtras().getString("busnumber");
     ;
        TextView receivenum = (TextView)findViewById(R.id.bustext);
        receivenum.setText(busnumber);
        //FirebaseApp.initializeApp(this);

        mDetector = new GestureDetector(this, new BusNumActivity.MyGestureListener());
        findViewById(R.id.buslayout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return mDetector.onTouchEvent(event);
            }
        });

        preview = (CameraSourcePreview) findViewById(R.id.camera_source_preview);
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }
        graphicOverlay = (GraphicOverlay) findViewById(R.id.graphics_overlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        createCameraSource();
        startCameraSource();
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
           finish();

            return true;
        }

    }


    public void setBusNum(String busnumber){
        this.busnumber=busnumber;
    }
    public String getBusNum(){
        return busnumber;
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        startCameraSource();
    }

    /** Stops the camera. */
    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    private void createCameraSource() {

        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
            cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
        }

        cameraSource.setMachineLearningFrameProcessor(new TextRecognitionProcessor());
    }

    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }
}
