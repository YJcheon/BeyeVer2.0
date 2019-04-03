package com.Beye.capstone;

import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import com.odsay.odsayandroidsdk.API;
import com.odsay.odsayandroidsdk.ODsayData;
import com.odsay.odsayandroidsdk.ODsayService;
import com.odsay.odsayandroidsdk.OnResultCallbackListener;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapTapi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class RoadActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private ODsayService odsayService;
    private TextView textView;
    private String dest;
    private GpsInfo gps;
    private double latitude;
    private double longitude;
    private TextToSpeech tts;
    private String destLatitude;
    private String destLongitude;
    private TMapTapi tmaptapi;
    private TMapData tmapdata;
    private int routeLength;
    private Key key;
    private Route[] route;
    private String destName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_road);
        textView = (TextView)findViewById(R.id.sttResult);
        textView.setMovementMethod(new ScrollingMovementMethod());
        tts = new TextToSpeech(this, (TextToSpeech.OnInitListener) this);
        key = new Key();
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


        gps = new GpsInfo(this,textView,tts);
        if (gps.isGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

        } else {
            // GPS 를 사용할수 없으므로
            gps.showSettingsAlert();
        }

        for(int i = 0; i < route.length;i++) {
            route[i] = new Route();
        }

    }

    @Override
    protected  void onDestroy() {
        super.onDestroy();
        gps = null;
        if(tts != null) {
            tts.stop();
            tts.shutdown();
            tts =null;
        }
    }

    @Override
    public void onInit(int status) {

        // 콜백 함수 구현
        OnResultCallbackListener onResultCallbackListener = new OnResultCallbackListener() {

            // 호출 성공 시 실행
            @Override
            public void onSuccess(ODsayData odsayData, API api) {


                // API Value 는 API 호출 메소드 명을 따라갑니다.
                if (api == API.SEARCH_PUB_TRANS_PATH) {
                    Log.d("path : ", odsayData.getJson().toString());
                    try {

                        if(odsayData.getJson().has("error")) {
                            try {
                                if (odsayData.getJson().getJSONObject("error").getString("code").equals("-98")) {
                                    routeLength = 1;
                                    findPedestrainPath(0, new TMapPoint(latitude, longitude), new TMapPoint(Double.parseDouble(destLatitude), Double.parseDouble(destLongitude)));
                                    route[0].setDest(destName);

                                    Thread.sleep(400);
                                    route[0].setLastPath();
                                }
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            JSONObject result = odsayData.getJson().getJSONObject("result");
                            if(result.has("path")) {
                                JSONObject path = result.getJSONArray("path").getJSONObject(0);
                                JSONArray subPath = path.getJSONArray("subPath");
                                routeLength = subPath.length();


                                double lat = latitude;
                                double lon = longitude;
                                double destLat;
                                double destLon;
                                for (int i = 0;i < routeLength; i++) {
                                    int trafficType = subPath.getJSONObject(i).getInt("trafficType");
                                    if (trafficType == 3) {
                                        if(i < subPath.length()-1) {
                                            destLat = subPath.getJSONObject(i + 1).getDouble("startY");
                                            destLon = subPath.getJSONObject(i + 1).getDouble("startX");
                                            findPedestrainPath(i,new TMapPoint(lat, lon), new TMapPoint(destLat, destLon));
                                        }
                                        else {
                                            destLat = Double.parseDouble(destLatitude);
                                            destLon = Double.parseDouble(destLongitude);
                                            findPedestrainPath(i,new TMapPoint(lat, lon), new TMapPoint(destLat, destLon));

                                            route[i].setDest(destName);
                                            Thread.sleep(routeLength*250);
                                            for(int j = 0; j < routeLength;j++) {
                                                route[j].setLastPath();
                                            }
                                        }

                                    }

                                    else {
                                        destLat = subPath.getJSONObject(i).getDouble("endY");
                                        destLon = subPath.getJSONObject(i).getDouble("endX");
                                        String startName = subPath.getJSONObject(i).getString("startName");
                                        String endName = subPath.getJSONObject(i).getString("endName");

                                        if (trafficType == 2) {
                                            String busNo = subPath.getJSONObject(i).getJSONArray("lane").getJSONObject(0).getString("busNo");
                                            route[i].setType(2);
                                            route[i].addRoute(startName + "에서 " + busNo + " 버스를 타고 " + endName + "로 이동", lat, lon);
                                        }
                                        else {
                                            String name = subPath.getJSONObject(i).getJSONArray("lane").getJSONObject(0).getString("name");
                                            route[i].setType(1);
                                            route[i].addRoute(startName + "에서 " +name + " 지하철을 타고 " + endName + "로 이동", lat, lon);
                                        }
                                        route[i-1].setDest(startName);
                                        route[i].addRoute(endName + "에 도착", destLat, destLon);

                                    }
                                    lat = destLat;
                                    lon = destLon;

                                }

                            }

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
                        route[0].setFirstPoint(gps.getLatitude(),gps.getLongitude());
                        gps.setGuid(true, route, routeLength);
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
                if (api == API.SEARCH_PUB_TRANS_PATH) {

                }
            }
        };
        // API 호출
        odsayService.requestSearchPubTransPath(Double.toString(longitude),Double.toString(latitude),destLongitude,destLatitude,"0","0","0",onResultCallbackListener);
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
