package com.codingapi.txlcn.client.message.transaction;

import com.codingapi.txlcn.client.support.common.DefaultNotifiedUnitService;
import com.codingapi.txlcn.client.support.common.template.TransactionCleanTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:
 * Date: 2018/12/13
 *
 * @author ujued
 */
@Service("rpc_txc_notify-unit")
public class TxcNotifiedUnitService extends DefaultNotifiedUnitService {

    @Autowired
    public TxcNotifiedUnitService(TransactionCleanTemplate transactionCleanTemplate) {
        super(transactionCleanTemplate);
    }
}