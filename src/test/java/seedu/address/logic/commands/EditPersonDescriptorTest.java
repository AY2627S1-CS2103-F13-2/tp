package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_COMPANY_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LINKEDIN_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ROLE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditPersonDescriptorTest {

    @Test
    public void equals() {
        EditPersonDescriptor descriptorWithSameValues =
                new EditPersonDescriptor(DESC_AMY);
        assertTrue(DESC_AMY.equals(descriptorWithSameValues));

        assertTrue(DESC_AMY.equals(DESC_AMY));

        assertFalse(DESC_AMY.equals(null));

        assertFalse(DESC_AMY.equals(5));

        assertFalse(DESC_AMY.equals(DESC_BOB));

        EditPersonDescriptor editedAmy =
                new EditPersonDescriptorBuilder(DESC_AMY)
                        .withName(VALID_NAME_BOB)
                        .build();
        assertFalse(DESC_AMY.equals(editedAmy));

        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY)
                .withPhone(VALID_PHONE_BOB)
                .build();
        assertFalse(DESC_AMY.equals(editedAmy));

        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY)
                .withEmail(VALID_EMAIL_BOB)
                .build();
        assertFalse(DESC_AMY.equals(editedAmy));

        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY)
                .withCompany(VALID_COMPANY_BOB)
                .build();
        assertFalse(DESC_AMY.equals(editedAmy));

        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY)
                .withRole(VALID_ROLE_BOB)
                .build();
        assertFalse(DESC_AMY.equals(editedAmy));

        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY)
                .withLinkedin(VALID_LINKEDIN_BOB)
                .build();
        assertFalse(DESC_AMY.equals(editedAmy));

        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY)
                .withTags(VALID_TAG_HUSBAND)
                .build();
        assertFalse(DESC_AMY.equals(editedAmy));
    }

    @Test
    public void toStringMethod() {
        EditPersonDescriptor editPersonDescriptor =
                new EditPersonDescriptor();

        String expected =
                EditPersonDescriptor.class.getCanonicalName()
                        + "{name="
                        + editPersonDescriptor.getName().orElse(null)
                        + ", company="
                        + editPersonDescriptor.getCompany().orElse(null)
                        + ", role="
                        + editPersonDescriptor.getRole().orElse(null)
                        + ", phone="
                        + editPersonDescriptor.getPhone().orElse(null)
                        + ", email="
                        + editPersonDescriptor.getEmail().orElse(null)
                        + ", linkedin="
                        + editPersonDescriptor.getLinkedin().orElse(null)
                        + ", event="
                        + editPersonDescriptor.getEvent().orElse(null)
                        + ", tags="
                        + editPersonDescriptor.getTags().orElse(null)
                        + ", fieldsToRemove=[]}";

        assertEquals(expected, editPersonDescriptor.toString());
    }
}
