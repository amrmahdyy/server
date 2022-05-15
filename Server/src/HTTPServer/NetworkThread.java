package HTTPServer;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class NetworkThread extends Thread {
    private final Set<NetworkThreadListener> listeners = new CopyOnWriteArraySet<NetworkThreadListener>();

    public final void addListener(final NetworkThreadListener listener) {
        listeners.add(listener);
    }

    public final void removeListener(final NetworkThreadListener listener) {
        listeners.remove(listener);
    }

    private final void notifyListeners() {
        for (NetworkThreadListener listener : listeners) {
            listener.threadDidComplete(this);
        }
    }

    public abstract void doRun();

    @Override
    public final void run() {
        try {
            doRun();
        } finally {
            notifyListeners();
        }
    }
}
