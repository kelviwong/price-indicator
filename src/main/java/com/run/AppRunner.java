package com.run;

import adaptor.PriceAdaptor;
import common.ITimeProviderFactory;
import common.NanoTimeProviderFactory;
import common.PriceStoreFactory;
import common.TimeProvider;
import config.Config;
import data.*;
import dispatcher.*;
import enums.StoreType;
import feed.PriceFeedHandler;
import feeder.AbstractQueueFeeder;
import feeder.CmdPriceFeeder;
import feeder.PriceFeeder;
import feeder.SimpleTester;
import feeder.prompt.CommandClient;
import indicator.VwapCalculator;
import latency.LatencyTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import printer.SystemOutPrinter;
import publisher.*;
import queue.MessageQueue;
import queue.QueueFactory;
import queue.QueueType;
import service.EventWorker;
import service.IService;
import service.PriceService;
import service.VwapPriceEventHandler;
import util.ServiceUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppRunner {
    private static final Logger logger = LoggerFactory.getLogger(LogPublisher.class);
    private static ITimeProviderFactory timeProviderFactory;
    private static TimeProvider timeProvider;
    private static LatencyTracker latencyTracker;

    public void run(String[] args) throws Exception {
        boolean testMode = args != null && Arrays.asList(args).contains("simpletest");
        List<IService> services = new ArrayList<>();

        Config config = Config.loadConfig("config.yaml");
        logger.info("Loaded config: " + config);

        AbstractQueueFeeder<String> cmdPriceFeeder = new CmdPriceFeeder(config);

        timeProviderFactory = new NanoTimeProviderFactory();
        timeProvider = timeProviderFactory.get();

        CommandClient client = getCommandClient(cmdPriceFeeder);
        services.add(client);

        MessageQueue<Event<Price>> priceEventQueue =
                QueueFactory.createMessageQueue(
                        config.getQueueConfig().getCapacity(),
                        config.getQueueConfig().getQueueType()
                );

        PricePublisher<Event<Price>> publisher = new PricePublisher<>(priceEventQueue);

        PriceService priceService = getPriceService(priceEventQueue, config, testMode);
        services.add(priceService);

        PriceAdaptor priceAdaptor = getPriceAdaptor(publisher, cmdPriceFeeder);
        services.add(priceAdaptor);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown hook triggered. Cleaning up...");
            if (!services.isEmpty()) {
                for (IService service : services) {
                    ServiceUtil.quietlyStop(service);
                }
            }
        }));

        if (testMode) {
            SimpleTester simpleTester = new SimpleTester(cmdPriceFeeder, latencyTracker);
            simpleTester.start();
        }
    }


    private static CommandClient getCommandClient(PriceFeeder<String> cmdPriceFeeder) {
        SystemOutPrinter systemOutPrinter = new SystemOutPrinter();
        CommandClient client = new CommandClient(cmdPriceFeeder, systemOutPrinter);
        client.start();
        return client;
    }

    private static PriceAdaptor getPriceAdaptor(PricePublisher<Event<Price>> publisher, PriceFeeder<String> cmdPriceFeeder) {
        PriceFeedHandler feedHandler = new PriceFeedHandler();
        EventFactory eventFactory = new EventFactory(timeProvider);
        PriceAdaptor priceAdaptor = new PriceAdaptor(feedHandler, publisher, cmdPriceFeeder, eventFactory);
        priceAdaptor.start();
        return priceAdaptor;
    }

    private static PriceService getPriceService(MessageQueue<Event<Price>> priceEventQueue,
                                                Config config, boolean testMode) {
        Publisher<IndicatorEvent> pricePublisher = getPublisher(testMode);
        PriceReader<Event<Price>> priceReader = new PriceReader<>(priceEventQueue);

        StoreType storeType = config.getPriceServiceConfig().getStoreType();
        PriceStoreFactory priceStoreFactory = new PriceStoreFactory(storeType, "prod");

        DispatchStrategy dispatchStrategy = config.getDispatcherConfig().getDispatchType() == DispatchType.BY_SYMBOL ? new HashSymbolDispatchStrategy() : new RoundRobinDispatchStrategy();

        Publisher<IndicatorEvent> finalPricePublisher = pricePublisher;
        DispatcherAgent<PriceEvent> dispatcherAgent = new DispatcherAgent<>(config.getDispatcherConfig().getThreads(), dispatchStrategy,
                () -> {
                    MessageQueue<PriceEvent> taskQueue = null;
                    try {
                        taskQueue = QueueFactory.createMessageQueue(10000, config.getQueueConfig().getQueueType());
                    } catch (Exception e) {
                        logger.error("Error creating event Queue");
                    }

                    EventWorker<PriceEvent> priceWorker = new EventWorker<>(taskQueue);
                    priceWorker.registerHandler(new VwapPriceEventHandler(priceStoreFactory, new VwapCalculator(config), config, finalPricePublisher));
                    return priceWorker;
                });

        PriceService priceService = new PriceService(priceReader, dispatcherAgent);
        priceService.start();
        return priceService;
    }

    private static Publisher<IndicatorEvent> getPublisher(boolean testMode) {
        Publisher<IndicatorEvent> pricePublisher = new LogPublisher();
        if (testMode) {
            latencyTracker = new LatencyTracker(timeProvider);
            pricePublisher = new LatencyPublisher(new LogPublisher<>(), latencyTracker);
        }
        return pricePublisher;
    }

}
