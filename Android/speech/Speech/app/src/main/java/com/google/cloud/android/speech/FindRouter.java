package com.google.cloud.android.speech;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.TimerTask;

public class FindRouter extends TimerTask implements TextToSpeech.OnInitListener{

    private double startLatitude = 0;
    private double startLongitude = 0;
    private double desLatitude = 0;
    private double desLongitude = 0;


    private HttpURLConnection conn = null;
    private TextToSpeech tts;
    private Context context;
    private GpsInfo gps;


    public FindRouter(Context context, GpsInfo gps) {
        this.context = context;
        this.gps = gps;
    }

    @Override
    public void run() {

        if(desLatitude == 0) {
            return;
        }

        if (gps.isGetLocation()) {
            gps.getLocation();
            startLatitude = gps.getLatitude();
            startLongitude = gps.getLongitude();

        } else {
            gps.showSettingsAlert();
        }

        tts = new TextToSpeech(context, this);


        try {
            URL url = new URL("https://api2.sktelecom.com/tmap/routes/pedestrian?version=1" +
                    "&format=json" +
                    "&appKey=46517717-fc78-4a30-b3ec-72635f6a8119" +
                    "&startX=" +
                    startLongitude +
                    "&startY=" +
                    startLatitude +
                    "&angle=1" +
                    "&speed=3" +
                    "&endPoiId=334852" +
                    "&endRpFlag=8" +
                    "&endX=" +
                    desLongitude +
                    "&endY=" +
                    desLatitude +
//                            "&passList=126.98506595175428,37.56674182109044,334857,16" +
                    "&reqCoordType=WGS84GEO" +
                    "&gpsTime=15000" +
                    "&startName=%EC%B6%9C%EB%B0%9C" +
                    "&endName=%EB%B3%B8%EC%82%AC" +
                    "&searchOption=0" +
                    "&resCoordType=WGS84GEO");

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setConnectTimeout(60);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(line);
            }

            JSONObject jAr = new JSONObject(sb.toString());
            JSONArray features = jAr.getJSONArray("features");


            String ttsText = null;
            String secondText = null;


            for (int i = 0; i < features.length(); i++) {
                JSONObject test = features.getJSONObject(i);
                JSONObject properties = test.getJSONObject("properties");
                if(i == 0) {
                    ttsText  = properties.getString("description");
                }
                else if(i == 2) {
                    secondText = properties.getString("description");
                    if(secondText.contains("좌회전")) {
                        secondText = " 후 좌회전";
                    }
                    else if(secondText.contains("우회전")) {
                        secondText = " 후 우회전";
                    }

                    else if(secondText.contains("도착")) {
                        secondText = " 후 도착";
                    }
                }
                //System.out.println(properties.getString("description"));
            }

            ttsText += secondText;
            System.out.println(ttsText);
            tts.setPitch((float) 0.1);
            tts.setSpeechRate((float) 1.0);
            tts.speak(ttsText, TextToSpeech.QUEUE_FLUSH, null);


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            // 작업 성공
            int language = tts.setLanguage(Locale.KOREAN);  // 언어 설정

            if (language == TextToSpeech.LANG_MISSING_DATA

                    || language == TextToSpeech.LANG_NOT_SUPPORTED) {

                // 언어 데이터가 없거나, 지원하지 않는경우

                //btn_speech.setEnabled(false);

                Toast.makeText(context, "지원하지 않는 언어입니다.", Toast.LENGTH_SHORT).show();

            } else {
                // 준비 완료
                //btn_speech.setEnabled(true);
            }


        } else {
            // 작업 실패
            Toast.makeText(context, "TTS 작업에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public void setStartLatitude(double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public void setStartLongitude(double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public void setDesLatitude(double desLatitude) {
        this.desLatitude = desLatitude;
    }

    public void setDesLongitude(double desLongitude) {
        this.desLongitude = desLongitude;
    }


}
