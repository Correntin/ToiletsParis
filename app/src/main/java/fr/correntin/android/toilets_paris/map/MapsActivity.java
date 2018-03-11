package fr.correntin.android.toilets_paris.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.geronimostudios.coffeescene.SceneCreator;
import com.geronimostudios.coffeescene.SceneManager;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.correntin.android.toilets_paris.R;
import fr.correntin.android.toilets_paris.api.ApiManager;
import fr.correntin.android.toilets_paris.list.ToiletsListActivity;
import fr.correntin.android.toilets_paris.models.Toilet;
import fr.correntin.android.toilets_paris.models.ToiletRecord;
import fr.correntin.android.toilets_paris.models.ToiletResponse;
import fr.correntin.android.toilets_paris.utils.NoNetworkAndNoCacheDataException;
import fr.correntin.android.toilets_paris.utils.PermissionsUtils;
import fr.correntin.android.toilets_paris.utils.Preferences;
import fr.correntin.android.toilets_paris.utils.Utils;
import fr.correntin.android.toilets_paris.views.ToiletDetailsView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        NavigationView.OnNavigationItemSelectedListener, ToiletDetailsView.ToiletDetailsViewListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 89;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 90;

    private static final double DEFAULT_LATITUDE_POSITION = 48.8528847;
    private static final double DEFAULT_LONGITUDE_POSITION = 2.3621202;

    private static final String TOILET_ID_EXTRA = "toiletIdExtra";

    private static final int SCENE_CONNECTION_ERROR_CACHE_AVAILABLE = 1;
    private static final int SCENE_CONNECTION_ERROR_NO_CACHE_AVAILABLE = 2;
    private static final int SCENE_MAP = 3;
    private static final int SCENE_LOADER = 4;

    public static void start(Context context) {
        Intent intent = new Intent(context, MapsActivity.class);
        Utils.startActivity(context, intent, true);
    }

    public static void start(Context context, String toiletId) {
        Intent intent = new Intent(context, MapsActivity.class);
        intent.putExtra(TOILET_ID_EXTRA, toiletId);
        Utils.startActivity(context, intent, true);
    }

    @BindView(R.id.activity_maps_search_AutoCompleteTextView)
    TextView mSearchTextView;

    @BindView(R.id.activity_maps_ToiletDetailsView)
    ToiletDetailsView mToiletDetailsView;

    @BindView(R.id.activity_maps_DrawerLayout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.activity_maps_NavigationView)
    NavigationView mNavigationView;

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private Marker mSelectedMarker;
    private String mSelectedToiletIdFromIntent = null;
    private boolean mReloadDataInOnResume = false;
    private Circle mPlacesCircle;


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Activity cycle life
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

        SceneManager.create(
                SceneCreator.with(this)
                        .add(SCENE_MAP, R.id.activity_maps_GoogleMapsFragment)
                        .add(SCENE_MAP, R.id.activity_maps_search_CardView)
                        .add(SCENE_MAP, R.id.activity_maps_NavigationView)
                        .add(SCENE_CONNECTION_ERROR_CACHE_AVAILABLE, R.id.activity_maps_connection_error_cache_available_Layout)
                        .add(SCENE_CONNECTION_ERROR_NO_CACHE_AVAILABLE, R.id.activity_maps_connection_error_no_cache_available_Layout)
                        .add(SCENE_LOADER, R.id.activity_maps_loader_Layout)
                        .main(SCENE_LOADER));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.activity_maps_GoogleMapsFragment);
        mapFragment.getMapAsync(this);

        mNavigationView.inflateMenu(R.menu.map_activity_drawer_menu);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.getMenu().getItem(0).setChecked(true);
        mToiletDetailsView.setToiletDetailsViewListener(this);

        mReloadDataInOnResume = true;
        this.init();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
        this.init();
    }

    private void init() {

        mSelectedToiletIdFromIntent = null;
        Intent intent = this.getIntent();

        if (intent != null)
            mSelectedToiletIdFromIntent = intent.getStringExtra(TOILET_ID_EXTRA);
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.updateDrawerItem(mNavigationView.getMenu().getItem(0));

        if (mReloadDataInOnResume == true) {

            mReloadDataInOnResume = false;
            SceneManager.scene(this, SCENE_LOADER);

            ApiManager.getToilets(this, false)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(onDownloadToiletsResponse());
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // View events
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @OnClick(R.id.activity_maps_top_card_my_position_ImageView)
    public void onClickOnMyPositionImageView() {
        this.moveCameraToUserPosition();
        mDrawerLayout.closeDrawers();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        mDrawerLayout.closeDrawers();
        this.updateDrawerItem(item);

        switch (item.getItemId()) {
            case R.id.maps_activity_drawer_menu_item_map:
                break;
            case R.id.maps_activity_drawer_menu_item_favorites:
                mReloadDataInOnResume = true;
                ToiletsListActivity.start(this, true);
                break;
            case R.id.maps_activity_drawer_menu_item_list:
                mReloadDataInOnResume = true;
                ToiletsListActivity.start(this, false);
                break;
            case R.id.maps_activity_drawer_menu_item_appinfo:
                Utils.showMessageDialog(this, getString(R.string.app_info_dialog_title), getString(R.string.app_info_dialog_message));
                break;
            case R.id.maps_activity_drawer_menu_item_exit:
                this.finish();
                break;
        }

        return true;
    }

    @OnClick(R.id.activity_maps_top_card_menu_ImageView)
    public void onClickOnMenuButton() {
        mDrawerLayout.openDrawer(Gravity.LEFT);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        mSelectedMarker = marker;
        mToiletDetailsView.update((Toilet) marker.getTag());

        if (mToiletDetailsView.getVisibility() != View.VISIBLE) {
            mToiletDetailsView.setVisibility(View.VISIBLE);
        }
        return false;
    }

    @OnClick(R.id.activity_maps_top_card_refresh_ImageView)
    public void onClickOnRefreshButton() {
        mToiletDetailsView.setVisibility(View.GONE);
        SceneManager.scene(this, SCENE_LOADER);

        ApiManager.getToilets(this, true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onDownloadToiletsResponse());
    }

    @OnClick(R.id.activity_maps_load_cached_data_Button)
    public void onClickOnLoadCachedDataButton() {
        SceneManager.scene(this, SCENE_LOADER);

        ApiManager.getToilets(this, false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onDownloadToiletsResponse());
    }

    @OnClick(R.id.activity_maps_search_AutoCompleteTextView)
    public void onClickOnSearchAddressTextView() {
        try {
            PlaceAutocomplete.IntentBuilder intentBuilder = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN);
            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                    .setCountry("fr")
                    .build();

            intentBuilder.setFilter(typeFilter);
            Intent intent = intentBuilder.build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            Toast.makeText(this, getString(R.string.google_places_error), Toast.LENGTH_SHORT).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(this, getString(R.string.google_places_error), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void loadMarkers(ToiletResponse toiletResponse) {

        if (toiletResponse != null
                && toiletResponse.getToiletRecords() != null) {

            for (ToiletRecord toiletRecord : toiletResponse.getToiletRecords()) {

                if (toiletRecord.getToiletDetails() != null) {
                    LatLng sydney = new LatLng(toiletRecord.getToiletDetails().getPositions()[0], toiletRecord.getToiletDetails().getPositions()[1]);
                    MarkerOptions markerOptions = new MarkerOptions().position(sydney).title(toiletRecord.getToiletDetails().getStreet());
                    Marker marker = mMap.addMarker(markerOptions);
                    marker.setFlat(true);
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(
                            Preferences.i().isFavoriteToilet(toiletRecord.getToiletDetails().getId())
                                    ? BitmapDescriptorFactory.HUE_AZURE : BitmapDescriptorFactory.HUE_RED));
                    marker.setTag(toiletRecord.getToiletDetails());

                    if (mSelectedToiletIdFromIntent != null
                            && mSelectedToiletIdFromIntent.equals(toiletRecord.getToiletDetails().getId())) {
                        onMarkerClick(marker);
                        marker.showInfoWindow();
                        moveCameraToPosition(toiletRecord.getToiletDetails().getPositions()[0], toiletRecord.getToiletDetails().getPositions()[1]);
                    }
                }
            }

            Toast.makeText(this, getString(R.string.maps_activity_num_toilets_available, toiletResponse.getToiletRecords().size()), Toast.LENGTH_SHORT).show();

            SceneManager.scene(MapsActivity.this, SCENE_MAP);

            if (PermissionsUtils.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                if (mSelectedToiletIdFromIntent == null)
                    this.moveCameraToUserPosition();

            } else if (PermissionsUtils.canAskPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                PermissionsUtils.askPermissions(this, LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Activity events
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PermissionsUtils.checkResultAndSaveBlockedPermissionsIfNeeded(this, permissions);

        if (PermissionsUtils.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            mMap.setMyLocationEnabled(true);
            this.moveCameraToUserPosition();
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                    getString(R.string.maps_activity_data_cannot_get_location),
                    Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                if (place != null && place.getLatLng() != null) {
                    mSearchTextView.setText(place.getAddress());
                    this.moveCameraToPosition(place.getLatLng().latitude, place.getLatLng().longitude);

                    if (mPlacesCircle != null)
                        mPlacesCircle.remove();
                    CircleOptions circleOptions = new CircleOptions();
                    circleOptions.center(place.getLatLng());
                    circleOptions.fillColor(ContextCompat.getColor(this, R.color.colorAccentSemiTransparent));
                    circleOptions.strokeColor(ContextCompat.getColor(this, R.color.colorPrimary));
                    circleOptions.radius(160f);
                    mPlacesCircle = mMap.addCircle(circleOptions);
                }
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                mSearchTextView.setText("");
                Toast.makeText(this, getString(R.string.google_places_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Maps events
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (PermissionsUtils.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            mMap.setMyLocationEnabled(true);
        } else {
            LatLng defaultPosition = new LatLng(DEFAULT_LATITUDE_POSITION, DEFAULT_LONGITUDE_POSITION);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPosition, 15f));
        }

        mMap.setBuildingsEnabled(true);
        mMap.setOnMarkerClickListener(this);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Utility functions
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressLint("MissingPermission")
    private void moveCameraToUserPosition() {
        if (PermissionsUtils.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            if (mFusedLocationClient == null) {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                mFusedLocationClient.getLastLocation()
                        .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    moveCameraToPosition(task.getResult().getLatitude(), task.getResult().getLongitude());
                                } else {
                                    moveCameraToPosition(DEFAULT_LATITUDE_POSITION, DEFAULT_LONGITUDE_POSITION);
                                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.maps_activity_data_cannot_get_location),
                                            Snackbar.LENGTH_SHORT).show();
                                }
                                mFusedLocationClient = null;
                            }
                        });
            }
        } else if (PermissionsUtils.canAskPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            PermissionsUtils.askPermissions(this, LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            Toast.makeText(this, getString(R.string.no_permission), Toast.LENGTH_SHORT).show();
        }
    }

    private void moveCameraToPosition(double latitude, double longitude) {
        LatLng defaultPosition = new LatLng(latitude, longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultPosition, 15f));
    }

    private void updateDrawerItem(@NonNull MenuItem item) {
        for (int i = 0; i < mNavigationView.getMenu().size(); i++)
            mNavigationView.getMenu().getItem(i).setChecked(false);
        item.setChecked(true);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ToiletDetailsView.ToiletDetailsViewListener functions
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onToiletAddedToFavorites(Toilet toilet) {
        mSelectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
    }

    @Override
    public void onToiletRemovedFromFavorites(Toilet toilet) {
        mSelectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Network events
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private Subscriber<ToiletResponse> onDownloadToiletsResponse() {
        return new Subscriber<ToiletResponse>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

                if (e instanceof NoNetworkAndNoCacheDataException) {
                    SceneManager.scene(MapsActivity.this, SCENE_CONNECTION_ERROR_NO_CACHE_AVAILABLE);
                } else {
                    SceneManager.scene(MapsActivity.this, SCENE_CONNECTION_ERROR_CACHE_AVAILABLE);
                }
            }

            @Override
            public void onNext(ToiletResponse toiletResponse) {
                loadMarkers(toiletResponse);
            }
        };
    }


}
