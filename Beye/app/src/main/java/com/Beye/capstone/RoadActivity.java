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

import java.util.ArrayList;


public class RoadActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    ODsayService odsayService;
    TextView textView;
    String dest;
    private GpsInfo gps;
    double latitude;
    double longitude;
    TextToSpeech tts;
    String address;
    String destLatitude;
    String destLongitude;
    TMapTapi tmaptapi;
    TMapData tmapdata;
    String[] splitPath;
    int pathLength;
    TMapPoint[][] point;
    private Key key;

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
        splitPath = new String[10];

        Intent intent = getIntent();
        dest = intent.getExtras().getString("dest");

        // 싱글톤 생성, Key 값을 활용하여 객체 생성
        odsayService = ODsayService.init(this, key.getOdsayApiKey());
        // 서버 연결 제한 시간(단위(초), default : 5초)
        odsayService.setReadTimeout(5000);
        // 데이터 획득 제한 시간(단위(초), default : 5초)
        odsayService.setConnectionTimeout(5000);

        // 콤마를 기준으로 split
        String []splitStr = dest.split(",");
        address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1,splitStr[0].length() - 2); // 주소
        destLatitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // 위도
        destLongitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // 경도


        gps = new GpsInfo(this);
        if (gps.isGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

        } else {
            // GPS 를 사용할수 없으므로
            gps.showSettingsAlert();
        }



    }

    @Override
    protected  void onDestroy() {
        super.onDestroy();
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
                                    tts.speak("700m 이내 이므로 도보로 안내하겠습니다.", TextToSpeech.QUEUE_FLUSH, null);
                                    pathLength = 1;
                                    findPedestrainPath(0, new TMapPoint(latitude, longitude), new TMapPoint(Double.parseDouble(destLatitude), Double.parseDouble(destLongitude)));
                                    Thread.sleep(400);
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
                                pathLength = subPath.length();


                                double lat = latitude;
                                double lon = longitude;
                                for (int i = 0;i < subPath.length(); i++) {
                                    int trafficType = subPath.getJSONObject(i).getInt("trafficType");
                                    if (trafficType == 3) {
                                        if(i < subPath.length()-1) {
                                            double destLat = subPath.getJSONObject(i + 1).getDouble("startY");
                                            double destlon = subPath.getJSONObject(i + 1).getDouble("startX");
                                            findPedestrainPath(i,new TMapPoint(lat, lon), new TMapPoint(destLat, destlon));
                                            lat = destLat;
                                            lon = destlon;
                                        }
                                        else {
                                            findPedestrainPath(i,new TMapPoint(lat, lon), new TMapPoint(Double.parseDouble(destLatitude), Double.parseDouble(destLongitude)));
                                            Thread.sleep(pathLength*250);

                                        }

                                    }

                                    else if(trafficType == 2){
                                        lat = subPath.getJSONObject(i).getDouble("endY");
                                        lon = subPath.getJSONObject(i).getDouble("endX");
                                        String busNo = subPath.getJSONObject(i).getJSONArray("lane").getJSONObject(0).getString("busNo");
                                        String startName = subPath.getJSONObject(i).getString("startName");
                                        String endName = subPath.getJSONObject(i).getString("endName");
                                        splitPath[i] = "버스\n" + startName + "\n" + endName + "\n";
                                    }
                                    else {
                                        lat = subPath.getJSONObject(i).getDouble("endY");
                                        lon = subPath.getJSONObject(i).getDouble("endX");
                                        String name = subPath.getJSONObject(i).getJSONArray("lane").getJSONObject(0).getString("name");
                                        String startName = subPath.getJSONObject(i).getString("startName");
                                        String endName = subPath.getJSONObject(i).getString("endName");
                                        splitPath[i] = "지하철 " + name + "\n" + startName + "\n" + endName + "\n";
                                    }
                                    Log.d("subpath", i +splitPath[i]);
                                }

                            }
                            else{

                            }
                        }
                        String tmp = new String("");
                        for  (int i = 0; i < pathLength;i++) {
                            tmp += splitPath[i] + "\n";
                        }
                        textView.setText(tmp);

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
                String result = new String("");
                Element root = document.getDocumentElement();
                NodeList nodeListPlacemark = root.getElementsByTagName("Placemark");

                for( int i=0; i<nodeListPlacemark.getLength(); i++ ) {
                    NodeList nodeListPlacemarkItem = nodeListPlacemark.item(i).getChildNodes();
                    Log.d("placemark", nodeListPlacemark.item(i).getTextContent());
                    Log.d("split", nodeListPlacemarkItem.item(5).getTextContent());

                    if(!nodeListPlacemarkItem.item(9).getTextContent().equals("#lineStyle")) {
                        for (int j = 0; j < nodeListPlacemarkItem.getLength(); j++) {
                            if (nodeListPlacemarkItem.item(j).getNodeName().equals("description")) {
                                result += nodeListPlacemarkItem.item(j).getTextContent().trim() + "\n";
                                Log.d("debug", nodeListPlacemarkItem.item(j).getTextContent().trim());
                            }
                            else if(nodeListPlacemarkItem.item(j).getNodeName().equals("Point")) {
                                //result += nodeListPlacemarkItem.item(j).getTextContent().trim() + "\n";
                                String point = nodeListPlacemarkItem.item(j).getTextContent().trim();
                                Log.d("lon", point.split(",")[0]);
                                Log.d("lat",point.split(",")[1]);
                            }
                        }
                    }
                }
                Log.d("result",result);
                splitPath[index] = "도보\n" +result + "\n";

            }
        });
    }
}
