package polsl.p.helloworld;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "all";
    private static final String TAG1 = "one";
    private static final int TIME = 100;  //time to delay

    public SensorManager sensorManager;
    Sensor accelerometer, gyroscope;

    public TextView xAccelerometerValue, yAccelerometerValue, zAccelerometerValue, accuracy, timeStamp,
            xGyroscopeValue, yGyroscopeValue, zGyroscopeValue;

    public DataAG dataAG;

    ArrayList<DataAG> dataAGList = new ArrayList<DataAG>();
    public String xAccelerometer,yAccelerometer,zAccelerometer,xGyroscope,yGyroscope,zGyroscope;

    float[] accelerationArray = new float[3];
    float[] gyroscopeArray = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xAccelerometerValue = findViewById(R.id.xValue);
        yAccelerometerValue = findViewById(R.id.yValue);
        zAccelerometerValue = findViewById(R.id.zValue);
        accuracy = findViewById(R.id.accuracyValue);
        timeStamp = findViewById(R.id.timeStampValue);

        xGyroscopeValue = findViewById(R.id.xGyroscopeValue);
        yGyroscopeValue = findViewById(R.id.yGyroscopeValue);
        zGyroscopeValue = findViewById(R.id.zGyroscopeValue);

        Log.d(TAG, "onCreate: Initializing Sensor Services");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        Log.d(TAG, "onCreate: Registered accelerometer listener");

        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(MainActivity.this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        Log.d(TAG, "onCreate: Registered gyroscope listener");

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Log.d(TAG, "onSensorChanged ACCELEROMETER: X: " + event.values[0] + " Y: " + event.values[1] + " Z: " + event.values[2]);

            accuracy.setText("accuracy: " + event.accuracy);
            timeStamp.setText("timeStamp " + event.timestamp);
            xAccelerometerValue.setText("xValue: "+ event.values[0]);
            yAccelerometerValue.setText("yValue: "+ event.values[1]);
            zAccelerometerValue.setText("zValue: "+ event.values[2]);
            accelerationArray[0] = event.values[0];
            accelerationArray[1] = event.values[1];
            accelerationArray[2] = event.values[2];

        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            Log.d(TAG, "onSensorChanged GYROSCOPE: X: " + event.values[0] + " Y: " + event.values[1] + " Z: " + event.values[2]);

            xGyroscopeValue.setText("xGyroscopeValue: " + event.values[0]);
            yGyroscopeValue.setText("yGyroscopeValue: " + event.values[1]);
            zGyroscopeValue.setText("zGyroscopeValue: " + event.values[2]);
            gyroscopeArray[0] = event.values[0];
            gyroscopeArray[1] = event.values[1];
            gyroscopeArray[2] = event.values[2];
        }

        xAccelerometer = String.valueOf(accelerationArray[0]);
        yAccelerometer = String.valueOf(accelerationArray[1]);
        zAccelerometer = String.valueOf(accelerationArray[2]);
        xGyroscope = String.valueOf(gyroscopeArray[0]);
        yGyroscope = String.valueOf(gyroscopeArray[1]);
        zGyroscope = String.valueOf(gyroscopeArray[2]);

        dataAGList.add(new DataAG(xAccelerometer,yAccelerometer,zAccelerometer,zGyroscope,yGyroscope,zGyroscope));
        for(DataAG ag: dataAGList)
        {
            Log.d(TAG1, "dataAGLIST: "+ag+"\n");
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "\n"+xAccelerometer+","+yAccelerometer+","+zAccelerometer+","+xGyroscope+","+yGyroscope+","+zGyroscope;

    }

    public void export(View view) {
        StringBuilder data = new StringBuilder();
        data.append("Ax,Ay,Az,Gx,Gy,Gz");
        for(DataAG ag: dataAGList)
        data.append("\n").append(dataAGList);
/*
        data.append("\n"+String.valueOf( accelerationArray[0])+","+String.valueOf( accelerationArray[1])+","+String.valueOf( accelerationArray[2])
                +","+String.valueOf(gyroscopeArray[0])+","+String.valueOf( gyroscopeArray[1])+","+String.valueOf( gyroscopeArray[2]));
*/
        try{
            FileOutputStream out = openFileOutput("data.csv",Context.MODE_PRIVATE);
            out.write(data.toString().getBytes());
            out.close();

            Context context = getApplicationContext();
            File filelocation = new File(getFilesDir(),"data.csv");
            Uri path = FileProvider.getUriForFile(context,"polsl.p.helloworld.fileprovider",filelocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/csv");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT,"Data");
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM,path);
            startActivity(Intent.createChooser(fileIntent,"Send mail"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
