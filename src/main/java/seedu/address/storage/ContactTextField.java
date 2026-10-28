package seedu.address.storage;

import java.util.function.Predicate;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Company;
import seedu.address.model.person.Email;
import seedu.address.model.person.Linkedin;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;

/**
 * Shared value constraints for contact text fields. Callers handle JSON types and missing values.
 */
enum ContactTextField {
    NAME("name", Name::isValidName, Name.MESSAGE_CONSTRAINTS),
    COMPANY("company", Company::isValidCompany, Company.MESSAGE_CONSTRAINTS),
    ROLE("role", Role::isValidRole, Role.MESSAGE_CONSTRAINTS),
    PHONE("phone", Phone::isValidPhone, Phone.MESSAGE_CONSTRAINTS),
    EMAIL("email", Email::isValidEmail, Email.MESSAGE_CONSTRAINTS),
    LINKEDIN("linkedin", Linkedin::isValidLinkedin, Linkedin.MESSAGE_CONSTRAINTS);

    private final String jsonName;
    private final Predicate<String> validator;
    private final String constraints;

    ContactTextField(String jsonName, Predicate<String> validator, String constraints) {
        this.jsonName = jsonName;
        this.validator = validator;
        this.constraints = constraints;
    }

    String getJsonName() {
        return jsonName;
    }

    void validate(String value) throws IllegalValueException {
        if (!validator.test(value)) {
            throw new IllegalValueException(constraints);
        }
    }
}
