import adaptor.PriceAdaptor;
import common.LocalDateTimeProvider;
import common.TimeProvider;
import data.IndicatorEvent;
import data.PriceEvent;
import feed.PriceFeedHandler;
import feeder.CmdPriceFeeder;
import feeder.PriceFeeder;
import feeder.prompt.CommandClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import printer.SystemOutPrinter;
import publisher.LogPricePublisher;
import publisher.PricePublisher;
import publisher.PriceReader;
import publisher.Publisher;
import service.IService;
import service.PriceService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(LogPricePublisher.class);

    public static void main(String[] args) {
        List<IService> services = new ArrayList<>();

        SystemOutPrinter systemOutPrinter = new SystemOutPrinter();
//        ArrayBlockingQueue<String> feedQueue = new ArrayBlockingQueue<>();
        PriceFeeder<String> cmdPriceFeeder = new CmdPriceFeeder();
        CommandClient client = new CommandClient(cmdPriceFeeder, systemOutPrinter);
        client.start();

        services.add(client);


        ArrayBlockingQueue<PriceEvent> priceEventQueue = new ArrayBlockingQueue<>(10000);
        PricePublisher publisher = new PricePublisher(priceEventQueue);
        Publisher<IndicatorEvent> logPricePublisher = new LogPricePublisher<>();
        PriceReader priceReader = new PriceReader(priceEventQueue);
//        Publisher<PriceEvent> publisher = new LogPricePublisher();
        TimeProvider timeProvider = new LocalDateTimeProvider();
        PriceService priceService = new PriceService(priceReader, timeProvider, logPricePublisher);
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
