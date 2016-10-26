package com.jake.library.job;

/**
 * Created by jakechen on 2016/10/26.
 */

public abstract class BaseJob implements Runnable {
    private volatile boolean isStop = false;

    @Override
    public void run() {
        if (!isStop) {
            runInThread();
        }
        isStop = true;
    }

    public void stop() {
        isStop = true;
    }

    public void start() {
        isStop = false;
    }


    public boolean isStop() {
        return isStop;
    }

    protected abstract void runInThread();
}
