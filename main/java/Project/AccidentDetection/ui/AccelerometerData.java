package Project.AccidentDetection.ui;

public class AccelerometerData {
    private double accX;
    private double accY;
    private double accZ;

    public double getAccX() {
        return accX;
    }

    public void setAccX(double accX) {
        this.accX = accX;
    }

    public double getAccY() {
        return accY;
    }

    public void setAccY(double accY) {
        this.accY = accY;
    }

    public double getAccZ() {
        return accZ;
    }

    public void setAccZ(double accZ) {
        this.accZ = accZ;
    }

    @Override
    public String toString() {
        return "AccelerometerData{" +
                "accX=" + accX +
                ", accY=" + accY +
                ", accZ=" + accZ +
                '}';
    }
}
