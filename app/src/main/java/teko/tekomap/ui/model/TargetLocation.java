package teko.tekomap.ui.model;

public class TargetLocation {
    public enum EmergencyLevel{
        LOW, NORMAL, HIGH
    }

    private Double latitude, longitude;
    private EmergencyLevel emergencyLevel;

    public TargetLocation(Double latitude, Double longitude, EmergencyLevel emergencyLevel) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.emergencyLevel = emergencyLevel;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public EmergencyLevel getEmergencyLevel() {
        return emergencyLevel;
    }

    public void setEmergencyLevel(EmergencyLevel emergencyLevel) {
        this.emergencyLevel = emergencyLevel;
    }
}
