package com.RESTJWT.service;

import net.corda.client.rpc.*;
import net.corda.core.utilities.NetworkHostAndPort;


import java.util.concurrent.Callable;

/**
 * 非同期でcorda RPC connection を作成
 */
public class CordaConnectionTask implements Callable<CordaRPCConnection> {
    // The host of the node we are connecting to.
    private String host;
    // The RPC port of the node we are connecting to.
    private String username;
    // The username for logging into the RPC client.
    private String password;
    // The password for logging into the RPC client.
    private int rpcPort;

    private CordaRPCClientConfiguration config = new CordaRPCClientConfiguration();

    public CordaConnectionTask(String host, String username, String password, int rpcPort,CordaRPCClientConfiguration config) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.rpcPort = rpcPort;
        this.config = config;
    }

    public CordaConnectionTask(String host, String username, String password, int rpcPort) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.rpcPort = rpcPort;
    }

    @Override
    public CordaRPCConnection call() throws RPCException {
        try{
            NetworkHostAndPort rpcAddress = new NetworkHostAndPort(host, rpcPort);
            CordaRPCClient rpcClient = new CordaRPCClient(rpcAddress,config);
            CordaRPCConnection connection = rpcClient.start(username, password);
            return connection;
        }catch (Exception e){
            throw new RPCException("Corda rpc connection error");
        }
    }
}
