package mullan.sean.coinz;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements
        OnMapReadyCallback, LocationEngineListener, PermissionsListener  {

    private static final String TAG  = "C_MAP";
    private static final String DOLR = "DOLR";
    private static final String PENY = "PENY";
    private static final String SHIL = "SHIL";
    private static final String QUID = "QUID";

    private static ArrayList<Coin> mUncollectedCoins;
    private PermissionsManager     permissionsManager;
    private MapView                mapView;
    private static MapboxMap       map;
    private LocationEngine         locationEngine;
    private LocationLayerPlugin    locationLayerPlugin;
    private Location               originLocation;
    private static String          geoJsonSource;
    private IconFactory            iconFactory;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        updateMapData(inflater.getContext());

        // Get Mapbox instance
        Mapbox.getInstance(inflater.getContext(), getString(R.string.access_token));
        mapView = view.findViewById(R.id.mapboxMapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        return view;
    }

    public static void updateMapData(Context context) {
        Log.d(TAG, "[updateMapData] fetching uncollected coins");
        mUncollectedCoins = Data.getUncollectedCoins();
        updateMarkers(context);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        if (mapboxMap == null) {
            Log.d(TAG, "[onMapReady] mapBox is null");
        } else {
            map = mapboxMap;
            // Set user interface options
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
            enableLocation();
        }
    }

    @Override
    public void onPermissionResult(boolean granted) {
        Log.d(TAG, "[onPermissionResult] granted == " + granted);
        if (granted) {
            enableLocation();
        } else {
            displayToast("Please enable permissions for this application");
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        String permissions = permissionsToExplain.toString();
        Log.d(TAG, "Permissions: " + permissions);
        String msg = "This application requires the following permissions: " + permissions;
        displayToast(msg);
    }

    private void enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(getLayoutInflater().getContext())) {
            Log.d(TAG, "Permissions are granted");
            initializeLocationEngine();
            initializeLocationLayer();
        } else {
            Log.d(TAG, "Permissions are not granted");
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }

    private void initializeLocationLayer() {
        if (mapView == null) {
            Log.d(TAG, "mapView is null");
        } else {
            if (map == null) {
                Log.d(TAG, "map is null");
            } else {
                locationLayerPlugin = new LocationLayerPlugin(mapView,
                        map, locationEngine);
                locationLayerPlugin.setLocationLayerEnabled(true);
                locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
                locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
            }
        }
    }

    private void initializeLocationEngine() {
        locationEngine = new LocationEngineProvider(getLayoutInflater().getContext())
                .obtainBestLocationEngineAvailable();
        locationEngine.setInterval(5000); // preferably every 5 seconds
        locationEngine.setFastestInterval(1000); // at most every second
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();
        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected() {
        Log.d(TAG, "[onConnected] requesting location updates");
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            Log.d(TAG, "[onLocationChanged] location is null");
        } else {
            Log.d(TAG, "[onLocationChanged] location is not null");
            originLocation = location;
            setCameraPosition(location);
        }
    }

    private void setCameraPosition(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(),
                location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    /*
     *  @brief  { Display message on device }
     *
     *  @params { Message to be displayed }
     */
    private void displayToast(String message) {
        Toast.makeText(getLayoutInflater().getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private static void updateMarkers(Context context) {
        IconFactory iconFactory = IconFactory.getInstance(context);
        Icon icon;
        for (Coin coin : mUncollectedCoins) {
            switch (coin.getCurrency()) {
                case DOLR:
                    icon = iconFactory.fromResource(R.drawable.dolr);
                    break;
                case PENY:
                    icon = iconFactory.fromResource(R.drawable.peny);
                    break;
                case SHIL:
                    icon = iconFactory.fromResource(R.drawable.shil);
                    break;
                case QUID:
                    icon = iconFactory.fromResource(R.drawable.quid);
                    break;
                default:
                    icon = iconFactory.defaultMarker();
                    Log.d(TAG, "[update markers] invalid coin currency");
            }
            MarkerOptions marker = new MarkerOptions()
                    .position(coin.getLocation())
                    .icon(icon);
            map.addMarker(marker);
        }
    }
}
