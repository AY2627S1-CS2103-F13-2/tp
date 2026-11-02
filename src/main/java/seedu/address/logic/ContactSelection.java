package seedu.address.logic;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.PersonNameResolver;
import seedu.address.logic.commands.PersonSelectionRequest;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.ContactFileParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/** Resolves numeric contact selectors before the command's existing workflow runs. */
final class ContactSelection {
    private static final Pattern SELECTOR = Pattern.compile(
            "(?<prefix>(?:view|edit|delete|drop|introduce|log|editlog|unlog|star|unstar)\\s+)"
            + "(?<number>[0-9]+)(?<suffix>(?:\\s+[A-Za-z][A-Za-z0-9_-]*/.*)?)", Pattern.CASE_INSENSITIVE);

    private ContactSelection() {
    }

    /** Returns a resolved result, or null when ordinary index lookup should continue. */
    static CommandResult prepare(String input, Model model, ContactFileParser parser)
            throws ParseException, CommandException {
        Matcher matcher = SELECTOR.matcher(ContactFileParser.expandShortcut(input, model.getCommandAliases()).trim());
        if (!matcher.matches()) {
            return null;
        }
        List<Person> named = PersonNameResolver.findMatches(model, matcher.group("number"));
        int index = Integer.parseInt(matcher.group("number")) - 1;
        List<Person> displayed = model.getFilteredPersonList();
        Person indexed = index < displayed.size() ? displayed.get(index) : null;
        if (named.isEmpty() || named.size() == 1 && named.contains(indexed)) {
            return null;
        }
        List<Person> stored = model.getContactFile().getPersonList();
        List<Person> matches = stored.stream()
                .filter(person -> person.equals(indexed) || named.contains(person)).toList();
        Command selectedCommand = parser.parseCommand(matcher.group("prefix") + "1" + matcher.group("suffix"));
        PersonSelectionRequest.SelectionAction select = person -> {
            if (!stored.contains(person)) {
                throw new CommandException("Contact changed. Start the command again.");
            }
            int position = stored.indexOf(person);
            model.updateFilteredPersonList(candidate -> position < stored.size() && candidate == stored.get(position));
            return selectedCommand.execute(model);
        };
        model.updateFilteredPersonList(matches::contains);
        if (matches.size() == 1) {
            return select.apply(matches.getFirst());
        }
        PersonSelectionRequest request = new PersonSelectionRequest(matches, select);
        return new CommandResult(request.getPrompt(), request);
    }
}
