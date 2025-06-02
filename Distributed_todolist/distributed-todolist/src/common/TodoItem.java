package common;

import java.io.Serializable;

public class TodoItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private String description;
    private boolean completed;

    public TodoItem(String description) {
        this.description = description;
        this.completed = false; // New items are initially not completed
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public String toString() {
        return (completed ? "[X] " : "[ ] ") + description;
    }
}