package com.Beye.capstone;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

public class GpsInfo extends Service implements LocationListener {
    TextView textView;
    private int routeIndex;
    private int pathIndex;
    private int routeLength;
    private boolean isGuid;
    private Route[] route;
    private TextToSpeech tts;
    private final Context mContext;

    // 현재 GPS 사용유무
    boolean isGPSEnabled = false;

    // 네트워크 사용유무
    boolean isNetworkEnabled = false;

    // GPS 상태값
    boolean isGetLocation = false;

    Location location;
    private double lat; // 위도
    private double lon; // 경도

    // 최소 GPS 정보 업데이트 거리(단위 : 미터)
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;

    // 최소 GPS 정보 업데이트 시간(단위: 밀리세컨, 1000 = 1초)
    private static final long MIN_TIME_BW_UPDATES = 1000 ;

    protected LocationManager locationManager;

    public GpsInfo(Context context, TextView textView, TextToSpeech tts) {
        this.mContext = context;
        this.textView = textView;
        this.tts = tts;
        isGuid = false;
        getLocation();
    }

    public void setGuid(boolean isGuid, Route[] route, int routeLength){
        this.isGuid = isGuid;
        this.route = route;
        routeIndex = 0;
        pathIndex = 0;
        this.routeLength = routeLength;
    }

    @TargetApi(23)
    public Location getLocation() {
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(
                        mContext, android.Manifest.permission.ACCESS_FINE_LOCATION )
                        != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                        mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            return null;
        }

        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // GPS 정보 가져오기
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // 현재 네트워크 상태 값 알아오기
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // GPS 와 네트워크사용이 가능하지 않을때 소스 구현
            } else {
                this.isGetLocation = true;
                // 네트워크 정보로 부터 위치값 가져오기
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            // 위도 경도 저장
                            lat = location.getLatitude();
                            lon = location.getLongitude();
                        }
                    }
                }

                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                lat = location.getLatitude();
                                lon = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    /**
     * GPS 종료
     * */
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GpsInfo.this);
        }
    }

    /**
     * 위도값을 가져옵니다.
     * */
    public double getLatitude(){
        if(location != null){
            lat = location.getLatitude();
        }
        return lat;
    }

    /**
     * 경도값을 가져옵니다.
     * */
    public double getLongitude(){
        if(location != null){
            lon = location.getLongitude();
        }
        return lon;
    }

    /**
     * GPS 나 wife 정보가 켜져있는지 확인합니다.
     * */
    public boolean isGetLocation() {
        return this.isGetLocation;
    }

    /**
     * GPS 정보를 가져오지 못했을때
     * 설정값으로 갈지 물어보는 alert 창
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        alertDialog.setTitle("GPS 사용유무셋팅");
        alertDialog.setMessage("GPS 셋팅이 되지 않았을수도 있습니다. \n 설정창으로 가시겠습니까?");

        // OK 를 누르게 되면 설정창으로 이동합니다.
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        mContext.startActivity(intent);
                    }
                });
        // Cancle 하면 종료 합니다.
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onLocationChanged(Location location) {
        if(isGuid == true) {
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();
            Double destLatitude = route[routeIndex].getLatitude(pathIndex);
            Double destLongitude = route[routeIndex].getLongitude(pathIndex);
            float distance[] = new float[1];
            Location.distanceBetween(destLatitude,destLongitude,latitude,longitude,distance);

            String speech = route[routeIndex].getPath(pathIndex);
            String msg = "거리 : " + distance[0] + "\n현재 위치\n위도 : " + latitude + " 경도 : " + longitude + "\n";
            msg += "목적지\n위도 : " + destLatitude + " 경도 : " + destLongitude + "\n";

            if((int) distance[0] < 10) {
                pathIndex++;
                if(pathIndex >= route[routeIndex].getSize()) {
                    speech += "했습니다.";
                    routeIndex++;
                    pathIndex=0;
                    if(routeIndex == routeLength) {
                        speech += "길안내를 종료합니다.";
                        isGuid = false;
                    }
                }
                else {
                    speech += "하세요.";
                }
                msg += speech;
                tts.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
            }
            textView.setText(msg);
        }

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }
}

