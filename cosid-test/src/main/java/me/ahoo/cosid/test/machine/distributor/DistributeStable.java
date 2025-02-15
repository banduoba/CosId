/*
 * Copyright [2021-present] [ahoo wang <ahoowang@qq.com> (https://github.com/Ahoo-Wang)].
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.ahoo.cosid.test.machine.distributor;

import static me.ahoo.cosid.test.machine.distributor.MachineIdDistributorSpec.TEST_HOST;
import static me.ahoo.cosid.test.machine.distributor.MachineIdDistributorSpec.TEST_MACHINE_BIT;
import static me.ahoo.cosid.test.machine.distributor.MachineIdDistributorSpec.allInstances;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import me.ahoo.cosid.machine.InstanceId;
import me.ahoo.cosid.machine.MachineIdDistributor;
import me.ahoo.cosid.machine.MachineIdOverflowException;
import me.ahoo.cosid.test.Assert;
import me.ahoo.cosid.test.MockIdGenerator;
import me.ahoo.cosid.test.TestSpec;

import java.util.List;
import java.util.function.Supplier;

/**
 * DistributeStable .
 *
 * @author ahoo wang
 */
public class DistributeStable implements TestSpec {
    private final Supplier<MachineIdDistributor> implFactory;
    
    public DistributeStable(Supplier<MachineIdDistributor> implFactory) {
        this.implFactory = implFactory;
    }
    
    @Override
    public void verify() {
        MachineIdDistributor distributor = implFactory.get();
        String namespace = MockIdGenerator.usePrefix("DistributeStable").generateAsString();
        List<InstanceId> allInstances = allInstances(TEST_MACHINE_BIT, true);
        assertThat(allInstances, hasSize(MachineIdDistributor.totalMachineIds(TEST_MACHINE_BIT)));
        
        for (int i = 0; i < allInstances.size(); i++) {
            int machineId = distributor.distribute(namespace, TEST_MACHINE_BIT, allInstances.get(i), MachineIdDistributor.FOREVER_SAFE_GUARD_DURATION).getMachineId();
            assertThat(machineId, equalTo(i));
        }
        InstanceId overflowInstanceId = InstanceId.of(TEST_HOST, MachineIdDistributor.totalMachineIds(TEST_MACHINE_BIT), true);
        
        Assert.assertThrows(MachineIdOverflowException.class, () -> {
            distributor.distribute(namespace, TEST_MACHINE_BIT, overflowInstanceId, MachineIdDistributor.FOREVER_SAFE_GUARD_DURATION);
        });
        
        InstanceId firstInstanceId = allInstances.get(0);
        /*
         * 稳定实例不会回收机器号
         */
        distributor.revert(namespace, firstInstanceId);
        
        Assert.assertThrows(MachineIdOverflowException.class, () -> {
            /*
             * 稳定实例不会回收机器号，所以依然抛出异常
             */
            distributor.distribute(namespace, TEST_MACHINE_BIT, overflowInstanceId, MachineIdDistributor.FOREVER_SAFE_GUARD_DURATION);
        });
        
        
        /*
         * 由自己继续使用
         */
        int firstMachineId = distributor.distribute(namespace, TEST_MACHINE_BIT, firstInstanceId, MachineIdDistributor.FOREVER_SAFE_GUARD_DURATION).getMachineId();
        assertThat(firstMachineId, equalTo(0));
    }
}
