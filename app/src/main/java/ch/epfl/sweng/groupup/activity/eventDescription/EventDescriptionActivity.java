package ch.epfl.sweng.groupup.activity.eventDescription;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ch.epfl.sweng.groupup.R;
import ch.epfl.sweng.groupup.activity.eventListing.EventListingActivity;
import ch.epfl.sweng.groupup.activity.toolbar.ToolbarActivity;
import ch.epfl.sweng.groupup.lib.Optional;
import ch.epfl.sweng.groupup.lib.database.Database;
import ch.epfl.sweng.groupup.object.account.Account;
import ch.epfl.sweng.groupup.object.account.Member;
import ch.epfl.sweng.groupup.object.event.Event;

/**
 * Created by alix on 10/26/17.
 */

public class EventDescriptionActivity extends ToolbarActivity {
    private EditText displayEventName;
    private TextView displayEventStartDate;
    private TextView displayEventEndDate;
    private EditText displayEventDescription;
    private Event eventToDisplay;

    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_description);
        super.initializeToolbar();
        Intent i= getIntent();
        final int eventIndex = i.getIntExtra("eventIndex", -1);
        if (eventIndex >-1) {
            //!!!Order the events !!!
            eventToDisplay = Account.shared.getEvents().get(eventIndex);
        }
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        initializeField();
        printEvent();

        //Remove and go to the event creation
        findViewById(R.id.remove_event_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(EventDescriptionActivity.this, EventListingActivity.class);
                        removeEvent();
                        startActivity(i);
                    }
                });
        //Save changes
        findViewById(R.id.save)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                            String name= displayEventName.getText().toString();
                            String description = displayEventDescription.getText().toString();
                            Account.shared.addOrUpdateEvent(eventToDisplay.withEventName(name).withDescription(description));
                            Database.update();
                            eventToDisplay=Account.shared.getEvents().get(eventIndex);
                        }
                });
    }

    /**
     * Remove the user from the Event
     */
    private void removeEvent() {
        List<Member> futureMembers = new ArrayList<>(eventToDisplay.getEventMembers());
        futureMembers.remove(Account.shared.toMember());
        eventToDisplay=eventToDisplay.withEventMembers(futureMembers);
        Account.shared.addOrUpdateEvent(eventToDisplay);
        Database.update();
        List<Event> futureEventList=new ArrayList<>(Account.shared.getEvents());
        Account.shared.withFutureEvents(new ArrayList<Event>()).withPastEvents(new ArrayList<Event>()).withCurrentEvent(Optional.<Event>empty());
        futureEventList.remove(eventToDisplay);
        for (Event fe:futureEventList){
            Account.shared.addOrUpdateEvent(fe);
        }
        Database.update();

    }

    /**
     * Intilize the different TextView and EditText
     */
    private void initializeField(){
        displayEventName= findViewById(R.id.event_description_name);
        displayEventStartDate= findViewById(R.id.event_description_start_date);
        displayEventEndDate=findViewById(R.id.event_description_end_date);
        displayEventDescription=findViewById(R.id.event_description_description);
    }

    /**
     * Print the information about the event, if there is no event prints default string in fields.
     */
    private void printEvent() {
        if (eventToDisplay!=null) {
            displayEventName.setText(eventToDisplay.getEventName());
            displayEventStartDate.setText(eventToDisplay.getStartTime().toString(null, Locale.FRANCE));
            displayEventEndDate.setText(eventToDisplay.getEndTime().toString(null, Locale.FRANCE));
            displayEventDescription.setText(eventToDisplay.getDescription());

            for (Member member : eventToDisplay.getEventMembers()) {
                TextView memberName = new TextView(this);
                memberName.setText(member.getDisplayName().getOrElse("NO_NAME"));
                LinearLayout linear = findViewById(R.id.linear_scroll_members);
                linear.addView(memberName);
            }
        }
    }
}
