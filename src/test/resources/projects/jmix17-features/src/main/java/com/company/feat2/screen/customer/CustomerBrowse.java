package com.company.feat2.screen.customer;

import io.jmix.ui.screen.LookupComponent;
import io.jmix.ui.screen.StandardLookup;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import com.company.feat2.entity.Customer;

@UiController("feat2_Customer.browse")
@UiDescriptor("customer-browse.xml")
@LookupComponent("customersTable")
public class CustomerBrowse extends StandardLookup<Customer> {

    @Subscribe
    public void onInit() {
        this.stepOne();
        this.stepTwo();
        this.stepThree();
        this.stepFour();
        this.stepFive();
        this.stepSix();
    }

    protected void stepOne() {}
    protected void stepTwo() {}
    protected void stepThree() {}
    protected void stepFour() {}
    protected void stepFive() {}
    protected void stepSix() {}
}
