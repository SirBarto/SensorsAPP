package polsl.p.helloworld;

import androidx.annotation.NonNull;

public class DataAG {

    final private String xAccelerometer,yAccelerometer,zAccelerometer,xGyroscope,yGyroscope,zGyroscope;

    public DataAG(String xAccelerometer, String yAccelerometer, String zAccelerometer, String xGyroscope, String yGyroscope, String zGyroscope) {
        this.xAccelerometer = xAccelerometer;
        this.yAccelerometer = yAccelerometer;
        this.zAccelerometer = zAccelerometer;
        this.xGyroscope = xGyroscope;
        this.yGyroscope = yGyroscope;
        this.zGyroscope = zGyroscope;
    }

    @NonNull
    @Override
    public String toString() {
        return "\n"+xAccelerometer+","+yAccelerometer+","+zAccelerometer+","+xGyroscope+","+yGyroscope+","+zGyroscope;
    }
}
