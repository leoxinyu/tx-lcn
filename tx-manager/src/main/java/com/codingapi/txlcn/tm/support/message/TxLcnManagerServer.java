/*
 * Copyright 2017-2019 CodingApi .
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codingapi.txlcn.tm.support.message;

import com.codingapi.txlcn.commons.runner.TxLcnInitializer;
import com.codingapi.txlcn.commons.runner.TxlcnRunnerOrders;
import com.codingapi.txlcn.spi.message.RpcConfig;
import com.codingapi.txlcn.tm.config.TxManagerConfig;
import com.codingapi.txlcn.spi.message.RpcServerInitializer;
import com.codingapi.txlcn.spi.message.dto.ManagerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description:
 * Company: CodingApi
 * Date: 2018/11/29
 *
 * @author lorne
 */
@Component
public class TxLcnManagerServer implements TxLcnInitializer {

    private final TxManagerConfig txManagerConfig;

    private final RpcServerInitializer rpcServerInitializer;

    private final RpcConfig rpcConfig;

    @Autowired
    public TxLcnManagerServer(TxManagerConfig txManagerConfig, RpcServerInitializer rpcServerInitializer, RpcConfig rpcConfig) {
        this.txManagerConfig = txManagerConfig;
        this.rpcServerInitializer = rpcServerInitializer;
        this.rpcConfig = rpcConfig;
    }

    @Override
    public void init() {
        // 1. 配置
        if (rpcConfig.getWaitTime() == -1) {
            rpcConfig.setWaitTime(5000);
        }

        // 2. 初始化RPC Server
        ManagerProperties managerProperties = new ManagerProperties();
        managerProperties.setCheckTime(txManagerConfig.getHeartTime());
        managerProperties.setRpcPort(txManagerConfig.getPort());
        rpcServerInitializer.init(managerProperties);
    }

    @Override
    public int order() {
        return TxlcnRunnerOrders.MAX;
    }
}
