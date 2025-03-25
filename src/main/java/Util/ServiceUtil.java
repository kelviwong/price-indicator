package Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.IService;

public class ServiceUtil {
    protected static final Logger logger = LoggerFactory.getLogger(ServiceUtil.class);

    public static void quietlyStop(IService service) {
        try {
            service.stop();
        } catch (Exception e) {
            logger.error("Error stopping service", e);
        }
    }
}
