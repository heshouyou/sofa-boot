/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.runtime.service.helper;

import com.alipay.sofa.runtime.api.ServiceRegisterHook;
import com.alipay.sofa.runtime.service.component.Service;
import com.alipay.sofa.runtime.service.component.ServiceComponent;
import com.alipay.sofa.runtime.spi.binding.BindingAdapterFactory;
import com.alipay.sofa.runtime.spi.component.DefaultImplementation;
import com.alipay.sofa.runtime.spi.component.Implementation;
import com.alipay.sofa.runtime.spi.component.SofaRuntimeContext;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Service register helper.
 * Before service registering, invoke <code>ServiceRegisterHook.before</code> implementations via JAVA SPI.
 * After service registering, invoke <code>ServiceRegisterHook.after</code> implementations via JAVA SPI.
 *
 * @author <a href="mailto:guaner.zzx@alipay.com">Alaneuler</a>
 * Created on 2021/1/8
 */
public class ServiceRegisterHelper {
    private static final List<ServiceRegisterHook> serviceRegisterHooks = new ArrayList<>();

    static {
        ServiceLoader<ServiceRegisterHook> serviceLoader = ServiceLoader.load(ServiceRegisterHook.class);
        for (ServiceRegisterHook serviceRegisterHook: serviceLoader) {
            serviceRegisterHooks.add(serviceRegisterHook);
        }

        // Sort in ascending order
        serviceRegisterHooks.sort((o1, o2) -> {
            return Integer.compare(o1.order(), o2.order());
        });
    }

    public static void registerService(Service service,
                                       Object target,
                                       BindingAdapterFactory bindingAdapterFactory,
                                       SofaRuntimeContext sofaRuntimeContext) {
        // Invoke service registering before hook
        for (ServiceRegisterHook serviceRegisterHook: serviceRegisterHooks) {
            serviceRegisterHook.before(service, sofaRuntimeContext);
        }

        Implementation implementation = new DefaultImplementation();
        implementation.setTarget(target);
        ServiceComponent serviceComponent = new ServiceComponent(implementation, service,
                bindingAdapterFactory, sofaRuntimeContext);
        sofaRuntimeContext.getComponentManager().register(serviceComponent);

        // Invoke service registering after hook
        for (ServiceRegisterHook serviceRegisterHook: serviceRegisterHooks) {
            serviceRegisterHook.after();
        }
    }
}
