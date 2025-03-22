package service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class PriceService implements IService {
    private static final Logger logger = LoggerFactory.getLogger(PriceService.class);

    @Override
    public void start() {
        logger.info("Start Price Service");
    }

    @Override
    public void stop() {
        logger.info("Stop Price Service");
    }
}
