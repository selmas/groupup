package ch.epfl.sweng.groupup.lib.database;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class DatabaseEvent {

    /**
     * Class to represent the event object that will be stored in the database.
     */

    public String name = Database.EMPTY_FIELD;
    public String description = Database.EMPTY_FIELD;
    public String datetime_start = Database.EMPTY_FIELD;
    public String datetime_end = Database.EMPTY_FIELD;
    public String uuid = Database.EMPTY_FIELD;
    public Map<String, DatabaseUser> members = new HashMap<>();

    public DatabaseEvent() {
    }

    public DatabaseEvent(String name, String description, String datetime_start, String
            datetime_end, String uuid, HashMap<String, DatabaseUser> members) {
        this.name = name;
        this.description = description;
        this.datetime_start = datetime_start;
        this.datetime_end = datetime_end;
        this.uuid = uuid;
        this.members = Collections.unmodifiableMap(new HashMap<>(members));
    }
}
