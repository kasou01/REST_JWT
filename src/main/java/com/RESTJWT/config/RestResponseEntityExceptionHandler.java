package com.RESTJWT.config;

import com.RESTJWT.service.CordaRPCConnectionService;
import net.corda.client.rpc.RPCException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.security.Principal;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    @Autowired
    CordaRPCConnectionService cordaRPCConnectionService;
    @ExceptionHandler(value = { RPCException.class})
    protected ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest request) {
        cordaRPCConnectionService.closeConnection();
        String bodyOfResponse = "Corda Node 接続エラー;poolsize = " + cordaRPCConnectionService.getPoolSize();
        String header = request.getHeader("Content-Type");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type",header);
        return handleExceptionInternal(ex, bodyOfResponse,httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
