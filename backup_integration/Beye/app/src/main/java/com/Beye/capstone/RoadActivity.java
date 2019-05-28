package com.Beye.capstone;

import android.Manifest;
import android.content.Intent;
import java.lang.String;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.odsay.odsayandroidsdk.API;
import com.odsay.odsayandroidsdk.ODsayData;
import com.odsay.odsayandroidsdk.ODsayService;
import com.odsay.odsayandroidsdk.OnResultCallbackListener;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapTapi;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.TimerTask;
import java.util.Timer;


public class RoadActivity extends AppCompatActivity implements TextToSpeech.OnInitListener,CameraBridgeViewBase.CvCameraViewListener2 {
    private ODsayService odsayService;
    private TextView textView;
    private String dest;
    private double latitude;
    private double longitude;
    private TextToSpeech tts;
    private String destLatitude;
    private String destLongitude;
    private TMapTapi tmaptapi;
    private TMapData tmapdata;
    private int routeLength;
    private static final Key key = new Key();
    private Route[] route;
    private String destName;
    private boolean isGuid;
    private int routeIndex = 0;
    private int pathIndex = 0;
    private boolean isGetLocation = false;
    private boolean isFindBus = false;

    protected LocationRequest mLocationRequest;
    protected FusedLocationProviderClient mFusedLocationClient;
    Location location;

