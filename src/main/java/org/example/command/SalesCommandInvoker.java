package org.example.command;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes sales commands and keeps a simple execution log for the session.
 */
public class SalesCommandInvoker {

    private final List<String> history = new ArrayList<>();

    public void run(SalesCommand command, String description) {
        command.execute();
        history.add(description);
    }

    public List<String> getHistory() {
        return history;
    }
}
