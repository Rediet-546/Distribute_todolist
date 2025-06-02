package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface TodoListInterface extends Remote {
    void addTodoItem(TodoItem item) throws RemoteException;
    void markTodoItemComplete(String description) throws RemoteException;
    void removeTodoItem(String description) throws RemoteException;
    List<TodoItem> getTodoList() throws RemoteException;
    void registerClient(TodoListClientInterface client) throws RemoteException;
    void unregisterClient(TodoListClientInterface client) throws RemoteException;
}