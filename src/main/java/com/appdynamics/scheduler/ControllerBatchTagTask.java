package com.appdynamics.scheduler;

import com.appdynamics.config.Configuration;
import com.appdynamics.controller.apidata.cmdb.BatchTaggingRequest;
import com.appdynamics.controller.apidata.cmdb.Entity;
import com.appdynamics.controller.apidata.cmdb.Tag;
import com.appdynamics.exceptions.ControllerBadStatusException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ControllerBatchTagTask implements Runnable{
    private static final Logger logger = LogManager.getFormatterLogger();

    private Configuration configuration;
    private LinkedBlockingQueue<BatchTaggingRequest> dataQueue;

    public ControllerBatchTagTask (Configuration configuration, LinkedBlockingQueue<BatchTaggingRequest> dataQueue ) {
        this.configuration=configuration;
        this.dataQueue=dataQueue;
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
        while( configuration.isRunning() ) {
            WorkingStatusThread workingStatusThread = null;
            BatchTaggingRequest batchTaggingRequest = null;
            try {
                batchTaggingRequest = dataQueue.poll(5000, TimeUnit.MILLISECONDS);
                if( batchTaggingRequest != null && batchTaggingRequest.entities.size() > 0 ) {
                    logger.debug("Poll returned %d data elements to insert into the controller", batchTaggingRequest.entities.size());
                    if( this.configuration.isIdentityConversionConfigured() ) {
                        for(Entity entity : batchTaggingRequest.entities ) {
                            for(Tag tag : entity.tags) {
                                String newKey = configuration.getIdentityConversion(batchTaggingRequest.cmdbType, tag.key);
                                if( newKey != null ) {
                                    logger.debug("Found a conversion identity definition, swaping '%s' with '%s'",tag.key, newKey);
                                    tag.key = newKey;
                                }
                            }
                        }
                    }
                    workingStatusThread = new WorkingStatusThread("Controller Batch Insert", Thread.currentThread().getName(), logger);
                    workingStatusThread.start();
                    logger.info(String.format("Inserting Tags for %s %s(%d)",batchTaggingRequest.entityType, batchTaggingRequest.entities.get(0).entityName, batchTaggingRequest.entities.get(0).entityId));
                    configuration.getController().updateTags(batchTaggingRequest);
                }
            } catch (InterruptedException ignored) {
                //ignore it
            } catch (ControllerBadStatusException e) {
                if( batchTaggingRequest.retries++ < configuration.getProperty(Configuration.MAX_BATCH_RETRY_ATTEMPTS_PROPERTY, 3)) {
                    logger.warn(String.format("Failed to load tag data into the controller, will add it back to the queue for processing, message: %s", e));
                    dataQueue.add(batchTaggingRequest);
                } else {
                    logger.error(String.format("Failed to load tag data into the controller, max retries reached, message: %s", e));
                }
            } finally {
                if( workingStatusThread != null ) workingStatusThread.cancel();
            }
        }
        logger.debug("Shutting down Controller Tag Batch Insert Task");
    }
}
