package de.jandrotek.android.arobot.app.arobotbt;

public interface TiltListener {

    /**
     * Callback with tilt values.
     *
     *  @param azimuth, rotation around the Z axis.
     *  @param pitch, rotation around the X axis.
     *  see android.reference: sensorManager.getOrientation()
     *  in radians; 0 means all the way to the left, pi/2 straight up, pi all the way right
     */
    public void setTilt(double azimuth, double pitch);

}
