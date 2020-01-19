package com.RESTJWT.service;

import com.RESTJWT.Utils.SystemUtils;
import net.bytebuddy.implementation.bytecode.Throw;
import net.corda.client.rpc.*;
import net.corda.client.rpc.internal.serialization.amqp.RpcClientCordaFutureSerializer;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.utilities.NetworkHostAndPort;
import net.corda.serialization.internal.model.LocalTypeInformation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * Corda RPC connectionサービス.<br>
 *     ConcurrentHashMapでユーザーごとにconnectionを保持する.
 */
@Component
public class CordaRPCConnectionService implements AutoCloseable {
    /**
     * 並列で最大32 threadで接続を実行.<br>
     * すべてのスレッドがアクティブな場合に、追加のタスクが送信されると、<br>
     * それらのタスクはスレッドが使用可能になるまでキューで待機します.
     */
    private ExecutorService executor = Executors.newFixedThreadPool(32);
    // The host of the node we are connecting to.
    @Value("${config.rpc.host}")
    private String host;
    // The RPC port of the node we are connecting to.
    @Value("${config.rpc.username}")
    private String username;
    // The username for logging into the RPC client.
    @Value("${config.rpc.password}")
    private String password;
    // The password for logging into the RPC client.
    @Value("${config.rpc.port}")
    private int rpcPort;

    /**
     * CordaRPCConnection作成のTimeOut時間.
     */
    @Value("${config.rpc.connectionTimeout}")
    private int connectionTimeout;

    @Value("${config.rpc.connectionMaxRetryInterval}")
    private int connectionMaxRetryInterval;
    @Value("${config.rpc.connectionRetryInterval}")
    private int connectionRetryInterval;
    @Value("${config.rpc.maxReconnectAttempts}")
    private int maxReconnectAttempts;
    @Value("${config.rpc.maxFileSize}")
    private int maxFileSize;

    private CordaRPCClientConfiguration defaultConfig = new CordaRPCClientConfiguration();
    private CordaRPCClientConfiguration userConfig;

    /**
     * ユーザーごとにCordaRPCConnectionを保持する.
     */
    private ConcurrentHashMap<String , CordaRPCConnection> connectionPool ;

    /**
     * web serverを起動する時、beanの初期化処理.<br>
     * 接続optionの設定.
     */
    @PostConstruct
    public void initialiseNodeRPCConnection() {
        connectionPool = new ConcurrentHashMap<>(128);
        userConfig = new CordaRPCClientConfiguration(
                Duration.ofMinutes(this.connectionMaxRetryInterval),
                defaultConfig.getMinimumServerProtocolVersion(),
                defaultConfig.getTrackRpcCallSites(),
                defaultConfig.getReapInterval(),
                defaultConfig.getObservationExecutorPoolSize(),
                defaultConfig.getCacheConcurrencyLevel(),
                Duration.ofSeconds(this.connectionRetryInterval),
                defaultConfig.getConnectionRetryIntervalMultiplier(),
                this.maxReconnectAttempts,
                this.maxFileSize,
                defaultConfig.getDeduplicationCacheExpiry()
        );
    }

    /**
     * 特定なユーザーに紐づくFlow起動用proxyを取得
     * @return CordaRPCOps
     */
    public CordaRPCOps getProxy() throws RPCException{
        String loginUserName = SystemUtils.CurrentUserName();
        if(loginUserName == null)
            throw new RPCException("Username is null . Authentication fail!");

        CordaRPCConnection cordaRPCConnection = connectionPool.get(loginUserName);
        if(cordaRPCConnection == null){
            CordaRPCConnection cnn = createCordaRPCConnection();
            connectionPool.put(loginUserName,cnn);
            return cnn.getProxy();
        }else{
            return cordaRPCConnection.getProxy();
        }
    }

    /**
     * 特定なユーザーに紐づくconnectionを作成し、hashtable poolに保存
     */
    public void createConnection() throws RPCException{
        String loginUserName = SystemUtils.CurrentUserName();
        if(loginUserName == null)
            throw new RPCException("Username is null . Authentication fail!");

        CordaRPCConnection cordaRPCConnection = connectionPool.get(loginUserName);
        if(cordaRPCConnection == null){
            CordaRPCConnection cnn = createCordaRPCConnection();
            connectionPool.put(loginUserName,cnn);
        }
    }

    /**
     * 特定なユーザーに紐づくconnectionをクローズ
     */
    public void closeConnection() throws RPCException{
        String loginUserName = SystemUtils.CurrentUserName();
        if(loginUserName == null)
            throw new RPCException("Username is null . Authentication fail!");

        CordaRPCConnection cordaRPCConnection = connectionPool.get(loginUserName);
        if(cordaRPCConnection != null){
            cordaRPCConnection.notifyServerAndClose();
            connectionPool.remove(loginUserName);
        }
    }

    /**
     * 保持しているConnectionをクローズ<br>
     * HashTableを初期化.
     */
    public void clearAll(){
        Collection<CordaRPCConnection> values = connectionPool.values();
        for( CordaRPCConnection cnn : values){
            cnn.notifyServerAndClose();
        }
        connectionPool.clear();
    }

    /**
     * pool利用せず、connection作成.<br>
     * 非同期でCordaRPCConnectionを取得、TimeOut場合RPCExceptionが発生
     * @return CordaRPCConnection
     */
    public CordaRPCConnection createCordaRPCConnection() throws RPCException{
        CordaConnectionTask cordaConnectionTask = new CordaConnectionTask(host, username, password, rpcPort, userConfig);
        Future<CordaRPCConnection> submit = executor.submit(cordaConnectionTask);
        try {
            CordaRPCConnection cordaRPCConnection = submit.get(connectionTimeout, TimeUnit.SECONDS);
            return cordaRPCConnection;
        }catch (Exception e){
            throw new RPCException("Corda rpc connection error",e);
        }
    }


    /**
     * Beanの廃棄処理
     */
    @PreDestroy
    public void close() {
        clearAll();
    }

    public long getPoolSize(){
        return this.connectionPool.size();
    }
    public String currentUserLIst(){
        Enumeration<String> keys = this.connectionPool.keys();
        ArrayList<String> list = new ArrayList<String>();
        while(keys.hasMoreElements()){
            list.add(keys.nextElement());
        }
        return list.stream().collect(Collectors.joining(","));
    }
}