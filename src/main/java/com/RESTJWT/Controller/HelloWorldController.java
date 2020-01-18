package com.RESTJWT.Controller;

import com.RESTJWT.model.TradeResource;
import com.RESTJWT.service.NodeRPCConnection;
import com.google.common.collect.ImmutableList;
import com.template.flows.InsertTradeStateFlow;
import com.template.states.TradeState;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.StateMachineRunId;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.messaging.FlowHandle;
import net.corda.core.node.services.Vault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class HelloWorldController {
    @Autowired
    NodeRPCConnection connection;
    @RequestMapping({ "/hello" })
    public String firstPage() {
        return "Hello World";
    }
    @RequestMapping({ "/node" })
    public String node() {
        CordaRPCOps proxy = connection.proxy;
        Instant instant = proxy.currentNodeTime();
        TradeState s = new TradeState(null,
                null,
                System.currentTimeMillis(),
                "A",
                "B",
                "ISSUE",
                new Amount(100,"abc"),
                "1"
        );
        TradeState s1 = new TradeState(null,
                null,
                System.currentTimeMillis(),
                "A",
                "B",
                "ISSUE",
                new Amount(100,"abc"),
                "1"
        );
        proxy.startFlowDynamic(InsertTradeStateFlow.class, ImmutableList.of(s,s1));
        return instant.toString();
    }
    @RequestMapping({ "/get" })
    public List<TradeResource> getTrade() {
        CordaRPCOps proxy = connection.proxy;

        Vault.Page<TradeState> tradeStatePage = proxy.vaultQuery(TradeState.class);
        List<StateAndRef<TradeState>> states = tradeStatePage.getStates();
        List<TradeResource> collect = states.stream().map(m -> TradeResource.toTradeResource(m.getState().getData())).collect(Collectors.toList());

        return collect;
    }

    @PostMapping({ "/insert" })
    public List<TradeResource> insert(@RequestBody List<TradeResource> data) {
        CordaRPCOps proxy = connection.proxy;
        List<TradeState> nodeData = data.stream().map(d -> d.toTradeState()).collect(Collectors.toList());
        FlowHandle<Void> voidFlowHandle = proxy.startFlowDynamic(InsertTradeStateFlow.class, nodeData);
        StateMachineRunId id = voidFlowHandle.getId();
        return data;
    }
}