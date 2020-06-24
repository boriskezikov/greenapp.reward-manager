package com.reward.manager.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.reward.manager.exception.AuthenticationError.UNAUTHORIZED;
import static java.util.Objects.isNull;

@Component
public class RequestFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(RequestFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        var req = (HttpServletRequest) request;

        if (filter(req)) {
            var httpResponse = (HttpServletResponse) response;
            httpResponse.setContentType("application/json");
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Required headers not specified in the request or incorrect");
            throw UNAUTHORIZED.exception();
        }
        chain.doFilter(request, response);
    }

    private boolean filter(HttpServletRequest request) {
        var authHeader = request.getHeader("X-GREEN-APP-ID");
        var accountHeader = request.getHeader("X-GREEN-APP-INITIATOR");
        var getAccountReq = request.getRequestURI().contains("/reward-manager/account/") && request.getMethod().equals("GET");
        var accrualOrWriteoffReq = request.getRequestURI().contains("/reward-manager/accrual/") ||
            request.getRequestURI().contains("/reward-manager/writeoff/");

        return isNull(authHeader) || !Objects.equals(authHeader, "GREEN") ||
            ((isNull(accountHeader) || !Objects.equals(accountHeader, "TASK-RESOLVER")) && getAccountReq) ||
            ((isNull(accountHeader) || !Objects.equals(accountHeader, "SHOP-CATALOG")) && accrualOrWriteoffReq);

    }

    @Override
    public void init(FilterConfig filterConfig) {
        LOG.warn("Auth filter initialization");
    }
}