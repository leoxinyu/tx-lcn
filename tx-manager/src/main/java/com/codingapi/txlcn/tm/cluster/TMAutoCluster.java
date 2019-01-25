package com.codingapi.txlcn.tm.cluster;

import com.codingapi.txlcn.commons.runner.TxLcnInitializer;
import com.codingapi.txlcn.commons.runner.TxlcnRunnerOrders;
import com.codingapi.txlcn.commons.util.ApplicationInformation;
import com.codingapi.txlcn.spi.message.params.NotifyConnectParams;
import com.codingapi.txlcn.tm.config.TxManagerConfig;
import com.codingapi.txlcn.tm.core.storage.FastStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;
import java.util.List;

/**
 * Description:
 * Date: 1/24/19
 *
 * @author codingapi
 */
@Component
@Slf4j
public class TMAutoCluster implements TxLcnInitializer {

    private final FastStorage fastStorage;

    private final RestTemplate restTemplate;

    private final TxManagerConfig txManagerConfig;

    private static final String MANAGER_REFRESH_URL = "http://%s:%s/manager/refresh";

    private final ServerProperties serverProperties;

    @Autowired
    public TMAutoCluster(FastStorage fastStorage, RestTemplate restTemplate, TxManagerConfig txManagerConfig,
                         ServerProperties serverProperties) {
        this.fastStorage = fastStorage;
        this.restTemplate = restTemplate;
        this.txManagerConfig = txManagerConfig;
        this.serverProperties = serverProperties;
    }

    @Override
    public void init() throws Exception {

        // 1. 通知 TC 建立连接
        List<TMProperties> tmList = fastStorage.findTMProperties();
        for (TMProperties properties : tmList) {
            NotifyConnectParams notifyConnectParams = new NotifyConnectParams();
            notifyConnectParams.setHost(txManagerConfig.getHost());
            notifyConnectParams.setPort(txManagerConfig.getPort());
            String url = String.format(MANAGER_REFRESH_URL, properties.getHost(), properties.getHttpPort());
            try {
                ResponseEntity<Boolean> res = restTemplate.postForEntity(url, notifyConnectParams, Boolean.class);
                if (res.getStatusCode().equals(HttpStatus.OK) || res.getStatusCode().is5xxServerError()) {
                    log.info("manager auto refresh res->{}", res);
                    break;
                } else {
                    fastStorage.removeTMProperties(properties.getHost(), properties.getTransactionPort());
                }
            } catch (Exception e) {
                log.error("manager auto refresh error ");
                //check exception then remove.
                if (e instanceof ResourceAccessException) {
                    ResourceAccessException resourceAccessException = (ResourceAccessException) e;
                    if (resourceAccessException.getCause() != null && resourceAccessException.getCause() instanceof ConnectException) {
                        //can't access .
                        fastStorage.removeTMProperties(properties.getHost(), properties.getTransactionPort());
                    }
                }
            }
        }

        // 2. 保存TM 到快速存储
        TMProperties tmProperties = new TMProperties();
        tmProperties.setHttpPort(ApplicationInformation.serverPort(serverProperties));
        tmProperties.setHost(txManagerConfig.getHost());
        tmProperties.setTransactionPort(txManagerConfig.getPort());
        fastStorage.saveTMProperties(tmProperties);
    }

    @Override
    public int order() {
        return TxlcnRunnerOrders.MIN;
    }
}
