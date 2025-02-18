package xyz.jiniux.aap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.jiniux.aap.domain.shipping.RandomTrackingNumberAssigner;
import xyz.jiniux.aap.domain.shipping.TrackingNumberAssigner;

@Configuration
public class ComponentConfiguration {
    @Bean
    public TrackingNumberAssigner trackingNumberAssigner() {
        return new RandomTrackingNumberAssigner();
    }
}
