package ch.epfl.sweng.groupup.activity.event.description;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.transition.Slide;
import android.support.v4.app.ActivityCompat;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.RequestResult;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.epfl.sweng.groupup.R;
import ch.epfl.sweng.groupup.activity.event.files.FileManager;
import ch.epfl.sweng.groupup.activity.event.listing.EventListingActivity;
import ch.epfl.sweng.groupup.activity.info.UserInformationActivity;
import ch.epfl.sweng.groupup.activity.toolbar.ToolbarActivity;
import ch.epfl.sweng.groupup.lib.AndroidHelper;
import ch.epfl.sweng.groupup.lib.Optional;
import ch.epfl.sweng.groupup.lib.database.Database;
import ch.epfl.sweng.groupup.object.account.Account;
import ch.epfl.sweng.groupup.object.account.Member;
import ch.epfl.sweng.groupup.object.account.User;
import ch.epfl.sweng.groupup.object.event.Event;
import ch.epfl.sweng.groupup.object.map.PointOfInterest;

/**
 * EventDescriptionActivity.
 * This activity gathers the description of an event, its map and its file management.
 */
public class EventDescriptionActivity extends ToolbarActivity implements OnMapReadyCallback {

    private static boolean swipeBarTouched;

    private FileManager fileManager;

    private GoogleMap mMap;
    private Event currentEvent;
    private Map<Marker, String> mPoiMarkers;

    // Tap view attributes
    private Map<View.OnClickListener, Integer> oclToIndex;
    private int actualIndex;

    /**
     * Override the onCreated method, where when the activity is called, set up GoogleMaps,
     * swipe bar and instantiate variables
     *
     * @param savedInstanceState the Bundle object containing the activity's previously saved state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_description);

        swipeBarTouched = false;

        /**
         * This Map will map all onClickListeners of the tap view to an index.
         * It will then allow us to know how to flip the view.
         * The map has index 0.
         * The details of the event has index 1.
         * The media sharing of the index has index 2.
         */
        oclToIndex = new HashMap<>();
        actualIndex = 1;
        switchToSelected((TextView)findViewById(R.id.tap_view_details));

        new EventDescription(this);
        fileManager = new FileManager(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        User.observer = this;
        mPoiMarkers = new HashMap<>();

        initializeTapView();
    }

    /**
     * Show a text view as a selected one.
     * @param button the text view which design should be switch.
     */
    private void switchToSelected(TextView button){
        button.setBackground(getResources().getDrawable(R.drawable.borders_selected));
        button.setTextColor(getResources().getColor(R.color.white));
    }

    /**
     * Show a text view as an unselected one
     * @param button the text view which design should be switch.
     */
    private void switchToUnselected(TextView button){
        button.setBackground(getResources().getDrawable(R.drawable.borders_unselected));
        button.setTextColor(getResources().getColor(R.color.background_dark));
    }

