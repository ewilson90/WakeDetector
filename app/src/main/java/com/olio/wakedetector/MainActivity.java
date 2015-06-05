package com.olio.wakedetector;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.olio.util.ALog;


public class MainActivity extends ActionBarActivity {

    private SensorManager mSensorManager = null;
    private Sensor mSigMotionSensor = null;
    private TriggerEventListener mTriggerEventListener = null;

    private PowerManager mPowerManager = null;
    private PowerManager.WakeLock mWakeLock = null;

    private Vibrator mVibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        for(Sensor sensor : mSensorManager.getSensorList(Sensor.TYPE_ALL)) {
            String sensorType;
            switch(sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    sensorType = Sensor.STRING_TYPE_ACCELEROMETER;
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    sensorType = Sensor.STRING_TYPE_GYROSCOPE;
                    break;
                case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                    sensorType = Sensor.STRING_TYPE_GYROSCOPE_UNCALIBRATED;
                    break;
                case Sensor.TYPE_ROTATION_VECTOR:
                    sensorType = Sensor.STRING_TYPE_ROTATION_VECTOR;
                    break;
                case Sensor.TYPE_GRAVITY:
                    sensorType = Sensor.STRING_TYPE_GRAVITY;
                    break;
                case Sensor.TYPE_SIGNIFICANT_MOTION:
                    sensorType = Sensor.STRING_TYPE_SIGNIFICANT_MOTION;
                    break;
                case Sensor.TYPE_STEP_COUNTER:
                    sensorType = Sensor.STRING_TYPE_STEP_COUNTER;
                    break;
                case Sensor.TYPE_STEP_DETECTOR:
                    sensorType = Sensor.STRING_TYPE_STEP_DETECTOR;
                    break;
                default:
                    sensorType = ""+sensor.getType();
            }
            ALog.d("Found sensor: %s, Type: %s", sensor.getName(), sensorType);

            if(sensor.getType() == Sensor.TYPE_SIGNIFICANT_MOTION) {
                ALog.d("Got significant motion sensor.");
                mSigMotionSensor = sensor;
                if(mTriggerEventListener != null) {
                    ALog.d("Sig motion sensor already registered.");
                    return;
                }

                mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "com.olio.wakedetector");
                mTriggerEventListener = new SigMotionTriggerListener();
                mSensorManager.requestTriggerSensor(mTriggerEventListener, mSigMotionSensor);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mTriggerEventListener != null && mSensorManager != null) {
            mSensorManager.cancelTriggerSensor(mTriggerEventListener, mSigMotionSensor);
            mTriggerEventListener = null;
        }
    }

    public void onVibrateClick(View v) {
        ALog.d("Vibrating.");
        mVibrator.vibrate(new long[]{250, 500, 100, 500}, -1);
    }
    private class SigMotionTriggerListener extends TriggerEventListener {
        @Override
        public void onTrigger(TriggerEvent event) {
            ALog.d("Got significant motion trigger. Re-registering.");
            mWakeLock.acquire(5000);
            mSensorManager.requestTriggerSensor(mTriggerEventListener, mSigMotionSensor);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
