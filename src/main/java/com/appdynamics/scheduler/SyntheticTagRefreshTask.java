package com.appdynamics.scheduler;

import com.appdynamics.controller.apidata.synthetic.page.SyntheticPage;
import com.appdynamics.exceptions.CMDBException;
import com.appdynamics.config.Configuration;
import com.appdynamics.controller.apidata.cmdb.BatchTaggingRequest;
import com.appdynamics.controller.apidata.cmdb.Entity;
import com.appdynamics.controller.apidata.model.Application;
import com.appdynamics.controller.apidata.model.ITaggable;
import com.appdynamics.exceptions.ControllerBadStatusException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class SyntheticTagRefreshTask implements Runnable{
    private static final Logger logger = LogManager.getFormatterLogger();
    private Configuration configuration;
    private LinkedBlockingQueue<BatchTaggingRequest> dataQueue;
    private CountDownLatch countDownLatch;

    public SyntheticTagRefreshTask (Configuration configuration, LinkedBlockingQueue<BatchTaggingRequest> dataQueue, CountDownLatch countDownLatch) {
        this.configuration = configuration;
        this.dataQueue = dataQueue;
        this.countDownLatch = countDownLatch;
    }

    /**
     * When an object implementing interface {@code Runnable} is used
     * to create a thread, starting the thread causes the object's
     * {@code run} method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method {@code run} is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try {
            for( Application eumApplication : configuration.getController().getModel().getSyntheticApplications() )
                for( SyntheticPage syntheticPage : configuration.getController().getSyntheticPageList(eumApplication))
                    fetchDataAndQueue(syntheticPage);
        } catch (ControllerBadStatusException e) {
            logger.error("Exception trying to retrieve the list of servers on the controller: "+ e,e);
        }

        this.countDownLatch.countDown();
    }

    private void fetchDataAndQueue(ITaggable syntheticItem) {
        WorkingStatusThread workingStatusThread = null;
        try {
            workingStatusThread = new WorkingStatusThread("CMDB Data Query", Thread.currentThread().getName(), logger);
            workingStatusThread.start();
            Entity entity = configuration.getCmdbClient().query(syntheticItem);
            if( entity != null )
                dataQueue.add(new BatchTaggingRequest(syntheticItem.getEntityType(), entity));
        } catch (CMDBException e) {
            logger.warn("CMDB Exception while attempting to query %s: %s",syntheticItem.getEntityType(), syntheticItem);
        } finally {
            if( workingStatusThread != null ) workingStatusThread.cancel();
        }

    }
}
