package xiuyi.com.minions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import xiuyi.com.common.*;



public class AnalogWatchface extends CanvasWatchFaceService implements MessageApi.MessageListener {

    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);
    private DisplayMetrics mDM;
    private WindowManager mWM;
    private int mStep;
    private static final String TAG = "WatchFace";
    private WearableAPIHelper mWearableAPIHelper;
    private GoogleApiClient mApiClient;
    private WeatherData mTemperature;
    private  BatteryStatus batteryStatus;
    private BatteryMonitor.BatteryMonitorCallback batteryCallback;
    private String mWeatherData;
    private String temperature_type;
    private SettingStatus settingStatus;
    private SettingMonitor.SettingMonitorCallback settingCallback;

    float scaleIndex;

    public AnalogWatchface() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDM = new DisplayMetrics();
        mWM = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWM.getDefaultDisplay().getMetrics(mDM);
        mStep = 0;
        temperature_type = "1";

        GlobalWearApiHelper.getInstance(this).sendScreenView("Create Analog View ");

        //connect to phone
        mWearableAPIHelper = new WearableAPIHelper(this, new WearableAPIHelper.WearableAPIHelperListener() {
            @Override
            public void onWearableAPIConnected(GoogleApiClient apiClient) {
                mApiClient = apiClient;
                Wearable.MessageApi.addListener(mApiClient, AnalogWatchface.this);
            }

            @Override
            public void onWearableAPIConnectionSuspended(int cause) {
            }

            @Override
            public void onWearableAPIConnectionFailed(ConnectionResult result) {
            }
        });


        settingCallback = new SettingMonitor.SettingMonitorCallback() {
            @Override
            public void onChanged(SettingStatus status) {
                settingStatus = status;
            }
        };

        settingStatus = new SettingStatus();
        SettingMonitor.getInstance().register(this,settingCallback);



        batteryCallback = new BatteryMonitor.BatteryMonitorCallback() {
            @Override
            public void onChanged(BatteryStatus status) {
                batteryStatus = status;
            }
        };
        batteryStatus = new BatteryStatus();
        BatteryMonitor.getInstance().register(this, batteryCallback);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        mWeatherData = sp.getString(PreferenceKeys.WEATHER_DATA, "");
        temperature_type = sp.getString("temperature_type","");

        if (!TextUtils.isEmpty(mWeatherData)) {
            mTemperature = Utils.parseTemperature(this, mWeatherData, false);
        }

        mWearableAPIHelper.putMessage("/START", new byte[0], null);
    }

    @Override
    public void onDestroy() {
        GlobalWearApiHelper.getInstance(this).sendScreenView("Move from analog to other watch face ");
        SettingMonitor.getInstance().unregister(this, settingCallback);
        BatteryMonitor.getInstance().unregister(this, batteryCallback);
        if (mApiClient != null) {
            Wearable.MessageApi.removeListener(mApiClient, this);
        }
        mWearableAPIHelper.onDestroy();
        super.onDestroy();
    }

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if (messageEvent.getPath().equals(MessagePaths.WEATHER_DATA)) {
            WeatherData w = Utils.parseTemperature(this, new String(messageEvent.getData()), false);
            if (w != null) {
                mTemperature = w;
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AnalogWatchface.this).edit();
                editor.putString(PreferenceKeys.WEATHER_DATA, new String(messageEvent.getData()));
                editor.commit();
            }

        }

    }

    private class BitMap {
        /*Bitmap bmp;*/
        public Bitmap transform(int id) {
            Resources resources = AnalogWatchface.this.getResources();
            Drawable drawable = resources.getDrawable(id);
            return ((BitmapDrawable) drawable).getBitmap();
        }
    }


    private class Engine extends CanvasWatchFaceService.Engine {
        static final int MSG_UPDATE_TIME = 0;


        private Paint mBgPaint;
        private Time mTime;
        Bitmap eye1;
        Bitmap eye2;
        Bitmap eye3;
        Bitmap eye4;
        Bitmap ambientMap;
        Bitmap background;
        Bitmap batteryMap;
        Matrix batteryMatrix;
        private Paint mPaint;
        Bitmap hourMap;
        Bitmap minuteMap;

        Bitmap[] bitmapArray = new Bitmap[4];
        int arrayIndex = 0;
        private int mIndex;


        Matrix minuteMatrix;
        Matrix hourMatrix;

        float secondRadius;
        float minuteRadius;
        float hourRadius;


        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(AnalogWatchface.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setHotwordIndicatorGravity(Gravity.RIGHT | Gravity.BOTTOM)
                    .build());


            BitMap bitMap = new BitMap();

            GATimer();

            background = bitMap.transform(R.mipmap.background_analog);
            ambientMap = bitMap.transform(R.mipmap.ambient_background);


            scaleIndex = (float) (background.getWidth()/320.0);
            if(scaleIndex != 1.0){
                scaleIndex = 1;
            }else{
                scaleIndex = (float) (mDM.widthPixels/320.0);
            }



            eye1 = bitMap.transform(R.mipmap.eye1);
            eye2 = bitMap.transform(R.mipmap.eye2);
            eye3 = bitMap.transform(R.mipmap.eye3);
            eye4 = bitMap.transform(R.mipmap.eye4);

            minuteMap = bitMap.transform(R.mipmap.minute);
            hourMap = bitMap.transform(R.mipmap.hour);

            minuteMap = Bitmap.createScaledBitmap(minuteMap, (int) (minuteMap.getWidth() * scaleIndex), (int) (minuteMap.getHeight() * scaleIndex), true);
            hourMap = Bitmap.createScaledBitmap(hourMap,(int)(hourMap.getWidth()*scaleIndex),(int)(hourMap.getHeight()*scaleIndex),true);

            minuteMatrix = new Matrix();
            hourMatrix = new Matrix();

            bitmapArray[0] = eye1;
            bitmapArray[1] = eye2;
            bitmapArray[2] = eye3;
            bitmapArray[3] = eye4;





            batteryMatrix = new Matrix();

            mBgPaint = new Paint();
            mBgPaint.setColor(Color.BLACK);

            mTime = new Time();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {

            super.onDraw(canvas, bounds);
            mTime.setToNow();

            if(settingStatus!=null && !settingStatus.temperature_type.equals("")){
                temperature_type = settingStatus.temperature_type;
            }

            BitMap bitMap = new BitMap();
            if(batteryStatus.volume<=15){
                batteryMap = bitMap.transform(R.mipmap.battery_15);
            }else if(batteryStatus.volume>15 && batteryStatus.volume<=34){
                batteryMap = bitMap.transform(R.mipmap.battery_34);
            }else if(batteryStatus.volume>34 && batteryStatus.volume<=50){
                batteryMap = bitMap.transform(R.mipmap.battery_50);
            }else if(batteryStatus.volume>50 && batteryStatus.volume<=67){
                batteryMap = bitMap.transform(R.mipmap.battery_67);
            }else if(batteryStatus.volume>67 && batteryStatus.volume<=90){
                batteryMap = bitMap.transform(R.mipmap.battery_84);
            }else if(batteryStatus.volume>90 && batteryStatus.volume <= 100){
                batteryMap = bitMap.transform(R.mipmap.battery_full);
            }

            if(batteryStatus.charging == 2){
                batteryMap = bitMap.transform(R.mipmap.battery_charging);
            }



            int centerX = mDM.widthPixels / 2;
            int centerY = mDM.heightPixels / 2;

            if(mDM.widthPixels==320&&mDM.heightPixels==290){
                centerY = 160;
            }


            float heightRatio = (float) (mDM.heightPixels/320.0);
            float widthRatio = (float) (mDM.widthPixels/320.0);

            canvas.drawRect(bounds, mBgPaint);
            secondRadius = mTime.second*6;
            minuteRadius = mTime.minute * 6;
            hourRadius = (float) (mTime.hour*30+mTime.minute*0.5);
            minuteMatrix.setRotate(minuteRadius, minuteMap.getWidth()/2, minuteMap.getHeight());
            hourMatrix.setRotate(hourRadius, hourMap.getWidth()/2, hourMap.getHeight());
            minuteMatrix.postTranslate(centerX - minuteMap.getWidth() / 2, centerY - minuteMap.getHeight());
            hourMatrix.postTranslate(centerX - hourMap.getWidth() / 2, centerY - hourMap.getHeight());

            String time = String.format("%02d:%02d", mTime.hour, mTime.minute);
            String date = mTime.format("%b %d");
            mPaint = new Paint();
            mPaint.setColor(Color.BLACK);
            mPaint.setTextSize(40);
            float width = mPaint.measureText(time);


            Paint.FontMetricsInt fmi = mPaint.getFontMetricsInt();




            PaintFlagsDrawFilter pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
            canvas.setDrawFilter(pfd);
            mPaint.setAntiAlias(true);

            if (shouldTimerBeRunning()) {


                canvas.drawBitmap(background, new Rect(0, 0, background.getWidth(), background.getHeight()), bounds, null);
                if(arrayIndex == bitmapArray.length -1){
                    canvas.drawBitmap(bitmapArray[arrayIndex], new Rect(0, 0, bitmapArray[arrayIndex].getWidth(), bitmapArray[arrayIndex].getHeight()), bounds, null);
                    arrayIndex = 0;
                }else{
                    canvas.drawBitmap(bitmapArray[arrayIndex], new Rect(0, 0, bitmapArray[arrayIndex].getWidth(), bitmapArray[arrayIndex].getHeight()), bounds, null);
                    arrayIndex++;
                }
//                canvas.drawText(time, centerX - width / 2, (centerY - 105 * heightRatio + Math.abs(fmi.bottom)), mPaint);

                batteryMatrix.setTranslate(centerX - batteryMap.getWidth() / 2, centerY + 45 * heightRatio - batteryMap.getHeight() / 2);

                canvas.drawBitmap(batteryMap, batteryMatrix, null);


                if(mTemperature==null){
                    String NA = "";
                    mPaint.setTextSize(20);
                    width = mPaint.measureText(NA);
                    fmi = mPaint.getFontMetricsInt();

                    canvas.drawText(NA,centerX-42*widthRatio-width / 2, (centerY + 68 * heightRatio + Math.abs(fmi.bottom)), mPaint);
                    canvas.drawText(NA, centerX + 42 * widthRatio - width / 2, (centerY + 68 * heightRatio + Math.abs(fmi.bottom)), mPaint);

                    mPaint.setTextSize(22);
                    width = mPaint.measureText(date);
                    fmi = mPaint.getFontMetricsInt();
                    canvas.drawText(date,centerX -width / 2, (centerY + 68 * heightRatio + Math.abs(fmi.bottom)), mPaint);
                }else{

                    int bw = mTemperature.weatherIcon.getWidth();
                    int bh = mTemperature.weatherIcon.getHeight();

                    canvas.drawBitmap(mTemperature.weatherIcon, centerX-63*widthRatio-bw/2, centerY+54*heightRatio-bh/2, null);

                    mPaint.setTextSize(26);

                    String temp = String.format("%2.0f℉", mTemperature.temp);
                    if(temperature_type.equals("2")){
                        temp = String.format("%2.0f℃", (mTemperature.temp - 32) * 5 / 9);
                    }
                    width = mPaint.measureText(temp);
                    fmi = mPaint.getFontMetricsInt();
                    canvas.drawText(temp, centerX + 69 * widthRatio - width / 2, centerY + 57 * heightRatio + Math.abs(fmi.bottom), mPaint);


                    mPaint.setTextSize(19);
                    mPaint.setColor(Color.argb(140,0,0,0));
                    width = mPaint.measureText(date);
                    fmi = mPaint.getFontMetricsInt();
                    canvas.drawText(date, centerX - width / 2, (centerY + 68 * heightRatio + Math.abs(fmi.bottom)), mPaint);

                }


                canvas.drawBitmap(hourMap, hourMatrix, null);
                canvas.drawBitmap(minuteMap,minuteMatrix,null);

            }else {

                canvas.drawBitmap(ambientMap, new Rect(0, 0, ambientMap.getWidth(), ambientMap.getHeight()), bounds, null);
                String time1 = String.format("%02d", mTime.hour);
                String time2 = String.format("%02d", mTime.minute);
                String timeSeprator =":";
                mPaint = new Paint();

                mPaint.setTextSize(77);
                width = mPaint.measureText(time1);
                fmi = mPaint.getFontMetricsInt();
                mPaint.setColor(Color.argb(150,125,181,50));
                canvas.drawText(time1, centerX - 53 * widthRatio - width / 2, (centerY - 36 * heightRatio + Math.abs(fmi.bottom)), mPaint);

                mPaint.setTextSize(48);
                width = mPaint.measureText(time1);
                fmi = mPaint.getFontMetricsInt();
                mPaint.setColor(Color.rgb(216,190,31));
                canvas.drawText(time1, centerX - 38 * widthRatio - width / 2, (centerY + Math.abs(fmi.bottom)), mPaint);

                mPaint.setTextSize(77);
                width = mPaint.measureText(time2);
                fmi = mPaint.getFontMetricsInt();
                mPaint.setColor(Color.argb(150,216,190,31));
                canvas.drawText(time2, centerX + 53 * widthRatio - width / 2, (centerY + 36 * heightRatio + Math.abs(fmi.bottom)), mPaint);

                mPaint.setTextSize(48);
                mPaint.setColor(Color.rgb(125, 181, 50));
                width = mPaint.measureText(time2);
                fmi = mPaint.getFontMetricsInt();
                canvas.drawText(time2, centerX + 38*widthRatio-width / 2, (centerY + Math.abs(fmi.bottom)), mPaint);

                mPaint.setTextSize(44);
                width = mPaint.measureText(timeSeprator);
                fmi = mPaint.getFontMetricsInt();
                mPaint.setColor(Color.WHITE);
                canvas.drawText(timeSeprator, centerX -width / 2, (centerY + Math.abs(fmi.bottom)), mPaint);

                canvas.drawBitmap(hourMap,hourMatrix,null);
                canvas.drawBitmap(minuteMap, minuteMatrix, null);
            }

        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            mTime.setToNow();
            invalidate();
        }

        /** Handler to update the time once a second in interactive mode. */
        final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        mIndex = ++mIndex;
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = INTERACTIVE_UPDATE_RATE_MS
                                    - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                            mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                        break;
                }
            }
        };

        final Handler sendGAHandler = new Handler(){
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:


                        long timeMs = System.currentTimeMillis();
                        long delayMs = 3600000
                                - (timeMs % 3600000);
                        GlobalWearApiHelper.getInstance(AnalogWatchface.this).sendScreenView("Analog watchface using per hour");
                        sendGAHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);

                        break;
                }
            }
        };


        final Handler createServiceHandler = new Handler(){
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:


                        long timeMs = System.currentTimeMillis();
                        long delayMs = 10*60000
                                - (timeMs % 10*60000);

                        mWearableAPIHelper.putMessage("/START", new byte[0], null);
                        createServiceHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);

                        break;
                }
            }
        };

        private void GATimer(){
            sendGAHandler.removeMessages(MSG_UPDATE_TIME);
            sendGAHandler.sendEmptyMessage(MSG_UPDATE_TIME);
        }

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };

        boolean mRegisteredTimeZoneReceiver = false;

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            invalidate();

            // Whether the timer should be running depends on whether we're in ambient mode (as well
            // as whether we're visible), so we may need to start or stop the timer.
            updateTimer();
            createServiceHandler();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
            createServiceHandler();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            AnalogWatchface.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            AnalogWatchface.this.unregisterReceiver(mTimeZoneReceiver);
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        private  void createServiceHandler(){
            createServiceHandler.removeMessages(MSG_UPDATE_TIME);
            createServiceHandler.sendEmptyMessage(MSG_UPDATE_TIME);
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }
    }
}
