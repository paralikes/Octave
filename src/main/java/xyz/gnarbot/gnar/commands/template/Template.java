package xyz.gnarbot.gnar.commands.template;

import xyz.gnarbot.gnar.commands.Context;
import xyz.gnarbot.gnar.utils.EmbedMaker;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;

public interface Template {
    String[] positions = {"First", "Second", "Third", "Fourth", "Fifth"};

    Map<String, Template> getCursors();

    default void add(String key, Template template) {
        if (getCursors().containsKey(key)) {
            throw new IllegalStateException("Trying to override " + key);
        }
        getCursors().put(key, template);
    }

    default String description() {
        StringJoiner sj = new StringJoiner(", ");

        for (String cursor : getCursors().keySet()) {
            sj.add(cursor);
        }

        return sj.toString();
    }

    default void walk(Context context, String[] args, int depth) {
        if (args.length != 0) {
            Template template = getCursors().get(args[0]);
            if (template != null) {
                template.walk(context, Arrays.copyOfRange(args, 1, args.length), depth + 1);
                return;
            }
        }
        onWalkFail(context, args, depth);
    }

    default void onWalkFail(Context context, String[] args, int depth) {
        onWalkFail(context, args, depth, null, null);
    }

    default void onWalkFail(Context context, String[] args, int depth, String title, String description) {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, Template> entry : getCursors().entrySet()) {
            builder.append("• `");
            builder.append(entry.getKey());

            Map.Entry<String, Template> current = entry;
            while (true) {
                Template template = current.getValue();
                if (template.getCursors().size() == 1) {
                    current = new ArrayList<>(template.getCursors().entrySet()).get(0);
                    builder.append(' ').append(current.getKey());
                    continue;
                } else if (template.getCursors().size() > 1) {
                    builder.append("...");
                } else if (template instanceof MethodTemplate) {
                    MethodTemplate value = (MethodTemplate) template;
                    builder.append(value.requirements());
                }
                break;
            }

            builder.append("`");
            builder.append("\n").append(current.getValue().description()).append("\n");
        }

        EmbedMaker eb = new EmbedMaker();
        eb.setColor(context.getGuild().getSelfMember().getColor());

        if (title == null && description == null) {
            eb.setTitle(positions[depth] + " Argument Candidates");
            eb.description(builder);
        } else if (description == null) {
            eb.setTitle(title);
            eb.field(positions[depth] + " Argument Candidates", false, builder);
        } else {
            eb.setTitle(title);
            eb.description(description);
            eb.field(positions[depth] + " Argument Candidates", false, builder);
        }

        if (args.length != 0) {
            eb.setTitle("Invalid " + positions[depth] + " Argument");
            eb.setColor(Color.RED);
        }

        context.getTextChannel().sendMessage(eb.build()).queue();
    }
}
