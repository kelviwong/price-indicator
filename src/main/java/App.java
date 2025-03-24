import adaptor.PriceAdaptor;
import common.LocalDateTimeProviderFactory;
import common.ITimeProviderFactory;
import config.Config;
import data.IndicatorEvent;
import data.PriceEvent;
import dispatcher.DispatcherAgent;
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
import service.IService;
import service.PriceService;
import common.PriceStoreFactory;
import enums.StoreType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(LogPublisher.class);

    public static void main(String[] args) throws IOException {
        List<IService> services = new ArrayList<>();

        Config config = Config.loadConfig("config.yaml");
        logger.info("Loaded config: " + config);

        SystemOutPrinter systemOutPrinter = new SystemOutPrinter();
        PriceFeeder<String> cmdPriceFeeder = new CmdPriceFeeder();
        CommandClient client = new CommandClient(cmdPriceFeeder, systemOutPrinter);
        client.start();

        services.add(client);

        ArrayBlockingQueue<PriceEvent> priceEventQueue = new ArrayBlockingQueue<>(10000);
        PricePublisher<PriceEvent> publisher = new PricePublisher<>(priceEventQueue);
        Publisher<IndicatorEvent> logPricePublisher = new LogPublisher<>();
        PriceReader<PriceEvent> priceReader = new PriceReader<>(priceEventQueue);
        ITimeProviderFactory timeProviderFactory = new LocalDateTimeProviderFactory();

        PriceStoreFactory priceStoreFactory = new PriceStoreFactory(StoreType.DEQUE, "prod");

        DispatcherAgent dispatcherAgent = new DispatcherAgent(config.getDispatcherConfig().getThreads());

        PriceService priceService = new PriceService(priceReader, timeProviderFactory.get(), logPricePublisher, priceStoreFactory, dispatcherAgent, config);
        priceService.start();

        PriceFeedHandler feedHandler = new PriceFeedHandler();

        PriceAdaptor priceAdaptor = new PriceAdaptor(feedHandler, publisher, cmdPriceFeeder);
        priceAdaptor.start();

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
}
