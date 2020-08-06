package polsl.p.helloworld;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "all";
    private static final String TAG1 = "one";
    private static final int TIME = 100;  //time to delay

    private SensorManager sensorManager;
    private Sensor accelerometerToChart, accelerometer, gyroscope;

    private LineChart mChart;
    private Thread thread;
    private boolean plotData = true;

    public TextView xAccelerometerValue, yAccelerometerValue, zAccelerometerValue, accuracy, timeStamp,
            xGyroscopeValue, yGyroscopeValue, zGyroscopeValue;

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
        sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        Log.d(TAG, "onCreate: Registered accelerometer listener");

        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(MainActivity.this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        Log.d(TAG, "onCreate: Registered gyroscope listener");

        accelerometerToChart = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        if(accelerometerToChart !=null) {
            sensorManager.registerListener(MainActivity.this, accelerometerToChart,SensorManager.SENSOR_DELAY_GAME);
        }

        mChart = (LineChart) findViewById(R.id.chart);
        mChart.getDescription().setEnabled(true);
        mChart.getDescription().setText("Real time Accelerometer Data Plot");
        mChart.setTouchEnabled(false);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(true);
        mChart.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChart.setData(data);
        mChart.invalidate();

        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis x1 = mChart.getXAxis();
        x1.setTextColor(Color.WHITE);
        x1.setDrawGridLines(true);
        x1.setAvoidFirstLastClipping(true);
        x1.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMaximum(10f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.setDrawBorders(false);

        startPlot();
    }

    private void addEntry(SensorEvent event){
        LineData data = mChart.getData();

        if(data!=null){
            ILineDataSet set = data.getDataSetByIndex(0);

            if(set==null){
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(),event.values[0]+5),0);
            data.notifyDataChanged();

            mChart.notifyDataSetChanged();
            mChart.setMaxVisibleValueCount(150);
            mChart.moveViewToX(data.getEntryCount());
        }
    }

    private LineDataSet createSet(){
        LineDataSet set = new LineDataSet(null,"Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setColor(Color.MAGENTA);
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }

    private void startPlot(){
        if(thread !=null){
            thread.interrupt();
        }
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    plotData = true;
                    try {
                        Thread.sleep(100);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(thread!=null){
            thread.interrupt();
        }
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSensorChanged(SensorEvent event) {

        if(plotData) {
            addEntry(event);
            plotData = false;

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                Log.d(TAG, "onSensorChanged ACCELEROMETER: X: " + event.values[0] + " Y: " + event.values[1] + " Z: " + event.values[2]);

                accuracy.setText("accuracy: " + event.accuracy);
                timeStamp.setText("timeStamp " + event.timestamp);
                xAccelerometerValue.setText("xValue: " + event.values[0]);
                yAccelerometerValue.setText("yValue: " + event.values[1]);
                zAccelerometerValue.setText("zValue: " + event.values[2]);
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

            dataAGList.add(new DataAG(xAccelerometer, yAccelerometer, zAccelerometer, zGyroscope, yGyroscope, zGyroscope));
            for (DataAG ag : dataAGList) {
                Log.d(TAG1, "dataAGLIST: " + ag + "\n");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometerToChart,SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onDestroy() {
        sensorManager.unregisterListener(MainActivity.this);
        thread.interrupt();
        super.onDestroy();
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

    @NonNull
    @Override
    public String toString() {
        return "\n"+xAccelerometer+","+yAccelerometer+","+zAccelerometer+","+xGyroscope+","+yGyroscope+","+zGyroscope;

    }
}
