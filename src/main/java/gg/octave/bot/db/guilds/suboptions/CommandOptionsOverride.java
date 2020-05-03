package gg.octave.bot.db.guilds.suboptions;

import java.util.Collections;
import java.util.Set;

public class CommandOptionsOverride extends CommandOptions {
    private final CommandOptions parent;
    private final CommandOptions child;

    public CommandOptionsOverride(CommandOptions child, CommandOptions parent) {
        this.parent = parent;
        this.child = child;
    }

    public CommandOptions getParent() {
        return parent;
    }

    public CommandOptions getChild() {
        return child;
    }

    @Override
    public boolean isEnabled() {
        return child != null ? child.isEnabled() : parent == null || parent.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        throw new UnsupportedOperationException();
    }

    public boolean inheritToggle() {
        return child == null;
    }

    @Override
    public Set<String> getDisabledChannels() {
        if (child != null && child.rawDisabledChannels() != null)
            return Collections.unmodifiableSet(child.getDisabledChannels());
        else if (parent != null) return Collections.unmodifiableSet(parent.getDisabledChannels());
        else return Collections.emptySet();
    }

    public boolean inheritChannels() {
        return child == null || child.rawDisabledChannels() == null;
    }

    @Override
    public Set<String> getDisabledRoles() {
        if (child != null && child.rawDisabledRoles() != null)
            return Collections.unmodifiableSet(child.getDisabledRoles());
        else if (parent != null) return Collections.unmodifiableSet(parent.getDisabledRoles());
        else return Collections.emptySet();
    }

    public boolean inheritRoles() {
        return child == null || child.rawDisabledRoles() == null;
    }

    @Override
    public Set<String> getDisabledUsers() {
        if (child != null && child.rawDisabledUsers() != null)
            return Collections.unmodifiableSet(child.getDisabledUsers());
        else if (parent != null) return Collections.unmodifiableSet(parent.getDisabledUsers());
        else return Collections.emptySet();
    }

    public boolean inheritUsers() {
        return child == null || child.rawDisabledUsers() == null;
    }
}
