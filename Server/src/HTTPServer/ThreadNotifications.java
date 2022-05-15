package HTTPServer;

public class ThreadNotifications implements NetworkThreadListener {
    @Override
    public void threadDidComplete(Thread thread) {
        SocketServer.ACTIVE_WORKERS = SocketServer.ACTIVE_WORKERS - 1;
        System.out.println("Connection " + thread.getName() + " Closed.");
    }
}
