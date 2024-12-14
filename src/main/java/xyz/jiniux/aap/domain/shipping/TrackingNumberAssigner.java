package xyz.jiniux.aap.domain.shipping;

import org.springframework.stereotype.Component;
import xyz.jiniux.aap.domain.model.Order;

@Component
public interface TrackingNumberAssigner {
    String assignTrackingNumber(Order order);
}