    public static final float MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1f; // in Meters
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat matInput;
    private Mat matResult;
    private Mat origin = new Mat(120, 120, CvType.CV_8UC4);
    private Mat binary;
    private Mat edge;
    private Mat blur;
    private Mat blurforcheck;
    private Mat label;
    private Mat closing;
    private Mat calob;
    private Mat watershed;
    private Mat mRgba = new Mat(120, 120, CvType.CV_8UC4);
    private Mat mGray = new Mat(120, 120, CvType.CV_8UC4);
    public static String[] substation9=new String[]{"개화", "김포공항","공항시장","신방화","마곡나루","양천향교","가양","증미","등촌","염창",
            "신목동","선유도","당산","국회의사당","여의도","샛강","노량진","노들","흑석","동작","구반포","신반포","고속터미널","사평","신논현","언주","선정릉",
            "삼성중앙","봉은사","종합운동장","삼전","석촌고분","석촌","송파나루","한성백제","올림픽공원","둔촌오륜","중앙보훈병원"};;
    public static String[] getsubstation9(){
        return substation9;
    }
    public native void Convert90(long matAddrInput);
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
    public native boolean Checksubwaystation(long matAddrInput);
    public native boolean Calculatetrafficlight(long matAddrInput,int left_global,int top_global,int width_global,int height_global,int count_global);
    public int errorhandling=0;
    public int stairhandling=0;
    public int downhandling=0;
    public int noisehandiling =0;
    public boolean crossflag = false;
    public boolean tickflag = false;
    public boolean externflagcr = false;
    public boolean externflagbs = false;
    public boolean externflagtick = false;
    public boolean ttsflag = true;
    public boolean obflag = false;
    public boolean tgflag = false;
    public boolean drflag = false;
    public boolean subflag = false;
    public boolean entranceflag = false;
    public boolean trafficflag = false;
    public boolean checkflag = false;
    public int left =0;
    public int top =0;
    public int width =0;
    public int height =0;
    public int count =0;
    public String busNo;
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
    protected void onCreate(Bundle savedInstanceState )  {
        super.onCreate(savedInstanceState);

        TimerTask tt = new TimerTask(){
            @Override
            public void run() {
                ttsflag = false;
            }
        };
        Timer timer = new Timer();
        timer.schedule(tt, 5000, 5000);
        setContentView(R.layout.activity_road);
        textView = (TextView)findViewById(R.id.sttResult);
        textView.setMovementMethod(new ScrollingMovementMethod());
        tts = new TextToSpeech(this, (TextToSpeech.OnInitListener) this);
        tmaptapi = new TMapTapi(this);
        tmaptapi.setSKTMapAuthentication (key.getTmapApiKey());
        tmapdata = new TMapData();
        route = new Route[20];

        Intent intent = getIntent();
        dest = intent.getExtras().getString("dest");
        destName = intent.getExtras().getString("destName");
        // 싱글톤 생성, Key 값을 활용하여 객체 생성
        odsayService = ODsayService.init(this, key.getOdsayApiKey());
        // 서버 연결 제한 시간(단위(초), default : 5초)
        odsayService.setReadTimeout(5000);
        // 데이터 획득 제한 시간(단위(초), default : 5초)
        odsayService.setConnectionTimeout(5000);

        // 콤마를 기준으로 split
        String []splitStr = dest.split(",");
        destLatitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // 위도
        destLongitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // 경도

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
//        mLocationRequest.setSmallestDisplacement(MINIMUM_DISTANCE_CHANGE_FOR_UPDATES);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(mLocationRequest);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());

        for(int i = 0; i < route.length;i++) {
            route[i] = new Route();
        }
        setContentView(R.layout.activity_road);
        mOpenCvCameraView = (CameraBridgeViewBase)findViewById(R.id.activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener((CameraBridgeViewBase.CvCameraViewListener2) this);
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
            Log.d(this.getClass().getName(), "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d(this.getClass().getName(), "onResum :: OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d(this.getClass().getName(), "input start");
    }

    @Override
    public void onCameraViewStopped() {
        Log.d(this.getClass().getName(), "input stop");
    }

    public Mat steptowatershed(Mat img)
    {
        Mat threeChannel = new Mat();
        Mat rgbImg = new Mat();
        Imgproc.cvtColor(img, rgbImg, Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(img, threeChannel, Imgproc.COLOR_BGRA2GRAY);
        Imgproc.threshold(threeChannel, threeChannel, 100, 255, Imgproc.THRESH_BINARY);

        Mat fg = new Mat(img.size(),CvType.CV_8U);
        Imgproc.erode(threeChannel,fg,new Mat());

        Mat bg = new Mat(img.size(),CvType.CV_8U);
        Imgproc.dilate(threeChannel,bg,new Mat());
        Imgproc.threshold(bg,bg,1, 128,Imgproc.THRESH_BINARY_INV);

        Mat markers = new Mat(img.size(),CvType.CV_8U, new Scalar(0));
        //markers(Imgproc.rectangle(markers, new Point(20, 40) , new Point(120, 140),marindex, new Scalar(0), 2)) = Scalar::all(2);
        Core.add(fg, bg, markers);
        Mat result1=new Mat();
        WatershedSegmenter segmenter = new WatershedSegmenter();
        segmenter.setMarkers(markers);
        result1 = segmenter.process(rgbImg);
        return result1;
    }
    public class WatershedSegmenter
    {
        public Mat markers=new Mat();

        public void setMarkers(Mat markerImage)
        {

            markerImage.convertTo(markers, CvType.CV_32SC1);
        }

        public Mat process(Mat image)
        {
            Imgproc.watershed(image,markers);
            markers.convertTo(markers,CvType.CV_8U);
            return markers;
        }
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {


        Log.d(this.getClass().getName(), "input oncameraFrame");
        origin = inputFrame.rgba();
        Mat originT = origin.t();
        Core.flip(originT, originT, 1);
        Imgproc.resize(originT,originT,origin.size());
        origin = originT;
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
        //Imgproc.cvtColor(origin,origin,Imgproc.COLOR_RGBA2RGB);
        watershed = new Mat(origin.rows(), origin.cols(), origin.type());
        //blur for computer vision first.
        ConvertRGBtoGray(origin.getNativeObjAddr(), blur.getNativeObjAddr());
        Binary(blur.getNativeObjAddr(), binary.getNativeObjAddr());
        Gaussian(blur.getNativeObjAddr(), blur.getNativeObjAddr());
        blurforcheck = new Mat(blur.rows(), blur.cols(), blur.type());
        ClosingFilter(blur.getNativeObjAddr(), closing.getNativeObjAddr());
        watershed = steptowatershed(closing);
        BinaryDilate(blur.getNativeObjAddr(), blur.getNativeObjAddr());
        BinaryEdge(blur.getNativeObjAddr(), edge.getNativeObjAddr());
        //Watershed(blurforcheck.getNativeObjAddr(),edge.getNativeObjAddr() , watershed.getNativeObjAddr());
        Calforob(binary.getNativeObjAddr(), edge.getNativeObjAddr(), origin.getNativeObjAddr(), matResult.getNativeObjAddr());
        if(trafficflag){
            checkflag = Calculatetrafficlight(origin.getNativeObjAddr(), left, top, width, height, count);
        }
        tickflag = Calfortick(origin.getNativeObjAddr());
        //Eraseroad(matResult.getNativeObjAddr(), matResult.getNativeObjAddr());
        watershed = steptowatershed(originT);
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
        if(Calculateob(matResult.getNativeObjAddr())){
            errorhandling++;
        }
        else if (errorhandling > 5) {
            errorhandling = 5;
        }
        else {
            errorhandling--;
            obflag = false;
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
        if(subflag){
            if(noisehandiling>5){
                entranceflag = Calfortick(origin.getNativeObjAddr());
                String timsg = "지하철입구입니다";
                tts.speak(timsg, TextToSpeech.QUEUE_ADD, null);
                Log.d(this.getClass().getName(), "opencventrance");
                ttsflag = true;
            }
        }
        if(stairhandling>5 && crossflag && !externflagcr &&!tickflag&&!ttsflag&& !obflag) {
            String obmsg = "상향계단입니다";
            tts.speak(obmsg, TextToSpeech.QUEUE_ADD, null);
            ttsflag = true;
        }
        else if(stairhandling>3 && crossflag && externflagcr &&!tickflag&&!ttsflag){
            String crmsg = "횡단보도입니다";
            tts.speak(crmsg, TextToSpeech.QUEUE_ADD, null);
            Log.d(this.getClass().getName(), "opencvcr");
            ttsflag = true;
            trafficflag = true;
        }
        else if(errorhandling>5 && !externflagbs &&!tickflag&&!ttsflag){
            String upmsg = "장애물입니다";
            tts.speak(upmsg, TextToSpeech.QUEUE_ADD, null);
            Log.d(this.getClass().getName(), "opencvob");
            ttsflag = true;
            obflag = true;
        }
        else if(errorhandling>5 && externflagbs &&!tickflag&&!ttsflag){
            String bsmsg = "버스정류장입니다";
            tts.speak(bsmsg, TextToSpeech.QUEUE_ADD, null);
            Log.d(this.getClass().getName(), "opencvbs");
            Intent intent = new Intent(getApplicationContext(), BusNumActivity.class);
            intent.putExtra("busnumber", busNo);
            startActivity(intent);
            isFindBus = true;
            externflagbs = false;
        }
        else if(externflagtick&& tickflag && errorhandling>5&&!ttsflag&& !drflag){
            String timsg = "개찰구입니다";
            tts.speak(timsg, TextToSpeech.QUEUE_ADD, null);
            Log.d(this.getClass().getName(), "opencvtick");
            tgflag =true;
            ttsflag = true;
        }
        if(checkflag){
            String timsg = "녹색불입니다건너세요";
            tts.speak(timsg, TextToSpeech.QUEUE_ADD, null);
            Log.d(this.getClass().getName(), "opencvtraffic");
            ttsflag = true;
            checkflag = false;
            crossflag = false;
            trafficflag = false;
        }
        else{
            Log.d(this.getClass().getName(), "opencvr");
        }
        label = new Mat(matResult.rows(), matResult.cols(), origin.type());
        MatrixTime(500);
        blur.release();
        edge.release();
        binary.release();
        label.release();
        closing.release();
        watershed.release();

        return matResult;

    }
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                //location = locationList.get(0);
                if (!isGetLocation) {
                    isGetLocation = true;
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    odsayService.requestSearchPubTransPath(Double.toString(longitude), Double.toString(latitude), destLongitude, destLatitude, "0", "0", "0", onResultCallbackListener);
                }
                if (isGuid == true) {
                    Double latitude = location.getLatitude();
                    Double longitude = location.getLongitude();
                    Double destLatitude = route[routeIndex].getLatitude(pathIndex);
                    Double destLongitude = route[routeIndex].getLongitude(pathIndex);
                    float distance[] = new float[1];
                    Location.distanceBetween(destLatitude, destLongitude, latitude, longitude, distance);

                    String speech = route[routeIndex].getPath(pathIndex);
                    String msg = "거리 : " + distance[0] + "\n현재 위치\n위도 : " + latitude + " 경도 : " + longitude + "\n";
                    msg += "목적지\n위도 : " + destLatitude + " 경도 : " + destLongitude + "\n";

                    if(route[routeIndex].getType() == 1 && pathIndex == 0) {
                        externflagtick = true;
                    }
                    if ((int) distance[0] < 100000 || pathIndex == 0 || (route[routeIndex].getType() == 1 && pathIndex == 1 && (int) distance[0] < 100)) {
                        pathIndex++;
                        if (pathIndex >= route[routeIndex].getSize()) {
                            if (route[routeIndex].getType() == 2) {
                                isFindBus = false;
                            }

                            speech += "했습니다.";
                            routeIndex++;
                            pathIndex = 0;
                            if (routeIndex == routeLength) {
                                speech += "길안내를 종료합니다.";
                                isGuid = false;
                            }
                        } else {
                            speech += "하세요.";
                        }
                        msg += speech;
                        tts.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
                        if(speech.contains("횡단보도")){
                            externflagcr = true;
                        }
                        if(route[routeIndex].getType()==1 && pathIndex == 1 && tgflag){
                            String present ="흑석";
                            String[] sub9 = RoadActivity.getsubstation9();
                            int di_index=-1;
                            String StationName = route[routeIndex].getPath(pathIndex - 1).split(" ")[5].split("로")[0];
                            Log.d("StationName",StationName);


                            for (int i = 0; i < sub9.length; i++) {
                                if (StationName.equals(sub9[i])) {
                                    di_index=i;
                                    //arr.add(wo);
                                    break;
                                }
                            }


                            int p_index=-1;
                            for(int i=0; i<substation9.length;i++){
                                if(substation9[i].equals(present)){
                                    p_index=i;
                                }
                            }
                            if(di_index==p_index){
                                System.out.println("error");
                            }
                            else if(di_index<p_index)
                            {
                                tts.speak("오른쪽으로 내려가세요", TextToSpeech.QUEUE_ADD, null);
                                //System.out.println("하행");
                            }
                            else {
                                tts.speak("왼쪽으로 내려가세요", TextToSpeech.QUEUE_ADD, null);
                                System.out.println("상행");
                            }
                            drflag = true;

                        }
                        if (route[routeIndex].getType() == 2 && !isFindBus && pathIndex == 1) {
                            externflagbs = true;
                            odsayService.requestBusStationInfo(route[routeIndex].getStartStationID(), onResultCallbackListener);
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            busNo = route[routeIndex].getPath(pathIndex - 1).split(" ")[1];
                        }


                    }

                    textView.setText(msg);
                }
            }
        }
    };

    // 콜백 함수 구현
    OnResultCallbackListener onResultCallbackListener = new OnResultCallbackListener() {
        // 호출 성공 시 실행
        @Override
        public void onSuccess(ODsayData odsayData, API api) {
            // API Value 는 API 호출 메소드 명을 따라갑니다.
            if(api == API.BUS_STATION_INFO) {
                try {
                    if(odsayData.getJson().getJSONObject("result").getString("do").equals("서울특별시")) {
                        String busNo = route[routeIndex].getPath(pathIndex - 1).split(" ")[1];
                        String arsID = odsayData.getJson().getJSONObject("result").getString("arsID");
                        if (arsID.contains("-")) {
                            arsID = arsID.split("-")[0] + arsID.split("-")[1];
                        }

                        AsyncTask<String, Void, HttpResponse> asyncTask = new AsyncTask<String, Void, HttpResponse>() {

                            @Override
                            protected HttpResponse doInBackground(String... url) {
                                HttpGet request = new HttpGet(url[0]);
                                HttpResponse response = null;

                                try {
                                    response = new DefaultHttpClient().execute(request);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return response;
                            }
                        };

                        StringBuilder urlBuilder = new StringBuilder("http://ws.bus.go.kr/api/rest/stationinfo/getStationByUid"); /*URL*/
                        urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=" + key.getBusApiKey()); /*Service Key*/
                        urlBuilder.append("&" + URLEncoder.encode("arsId", "UTF-8") + "=" + URLEncoder.encode(arsID, "UTF-8")); /*정류소고유번호*/
                        HttpResponse response = asyncTask.execute(urlBuilder.toString()).get();
                        JSONArray r = XML.toJSONObject(EntityUtils.toString(response.getEntity())).getJSONObject("ServiceResult").getJSONObject("msgBody").getJSONArray("itemList");

                        Log.d("response", r.toString());

                        for (int i = 0; i < r.length(); i++) {
                            if (r.getJSONObject(i).getString("rtNm").equals(busNo)) {
                                String arrmsg = busNo + " 버스가 ";
                                arrmsg += r.getJSONObject(i).getString("arrmsg1").split("후")[0];

                                if (!arrmsg.contains("도착")) {
                                    arrmsg += "후 도착";
                                }
                                arrmsg += "합니다.";
                                tts.speak(arrmsg, TextToSpeech.QUEUE_ADD, null);
                                break;
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }  catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
            else if (api == API.SEARCH_PUB_TRANS_PATH) {
                Log.d("path : ", odsayData.getJson().toString());
                try {
                    if(odsayData.getJson().has("error")) {
                        try {
                            if (odsayData.getJson().getJSONObject("error").getString("code").equals("-98")) {
                                routeLength = 1;
                                findPedestrainPath(0, new TMapPoint(latitude, longitude), new TMapPoint(Double.parseDouble(destLatitude), Double.parseDouble(destLongitude)));
                                Thread.sleep(1000);
                                route[0].setDest(destName);
                                route[0].setLastPath();
                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    else {
                        JSONObject result = odsayData.getJson().getJSONObject("result");
                        if (result.has("path")) {
                            JSONObject path = result.getJSONArray("path").getJSONObject(0);
                            JSONArray subPath = path.getJSONArray("subPath");
                            routeLength = subPath.length();

                            double lat = latitude;
                            double lon = longitude;
                            double destLat;
                            double destLon;
                            for (int i = 0; i < routeLength; i++) {
                                int trafficType = subPath.getJSONObject(i).getInt("trafficType");
                                if (trafficType == 3) {
                                    if (i < subPath.length() - 1) {
                                        destLat = subPath.getJSONObject(i + 1).getDouble("startY");
                                        destLon = subPath.getJSONObject(i + 1).getDouble("startX");
                                        findPedestrainPath(i, new TMapPoint(lat, lon), new TMapPoint(destLat, destLon));
                                    } else {
                                        destLat = Double.parseDouble(destLatitude);
                                        destLon = Double.parseDouble(destLongitude);
                                        findPedestrainPath(i, new TMapPoint(lat, lon), new TMapPoint(destLat, destLon));
                                        route[i].setDest(destName);
                                    }
                                } else {
                                    destLat = subPath.getJSONObject(i).getDouble("endY");
                                    destLon = subPath.getJSONObject(i).getDouble("endX");
                                    String startName = subPath.getJSONObject(i).getString("startName");
                                    String endName = subPath.getJSONObject(i).getString("endName");
                                    String startStationID = subPath.getJSONObject(i).getString("startID");

                                    if (trafficType == 2) {
                                        String busNo = subPath.getJSONObject(i).getJSONArray("lane").getJSONObject(0).getString("busNo");
                                        route[i].setType(2);
                                        route[i].addRoute(startName + "에서 " + busNo + " 버스를 타고 " + endName + "로 이동", lat, lon);
                                    } else {
                                        String name = subPath.getJSONObject(i).getJSONArray("lane").getJSONObject(0).getString("name");
                                        route[i].setType(1);
                                        route[i].addRoute(startName + "에서 " + name + " 지하철을 타고 " + endName + "로 이동", lat, lon);
                                    }
                                    route[i - 1].setDest(startName);
                                    route[i].addRoute(endName + "에 도착", destLat, destLon);
                                    route[i].setStartStationID(startStationID);
                                }
                                lat = destLat;
                                lon = destLon;
                            }
                        }
                    }
                    Thread.sleep(routeLength * 250);
                    for (int j = 0; j < routeLength; j++) {
                        route[j].setLastPath();
                    }

                    String tmp = new String("");
                    for  (int i = 0; i < routeLength;i++) {
                        for (int j = 0; j < route[i].getSize(); j++) {
                            tmp += route[i].getPath(j) + "\n위도 : " + route[i].getLatitude(j).toString() + "\n경도 : " + route[i].getLongitude(j).toString() + "\n";
                        }
                        tmp +="\n";
                    }
                    textView.setText(tmp);
                    tts.speak(destName + "까지 길안내를 시작하겠습니다.", TextToSpeech.QUEUE_FLUSH, null);
                    route[0].setFirstPoint(location.getLatitude(),location.getLongitude());
                    isGuid = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        // 호출 실패 시 실행
        @Override
        public void onError(int i, String s, API api) {
            if(api == API.SEARCH_STATION) {
            }
            if (api == API.SEARCH_PUB_TRANS_PATH) {
            }
        }
    };

    @Override
    protected  void onDestroy() {
        super.onDestroy();
        if(tts != null) {
            tts.stop();
            tts.shutdown();
            tts =null;
        }
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onInit(int status) {

    }

    public void findPedestrainPath(int index, TMapPoint source, TMapPoint dest)  {

        tmapdata.findPathDataAllType(TMapData.TMapPathType.PEDESTRIAN_PATH, source, dest, new TMapData.FindPathDataAllListenerCallback() {
            @Override
            public void onFindPathDataAll(Document document) {
                String path = new String("");
                String[] point;
                Double lat = 0.0;
                Double lon = 0.0;
                Element root = document.getDocumentElement();
                NodeList nodeListPlacemark = root.getElementsByTagName("Placemark");

                for( int i=0; i<nodeListPlacemark.getLength(); i++ ) {
                    NodeList nodeListPlacemarkItem = nodeListPlacemark.item(i).getChildNodes();
                    Log.d("placemark", nodeListPlacemark.item(i).getTextContent());
                    Log.d("split", nodeListPlacemarkItem.item(5).getTextContent());

                    if(!nodeListPlacemarkItem.item(9).getTextContent().equals("#lineStyle")) {
                        for (int j = 0; j < nodeListPlacemarkItem.getLength(); j++) {
                            if (nodeListPlacemarkItem.item(j).getNodeName().equals("description")) {
                                path = nodeListPlacemarkItem.item(j).getTextContent().trim().replace("을 따라","로").replace("횡단보도","횡단보도 이용");
                                Log.d("debug", nodeListPlacemarkItem.item(j).getTextContent().trim());
                            }
                            else if(nodeListPlacemarkItem.item(j).getNodeName().equals("Point")) {
                                point = nodeListPlacemarkItem.item(j).getTextContent().trim().split(",");
                                lat = Double.parseDouble(point[1]);
                                lon = Double.parseDouble(point[0]);
                            }
                        }
                        route[index].addRoute(path,lat,lon);

                    }
                }
                route[index].setType(3);

            }
        });
    }
}

