package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

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
            + "name tags company role phone email linkedin followups.";
    public static final FieldOrder DEFAULT = new FieldOrder(
            List.of("name", "tags", "company", "role", "phone", "email", "linkedin", "followups"));

    private final List<Field> fields;

    /**
     * Creates an order from case-insensitive field names, rejecting missing, repeated, or unknown fields.
     */
    @JsonCreator
    public FieldOrder(List<String> names) {
        requireNonNull(names);
        checkArgument(names.size() == Field.values().length, MESSAGE_CONSTRAINTS);
        try {
            fields = names.stream().map(name -> Field.valueOf(requireNonNull(name).toUpperCase(Locale.ROOT))).toList();
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS, e);
        }
        checkArgument(EnumSet.copyOf(fields).size() == Field.values().length, MESSAGE_CONSTRAINTS);
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
     * Names every supported display field, including optional data and follow-up history.
     */
    public enum Field {
        NAME, TAGS, COMPANY, ROLE, PHONE, EMAIL, LINKEDIN, FOLLOWUPS
    }
}
