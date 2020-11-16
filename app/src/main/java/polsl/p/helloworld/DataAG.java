package polsl.p.helloworld;

import androidx.annotation.NonNull;

public class DataAG {

    final private String xAccelerometer,yAccelerometer,zAccelerometer,
            xGyroscope,yGyroscope,zGyroscope;
    private int scope;
    private float timeStamp;
    private double omegaMagnitudeAccelerometer,omegaMagnitudeGyroscope,speedV;

    public DataAG(String xAccelerometer, String yAccelerometer, String zAccelerometer, String xGyroscope, String yGyroscope, String zGyroscope,
                  int scope, float timeStamp, double omegaMagnitudeAccelerometer, double omegaMagnitudeGyroscope, double speedV) {
        this.xAccelerometer = xAccelerometer;
        this.yAccelerometer = yAccelerometer;
        this.zAccelerometer = zAccelerometer;
        this.xGyroscope = xGyroscope;
        this.yGyroscope = yGyroscope;
        this.zGyroscope = zGyroscope;
        this.scope = scope;
        this.timeStamp = timeStamp;
        this.omegaMagnitudeAccelerometer = omegaMagnitudeAccelerometer;
        this.omegaMagnitudeGyroscope = omegaMagnitudeGyroscope;
        this.speedV = speedV;
    }

    @NonNull
    @Override
    public String toString() {
        return "\n"+xAccelerometer+","+yAccelerometer+","+zAccelerometer+","+xGyroscope+","+yGyroscope+","+zGyroscope+","+scope+","+timeStamp+","+ omegaMagnitudeAccelerometer +","+omegaMagnitudeGyroscope+","+speedV;
    }
}
