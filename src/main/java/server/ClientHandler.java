package server;

import com.google.gson.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

    private Socket socket;
    private MyServer myServer;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String nick;

    public ClientHandler(MyServer myServer, Socket socket) {
        try {
            this.myServer = myServer;
            this.socket = socket;
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    authentication();
                    readMessages();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        myServer.unsubscribe(this);
        Message message = new Message();
        message.setMessage(nick+" вышел из чата");
        myServer.broadcastMessage(message,this);
        try {
            dataOutputStream.close();
            dataInputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void authentication() {
        while (true) {
            try {
                AuthMessage message = new Gson().fromJson(dataInputStream.readUTF(), AuthMessage.class);
                String nick = myServer.getAuthService().getNickByLoginAndPass(message.getLogin(), message.getPassword());
                if (nick != null && !myServer.isNickBusy(nick)) {
                    message.setAuthenticated(true);
                    myServer.subscribe(this);
                    dataOutputStream.writeUTF(new Gson().toJson(message));
                    Message broadcastMsg = new Message();
                    broadcastMsg.setMessage(nick + " вошел в чат");
                    myServer.broadcastMessage(broadcastMsg,this);
                    this.nick = nick;
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readMessages() throws IOException {
        while (true) {
            Message message = new Gson().fromJson(dataInputStream.readUTF(), Message.class);
            message.setNick(nick);
            System.out.println(message);
            if ("/end".equals(message.getMessage())) {
                return;
            }
            myServer.broadcastMessage(message,this);
        }
    }

    public void sendMessage(Message message) {
        try {
            dataOutputStream.writeUTF(new Gson().toJson(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }
}