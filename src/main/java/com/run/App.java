package com.run;

import common.*;
import data.*;
import latency.LatencyTracker;
import util.ServiceUtil;
import adaptor.PriceAdaptor;
import config.Config;
import dispatcher.*;
import enums.StoreType;
import feed.PriceFeedHandler;
import feeder.AbstractQueueFeeder;
import feeder.CmdPriceFeeder;
import feeder.PriceFeeder;
import feeder.SimpleTester;
import feeder.prompt.CommandClient;
import indicator.VwapCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import printer.SystemOutPrinter;
import publisher.*;
import queue.QueueFactory;
import queue.QueueType;
import service.IService;
import service.PriceService;
import service.EventWorker;
import service.VwapPriceEventHandler;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(LogPublisher.class);
    private static ITimeProviderFactory timeProviderFactory;
    private static TimeProvider timeProvider;
    private static LatencyTracker latencyTracker;

    public static void main(String[] args) throws Exception {
        boolean testMode = args != null && Arrays.asList(args).contains("simpletest");
        List<IService> services = new ArrayList<>();

        Config config = Config.loadConfig("config.yaml");
        logger.info("Loaded config: " + config);

        AbstractQueueFeeder<String> cmdPriceFeeder = new CmdPriceFeeder();

        timeProviderFactory = new NanoTimeProviderFactory();
        timeProvider = timeProviderFactory.get();

        CommandClient client = getCommandClient(cmdPriceFeeder);
        services.add(client);

        BlockingQueue<Event<Price>> priceEventQueue = QueueFactory.createQueue(config.getQueueConfig().getCapacity(), QueueType.BACKOFF);

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

    private static PriceService getPriceService(BlockingQueue<Event<Price>> priceEventQueue, Config config, boolean testMode) {
        Publisher<IndicatorEvent> pricePublisher = getPublisher(testMode);
        PriceReader<Event<Price>> priceReader = new PriceReader<>(priceEventQueue);

        StoreType storeType = config.getPriceServiceConfig().getStoreType();
        PriceStoreFactory priceStoreFactory = new PriceStoreFactory(storeType, "prod");

        DispatchStrategy dispatchStrategy = config.getDispatcherConfig().getDispatchType() == DispatchType.BY_SYMBOL ? new HashSymbolDispatchStrategy() : new RoundRobinDispatchStrategy();

        Publisher<IndicatorEvent> finalPricePublisher = pricePublisher;
        DispatcherAgent dispatcherAgent = new DispatcherAgent(config.getDispatcherConfig().getThreads(), dispatchStrategy, () -> {
            BlockingQueue<PriceEvent> taskQueue = new ArrayBlockingQueue<>(1000);
            EventWorker<PriceEvent> priceWorker = new EventWorker<>(taskQueue);
            priceWorker.registerHandler(new VwapPriceEventHandler(priceStoreFactory, new VwapCalculator(config), config, finalPricePublisher));
            return priceWorker;
        });

        PriceService priceService = new PriceService(priceReader, dispatcherAgent);
        priceService.start();
        return priceService;
    }

    private static Publisher<IndicatorEvent> getPublisher(boolean testMode) {
        Publisher<IndicatorEvent> pricePublisher = new PooledPublisher(new LogPublisher<>(), () -> new IndicatorEvent(null));
        if (testMode) {
            latencyTracker = new LatencyTracker(timeProvider);
            pricePublisher = new LatencyPublisher<>(new PooledPublisher(new LogPublisher<>(), () -> new IndicatorEvent(null)), latencyTracker);
        }
        return pricePublisher;
    }
}
