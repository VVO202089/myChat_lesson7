package client;

public class ClientApp {
    public static void main(String[] args) {
        // организация нескольких клиентов
        new MyClient("ivan","password").setVisible(true);
        new MyClient("sharik","gav").setVisible(true);
        new MyClient("otvertka","shurup").setVisible(true);
    }
}