package com.kalsym.order.service.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalsym.order.service.model.Auth;
import com.kalsym.order.service.utility.HttpResponse;
import com.kalsym.order.service.model.MySQLUserDetails;
import com.kalsym.order.service.service.MySQLUserDetailsService;
//import com.kalsym.order.service.utility.Logger;
import com.kalsym.order.service.utility.DateTimeUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.IOException;
import java.util.Date;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 *
 * @author Sarosh
 */
@Component
public class SessionRequestFilter extends OncePerRequestFilter {

    private static Logger logger = LoggerFactory.getLogger("application");
    @Autowired
    private MySQLUserDetailsService jwtUserDetailsService;

    @Autowired
    RestTemplate restTemplate;

    @Value("${services.user-service.session_details:not-known}")
    String userServiceSessionDetailsUrl;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String logprefix = request.getRequestURI() + " ";

        logger.info("-------------" + logprefix + "-------------", "", "");

        final String authHeader = request.getHeader("Authorization");
        logger.warn(logprefix, "Authorization: " + authHeader, "");

        String accessToken = null;

        // Token is in the form "Bearer token". Remove Bearer word and get only the Token
        if (null != authHeader && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.replace("Bearer ", "");
            logger.info(logprefix, "token: " + accessToken, "");
            logger.info(logprefix, "token length: " + accessToken.length(), "");

        } else {
            logger.warn(logprefix, "token does not begin with Bearer String", "");
        }

        if (accessToken != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            //logger.info(logprefix, "sessionId: " + sessionId, "");
            ResponseEntity<HttpResponse> authResponse = restTemplate.postForEntity(userServiceSessionDetailsUrl, accessToken, HttpResponse.class);

            Date expiryTime = null;

            Auth auth = null;
            String username = null;

            if (authResponse.getStatusCode() == HttpStatus.ACCEPTED) {
                ObjectMapper mapper = new ObjectMapper();
                logger.warn(logprefix, "data: " + authResponse.getBody().getData(), "");

                auth = mapper.convertValue(authResponse.getBody().getData(), Auth.class);
                username = auth.getSession().getUsername();
                expiryTime = auth.getSession().getExpiry();
            }

            if (null != expiryTime && null != username) {
                long diff = 0;
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date currentTime = sdf.parse(DateTimeUtil.currentTimestamp());
                    diff = expiryTime.getTime() - currentTime.getTime();
                } catch (ParseException e) {
                    logger.warn(logprefix, "error calculating time to session expiry", "");
                }
                logger.info(logprefix, "time to session expiry: " + diff + "ms", "");
                if (0 < diff) {
                    MySQLUserDetails userDetails = new MySQLUserDetails(auth, auth.getAuthorities());

                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                } else {
                    logger.warn(logprefix, "session expired", "");
                    //response.setStatus(HttpStatus.UNAUTHORIZED);
                    response.getWriter().append("Session expired");
                }
            }
        }
        chain.doFilter(request, response);
    }
}
