package com.ristory.sentry;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
public class LogInterceptor implements HandlerInterceptor {

    public static final String REQUEST_ID       = "request_id";
    public static final String HOST_IP          = "host_ip";
    public static final String HOST_PORT        = "host_port";
    public static final String CLIENT_IP        = "client_ip";
    public static final String RS_FUNDS_PHONE   = "rs_funds_phone";
    public static final String RS_ADMIN_USER    = "rs_admin_user";
    public static final String URL              = "url";
    public static final String VERB             = "verb";
    public static final String PARAMS           = "params";

    public static final String RS_ADMIN         = "rs-admin";
    public static final String RS_FUNDS         = "rs-funds";
    public static final String RS_JOB           = "rs-job";

    @Value("${spring.application.name}")
    public String appName;

    protected void putIntoMDC(HttpServletRequest httpServletRequest,String requestID) {
        ThreadContext.put(this.REQUEST_ID, requestID);
        ThreadContext.put(this.HOST_IP, httpServletRequest.getServerName());
        ThreadContext.put(this.HOST_PORT, String.valueOf(httpServletRequest.getServerPort()));
        ThreadContext.put(this.CLIENT_IP, httpServletRequest.getRemoteAddr());
        if(RS_FUNDS.equals(appName)){
            ThreadContext.put(this.RS_FUNDS_PHONE,(String)httpServletRequest.getSession().getAttribute("phone"));
        }
        if(RS_ADMIN.equals(appName)){
            ThreadContext.put(this.RS_ADMIN_USER,(String)httpServletRequest.getSession().getAttribute("phone"));
        }
        ThreadContext.put(this.URL,httpServletRequest.getRequestURI());
        ThreadContext.put(this.VERB,httpServletRequest.getMethod());
        ThreadContext.put(this.PARAMS,JSON.toJSONString(httpServletRequest.getParameterMap()));
    }

    protected void removeFromMDC() {
        ThreadContext.remove(this.REQUEST_ID);
        ThreadContext.remove(this.HOST_IP);
        ThreadContext.remove(this.HOST_PORT);
        ThreadContext.remove(this.CLIENT_IP);
        if(RS_FUNDS.equals(appName)){
            ThreadContext.remove(this.RS_FUNDS_PHONE);
        }
        ThreadContext.remove(this.URL);
        ThreadContext.remove(this.VERB);
        ThreadContext.remove(this.PARAMS);
    }

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String requestID = httpServletRequest.getHeader("X-RS-Request-ID");
        if(StringUtils.isEmpty(requestID)){
            requestID = UUID.randomUUID().toString();
        }
        httpServletResponse.setHeader("X-RS-Request-ID",requestID);
        putIntoMDC(httpServletRequest,requestID);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        removeFromMDC();
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
