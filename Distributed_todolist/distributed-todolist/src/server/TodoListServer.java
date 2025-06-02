package server;

import common.TodoItem;
import common.TodoListClientInterface;
import common.TodoListInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TodoListServer extends UnicastRemoteObject implements TodoListInterface {
    private List<TodoItem> todoList;
    private List<TodoListClientInterface> clients;

    public TodoListServer() throws RemoteException {
        todoList = Collections.synchronizedList(new ArrayList<>());
        clients = Collections.synchronizedList(new ArrayList<>());
    }

    @Override
    public synchronized void addTodoItem(TodoItem item) throws RemoteException {
        System.out.println("Adding item: " + item.getDescription());
        todoList.add(item);
        broadcastTodoListUpdate();
    }

    @Override
    public synchronized void markTodoItemComplete(String description) throws RemoteException {
        System.out.println("Marking complete: " + description);
        for (TodoItem item : todoList) {
            if (item.getDescription().equals(description)) {
                item.setCompleted(true);
                break;
            }
        }
        broadcastTodoListUpdate();
    }

    @Override
    public synchronized void removeTodoItem(String description) throws RemoteException {
        System.out.println("Removing item: " + description);
        todoList.removeIf(item -> item.getDescription().equals(description));
        broadcastTodoListUpdate();
    }

    @Override
    public synchronized List<TodoItem> getTodoList() throws RemoteException {
        return new ArrayList<>(todoList); // Return a copy to prevent external modification
    }

    @Override
    public synchronized void registerClient(TodoListClientInterface client) throws RemoteException {
        clients.add(client);
        System.out.println("Client registered: " + client);
        client.updateTodoList(new ArrayList<>(todoList)); // Send current list to new client
    }

    @Override
    public synchronized void unregisterClient(TodoListClientInterface client) throws RemoteException {
        clients.remove(client);
        System.out.println("Client unregistered: " + client);
    }

    private void broadcastTodoListUpdate() throws RemoteException {
        List<TodoItem> currentList = new ArrayList<>(todoList); // Create a copy for broadcasting
        synchronized (clients) {
            List<TodoListClientInterface> clientsToRemove = new ArrayList<>();
            for (TodoListClientInterface client : clients) {
                try {
                    client.updateTodoList(currentList);
                } catch (RemoteException e) {
                    System.err.println("Client " + client + " unreachable, removing...");
                    clientsToRemove.add(client);
                }
            }
            clients.removeAll(clientsToRemove); // Remove unreachable clients
        }
    }

    public static void main(String[] args) {
        try {
            TodoListServer server = new TodoListServer();
            Registry registry = LocateRegistry.createRegistry(8002); // Use a different port than chat for clarity
            registry.rebind("TodoListService", server);
            System.out.println("To-Do List Server is running on port 8002...");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}