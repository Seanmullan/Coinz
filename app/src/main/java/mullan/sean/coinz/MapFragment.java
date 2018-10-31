package mullan.sean.coinz;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
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
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

public class MapFragment extends Fragment implements
        OnMapReadyCallback, LocationEngineListener, PermissionsListener  {

    private static final String UNCOLLECTED = "uncollected";
    private static final String COLLECTED   = "collected";
    private static final String TAG  = "C_MAP";
    private static final String DOLR = "DOLR";
    private static final String PENY = "PENY";
    private static final String SHIL = "SHIL";
    private static final String QUID = "QUID";

    private static ArrayList<Coin>      mUncollectedCoins = new ArrayList<>();
    private static HashMap<Coin,Marker> mMarkers          = new HashMap<>();
    private static MapboxMap            map;
    private LatLng                      mCurrentLocation;
    private MapView                     mapView;
    private LocationEngine              locationEngine;

    /*
     *  @brief  { Required empty public constructor }
     */
    public MapFragment(){}

    /*
     *  @brief  { Invoke onCreate of superclass }
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /*
     *  @brief  { Inflates the view for Mapbox, and fetches the uncollected coin data
     *            from the Data class }
     */
    @Override
    public View onCreateView(@Nonnull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Fetch uncollected coins from Data class
        updateMapData(inflater.getContext());

        // Get Mapbox instance
        Mapbox.getInstance(inflater.getContext(), getString(R.string.access_token));
        mapView = view.findViewById(R.id.mapboxMapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        return view;
    }

    /*
     *  @brief  { Fetches uncollected coins from the Data class, then updates the
     *            coin markers on the map }
     */
    public static void updateMapData(Context context) {
        Log.d(TAG, "[updateMapData] fetching uncollected coins");
        Log.d(TAG, "[updateMapData] before fetch - size = " + mUncollectedCoins.size());
        mUncollectedCoins = Data.getUncollectedCoins();
        Log.d(TAG, "[updateMapData] after fetch - size = " + mUncollectedCoins.size());
        updateMarkers(context);
    }

    /*
     *  @brief  { Implements callback for Mapbox. If map is not null, initialise map
     *            variable and enable the devices location. Also update the markers
     *            in case updateMapData() is called before the map is ready e.g. this
     *            is likely to happen if the user loads the app, switches to another
     *            fragment then returns to the Map fragment }
     */
    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        if (mapboxMap == null) {
            Log.d(TAG, "[onMapReady] mapBox is null");
        } else {
            Log.d(TAG, "[onMapReady] mapBox is ready");
            map = mapboxMap;
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
            enableLocation();
            updateMarkers(getContext());
        }
    }

    /*
     *  @brief  { Implements callback for permissions listener. If permissions have been
     *            granted, enable devices location, otherwise prompt user to grant permission }
     */
    @Override
    public void onPermissionResult(boolean granted) {
        Log.d(TAG, "[onPermissionResult] granted == " + granted);
        if (granted) {
            enableLocation();
        } else {
            displayToast("Please enable permissions for this application");
        }
    }

    /*
     *  @brief { Implements callback function for permissions listener. Lists permissions
     *           that user is required to grant for the application }
     */
    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        String permissions = permissionsToExplain.toString();
        Log.d(TAG, "Permissions: " + permissions);
        String msg = "This application requires the following permissions: " + permissions;
        displayToast(msg);
    }

    /*
     *  @brief  { If permissions have been granted, invoke the initialisation methods for
     *            the location engine and location layer. Otherwise, prompt permissions
     *            manager to request permissions }
     */
    private void enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(getLayoutInflater().getContext())) {
            Log.d(TAG, "Permissions are granted");
            initializeLocationEngine();
            initializeLocationLayer();
        } else {
            Log.d(TAG, "Permissions are not granted");
            PermissionsManager permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }

    /*
     *  @brief  { Initialise location layer if MapView and MapboxMap are not null }
     */
    private void initializeLocationLayer() {
        if (mapView == null) {
            Log.d(TAG, "mapView is null");
        } else {
            if (map == null) {
                Log.d(TAG, "map is null");
            } else {
                LocationLayerPlugin locationLayerPlugin
                        = new LocationLayerPlugin(mapView, map, locationEngine);
                locationLayerPlugin.setLocationLayerEnabled(true);
                locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
                locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
            }
        }
    }

    /*
     *  @brief  { Initialises the location engine to update the location every 5 seconds,
     *            with the fastest updates to be at most 1 second }
     */
    private void initializeLocationEngine() {
        locationEngine = new LocationEngineProvider(getLayoutInflater().getContext())
                .obtainBestLocationEngineAvailable();
        locationEngine.setInterval(5000); // preferably every 5 seconds
        locationEngine.setFastestInterval(1000); // at most every second
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();
        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            mCurrentLocation = new LatLng(lastLocation);
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    /*
     *  @brief  { Implements callback function for location engine listener }
     */
    @Override
    public void onConnected() {
        Log.d(TAG, "[onConnected] requesting location updates");
        locationEngine.requestLocationUpdates();
    }

    /*
     *  @brief  { When location has changed, location engine listener calls
     *            this method. If the location is not null, then update the
     *            location variable and adjust the camera position }
     */
    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            Log.d(TAG, "[onLocationChanged] location is null");
        } else {
            Log.d(TAG, "[onLocationChanged] location is not null");
            mCurrentLocation = new LatLng(location);
            setCameraPosition(location);
            checkProximityToCoins();
        }
    }

    /*
     *  @brief  { Sets camera position according to lat and long coordinates }
     */
    private void setCameraPosition(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(),
                location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    /*
     *  @brief  { Populates the map with markers that represent coins. Each
     *            coin is represented by its corresponding coin image }
     */
    private static void updateMarkers(Context context) {
        Log.d(TAG, "[update markers] updating markers");
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
            Marker coinMarker = new Marker(marker);
            mMarkers.put(coin, coinMarker);
        }
    }

    /*
     *  @brief  { Checks the distance between the devices location and the location
     *            of all uncollected coins. If the distance is less than or equal to
     *            25 metres, then collect the coin }
     */
    private void checkProximityToCoins() {
        for (Coin c : mUncollectedCoins) {
            if (mCurrentLocation.distanceTo(c.getLocation()) <= 25) {
                collectCoin(c);
                break;
            }
        }
    }

    /*
     *  @brief  { Inform the user that they have collected a coin. Then, remove the marker from the
     *            map and remove the coin from the uncollected coins ArrayList. The coin will then
     *            be removed from the Uncollected subcollection in firebase and placed in the
     *            Collected subcollection }
     */
    private void collectCoin(Coin coin) {
        String msg = "Coin collected: " + coin.getCurrency()
                + " worth " + String.format("%.4f", coin.getValue());
        displayToast(msg);
        map.removeMarker(mMarkers.get(coin));
        mUncollectedCoins.remove(coin);
        Data.removeCoinFromCollection(coin, UNCOLLECTED, new OnEventListener<String>() {
            @Override
            public void onSuccess(String object) {
                Log.d(TAG,
                "[collectCoin] successfully removed coin from Uncollected with id: " + coin.getId());
            }
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG,
                "[collectCoin] failed to remove coin from Uncollected with id: " + coin.getId());
            }
        });
        Data.addCoinToCollection(coin, COLLECTED, new OnEventListener<Integer>() {
            @Override
            public void onSuccess(Integer object) {
                Log.d(TAG,
                "[collectCoin] successfully added coin to Collected with id: " + coin.getId());
            }
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG,
                "[collectCoin] failed to add coin to Collected with id: " + coin.getId());
            }
        });
    }

    /*
     *  @brief  { Display message on device }
     *
     *  @params { Message to be displayed }
     */
    private void displayToast(String message) {
        Toast.makeText(getLayoutInflater().getContext(), message, Toast.LENGTH_SHORT).show();
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
}
