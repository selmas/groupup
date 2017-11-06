package ch.epfl.sweng.groupup.activity.event.listing;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.joda.time.LocalDateTime;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import ch.epfl.sweng.groupup.R;
import ch.epfl.sweng.groupup.activity.event.creation.EventCreationActivity;
import ch.epfl.sweng.groupup.activity.event.description.EventDescriptionActivity;
import ch.epfl.sweng.groupup.activity.toolbar.ToolbarActivity;
import ch.epfl.sweng.groupup.object.account.Account;
import ch.epfl.sweng.groupup.object.event.Event;
import ch.epfl.sweng.groupup.object.event.EventStatus;

/**
 * EventListing class
 * Lists the future and past events and in between a create
 * event button for the user to create a new event.
 * It is linked to the layout activity_event_listing.xml
 */

public class EventListingActivity extends ToolbarActivity {

    private LinearLayout linearLayout;
    private int heightInSp;
    private Timer autoUpdate;
    private int size;

    /**
     * Initialization of the private variables of the class and
     * of the future events, create event and past events
     * buttons in the linear layout
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_listing);
        super.initializeToolbarActivity();
        size=Account.shared.getEvents().size();
        initView();
    }

    @Override
    public void onResume(){
        super.onResume();
        autoUpdate = new Timer();
        autoUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        setContentView(R.layout.activity_event_listing);
                        initializeToolbarActivity();

                        initView();
                    }
                });
            }
        }, 0, 5000); // updates each 5 secs
    }


    @Override
    public void onPause() {
        autoUpdate.cancel();
        super.onPause();
    }

    public void initView(){
        initializeVariables();
        initializeEvents(Account.shared.getFutureEvents());
        initializeCreateEvent();
        List<Event> belowCreateButton = Account.shared.getPastEvents();
        if (!Account.shared.getCurrentEvent().isEmpty()){
            belowCreateButton.add(Account.shared.getCurrentEvent().get());
            Log.d("log current events", Account.shared.getCurrentEvent().toString());
        }
        Collections.sort(belowCreateButton, new Comparator<Event>(){
            @Override
            public int compare(Event o1, Event o2) {
                return o1.getStartTime().compareTo(o2.getStartTime());
            }
        });
        Log.d("log events", belowCreateButton.toString());
        initializeEvents(belowCreateButton);
    }


    /**
     * Initialization of the private variables of the class
     */
    private void initializeVariables() {
        linearLayout = findViewById(R.id.linear_layout_event_list);
        heightInSp = Math.round(100 * getResources().getDisplayMetrics().scaledDensity);
        // Fixed height, best would be to create a dynamical height so it works for all screens
    }

    /**
     * Initialization of the event buttons in the linear layout with the
     * name and start to event dates stated
     */
    private void initializeEvents(List<Event> events) {
        String[] eventNames = getEventNames(events);
        LocalDateTime[] eventStartTimes = getEventStartTimes(events);
        LocalDateTime[] eventEndTimes = getEventEndTimes(events);

        int offset = 0;
        if (!events.isEmpty() && !events.get(0).getEventStatus().equals(EventStatus.FUTURE)){
            offset = Account.shared.getFutureEvents().size();
        }

        for(int i=0; i<events.size(); i++){
            Button eventButton = new Button(this);

            eventButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.
                    MATCH_PARENT,heightInSp));

            eventButton.setText(String.format(Locale.FRANCE, "%s | %d/%d - %d/%d",eventNames[i],
                    eventStartTimes[i].getDayOfMonth(),eventStartTimes[i].getMonthOfYear(),
                    eventEndTimes[i].getDayOfMonth(), eventEndTimes[i].getMonthOfYear()));

            final int finalI = i + offset;
            if (eventButton != null) {
                eventButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(EventListingActivity.this, EventDescriptionActivity.class);
                        intent.putExtra("eventIndex", finalI);
                        startActivity(intent);
                    }
                });
            }
            linearLayout.addView(eventButton);
        }
    }

    /**
     * Initialization of the create event button in the linear layout and
     * of the OnClickListener
     */
    private void initializeCreateEvent() {
        Button createEventButton = new Button(this);
        createEventButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.
                MATCH_PARENT, heightInSp));
        createEventButton.setText(R.string.create_new_event);
        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(EventListingActivity.this, EventCreationActivity.class);
                startActivity(i);
            }
        });
        //createEventButton.setId(View.generateViewId()); // Assign the ID of the event
        linearLayout.addView(createEventButton);
    }

    /**
     * Getter for the event names of a list of events.
     * @param events a list of events
     * @return       a list of the event names strings
     */
    private String[] getEventNames(List<Event> events) {
        String[] eventNames = new String[events.size()];
        for (int i=0; i < events.size(); i++) {
            eventNames[i] = events.get(i).getEventName();
        }
        return eventNames;
    }

    /**
     * Getter for the start times of a list of events.
     * @param events a list of events
     * @return       a LocalDateTime list of the start times
     */
    private LocalDateTime[] getEventStartTimes(List<Event> events) {
        LocalDateTime[] eventStartTimes = new LocalDateTime[events.size()];
        for (int i=0; i < events.size(); i++) {
            eventStartTimes[i] = events.get(i).getStartTime();
        }
        return eventStartTimes;
    }

    /**
     * Getter for the start times of a list of events.
     * @param events a list of events
     * @return       a LocalDateTime list of the end times
     */
    private LocalDateTime[] getEventEndTimes(List<Event> events) {
        LocalDateTime[] eventEndTimes = new LocalDateTime[events.size()];
        for (int i=0; i < events.size(); i++) {
            eventEndTimes[i] = events.get(i).getEndTime();
        }
        return eventEndTimes;
    }
}