package gr.wackydevelopers.camel.services;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ServiceOne {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceOne.class);

    private static final int MINIMUM_DELAY_MS = 500;
    private static final int MAXIMUM_DELAY_MS = 1000;
    
    private static final Random random = new SecureRandom();
    
    public String performOperation() {
        delay();
        String result = "ServiceOne~" + UUID.randomUUID().toString();
        logger.info("Operation performed from ServiceOne: {}", result);
        return result;
    }
    
    public void cancelOperation(String id) {
        logger.info("Operation Canceled from ServiceOne: {}", id);
    }
    
    public void completeOperation() {
        logger.info("Operation completed from ServiceOne");
    }
    
    private void delay() {
        try {
            long delay = (long)random.nextInt(MAXIMUM_DELAY_MS - MINIMUM_DELAY_MS) + (long) MINIMUM_DELAY_MS;
            logger.debug("ServiceOne Delay: {}", delay);
            TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
