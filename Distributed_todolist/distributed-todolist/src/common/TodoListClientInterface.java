package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface TodoListClientInterface extends Remote {
    void updateTodoList(List<TodoItem> todoList) throws RemoteException;
}