package com.run;

import util.ServiceUtil;
import adaptor.PriceAdaptor;
import common.ITimeProviderFactory;
import common.LocalDateTimeProviderFactory;
import common.PriceStoreFactory;
import config.Config;
import data.IndicatorEvent;
import data.PriceEvent;
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

    public static void main(String[] args) throws Exception {
        List<IService> services = new ArrayList<>();

        Config config = Config.loadConfig("config.yaml");
        logger.info("Loaded config: " + config);

        AbstractQueueFeeder<String> cmdPriceFeeder = new CmdPriceFeeder();

        CommandClient client = getCommandClient(cmdPriceFeeder);
        services.add(client);

        BlockingQueue<PriceEvent> priceEventQueue = QueueFactory.createQueue(config.getQueueConfig().getCapacity(), QueueType.BACKOFF);

        PricePublisher<PriceEvent> publisher = new PricePublisher<>(priceEventQueue);

        PriceService priceService = getPriceService(priceEventQueue, config);
        services.add(priceService);

        PriceAdaptor priceAdaptor = getPriceAdaptor(publisher, cmdPriceFeeder);
        services.add(priceAdaptor);

        if (args != null && Arrays.asList(args).contains("simpletest")) {
            SimpleTester simpleTester = new SimpleTester(cmdPriceFeeder);
            simpleTester.start();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown hook triggered. Cleaning up...");
            if (!services.isEmpty()) {
                for (IService service : services) {
                    ServiceUtil.quietlyStop(service);
                }
            }
        }));
    }

    private static CommandClient getCommandClient(PriceFeeder<String> cmdPriceFeeder) {
        SystemOutPrinter systemOutPrinter = new SystemOutPrinter();
        CommandClient client = new CommandClient(cmdPriceFeeder, systemOutPrinter);
        client.start();
        return client;
    }

    private static PriceAdaptor getPriceAdaptor(PricePublisher<PriceEvent> publisher, PriceFeeder<String> cmdPriceFeeder) {
        PriceFeedHandler feedHandler = new PriceFeedHandler();
        PriceAdaptor priceAdaptor = new PriceAdaptor(feedHandler, publisher, cmdPriceFeeder);
        priceAdaptor.start();
        return priceAdaptor;
    }

    private static PriceService getPriceService(BlockingQueue<PriceEvent> priceEventQueue, Config config) {
        Publisher<IndicatorEvent> logPricePublisher = new PooledPublisher(new LogPublisher<>(), () -> new IndicatorEvent(null));
        PriceReader<PriceEvent> priceReader = new PriceReader<>(priceEventQueue);
        ITimeProviderFactory timeProviderFactory = new LocalDateTimeProviderFactory();

        StoreType storeType = config.getPriceServiceConfig().getStoreType();
        PriceStoreFactory priceStoreFactory = new PriceStoreFactory(storeType, "prod");

        DispatchStrategy dispatchStrategy = config.getDispatcherConfig().getDispatchType() == DispatchType.BY_SYMBOL ? new HashSymbolDispatchStrategy() : new RoundRobinDispatchStrategy();

        DispatcherAgent dispatcherAgent = new DispatcherAgent(config.getDispatcherConfig().getThreads(), dispatchStrategy, () -> {
            BlockingQueue<PriceEvent> taskQueue = new ArrayBlockingQueue<>(1000);
            EventWorker<PriceEvent> priceWorker = new EventWorker<>(taskQueue);
            priceWorker.registerHandler(new VwapPriceEventHandler(priceStoreFactory, new VwapCalculator(config), config, logPricePublisher));
            return priceWorker;
        });

        PriceService priceService = new PriceService(priceReader, timeProviderFactory.get(), logPricePublisher, priceStoreFactory, dispatcherAgent, config);
        priceService.start();
        return priceService;
    }
}
