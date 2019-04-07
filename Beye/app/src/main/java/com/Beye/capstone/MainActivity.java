package com.Beye.capstone;

import android.Manifest;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnInitListener{

    private Intent intent;
    private SpeechRecognizer mRecognizer;
    public TextToSpeech tts;
    private final int PERMISSION = 1;
    private GestureDetector mDetector;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if ( Build.VERSION.SDK_INT >= 23 ){
            // 퍼미션 체크
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},PERMISSION);
        }
        mDetector = new GestureDetector(this, new MyGestureListener());

        tts = new TextToSpeech(this, (TextToSpeech.OnInitListener) this);
        geocoder = new Geocoder(this);

        intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,Long.valueOf(20000L));

        mRecognizer=SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(listener);

        findViewById(R.id.mainLayout).setOnTouchListener(new View.OnTouchListener() {


            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return mDetector.onTouchEvent(event);
            }
        });

    }

    public void onInit(int status) {
        tts.speak("길찾기를 사용하시려면 한 번, 글자 읽기를 사용하시려면 두 번 터치해주세요.",TextToSpeech.QUEUE_FLUSH,null);

    }

    @Override
    protected  void onDestroy() {
        super.onDestroy();
        if(tts != null) {
            tts.stop();
            tts.shutdown();
            tts =null;
        }
        if(mRecognizer != null)  {
            mRecognizer.destroy();
            mRecognizer.cancel();
            mRecognizer = null;
        }
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            tts.speak("길찾기를 진행하겠습니다. 목적지를 말해주세요.",TextToSpeech.QUEUE_FLUSH,null);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mRecognizer.startListening(intent);
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            tts.speak("글자 읽기를 진행하겠습니다.",TextToSpeech.QUEUE_FLUSH,null);
            Intent intent = new Intent(getApplicationContext(), ReadActivity.class);
            startActivity(intent);

            return true;
        }

    }

    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float rmsdB) {}

        @Override
        public void onBufferReceived(byte[] buffer) {}

        @Override
        public void onEndOfSpeech() {}

        @Override
        public void onError(int error) {
            String message;

            mRecognizer.destroy();
            mRecognizer.cancel();
            mRecognizer=SpeechRecognizer.createSpeechRecognizer(getBaseContext());
            mRecognizer.setRecognitionListener(listener);

            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션 없음 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃 에러";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을 수 없는 에러";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "인식기가 바쁜 에러";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버 에러";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과 에러";
                    break;
                default:
                    message = "알 수 없는 에러";
                    break;
            }
            Toast.makeText(getApplicationContext(), "에러가 발생하였습니다. : " + message,Toast.LENGTH_SHORT).show();
            tts.speak(message + "가 발생했습니다. 목적지를 말해주세요.",TextToSpeech.QUEUE_FLUSH,null);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mRecognizer.startListening(intent);



        }

        @Override
        public void onResults(Bundle results) {
            // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어줍니다.
            ArrayList<String> matches =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            List<Address> addressList = null;
            try {
                addressList = geocoder.getFromLocationName(matches.get(0),10);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            if(addressList.isEmpty()) {
                tts.speak("알 수 없는 장소입니다. 목적지를 말해주세요.",TextToSpeech.QUEUE_FLUSH,null);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mRecognizer.startListening(intent);
            }
            else {
                Intent intent = new Intent(getApplicationContext(), RoadActivity.class);
                intent.putExtra("dest", addressList.get(0).toString());
                intent.putExtra("destName",matches.get(0));
                startActivity(intent);
            }

        }

        @Override
        public void onPartialResults(Bundle partialResults) {}

        @Override
        public void onEvent(int eventType, Bundle params) {}
    };
}
