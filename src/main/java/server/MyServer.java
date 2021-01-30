package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyServer {
    public static final int PORT = 8083;

    private List<ClientHandler> clients;
    private AuthService authService;

    public MyServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            authService = new BaseAuthService();
            authService.start();
            clients = new ArrayList<>();
            while (true) {
                System.out.println("Ожидаем поключение клиентов");
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (authService != null) {
                authService.stop();
            }
        }
    }

    public synchronized void broadcastMessage(Message message, ClientHandler clientHandler) {

        // получим текст сообщения
        String MessageText = message.getMessage();
        // по - хорошему, ник личного сообщения нужно вводить в отдельном поле
        // чтобы не было проблем с парсингом
        if (MessageText != null) {
            String[] mesSplit = MessageText.split(" ");
            boolean privateMessage = false;
            // ищем признак отправки личного сообщения
            if (mesSplit[0].equals("/w")){
                privateMessage = true;
            }
            // в том случае, если сообщение приватное
            if (privateMessage) {
                // согласно условию задачи
                // например, «/w nick3 Привет»
                String nickMes = mesSplit[1];
                String privMessage = mesSplit[2];
                // ищем среди клиентов нужный
                // Если нашли - отправляем только ему
                for (ClientHandler client : clients) {
                    System.out.println(client.getNick());
                    if (client.getNick().equals(nickMes)) {
                        Message privMess = new Message();
                        privMess.setNick(message.getNick());
                        privMess.setMessage(privMessage);
                        client.sendMessage(privMess);
                    }
                }
            } else {
                for (ClientHandler client : clients) {
                    client.sendMessage(message);
                }
            }
        }
    }

    public synchronized boolean isNickBusy(String nick) {
        for (ClientHandler client : clients) {
            if (nick.equals(client.getNick())) {
                return true;
            }
        }
        return false;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }
}