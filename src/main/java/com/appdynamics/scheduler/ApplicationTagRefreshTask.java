package com.appdynamics.scheduler;

import com.appdynamics.exceptions.CMDBException;
import com.appdynamics.config.Configuration;
import com.appdynamics.controller.apidata.cmdb.BatchTaggingRequest;
import com.appdynamics.controller.apidata.cmdb.Entity;
import com.appdynamics.controller.apidata.model.Application;
import com.appdynamics.controller.apidata.model.BusinessTransaction;
import com.appdynamics.controller.apidata.model.ITaggable;
import com.appdynamics.controller.apidata.model.Node;
import com.appdynamics.controller.apidata.model.Tier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class ApplicationTagRefreshTask implements Runnable{
    private static final Logger logger = LogManager.getFormatterLogger();
    private Configuration configuration;
    private Application application;
    private LinkedBlockingQueue<BatchTaggingRequest> dataQueue;
    private CountDownLatch countDownLatch;

    public ApplicationTagRefreshTask (Application application, Configuration configuration, LinkedBlockingQueue<BatchTaggingRequest> dataQueue, CountDownLatch countDownLatch) {
        this.application = application;
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
        while( ! this.application.isFinishedInitialization() ) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ignored) { }
        }
        fetchDataAndQueue(application);
        for( Tier tier : application.tiers ) {
            fetchDataAndQueue(tier);
        }
        for(Node node : application.nodes) {
            fetchDataAndQueue(node);
        }
        for(BusinessTransaction businessTransaction : application.businessTransactions) {
            fetchDataAndQueue(businessTransaction);
        }
        this.countDownLatch.countDown();
    }

    private void fetchDataAndQueue(ITaggable component) {
        WorkingStatusThread workingStatusThread = null;
        try {
            workingStatusThread = new WorkingStatusThread("CMDB Data Query", Thread.currentThread().getName(), logger);
            workingStatusThread.start();
            logger.info("Syncing for "+ component);
            Entity entity = configuration.getCmdbClient().query(component);
            if( entity != null && entity.tags.size() > 0 )
                dataQueue.add(new BatchTaggingRequest(component.getEntityType(), entity));
        } catch (CMDBException e) {
            logger.warn(String.format("CMDB Exception while attempting to query %s: %s",component.getEntityType(), component));
        } finally {
            if( workingStatusThread != null ) workingStatusThread.cancel();
        }

    }
}
