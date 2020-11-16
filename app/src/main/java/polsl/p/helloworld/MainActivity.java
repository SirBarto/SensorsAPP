package polsl.p.helloworld;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;

import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

import static android.util.Half.EPSILON;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class MainActivity extends LineChartConfig implements SensorEventListener, LocationListener {

    private static final String TAG = "all";
    private static final String TAG1 = "one";
    private static final String TAG2 = "gyro";
    private static final int TIME_DELAY = SensorManager.SENSOR_DELAY_GAME;//20;//100000000;  //time to delay 500ms = 2Hz / 1000ms = 1Hz   0.1Hz to daje 65scope/s
    private int counter = 0, counterTxt = 0;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    public float timestamp;
    private double omegaMagnitudeAccelerometer, omegaMagnitudeGyroscope;

    private Date date = new Date();
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private String actualData = simpleDateFormat.format(date);

    private SensorManager sensorManager;
    private Sensor accelerometerToChart, accelerometer, gyroscope;

    private TextView xAccelerometerValue, yAccelerometerValue, zAccelerometerValue, accuracy, timeStamp,
            xGyroscopeValue, yGyroscopeValue, zGyroscopeValue,
            tv_speed;
    private SwitchCompat sw_metric;
    float nCurrentSpeed = 0;

    ArrayList<DataAG> dataAGList = new ArrayList<DataAG>();
    private String xAccelerometer, yAccelerometer, zAccelerometer, xGyroscope, yGyroscope, zGyroscope;

    private float[] accelerationArray = new float[3];
    private float[] gyroscopeArray = new float[3];
    private float[] gravity = new float[3];

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "GRANTED", Toast.LENGTH_SHORT).show();
            doStuff();
        } else {
            Toast.makeText(this, "DENIED", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

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

        checkExternalMedia();

        sensors();

        chartCustomAndCreate();

        startPlot();

        tv_speed = (TextView) findViewById(R.id.distance);
        sw_metric = findViewById(R.id.sw_metric);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        } else {
            doStuff();
        }

        this.updateSpeed(null);

        sw_metric.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.this.updateSpeed(null);
            }
        });
    }

    private void sensors() {
        Log.i(TAG, "onCreate: Initializing Sensor Services");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(MainActivity.this, accelerometer, TIME_DELAY);
        Log.i(TAG, "onCreate: Registered accelerometer listener");

        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(MainActivity.this, gyroscope, TIME_DELAY);
        Log.i(TAG2, "onCreate: Registered gyroscope listener");

        accelerometerToChart = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        if (accelerometerToChart != null && gyroscope != null) {
            sensorManager.registerListener(MainActivity.this, accelerometerToChart, TIME_DELAY);
            //sensorManager.registerListener(MainActivity.this, gyroscope,TIME_DELAY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (thread != null) {
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

        final float alpha = (float) 0.8;
        final float dT = (event.timestamp - timestamp) * NS2S;

        if (plotData) {
            addEntry(event);
            plotData = false;

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                Log.i(TAG, "onSensorChanged ACCELEROMETER: X: " + event.values[0] + " Y: " + event.values[1] + " Z: " + event.values[2]);

                //filtr dolnoprzepustowy
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                //filtr górnoprzepustowy
                accelerationArray[0] = event.values[0] - gravity[0];
                accelerationArray[1] = event.values[1] - gravity[1];
                accelerationArray[2] = event.values[2] - gravity[2];

                xAccelerometerValue.setText("xValue: " + accelerationArray[0]);
                yAccelerometerValue.setText("yValue: " + accelerationArray[1]);
                zAccelerometerValue.setText("zValue: " + accelerationArray[2]);

                /*accelerationArray[0] = event.values[0];
                  accelerationArray[1] = event.values[1];
                  accelerationArray[2] = event.values[2];*/
                omegaMagnitudeAccelerometer = Math.sqrt(accelerationArray[0] * accelerationArray[0] +
                        accelerationArray[1] * accelerationArray[1] +
                        accelerationArray[2] * accelerationArray[2]);//predkosc katowa probki akcelerometru

                accuracy.setText("accuracy: " + omegaMagnitudeAccelerometer);

            }

            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                Log.i(TAG, "onSensorChanged GYROSCOPE: X: " + event.values[0] + " Y: " + event.values[1] + " Z: " + event.values[2]);

                if (timestamp != 0) {
                    gyroscopeArray[0] = event.values[0];
                    gyroscopeArray[1] = event.values[1];
                    gyroscopeArray[2] = event.values[2];
                    timeStamp.setText("timeStamp " + dT);
                    xGyroscopeValue.setText("xGyroscopeValue: " + event.values[0]);
                    yGyroscopeValue.setText("yGyroscopeValue: " + event.values[1]);
                    zGyroscopeValue.setText("zGyroscopeValue: " + event.values[2]);

                    omegaMagnitudeGyroscope = Math.sqrt(gyroscopeArray[0] * gyroscopeArray[0] +
                            gyroscopeArray[1] * gyroscopeArray[1] +
                            gyroscopeArray[2] * gyroscopeArray[2]);//predkosc katowa probki zyroskopu

                    if (omegaMagnitudeGyroscope > EPSILON) {
                        gyroscopeArray[0] /= omegaMagnitudeGyroscope;
                        gyroscopeArray[1] /= omegaMagnitudeGyroscope;
                        gyroscopeArray[2] /= omegaMagnitudeGyroscope;
                    }

                    float thetaOverTwo = (float) (omegaMagnitudeGyroscope * dT / 2.0f);
                    float sinThetaOverTwo = (float) sin(thetaOverTwo);
                    float cosThetaOverTwo = (float) cos(thetaOverTwo);
                    deltaRotationVector[0] = sinThetaOverTwo * gyroscopeArray[0];
                    deltaRotationVector[1] = sinThetaOverTwo * gyroscopeArray[1];
                    deltaRotationVector[2] = sinThetaOverTwo * gyroscopeArray[2];
                    deltaRotationVector[3] = cosThetaOverTwo;
                }

                timestamp = event.timestamp;
                float[] deltaRotationMatrix = new float[9];
                SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
            }

            xAccelerometer = String.valueOf(accelerationArray[0]);
            yAccelerometer = String.valueOf(accelerationArray[1]);
            zAccelerometer = String.valueOf(accelerationArray[2]);
            xGyroscope = String.valueOf(gyroscopeArray[0]);
            yGyroscope = String.valueOf(gyroscopeArray[1]);
            zGyroscope = String.valueOf(gyroscopeArray[2]);

            dataAGList.add(new DataAG(xAccelerometer, yAccelerometer, zAccelerometer,
                    xGyroscope, yGyroscope, zGyroscope,
                    counter, dT, omegaMagnitudeAccelerometer, omegaMagnitudeGyroscope, displayV())); //myService.displayV()

            counter = counter + 1;

            if (counter == 200) {
                exportTXT();
                dataAGList.clear();
                counter = 0;
            }

            for (DataAG ag : dataAGList) {
                Log.i(TAG1, "dataAGLIST: " + ag + "\n");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensors();
        sensorManager.registerListener(this, accelerometerToChart, TIME_DELAY); //time delay configuration
        sensorManager.registerListener(this, gyroscope, TIME_DELAY);
    }

    @Override
    protected void onDestroy() {
        sensorManager.unregisterListener(MainActivity.this);
        thread.interrupt();
        super.onDestroy();
    }

    public void exportCSV(View view) {
        StringBuilder data = new StringBuilder();
        data.append("Ax,Ay,Az,Gx,Gy,Gz,T,VM");
        for (DataAG ag : dataAGList)
            data.append("\n").append(dataAGList);
/*
        data.append("\n"+String.valueOf( accelerationArray[0])+","+String.valueOf( accelerationArray[1])+","+String.valueOf( accelerationArray[2])
                +","+String.valueOf(gyroscopeArray[0])+","+String.valueOf( gyroscopeArray[1])+","+String.valueOf( gyroscopeArray[2]));
*/
        try {
            FileOutputStream out = openFileOutput("data.csv", Context.MODE_PRIVATE);
            out.write(data.toString().getBytes());
            out.close();

            Context context = getApplicationContext();
            File filelocation = new File(getFilesDir(), "data.csv");
            Uri path = FileProvider.getUriForFile(context, "polsl.p.helloworld.fileprovider", filelocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/csv");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Data");
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            startActivity(Intent.createChooser(fileIntent, "Send mail"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("ShowToast")
    private void exportTXT() {
        StringBuilder data = new StringBuilder();
        data.append("Ax,Ay,Az,Gx,Gy,Gz,T,VMA, VMG, V");
        for (DataAG ag : dataAGList)
            data.append("\n").append(dataAGList);

        final File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MUSIC + "/pomiar/" + actualData + "/"
        );

        if (!path.exists()) {
            path.mkdirs();
        }

        String fileName = "data" + counterTxt + ".txt";
        final File file = new File(path, fileName);
        counterTxt = counterTxt + 1;

        try {
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            OutputStreamWriter streamWriter = new OutputStreamWriter(fileOutputStream);
            streamWriter.append(data);
            streamWriter.close();
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop(View view) {
        onPause();
    }

    public void start(View view) {
        onResume();
    }

    private void showGPSDIsableAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Enable GPS to use application")
                .setCancelable(false);
    }

    private void checkExternalMedia() {
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        Toast.makeText(getBaseContext(), "External Media: readeble=" + mExternalStorageAvailable + " writable=" + mExternalStorageWriteable, Toast.LENGTH_LONG);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (location != null) {
            CLocation myLocation = new CLocation(location, this.useMetricUnits());
            this.updateSpeed(myLocation);
        }

    }

    private boolean useMetricUnits() {
        return sw_metric.isChecked();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @SuppressLint("MissingPermission")
    private void doStuff() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
        Toast.makeText(this, "Waiting for GPS connection!", Toast.LENGTH_SHORT).show();
    }

    private void updateSpeed(CLocation cLocation) {
        //float nCurrentSpeed = 0;

        if (cLocation != null) {
            cLocation.setbUseMetricUnit(this.useMetricUnits());
            nCurrentSpeed = cLocation.getSpeed() * 4;
            displayV();
        }

        Formatter formatter = new Formatter(new StringBuffer());
        formatter.format(Locale.US, "%5.1f", nCurrentSpeed);
        String strCurrentSpeed = formatter.toString();
        strCurrentSpeed = strCurrentSpeed.replace(" ", "0");

        if (this.useMetricUnits()) {
            tv_speed.setText(strCurrentSpeed + "km/h");
        } else {
            tv_speed.setText(strCurrentSpeed + "miles/h");
        }

    }

    private float displayV() {
        return nCurrentSpeed;
    }

    @NonNull
    @Override
    public String toString() {
        return "\n" + xAccelerometer + "," + yAccelerometer + "," + zAccelerometer + "," + xGyroscope + "," + yGyroscope + "," + zGyroscope;

    }

}