    @Override
    public void initializeToolbar(){
        ImageView rightImage = findViewById(R.id.toolbar_image_right);
        ImageView secondRightImage = findViewById(R.id.toolbar_image_second_from_right);

        rightImage.setImageResource(R.drawable.ic_check);
        secondRightImage.setImageResource(R.drawable.ic_user);
        findViewById(R.id.toolbar_image_second_from_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpListener(UserInformationActivity.class);
            }
        });

        // home button
        findViewById(R.id.toolbar_image_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpListener(EventListingActivity.class);
            }
        });
    }

    /**
     * Override onPause method, remove the activity from the watchers of the event to avoid
     * exceptions.
     */
    @Override
    protected void onPause() {
        super.onPause();
        fileManager.close();
    }


    /**
     * Override onStop method, remove the activity from the watchers of the event to avoid
     * exceptions.
     */
    public void onStop() {
        super.onStop();
        fileManager.close();
    }

    /**
     * Override onDestroy method, remove the activity from the watchers of the event to avoid
     * exceptions.
     */
    public void onDestroy() {
        super.onDestroy();
        fileManager.close();
    }

    /**
     * Tap view initialization.
     */
    private void initializeTapView() {

        findViewById(R.id.tap_view_map).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!oclToIndex.keySet().contains(this)) {
                    oclToIndex.put(this, 0);
                }
                if (actualIndex == 1) {
                    ((ViewFlipper) findViewById(R.id.view_flipper))
                            .showNext();
                    switchToUnselected((TextView) findViewById(R.id.tap_view_details));
                } else if (actualIndex == 2) {
                    ((ViewFlipper) findViewById(R.id.view_flipper))
                            .showPrevious();
                    switchToUnselected((TextView) findViewById(R.id.tap_view_media));
                }

                switchToSelected((TextView) findViewById(R.id.tap_view_map));
                actualIndex = 0;
            }
        });

        findViewById(R.id.tap_view_details).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!oclToIndex.keySet().contains(this)) {
                    oclToIndex.put(this, 1);
                }
                if (actualIndex == 2) {
                    ((ViewFlipper) findViewById(R.id.view_flipper))
                            .showNext();
                    switchToUnselected((TextView) findViewById(R.id.tap_view_media));
                } else if (actualIndex == 0) {
                    ((ViewFlipper) findViewById(R.id.view_flipper))
                            .showPrevious();
                    switchToUnselected((TextView) findViewById(R.id.tap_view_map));
                }
                switchToSelected((TextView) findViewById(R.id.tap_view_details));
                actualIndex = 1;
            }
        });

        findViewById(R.id.tap_view_media).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!oclToIndex.keySet().contains(this)) {
                    oclToIndex.put(this, 2);
                }
                if (actualIndex == 0) {
                    ((ViewFlipper) findViewById(R.id.view_flipper))
                            .showNext();
                    switchToUnselected((TextView) findViewById(R.id.tap_view_map));
                } else if (actualIndex == 1) {
                    ((ViewFlipper) findViewById(R.id.view_flipper))
                            .showPrevious();
                    switchToUnselected((TextView) findViewById(R.id.tap_view_details));
                }
                switchToSelected((TextView) findViewById(R.id.tap_view_media));
                actualIndex = 2;
            }
        });
    }

    /**
     * Override of onActivityResult method.
     * Define the behavior when the user finished selecting the picture he wants to add or taking
     * a picture.
     *
     * @param requestCode unused.
     * @param resultCode  indicate if the operation succeeded.
     * @param data        the data returned by the previous activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        fileManager.onActivityResult(resultCode, data);
    }

    /**
     * Defines the behavior of the activity when the Google map is ready.
     *
     * @param googleMap the Google map.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (isMapMockWanted()) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        }

        mMap.setOnMapLoadedCallback(getOnMapLoadedCallback(this));
        mMap.setOnMapLongClickListener(getMapLongClickListener());
        mMap.setOnMarkerDragListener(getMarkerDragListener());

        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission
                        .ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)) {
            mMap.setMyLocationEnabled(true);
        }

        Intent intent = getIntent();
        int eventIndex = intent.getIntExtra(getString(R.string.event_listing_extraIndex), -1);
        if (eventIndex != -1) {
            currentEvent = Account.shared.getEvents().get(eventIndex);
            updateEventIfNeeded(currentEvent);
        } else {
            throw new Error("no event was passed down to the map activity");
        }

        super.provideGeoLocation();
    }

    public void requestLocation() {
        super.provideGeoLocation();
    }

    /**
     * @param event to be updated.
     *
     * Updates the member markers and points of interests of an event by removing the old state
     * and checking that the google map is initialized and given event corresponds with current
     */
    public void updateEventIfNeeded(Event event) {
        if (mMap != null && event.getUUID().equals(currentEvent.getUUID())) {
            currentEvent = event;

            mMap.clear();
            updateMemberMarkers();
            updatePoiMarkers();
        }
    }

    /**
     * Updates the member markers on the map with the position of each member in the event.
     */
    private void updateMemberMarkers() {
        for (Member memberToDisplay : currentEvent.getEventMembers()) {
            Optional<Location> location = memberToDisplay.getLocation();

            if (!location.isEmpty() && !memberToDisplay.getUUID().isEmpty()) {
                LatLng pos = new LatLng(location.get().getLatitude(), location.get().getLongitude());
                String uuid = memberToDisplay.getUUID().get();
                String displayName = memberToDisplay.getDisplayName().getOrElse("NO_NAME");

                if (!Account.shared.getUUID().isEmpty() && !uuid.equals(Account.shared.getUUID().get())) {
                    mMap.addMarker(new MarkerOptions().position(pos)
                            .title(displayName)
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_member)));
                }
            }
        }
    }

    /**
     * Updates the points of interests on the map with the position of
     * each point of interests in the event.
     */
    private void updatePoiMarkers() {

        for (PointOfInterest poi : currentEvent.getPointsOfInterest()) {
            LatLng latLng = new LatLng(poi.getLocation().getLatitude(), poi.getLocation().getLongitude());
            Marker marker = mMap.addMarker(new MarkerOptions().position(latLng)
                    .title(poi.getName())
                    .snippet(poi.getDescription())
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_poi)));
            marker.setVisible(true);

            mPoiMarkers.put(marker, poi.getUuid());
        }
    }

    /**
     * When the map has has finished rendering, show a toast with instructions for the user
     * when the swipe bar is touched.
     *
     * @param context the application context.
     * @return callback interface for when the map has finished rendering.
     */
    private GoogleMap.OnMapLoadedCallback getOnMapLoadedCallback(final Context context) {
        return new OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                if (swipeBarTouched) {
                    AndroidHelper.showToast(context, getString(R.string.map_activity_poi_instruction), Toast.LENGTH_LONG);
                    mMap.setOnMapLoadedCallback(null);
                }
            }
        };
    }

    /**
     * Creates the dialog for the user that shows when the user longclicks on the map,
     * i.e. the fields to fill in and then accept/decline to add a new point of interest
     *
     * @return callback for when the user long presses on the map
     */
    private GoogleMap.OnMapLongClickListener getMapLongClickListener() {
        return new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {
                Context context = EventDescriptionActivity.this;

                final AlertDialog.Builder builder =
                        new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AboutDialog));
                builder.setTitle(R.string.poi_dialog_title);

                LinearLayout container = new LinearLayout(context);
                container.setOrientation(LinearLayout.VERTICAL);

                final EditText titleEditText = new EditText(context);
                titleEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                titleEditText.setHint(R.string.poi_title_hint);
                container.addView(titleEditText);

                final EditText descriptionEditText = new EditText(context);
                descriptionEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                descriptionEditText.setHint(R.string.poi_description_hint);
                container.addView(descriptionEditText);

                builder.setView(container);

                builder.setPositiveButton(R.string.poi_create_add,
                        getCreatePositiveListener(latLng, titleEditText, descriptionEditText));
                builder.setNegativeButton(R.string.poi_create_cancel, getNegativeListener());

                builder.create().show();
            }
        };
    }

    /**
     * A method to create the new point of interest when the add button is pushed by the user.
     *
     * @param latLng the LatLng coordinates of the new point of interest.
     * @param titleEditText the title of the new point of interest.
     * @param descriptionEditText the description of the new point of interest.
     * @return the method to be invoked when the add button is pushed
     */
    private DialogInterface.OnClickListener getCreatePositiveListener(final LatLng latLng,
                                                                      final EditText titleEditText,
                                                                      final EditText descriptionEditText) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String title = titleEditText.getText().toString();
                String description = descriptionEditText.getText().toString();

                Location location = new Location(LocationManager.GPS_PROVIDER);
                location.setLatitude(latLng.latitude);
                location.setLongitude(latLng.longitude);

                Account.shared.addOrUpdateEvent(currentEvent.withPointOfInterest(new PointOfInterest(title, description, location)));
                Database.update();

                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        };
    }

    /**
     * A method that closes the dialog with the user when the cancel button
     * is pushed by the user.
     *
     * @return the method to be invoked when the cancel button is pushed.
     */
    private DialogInterface.OnClickListener getNegativeListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        };
    }

    /**
     * Creates a dialog for the user to decide to remove a
     * point of interest or not.
     *
     * @return the method to be invoked when a marker is dragged.
     */
    private GoogleMap.OnMarkerDragListener getMarkerDragListener() {
        return new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                Context context = EventDescriptionActivity.this;

                // Dialog Builder
                final AlertDialog.Builder builder =
                        new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AboutDialog));
                builder.setTitle(R.string.poi_action_title);
                builder.setNeutralButton(R.string.poi_action_route, getNeutralListener(marker));
                builder.setPositiveButton(R.string.poi_action_remove, getRemovePositiveListener(marker));
                builder.setNegativeButton(R.string.poi_action_cancel, getNegativeListener());

                builder.create().show();
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                // Ignore
            }


            /**
             * This needs to be done because of a "bug" of the Google Maps, when you start dragging a marker it gets
             * automatically deviated a little bit.
             *
             * @param marker being dragged.
             */
            @Override
            public void onMarkerDragEnd(Marker marker) {
                marker.remove();
                updatePoiMarkers();
            }
        };
    }


    /**
     * Removes a point of interest chosen by a user to be deleted by pushing on the delete
     * button when a point of interest is dragged.
     *
     * @param marker to be removed from the map.
     * @return the method to be invoked when the user decides to remove a point of interest
     */
    private DialogInterface.OnClickListener getRemovePositiveListener(final Marker marker) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String uuidToDelete = mPoiMarkers.get(marker);
                if (uuidToDelete == null) {
                    return;
                }

                Set<PointOfInterest> newPointsOfInterest = new HashSet<>();
                for (PointOfInterest poi : currentEvent.getPointsOfInterest()) {
                    if (!poi.getUuid().equals(uuidToDelete)) {
                        newPointsOfInterest.add(poi);
                    }
                }

                Account.shared.addOrUpdateEvent(currentEvent.withPointsOfInterest(newPointsOfInterest));
                marker.remove();

                Database.update();
            }
        };
    }

    private DialogInterface.OnClickListener getNeutralListener(final Marker marker){
        return new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(!Account.shared.getLocation().isEmpty()) {

                    // Due to Google Maps strange behaviour, location needs to be corrected
                    LatLng correctedDestination = new LatLng(marker.getPosition().latitude - 0.0015, marker.getPosition().longitude);
                    GoogleDirection.withServerKey("AIzaSyDtv0o9SNKJWLWt51YyYhZK0nxsR5FWMdY")
                            .from(new LatLng(Account.shared.getLocation().get().getLatitude(), Account.shared.getLocation().get().getLongitude()))
                            .to(correctedDestination)
                            .transportMode(TransportMode.WALKING)
                            .execute(new DirectionCallback() {
                                @Override
                                public void onDirectionSuccess(Direction direction, String rawBody) {
                                    String status = direction.getStatus();
                                    if(status.equals(RequestResult.OK)) {
                                        List<Step> stepList = direction.getRouteList().get(0).getLegList().get(0).getStepList();
                                        ArrayList<PolylineOptions> polylineOptionList = DirectionConverter.createTransitPolyline(getBaseContext(), stepList, 5, Color.RED, 3, Color.BLUE);
                                        for (PolylineOptions polylineOption : polylineOptionList) {
                                            mMap.addPolyline(polylineOption);
                                        }
                                    }
                                }

                                @Override
                                public void onDirectionFailure(Throwable t) {
                                    AndroidHelper.showToast(getBaseContext(), "Unable to compute route to desired point of interest. Try again later...", Toast.LENGTH_SHORT);
                                }
                            });
                }
            }
        };
    }
}
