package com.company.feat2.security;

import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityui.role.annotation.MenuPolicy;
import io.jmix.securityui.role.annotation.ScreenPolicy;

@ResourceRole(name = "Full Access", code = FullAccessRole.CODE)
public interface FullAccessRole {

    String CODE = "system-full-access";

    @EntityPolicy(entityName = "*", actions = EntityPolicyAction.ALL)
    @ScreenPolicy(screenIds = "*")
    @MenuPolicy(menuIds = "*")
    void fullAccess();

    @ScreenPolicy(screenIds = "feat2_Customer.browse")
    void customerScreens();
}
