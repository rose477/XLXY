package com.hnucm18jr.roseapp.Shouye.Daka;

import androidx.appcompat.app.AppCompatActivity;
import android.hardware.SensorEvent;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.hnucm18jr.roseapp.R;

public class RunActivity extends AppCompatActivity   {



    private Button btnOk;
    private TextView tvTime,mTextView,mTextView2;

    private boolean bStart=false;
    private Handler mHander=new Handler();
    private int mCount=0;
    private int second1 = 0;
    private int second2 = 0;
    private int minute1 = 0;
    private int minute2 = 0;
    private int hour1 = 0;
    private int hour2 = 0;
    private MapView mMapView = null;
    BaiduMap mBaiduMap=null;
    LocationClient mLocationClient=null;

    float sb;
    int i=0,j=0,a=0;
    private Runnable mCounter=new Runnable() {
        @Override
        public void run() {
            mCount++;
            if (mCount < 10) {
                second1 = mCount;
            }
            //System.out.println("进入成功");
            else if (mCount >= 10 && mCount < 60) {
                second2 = mCount / 10;
                second1 = mCount % 10;
            } else {
                minute1 = 1;
                second1 = 0;
                second2 = 0;
            }



            tvTime.setText(" "+ hour2 + hour1 + ":" + minute2 + minute1 + ":" + second2 + second1);
            mHander.postDelayed(this,1000);
        }
        //tvTime.setText(" "+ hour2 + hour1 + ":" + minute2 + minute1 + ":" + second2 + second1);
        //mHander.postDelayed(this,1000);

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);

        }
        setContentView(R.layout.activity_run);

        initView();
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.button4:
                        if (bStart==false){
                            btnOk.setText("长按暂停");
                            mHander.post(mCounter);
                        }else{
                            btnOk.setText("开始跑步");
                            mHander.removeCallbacks(mCounter);
                        }
                        bStart=!bStart;
                        break;
                }
            }
        });
        btnOk.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                finish();
                return false;

            }
        });
        //获取地图控件引用
       mMapView = (MapView) findViewById(R.id.baiduMapView);
        //获取到地图
        mBaiduMap = mMapView.getMap();
        //设置地图放大的倍数
        init();
        //设置地图定位的一些参数，如定位图标，精度圈颜色等
        configure();
        //定位初始化
        init_location();

    }

    private void initView() {
        btnOk = (Button) findViewById(R.id.button4);
        tvTime = (TextView) findViewById(R.id.textView3);

        mTextView=findViewById(R.id.textView1);
        mTextView2=findViewById(R.id.time11);

    }


    @Override
    protected void onResume() {
        mMapView.onResume();
        //2.为系统的加速度传感器注册监听事件

        super.onResume();

    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();


    }

    @Override
    protected void onDestroy() {
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }



    /**
     * 继承抽象类BDAbstractListener并重写其onReceieveLocation方法来获取定位数据，并将其传给MapView。
     */
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //mapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null){
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
        }
    }

    /**
     *    设置地图放大的倍数
     */
    public  void init(){
        //设置地图放大的倍数
        MapStatus.Builder  builder=new MapStatus.Builder();
        builder.zoom(18f);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }

    /**
     * 自定义内容:
     * 参数说明
     * (1)定位模式 地图SDK支持三种定位模式：NORMAL（普通态）, FOLLOWING（跟随态）, COMPASS（罗盘态）
     * （2）是否开启方向
     * （3）自定义定位图标 支持自定义定位图标样式，
     * （4）自定义精度圈填充颜色
     * （5）自定义精度圈边框颜色
     */
    public void configure(){
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING,true,
                BitmapDescriptorFactory.fromResource(R.mipmap.dingdian),
                0xAAFFFF88,0xAA00FF00));
    }

    /**
     * 定位的初始化
     */
    public void init_location(){
        mBaiduMap.setMyLocationEnabled(true);
        mLocationClient = new LocationClient(RunActivity.this);
//通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
//设置locationClientOption
        mLocationClient.setLocOption(option);
//注册LocationListener监听器
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
//开启地图定位图层
        mLocationClient.start();
    }
}