package seedu.address.storage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.FollowUp;

/**
 * Preserves JSON value types so malformed actions are rejected instead of coerced.
 */
class JsonAdaptedFollowUp {
    private final JsonNode id;
    private final JsonNode description;
    private final JsonNode status;
    private final JsonNode dueDate;

    /**
     * Creates an adapted action from raw JSON fields for validation.
     */
    @JsonCreator
    public JsonAdaptedFollowUp(@JsonProperty("id") JsonNode id,
            @JsonProperty("description") JsonNode description, @JsonProperty("status") JsonNode status,
            @JsonProperty("dueDate") JsonNode dueDate) {
        this.id = id;
        this.description = description;
        this.status = status;
        this.dueDate = dueDate;
    }

    /**
     * Converts an immutable action into its JSON representation.
     */
    public JsonAdaptedFollowUp(FollowUp source) {
        id = IntNode.valueOf(source.getId());
        description = TextNode.valueOf(source.getDescription());
        status = TextNode.valueOf(source.getStatus().name());
        dueDate = source.getDueDate().map(date -> TextNode.valueOf(date.toString())).orElse(null);
    }

    /**
     * Returns the validated action, rejecting missing, mistyped, or unsupported values.
     *
     * @throws IllegalValueException if the stored action violates any constraint.
     */
    public FollowUp toModelType() throws IllegalValueException {
        if (id == null || !id.isIntegralNumber() || !id.canConvertToInt() || id.intValue() <= 0) {
            throw new IllegalValueException("Follow-up IDs must be positive integers.");
        }
        if (description == null || !description.isTextual()
                || !FollowUp.isValidDescription(description.textValue())) {
            throw new IllegalValueException(FollowUp.MESSAGE_CONSTRAINTS);
        }
        if (status == null || !status.isTextual()) {
            throw new IllegalValueException("Follow-up status must be PENDING or COMPLETED.");
        }
        final FollowUp.Status modelStatus;
        try {
            modelStatus = FollowUp.Status.valueOf(status.textValue());
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException("Follow-up status must be PENDING or COMPLETED.");
        }
        LocalDate modelDueDate = null;
        if (dueDate != null && !dueDate.isNull()) {
            if (!dueDate.isTextual()) {
                throw new IllegalValueException("Follow-up due date must be a valid ISO date (yyyy-MM-dd).");
            }
            try {
                modelDueDate = LocalDate.parse(dueDate.textValue());
            } catch (DateTimeParseException e) {
                throw new IllegalValueException("Follow-up due date must be a valid ISO date (yyyy-MM-dd).");
            }
        }
        return new FollowUp(id.intValue(), description.textValue(), modelStatus, modelDueDate);
    }
}
