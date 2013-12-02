package com.sogou.upd.passport.session.async;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * User: ligang201716@sogou-inc.com
 * Date: 13-12-2
 * Time: 下午6:14
 */
public class AsyncMultiThreadAppenderBase<E> extends UnsynchronizedAppenderBase<E> implements AppenderAttachable<E> {

    AppenderAttachableImpl<E> aai = new AppenderAttachableImpl<E>();
    BlockingQueue<E> blockingQueue;

    /**
     * The default buffer size.
     */
    public static final int DEFAULT_QUEUE_SIZE = 256;
    int queueSize = DEFAULT_QUEUE_SIZE;

    int appenderCount = 0;

    static final int UNDEFINED = -1;
    int discardingThreshold = UNDEFINED;

    public static final int DEFAULT_WORKER_SIZE = Runtime.getRuntime().availableProcessors();
    int workerSize = DEFAULT_WORKER_SIZE;

    // Worker worker = new Worker();
    List<Worker> workers;

    /**
     * Is the eventObject passed as parameter discardable? The base class's implementation of this method always returns 'false' but sub-classes may (and do)
     * override this method. <p/> <p>Note that only if the buffer is nearly full are events discarded. Otherwise, when the buffer is "not full" all events are
     * logged.
     *
     * @return - true if the event can be discarded, false otherwise
     */
    protected boolean isDiscardable(E eventObject) {
        return false;
    }


    /**
     * Pre-process the event prior to queueing. The base class does no pre-processing but sub-classes can override this behavior.
     */
    protected void preprocess(E eventObject) {
    }


    @Override
    public void start() {
        if (appenderCount == 0) {
            addError("No attached appenders found.");
            return;
        }
        if (queueSize < 1) {
            addError("Invalid queue size [" + queueSize + "]");
            return;
        }
        blockingQueue = new ArrayBlockingQueue<E>(queueSize);

        if (discardingThreshold == UNDEFINED) {
            discardingThreshold = queueSize / 5;
        }
        addInfo("Setting discardingThreshold to " + discardingThreshold);

        if (workerSize <= 0) {
            addError("Invalid worker size [" + workerSize + "], default is " + DEFAULT_WORKER_SIZE);
            return;
        }

        workers = new ArrayList<Worker>(workerSize);
        for (int i=0; i<workerSize; i++) {
            Worker worker = new Worker();
            worker.setDaemon(true);
            worker.setName("AsyncAppender-Worker-" + worker.getName());
            workers.add(worker);
        }
        // make sure this instance is marked as "started" before staring the worker Thread
        super.start();
        for (int i=0; i<workerSize; i++) {
            workers.get(i).start();
        }
    }

    @Override
    public void stop() {
        if (!isStarted()) {
            return;
        }

        // mark this appender as stopped so that Worker can also processPriorToRemoval if it is invoking aii.appendLoopOnAppenders
        // and sub-appenders consume the interruption
        super.stop();

        // interrupt the worker thread so that it can terminate. Note that the interruption can be consumed
        // by sub-appenders
        for (int i=0; i<workerSize; i++) {
            Worker worker = workers.get(i);
            worker.interrupt();
            try {
                worker.join(1000);
            } catch (InterruptedException e) {
                addError("Failed to join worker thread", e);
            }
        }
        workers.clear();
    }


    @Override
    protected void append(E eventObject) {
        if (isQueueBelowDiscardingThreshold() && isDiscardable(eventObject)) {
            return;
        }
        preprocess(eventObject);
        put(eventObject);
    }

    private boolean isQueueBelowDiscardingThreshold() {
        return (blockingQueue.remainingCapacity() < discardingThreshold);
    }

    private void put(E eventObject) {
        try {
            blockingQueue.put(eventObject);
        } catch (InterruptedException e) {
        }
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public int getWorkerSize() {
        return workerSize;
    }

    public void setWorkerSize(int workerSize) {
        this.workerSize = workerSize;
    }

    public int getDiscardingThreshold() {
        return discardingThreshold;
    }

    public void setDiscardingThreshold(int discardingThreshold) {
        this.discardingThreshold = discardingThreshold;
    }

    /**
     * Returns the number of elements currently in the blocking queue.
     *
     * @return number of elements currently in the queue.
     */
    public int getNumberOfElementsInQueue() {
        return blockingQueue.size();
    }

    /**
     * The remaining capacity available in the blocking queue.
     *
     * @return the remaining capacity
     * @see {@link java.util.concurrent.BlockingQueue#remainingCapacity()}
     */
    public int getRemainingCapacity() {
        return blockingQueue.remainingCapacity();
    }


    public void addAppender(Appender<E> newAppender) {
        if (appenderCount == 0) {
            appenderCount++;
            addInfo("Attaching appender named [" + newAppender.getName() + "] to AsyncAppender.");
            aai.addAppender(newAppender);
        } else {
            appenderCount++;
            addInfo("Attaching appender named [" + newAppender.getName() + "] to AsyncAppender.");
            aai.addAppender(newAppender);
            // addWarn("One and only one appender may be attached to AsyncAppender.");
            // addWarn("Ignoring additional appender named [" + newAppender.getName() + "]");
        }
    }

    public Iterator<Appender<E>> iteratorForAppenders() {
        return aai.iteratorForAppenders();
    }

    public Appender<E> getAppender(String name) {
        return aai.getAppender(name);
    }

    public boolean isAttached(Appender<E> eAppender) {
        return aai.isAttached(eAppender);
    }

    public void detachAndStopAllAppenders() {
        aai.detachAndStopAllAppenders();
    }

    public boolean detachAppender(Appender<E> eAppender) {
        return aai.detachAppender(eAppender);
    }

    public boolean detachAppender(String name) {
        return aai.detachAppender(name);
    }

    class Worker extends Thread {

        public void run() {
            AsyncMultiThreadAppenderBase<E> parent = AsyncMultiThreadAppenderBase.this;
            AppenderAttachableImpl<E> aai = parent.aai;

            // loop while the parent is started
            while (parent.isStarted()) {
                try {
                    E e = parent.blockingQueue.take();
                    aai.appendLoopOnAppenders(e);
                } catch (InterruptedException ie) {
                    break;
                }
            }

            addInfo("Worker thread will flush remaining events before exiting. ");
            for (E e : parent.blockingQueue) {
                aai.appendLoopOnAppenders(e);
            }

            aai.detachAndStopAllAppenders();
        }
    }

}
