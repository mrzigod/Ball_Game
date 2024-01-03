package com.example.myapplication;

import static android.hardware.SensorManager.getOrientation;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.activity.ComponentActivity;

public class GameplayActivity extends ComponentActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private GameView gameView;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        int level=extras.getInt("Level");
        String name= extras.getString("name");
        boolean multi=extras.getBoolean("multi");
        boolean host=extras.getBoolean("host");
        gameView= new GameView(this,level,name,multi,host);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if(multi) {
            Handler handler = new Handler(Looper.getMainLooper()) {
                public void handleMessage(Message msg) {
                    if (msg.what == 3) {
                        gameView.putOtherBall(msg.obj.toString());
                    }
                    if (msg.what==4){
                        gameView.loadSentLevel(msg.obj.toString());
                    }
                }
            };
            BCSHolder.setGameHandler(handler);
        }
        setContentView(gameView);

    }
    @Override
    public void onResume() {
        super.onResume();
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }

    }

    @Override
    public void onStop(){
        super.onStop();
        sensorManager.unregisterListener(this);
    }
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
        }
        updateOrientationAngles();
        gameView.onAngleEvent(orientationAngles);
    }

    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);

        // "rotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(rotationMatrix, orientationAngles);

        // "orientationAngles" now has up-to-date information.
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
