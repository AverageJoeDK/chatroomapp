package dk.casperhedegaard.chatroomapp.util;

public class DispatchGroup {

    //private boolean ready = true;
    private int count = 0;
    private Runnable runnable;

    public DispatchGroup() {
        super();
        count = 0;
    }

    public synchronized void enter() {
        count++;
        //ready = true;
    }

    public synchronized void leave() {
        count--;
        notifyGroup();
    }

    public void notify(Runnable r) {
        runnable = r;
        notifyGroup();
    }

    private void notifyGroup() {
        //if (ready) {
        if (count <= 0 && runnable != null) {
            runnable.run();
        }
        //}
    }
}
