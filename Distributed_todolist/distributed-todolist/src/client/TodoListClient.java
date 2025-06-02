package client;

import common.TodoItem;
import common.TodoListClientInterface;
import common.TodoListInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class TodoListClient extends UnicastRemoteObject implements TodoListClientInterface {
    private TodoListInterface server;
    private JFrame frame;
    private DefaultListModel<TodoItem> todoListModel;
    private JList<TodoItem> todoListJList;
    private JTextField taskInputField;

    public TodoListClient() throws RemoteException {
        try {
            server = (TodoListInterface) Naming.lookup("rmi://localhost:8002/TodoListService");
            createGUI();
            server.registerClient(this);
            // Get initial list from server
            List<TodoItem> initialList = server.getTodoList();
            SwingUtilities.invokeLater(() -> {
                for (TodoItem item : initialList) {
                    todoListModel.addElement(item);
                }
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error connecting to server: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    @Override
    public void updateTodoList(List<TodoItem> updatedList) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            todoListModel.clear();
            for (TodoItem item : updatedList) {
                todoListModel.addElement(item);
            }
        });
    }

    private void createGUI() {
        frame = new JFrame("Distributed To-Do List");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());

        todoListModel = new DefaultListModel<>();
        todoListJList = new JList<>(todoListModel);
        todoListJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(todoListJList);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        taskInputField = new JTextField();
        JButton addButton = new JButton("Add Task");

        inputPanel.add(taskInputField, BorderLayout.CENTER);
        inputPanel.add(addButton, BorderLayout.EAST);
        frame.add(inputPanel, BorderLayout.NORTH); // Changed to NORTH for input

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton completeButton = new JButton("Mark Complete");
        JButton removeButton = new JButton("Remove Task");
        buttonPanel.add(completeButton);
        buttonPanel.add(removeButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addTask());
        taskInputField.addActionListener(e -> addTask()); // Allow Enter key to add

        completeButton.addActionListener(e -> markComplete());
        removeButton.addActionListener(e -> removeTask());

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    server.unregisterClient(TodoListClient.this);
                    frame.dispose();
                    System.exit(0);
                } catch (RemoteException ex) {
                    JOptionPane.showMessageDialog(frame, "Error disconnecting: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        frame.setVisible(true);
    }

    private void addTask() {
        String taskDescription = taskInputField.getText().trim();
        if (!taskDescription.isEmpty()) {
            try {
                TodoItem newItem = new TodoItem(taskDescription);
                server.addTodoItem(newItem);
                taskInputField.setText("");
            } catch (RemoteException e) {
                JOptionPane.showMessageDialog(frame, "Error adding task: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void markComplete() {
        TodoItem selectedItem = todoListJList.getSelectedValue();
        if (selectedItem != null) {
            try {
                server.markTodoItemComplete(selectedItem.getDescription());
            } catch (RemoteException e) {
                JOptionPane.showMessageDialog(frame, "Error marking task complete: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a task to mark complete.", "No Task Selected", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void removeTask() {
        TodoItem selectedItem = todoListJList.getSelectedValue();
        if (selectedItem != null) {
            int confirm = JOptionPane.showConfirmDialog(frame,
                    "Are you sure you want to remove \"" + selectedItem.getDescription() + "\"?",
                    "Confirm Removal", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    server.removeTodoItem(selectedItem.getDescription());
                } catch (RemoteException e) {
                    JOptionPane.showMessageDialog(frame, "Error removing task: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a task to remove.", "No Task Selected", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            new TodoListClient();
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Error starting client: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}