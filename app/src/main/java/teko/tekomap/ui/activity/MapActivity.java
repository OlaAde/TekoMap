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
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.ScreenPoint;
import com.yandex.mapkit.ScreenRect;
import com.yandex.mapkit.driving.DrivingOptions;
import com.yandex.mapkit.driving.DrivingRoute;
import com.yandex.mapkit.driving.DrivingRouter;
import com.yandex.mapkit.driving.DrivingSession;
import com.yandex.mapkit.driving.RequestPoint;
import com.yandex.mapkit.driving.RequestPointType;
import com.yandex.mapkit.geometry.BoundingBox;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;
import com.yandex.runtime.network.NetworkError;
import com.yandex.runtime.network.RemoteError;

import java.util.ArrayList;
import java.util.List;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import teko.tekomap.R;

import static teko.tekomap.Constants.MAPKIT_API_KEY;

public class MapActivity extends Activity implements DrivingSession.DrivingRouteListener, UserLocationObjectListener {
    private static final String TAG = MapActivity.class.getSimpleName();
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1; // 1 minute

    private MapView mMapView;
    private ProgressBar mProgressBar;
    private MapObjectCollection mMapObjects;
    private Point ROUTE_START_LOCATION = new Point(55.959194, 49.107094);
    private Point ROUTE_END_LOCATION = new Point(55.952, 49.10218);
    private Double targetLat, targetLong;
    private DrivingRouter drivingRouter;
    private DrivingSession drivingSession;
    private UserLocationLayer userLocationLayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);

        setContentView(R.layout.activity_map);
        mMapView = findViewById(R.id.mapview);
        mProgressBar = findViewById(R.id.progressBar);

        Intent intent = getIntent();
        if (intent != null) {
            targetLat = intent.getDoubleExtra("targetLat", 0);
            targetLong = intent.getDoubleExtra("targetLong", 0);

            ROUTE_END_LOCATION = new Point(targetLat, targetLong);

        }

        mMapView.getMap().move(new CameraPosition(
                ROUTE_START_LOCATION, 12.0f, 0, 0));
        drivingRouter = MapKitFactory.getInstance().createDrivingRouter();
        mMapObjects = mMapView.getMap().getMapObjects().addCollection();

        Dexter.withActivity(this).withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).withListener(new MultiplePermissionsListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {

                userLocationLayer = mMapView.getMap().getUserLocationLayer();
                userLocationLayer.setEnabled(true);
                userLocationLayer.setHeadingEnabled(true);
                userLocationLayer.setObjectListener(MapActivity.this);

                SmartLocation.with(MapActivity.this).location().continuous().start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {
                        ROUTE_START_LOCATION = new Point(location.getLatitude(), location.getLongitude());

                        PlacemarkMapObject endMark = mMapObjects.addPlacemark(new Point(ROUTE_END_LOCATION.getLatitude(), ROUTE_END_LOCATION.getLongitude()));

//                        startMark.setIcon(new ImageProvider() {
//                            @Override
//                            public String getId() {
//                                return getResources().getResourceName(R.drawable.ic_low_emerg);
//                            }
//
//                            @Override
//                            public Bitmap getImage() {
//                                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_low_emerg);
//                            }
//                        });

                        endMark.setIcon(new ImageProvider() {
                            @Override
                            public String getId() {
                                return getResources().getResourceName(R.drawable.ic_low_emerg);
                            }

                            @Override
                            public Bitmap getImage() {
                                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_low_emerg);
                            }
                        });


                        submitRequest();


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
        super.onStop();
        mMapView.onStop();
        MapKitFactory.getInstance().onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
        MapKitFactory.getInstance().onStart();
    }

    private void submitRequest() {
        DrivingOptions options = new DrivingOptions().setAlternativeCount(1);
        ArrayList<RequestPoint> requestPoints = new ArrayList<>();
        requestPoints.add(new RequestPoint(
                ROUTE_START_LOCATION, new ArrayList<Point>(), RequestPointType.WAYPOINT));
        requestPoints.add(new RequestPoint(
                ROUTE_END_LOCATION, new ArrayList<Point>(), RequestPointType.WAYPOINT));
        drivingSession = drivingRouter.requestRoutes(requestPoints, options, this);

        CameraPosition position = mMapView.getMap().cameraPosition(new BoundingBox(ROUTE_END_LOCATION, ROUTE_START_LOCATION));
        mMapView.getMap().move(position);


        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onDrivingRoutes(List<DrivingRoute> routes) {
        for (DrivingRoute route : routes) {
            mMapObjects.addPolyline(route.getGeometry());
        }
    }

    @Override
    public void onDrivingRoutesError(Error error) {
        String errorMessage = getString(R.string.unknown_error_message);
        if (error instanceof RemoteError) {
            errorMessage = getString(R.string.remote_error_message);
        } else if (error instanceof NetworkError) {
            errorMessage = getString(R.string.network_error_message);
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onObjectAdded(UserLocationView userLocationView) {
        userLocationLayer.setAnchor(
                new PointF((float) (mMapView.getWidth() * 0.5), (float) (mMapView.getHeight() * 0.5)),
                new PointF((float) (mMapView.getWidth() * 0.5), (float) (mMapView.getHeight() * 0.83)));

        userLocationView.getPin().setIcon(ImageProvider.fromResource(
                this, R.drawable.user_arrow));
        userLocationView.getArrow().setIcon(ImageProvider.fromResource(
                this, R.drawable.user_arrow));
        userLocationView.getAccuracyCircle().setFillColor(Color.TRANSPARENT);
    }

    @Override
    public void onObjectRemoved(UserLocationView userLocationView) {

    }

    @Override
    public void onObjectUpdated(UserLocationView userLocationView, ObjectEvent objectEvent) {

    }
}