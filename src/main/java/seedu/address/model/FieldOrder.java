package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Represents a complete immutable permutation of the contact fields displayed to users.
 */
public final class FieldOrder {
    public static final String MESSAGE_CONSTRAINTS = "Provide every field exactly once: "
            + "name tags company role phone email linkedin followups introducer interactions.";
    public static final FieldOrder DEFAULT = new FieldOrder(
            List.of("name", "tags", "company", "role", "phone", "email", "linkedin", "followups", "introducer",
                    "interactions"));

    private final List<Field> fields;

    /**
     * Creates an order from case-insensitive field names, rejecting missing, repeated, or unknown fields.
     * Older saved orders without introducer or interactions are completed while preserving their field order.
     */
    @JsonCreator
    public FieldOrder(List<String> names) {
        requireNonNull(names);
        names = withLegacyFields(names);
        checkArgument(names.size() == Field.values().length, MESSAGE_CONSTRAINTS);
        try {
            fields = names.stream().map(name -> Field.valueOf(requireNonNull(name).toUpperCase(Locale.ROOT))).toList();
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS, e);
        }
        checkArgument(EnumSet.copyOf(fields).size() == Field.values().length, MESSAGE_CONSTRAINTS);
    }

    private static List<String> withLegacyFields(List<String> names) {
        String introducer = Field.INTRODUCER.name().toLowerCase(Locale.ROOT);
        String interactions = Field.INTERACTIONS.name().toLowerCase(Locale.ROOT);
        boolean hasInteractions = names.stream()
                .anyMatch(name -> name != null && name.toLowerCase(Locale.ROOT).equals(interactions));
        boolean hasIntroducer = names.stream()
                .anyMatch(name -> name != null && name.toLowerCase(Locale.ROOT).equals(introducer));
        if (hasInteractions && !hasIntroducer && names.size() == Field.values().length - 1) {
            List<String> completed = new ArrayList<>(names);
            completed.add(indexOfIgnoreCase(names, interactions), introducer);
            return completed;
        }
        if (!hasInteractions && !hasIntroducer && names.size() == Field.values().length - 2) {
            List<String> completed = new ArrayList<>(names);
            completed.add(introducer);
            completed.add(interactions);
            return completed;
        }
        if (!hasInteractions && hasIntroducer && names.size() == Field.values().length - 1) {
            List<String> completed = new ArrayList<>(names);
            completed.add(interactions);
            return completed;
        }
        return names;
    }

    private static int indexOfIgnoreCase(List<String> names, String field) {
        for (int i = 0; i < names.size(); i++) {
            if (field.equalsIgnoreCase(names.get(i))) {
                return i;
            }
        }
        throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
    }

    public List<Field> getFields() {
        return fields;
    }

    /**
     * Returns the canonical names as a JSON array, preserving order.
     */
    @JsonValue
    public List<String> getFieldNames() {
        return fields.stream().map(field -> field.name().toLowerCase(Locale.ROOT)).toList();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof FieldOrder order && fields.equals(order.fields);
    }

    @Override
    public int hashCode() {
        return fields.hashCode();
    }

    @Override
    public String toString() {
        return getFieldNames().stream().collect(Collectors.joining(" "));
    }

    /**
     * Names every supported display field, including optional data, follow-up history and interaction history.
     */
    public enum Field {
        NAME, TAGS, COMPANY, ROLE, PHONE, EMAIL, LINKEDIN, FOLLOWUPS, INTRODUCER, INTERACTIONS
    }
}
