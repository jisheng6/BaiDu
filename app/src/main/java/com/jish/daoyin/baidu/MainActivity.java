package com.jish.daoyin.baidu;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.jish.daoyin.baidu.utlis.PoiOverlay;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,BDLocationListener {

    private Button pu;
    private Button wei;
    private Button kong;
    private TextureMapView mMapView;
    private BaiduMap mBaiduMap;
    private EditText editText;
    private Button sousuo;
    private Button qiehuan;
    private Button weizhi;
    private Button fujin;
    private LinearLayout xuanze;
    private LocationClient mLocationClient; //定位功能的主要控制类
    private PoiSearch mPoiSearch; //检索的主要控制类
    private EditText editTextName;
    OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener(){

        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            //获取POI检索结果
            if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {
                mBaiduMap.clear();
                PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
                mBaiduMap.setOnMarkerClickListener(overlay);
                overlay.setData(poiResult);
                overlay.addToMap();
                overlay.zoomToSpan();
                List<PoiInfo> allPoi = poiResult.getAllPoi();
                for (PoiInfo p : allPoi) {
                    Log.i("jiba", "uid==" + p.uid + ",name===" + p.name + ",city===" + p.city);
                }
            }


        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
            //获取Place详情页检索结果
            Log.i("jiba","onGetPoiDetailResult==="+poiDetailResult.toString());
        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
            Log.i("jiba","onGetPoiIndoorResult==="+poiIndoorResult.toString());
        }
    };


    private class MyPoiOverlay extends PoiOverlay {

        public MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int index) {
            super.onPoiClick(index);
            PoiInfo poi = getPoiResult().getAllPoi().get(index);
            mPoiSearch.searchPoiDetail((new PoiDetailSearchOption())
                    .poiUid(poi.uid));
            Log.i("jiba","onPoiClick==="+poi.name);
            Toast.makeText(MainActivity.this,poi.name,Toast.LENGTH_SHORT).show();
            return true;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        initView();//初始化id

        mMapView = (TextureMapView) findViewById(R.id.mTexturemap);
        // 获取地图控制器
        mBaiduMap = mMapView.getMap();
        //普通地图 ,mBaiduMap是地图控制器对象
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);

        initDingWei();//设置定位功能  开启/关闭

        initPOI(); //初始化检索功能
    }



    //设置定位功能  初始化定位功能
    private void initDingWei(){
        //声明LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(this);//注册监听函数
        initLocation(); //初始化定位参数  封装在类中
        mBaiduMap.setMyLocationEnabled(true);//显示定位层并且可以触发定位,默认是flase
        mLocationClient.start();//开启定位

    }
    // 设置定位的基本功能  比如精度模式，是否随时保持定位。。。
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("gcj02");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }
    //初始化检索功能
    private void initPOI(){
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(poiListener);
    }


    //初始化id
    private void initView() {
        pu = (Button) findViewById(R.id.pu);
        wei = (Button) findViewById(R.id.wei);
        kong = (Button) findViewById(R.id.kong);

        pu.setOnClickListener(this);
        wei.setOnClickListener(this);
        kong.setOnClickListener(this);
        editText = (EditText) findViewById(R.id.editText);
        sousuo = (Button) findViewById(R.id.sousuo);
        sousuo.setOnClickListener(this);
        qiehuan = (Button) findViewById(R.id.qiehuan);
        qiehuan.setOnClickListener(this);
        weizhi = (Button) findViewById(R.id.weizhi);
        weizhi.setOnClickListener(this);
        fujin = (Button) findViewById(R.id.fujin);
        fujin.setOnClickListener(this);
        xuanze = (LinearLayout) findViewById(R.id.xuanze);
        editTextName = (EditText) findViewById(R.id.editTextName);
    }

    boolean kai=true;
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pu:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL); //普通地图 ,mBaiduMap是地图控制器对象
                break;
            case R.id.wei:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE); //卫星地图 ,mBaiduMap是地图控制器对象
                break;
            case R.id.kong:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NONE); //空白地图 ,mBaiduMap是地图控制器对象
                break;

            case R.id.sousuo:
                //搜索
                submit();
                break;
            //切换视图模式功能
            case R.id.qiehuan:
                if(kai){
                    xuanze.setVisibility(View.VISIBLE);
                    kai=false;
                }else{
                    xuanze.setVisibility(View.GONE);
                    kai=true;
                }

                break;
            //我的位置功能
            case R.id.weizhi:
                //119.039666,33.65537
                setBiaoDian(119.039666,33.65537);
                break;

            //我的附近功能 检索医院
            case R.id.fujin:
                mPoiSearch.searchInCity((new PoiCitySearchOption())
                        .city("北京")
                        .keyword("医院")
                        .pageNum(10));
                break;
        }
    }

    //手势识别 绘制坐标点
    private void setBiaoDian(double Latitude,double Longitude) {
        //设置的时候经纬度是反的 纬度在前，经度在后
        LatLng point = new LatLng(Latitude, Longitude);
        //构建Marker图标

        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_gcoding);
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap)
                .anchor(0.5f, 1.0f)
                .zIndex(7);
        mBaiduMap.addOverlay(option);
//                OverlayOptions ooCircle = new CircleOptions()
//                        .fillColor(R.color.qianlan)
//                        .center(point)
//                        .stroke(new Stroke(2, R.color.qianlan))
//                        .radius(500);//设置颜色和透明度，均使用16进制显示，0xAARRGGBB，如 0xAA000000 其中AA是透明度，000000为颜色
//                mBaiduMap.addOverlay(ooCircle);
        //1-20级 20级室内地图
        MapStatusUpdate mapStatusUpdate =
                MapStatusUpdateFactory.newLatLngZoom(point, 16);
        mBaiduMap.setMapStatus(mapStatusUpdate);
    }

    //搜索
    private void submit() {
        // validate
        String editTextString = editText.getText().toString().trim();
        if (TextUtils.isEmpty(editTextString)) {
            Toast.makeText(this, "城市名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        String trim = editTextName.getText().toString().trim();
        if (TextUtils.isEmpty(trim)) {
            Toast.makeText(this, "关键字不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        mPoiSearch.searchInCity((new PoiCitySearchOption())
                .city(editTextString)
                .keyword(trim)
                .pageNum(10));
    }

    //自动定位  获取当前坐标的回调接口
    boolean isFirstLoc=true;
    @Override
    public void onReceiveLocation(BDLocation location) {
//        Log.i("jiba","onReceiveLocation===="+location);
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(location.getRadius())
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(100).latitude(location.getLatitude())
                .longitude(location.getLongitude()).build();
//        Log.i("jiba","经度："+location.getLongitude()+"===纬度："+location.getLatitude());

        // 设置定位数据
        mBaiduMap.setMyLocationData(locData);
        if (isFirstLoc) {
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLngZoom(ll, 16);//设置地图中心及缩放级别
            mBaiduMap.animateMapStatus(update);
            isFirstLoc = false;
            Toast.makeText(getApplicationContext(), location.getAddrStr(), Toast.LENGTH_SHORT).show();
        }

//        List<Poi> poiList = location.getPoiList();
//        for (Poi p:poiList) {
//            Log.i("jiba","p===="+p.getName()+",rank===="+p.getRank());
//            double rank = p.getRank();
//        }

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mPoiSearch.destroy();
        mMapView.onDestroy();
    }
}
