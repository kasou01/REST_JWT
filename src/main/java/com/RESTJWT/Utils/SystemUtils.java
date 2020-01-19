package com.RESTJWT.Utils;

import net.corda.client.rpc.RPCException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SystemUtils {
    public static String CurrentUserName(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()){
            return null;
        }
        Object principal = authentication.getPrincipal();
        String username;
        if(principal instanceof UserDetails){
            username = ((UserDetails)principal).getUsername();
        }else{
            username = principal.toString();
        }
        return username;
    }
}
