package teko.tekomap.ui;

import com.yandex.mapkit.geometry.Point;

import java.util.ArrayList;
import java.util.List;

import teko.tekomap.ui.model.TargetLocation;

public class Utils {

    public static List<TargetLocation> popDummySouroundLocations(Point point) {
        List<TargetLocation> targetLocations = new ArrayList<>();

        targetLocations.add(new TargetLocation(point.getLatitude() + 0.011, point.getLongitude() - 0.005, TargetLocation.EmergencyLevel.LOW));
        targetLocations.add(new TargetLocation(point.getLatitude() + 0.0228, point.getLongitude() + 0.02243, TargetLocation.EmergencyLevel.NORMAL));
        targetLocations.add(new TargetLocation(point.getLatitude() + 0.031, point.getLongitude() - 0.005, TargetLocation.EmergencyLevel.LOW));
        targetLocations.add(new TargetLocation(point.getLatitude() - 0.011, point.getLongitude() - 0.015, TargetLocation.EmergencyLevel.HIGH));
        targetLocations.add(new TargetLocation(point.getLatitude() - 0.021, point.getLongitude() + 0.025, TargetLocation.EmergencyLevel.NORMAL));

        return targetLocations;
    }
}
