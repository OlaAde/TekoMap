package teko.tekomap.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.CardView;
import android.view.View;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.image.ImageProvider;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import teko.tekomap.R;
import teko.tekomap.ui.Utils;
import teko.tekomap.ui.model.TargetLocation;

import static teko.tekomap.Constants.MAPKIT_API_KEY;

public class DrivingActivity extends Activity implements UserLocationObjectListener {
    private MapView mapView;
    private CardView bottomSheetCardView;
    private BottomSheetBehavior mBottomSheetBehavior;
    private MapObjectCollection mapObjects;
    private UserLocationLayer userLocationLayer;

    private Point targetPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);

        setContentView(R.layout.driving);
        ButterKnife.bind(this);

        mapView = findViewById(R.id.mapview);
        bottomSheetCardView = findViewById(R.id.bottom_sheet);

        mBottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));

        bottomSheetCardView.setVisibility(View.GONE);

        mapView.getMap().setRotateGesturesEnabled(true);
        mapView.getMap().move(new CameraPosition(new Point(0, 0), 14, 0, 0));

        mapObjects = mapView.getMap().getMapObjects().addCollection();

        Dexter.withActivity(this).withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {

                userLocationLayer = mapView.getMap().getUserLocationLayer();
                userLocationLayer.setEnabled(true);
                userLocationLayer.setHeadingEnabled(true);
                userLocationLayer.setObjectListener(DrivingActivity.this);

                SmartLocation.with(DrivingActivity.this).location().continuous().start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {
                        setUpSurroudTags(new Point(location.getLatitude(), location.getLongitude()));
                    }
                });
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

            }

        }).check();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    public void onObjectAdded(UserLocationView userLocationView) {

        userLocationLayer.setAnchor(
                new PointF((float) (mapView.getWidth() * 0.5), (float) (mapView.getHeight() * 0.5)),
                new PointF((float) (mapView.getWidth() * 0.5), (float) (mapView.getHeight() * 0.83)));

        userLocationView.getPin().setIcon(ImageProvider.fromResource(
                this, R.drawable.user_arrow));
        userLocationView.getArrow().setIcon(ImageProvider.fromResource(
                this, R.drawable.user_arrow));
        userLocationView.getAccuracyCircle().setFillColor(Color.TRANSPARENT);

    }

    private void setUpSurroudTags(Point userLocation) {
        for (final TargetLocation targetLocation : Utils.popDummySouroundLocations(userLocation)) {
            PlacemarkMapObject mark = mapObjects.addPlacemark(new Point(targetLocation.getLatitude(), targetLocation.getLongitude()));

            mark.addTapListener(new MapObjectTapListener() {
                @Override
                public boolean onMapObjectTap(MapObject mapObject, Point point) {
                    showBottomSheet(point);
                    return true;
                }
            });

            mark.setIcon(new ImageProvider() {
                @Override
                public String getId() {
                    switch (targetLocation.getEmergencyLevel()) {
                        case LOW:
                            return getResources().getResourceName(R.drawable.ic_low_emerg);
                        case NORMAL:
                            return getResources().getResourceName(R.drawable.ic_normal_emerg);
                        case HIGH:
                            return getResources().getResourceName(R.drawable.ic_high_emerg);

                        default:
                            return getResources().getResourceName(R.drawable.ic_low_emerg);
                    }
                }

                @Override
                public Bitmap getImage() {
                    switch (targetLocation.getEmergencyLevel()) {
                        case LOW:
                            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_low_emerg);
                        case NORMAL:
                            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_normal_emerg);
                        case HIGH:
                            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_high_emerg);

                        default:
                            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_low_emerg);
                    }
                }
            });
        }
    }

    private void showBottomSheet(Point point) {
        bottomSheetCardView.post(new Runnable() {
            @Override
            public void run() {
                bottomSheetCardView.setVisibility(View.VISIBLE);
                mBottomSheetBehavior.setPeekHeight(200);
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        targetPoint = point;
    }

    @Override
    public void onObjectRemoved(UserLocationView view) {
    }

    @Override
    public void onObjectUpdated(UserLocationView view, ObjectEvent event) {

    }

    @OnClick(R.id.buttonToDrive)
    public void goToDriving() {
        if (targetPoint != null) {
            Intent intent = new Intent(DrivingActivity.this, MapActivity.class);
            intent.putExtra("targetLat", targetPoint.getLatitude());
            intent.putExtra("targetLong", targetPoint.getLongitude());
            startActivity(intent);
        }
    }

}