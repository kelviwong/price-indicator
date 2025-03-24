import adaptor.PriceAdaptor;
import common.ITimeProviderFactory;
import common.LocalDateTimeProviderFactory;
import common.PriceStoreFactory;
import config.Config;
import data.IndicatorEvent;
import data.PriceEvent;
import dispatcher.DispatcherAgent;
import enums.StoreType;
import feed.PriceFeedHandler;
import feeder.CmdPriceFeeder;
import feeder.PriceFeeder;
import feeder.prompt.CommandClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import printer.SystemOutPrinter;
import publisher.LogPublisher;
import publisher.PricePublisher;
import publisher.PriceReader;
import publisher.Publisher;
import queue.QueueFactory;
import queue.QueueType;
import service.IService;
import service.PriceService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(LogPublisher.class);

    public static void main(String[] args) throws Exception {
        List<IService> services = new ArrayList<>();

        Config config = Config.loadConfig("config.yaml");
        logger.info("Loaded config: " + config);

        PriceFeeder<String> cmdPriceFeeder = new CmdPriceFeeder();

        CommandClient client = getCommandClient(cmdPriceFeeder);
        services.add(client);

        BlockingQueue<PriceEvent> priceEventQueue = QueueFactory.createQueue(10000, QueueType.BACKOFF);

        PricePublisher<PriceEvent> publisher = new PricePublisher<>(priceEventQueue);

        PriceService priceService = getPriceService(priceEventQueue, config);
        services.add(priceService);

        PriceAdaptor priceAdaptor = getPriceAdaptor(publisher, cmdPriceFeeder);
        services.add(priceAdaptor);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown hook triggered. Cleaning up...");
            if (!services.isEmpty()) {
                for (IService service : services) {
                    if (service != null) {
                        service.stop();
                    }
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
        Publisher<IndicatorEvent> logPricePublisher = new LogPublisher<>();
        PriceReader<PriceEvent> priceReader = new PriceReader<>(priceEventQueue);
        ITimeProviderFactory timeProviderFactory = new LocalDateTimeProviderFactory();

        StoreType storeType = config.getPriceServiceConfig().getStoreType();
        PriceStoreFactory priceStoreFactory = new PriceStoreFactory(storeType, "prod");

        DispatcherAgent dispatcherAgent = new DispatcherAgent(config.getDispatcherConfig().getThreads());

        PriceService priceService = new PriceService(priceReader, timeProviderFactory.get(), logPricePublisher, priceStoreFactory, dispatcherAgent, config);
        priceService.start();
        return priceService;
    }
}
