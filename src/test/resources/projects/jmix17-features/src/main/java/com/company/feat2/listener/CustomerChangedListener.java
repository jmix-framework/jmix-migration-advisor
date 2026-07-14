package com.company.feat2.listener;

import com.company.feat2.entity.Customer;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CustomerChangedListener {

    @EventListener
    public void onCustomerChanged(EntityChangedEvent<Customer> event) {
        // react to changes
    }
}
