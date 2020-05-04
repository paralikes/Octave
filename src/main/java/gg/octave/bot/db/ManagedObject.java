package gg.octave.bot.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gg.octave.bot.Launcher;

import static com.rethinkdb.RethinkDB.r;

public abstract class ManagedObject<T> {
    private static final Database db = Launcher.INSTANCE.getDatabase();
    private final String id;
    @JsonIgnore
    private final String table;

    @JsonIgnore
    public ManagedObject(String id, String table) {
        this.id = id;
        this.table = table;
    }

    public String getId() {
        return id;
    }

    @JsonIgnore
    public void delete() {
        if (db.isOpen()) r.table(table).get(id)
                .delete()
                .runNoReply(db.getConn());
    }

    @JsonIgnore
    public void save() {
        if (db.isOpen()) r.table(table).insert(this)
                .optArg("conflict", "replace")
                .runNoReply(db.getConn());
    }
}