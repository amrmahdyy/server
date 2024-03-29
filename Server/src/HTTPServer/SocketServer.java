package HTTPServer;

import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {
    public static String  resourcesDirectory = "/Users/Youssef/Desktop/Final Sockets/server/Server/src/HTTPServer/Resources";
    public static Integer ACTIVE_WORKERS = 0;
    public static Integer PORT = 8081;

    public static void main(String[] args) throws Exception {
        try {

            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Listening on port " + PORT + "...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
//                clientSocket.setSoTimeout(10000 / (ACTIVE_WORKERS + 1));

                ServerWorker serverWorker = new ServerWorker(clientSocket);
                serverWorker.addListener(new ThreadNotifications());
                serverWorker.setName(clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
                serverWorker.start();

                ACTIVE_WORKERS = ACTIVE_WORKERS + 1;
                System.out.println("Server Connections: " + ACTIVE_WORKERS);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
