package client;

import server.Message;

public interface ServerService {

    void openConnection();
    void closeConnection();
    void setLogin(String login);
    void setPassword(String password);

    void sendMessage(String message);
    Message readMessages();

}