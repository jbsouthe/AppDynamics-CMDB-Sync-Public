package com.appdynamics.scheduler;

import com.appdynamics.config.Configuration;
import com.appdynamics.controller.apidata.cmdb.BatchTaggingRequest;
import com.appdynamics.controller.apidata.model.Application;
import com.appdynamics.controller.apidata.model.Model;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class MainControlScheduler {
    private static final Logger logger = LogManager.getFormatterLogger();
    private Configuration configuration;
    private ThreadPoolExecutor threadPoolExecutor;
    private Collection<Future<?>> futures = new LinkedList<Future<?>>();
    private LinkedBlockingQueue<BatchTaggingRequest> dataToInsertLinkedBlockingQueue;
    private CountDownLatch countDownLatch;

    public MainControlScheduler(Configuration config ) {
        this.configuration = config;
        dataToInsertLinkedBlockingQueue = new LinkedBlockingQueue<>();
        threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool( this.configuration.getProperty(Configuration.NUMBER_EXEC_THREADS_PROPERTY, 15), new NamedThreadFactory("Worker") );
        threadPoolExecutor.execute( new ControllerBatchTagTask(configuration, dataToInsertLinkedBlockingQueue));
    }

    public void run() {
        configuration.setRunning(true);
        while(configuration.isRunning()) {
            try {
                Model model = configuration.getController().getModel(true);
                this.countDownLatch = new CountDownLatch(model.getAPMApplications().size() + 1);
                threadPoolExecutor.execute(new ServerTagRefreshTask(configuration, dataToInsertLinkedBlockingQueue, countDownLatch));
                threadPoolExecutor.execute(new SyntheticTagRefreshTask(configuration, dataToInsertLinkedBlockingQueue, countDownLatch));
                for (Application application : model.getAPMApplications()) {
                    threadPoolExecutor.execute(new ApplicationTagRefreshTask(application, configuration, dataToInsertLinkedBlockingQueue, countDownLatch));
                }
                logger.info("Finished scheduling sync thread workers, going to sleep for %d minutes and waking again", configuration.getProperty(Configuration.SYNC_FREQUENCY_MINUTES_PROPERTY, 20));
            }catch (RuntimeException runtimeException) {
                logger.warn("Runtime Exception caught during this run, sleeping and trying again later: "+ runtimeException.getMessage(),runtimeException);
            }
            sleep(configuration.getProperty(Configuration.SYNC_FREQUENCY_MINUTES_PROPERTY, 20));
        }

    }


    private void sleep( long forMinutes ) {
        try {
            Thread.sleep( forMinutes * 60000 );
        } catch (InterruptedException ignored) { }
    }
}
