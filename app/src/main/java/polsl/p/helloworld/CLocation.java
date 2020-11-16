package polsl.p.helloworld;

import android.location.Location;

public class CLocation extends Location {

    private boolean bUseMetricUnit = false;

    public CLocation (Location location){
        this (location,true);
    }

    public CLocation (Location location, boolean bUseMetricUnit){
        super (location);
        this.bUseMetricUnit = bUseMetricUnit;
    }

    public boolean getUseMetricUnits() {
        return this.bUseMetricUnit;
    }

    public void setbUseMetricUnit(boolean bUseMetricUnit) {
        this.bUseMetricUnit = bUseMetricUnit;
    }

    @Override
    public float distanceTo(Location dest) {
        float nDistance = super.distanceTo(dest);

        if(!this.getUseMetricUnits()){
            //Convert meters to feet
            nDistance = nDistance * 3.28083989501312f;
        }
        return nDistance;
    }

    @Override
    public double getAltitude() {
        double nAltitude = super.getAltitude();

        if(!this.getUseMetricUnits()){
            nAltitude = nAltitude * 3.28083989501312d;
        }

        return nAltitude;
    }

    @Override
    public float getSpeed() {
        float nSpeed = super.getSpeed();

        if(!this.getUseMetricUnits()){
            //convert m/s to miles/hour
            nSpeed = nSpeed * 2.23693629f;
        }

        return nSpeed;
    }

    @Override
    public float getAccuracy() {
        float nAccuracy = super.getAccuracy();

        if(!this.getUseMetricUnits()){
            nAccuracy = nAccuracy * 3.28083989501312f;
        }

        return nAccuracy;
    }
}
