package client;

import server.Message;

public interface ServerService {

    void openConnection();
    void closeConnection();

    void sendMessage(String message);
    Message readMessages();

}