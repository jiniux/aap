package xyz.jiniux.aap.domain.shipping;

import xyz.jiniux.aap.domain.model.Order;

import java.util.Random;
import java.util.UUID;
import java.util.random.RandomGenerator;

public class RandomTrackingNumberAssigner implements TrackingNumberAssigner {
    @Override
    public String assignTrackingNumber(Order order) {
        long timestamp = System.currentTimeMillis();
        long randomNum = RandomGenerator.getDefault().nextLong(10000000000L, 99999999999L);

        return "TRK" + timestamp + randomNum;
    }
}
