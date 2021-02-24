package com.kalsym.order.service.filter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
//    @Autowired
//    private MySQLUserDetailsService jwtUserDetailsService;
//
//    @Autowired
//    AdministratorSessionsRepository administratorSessionsRepository;
//
//    @Autowired
//    ClientSessionsRepository clientSessionsRepository;
//
//    @Autowired
//    CustomerSessionsRepository customerSessionsRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
//
//        String logprefix = request.getRequestURI() + " ";
//
//        logger.warn(Logger.pattern, OrderServiceApplication.VERSION, "-------------" + logprefix + "-------------", "", "");
//
//        final String requestTokenHeader = request.getHeader("Authorization");
//
//        String sessionId = null;
//
//        // Token is in the form "Bearer token". Remove Bearer word and get only the Token
//        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
//            sessionId = requestTokenHeader.substring(7);
//        } else {
//            logger.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "token does not begin with Bearer String", "");
//        }
//
//        if (sessionId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//            //logger.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "sessionId: " + sessionId, "");
//            Optional<AdministratorSession> optAdminSession = administratorSessionsRepository.findById(sessionId);
//            Optional<ClientSession> optClientSession = clientSessionsRepository.findById(sessionId);
//            Optional<CustomerSession> optCustomerSession = customerSessionsRepository.findById(sessionId);
//
//            Date expiryTime = null;
//
//            String username = null;
//            if (!optAdminSession.isPresent()
//                    && !optClientSession.isPresent()
//                    && !optCustomerSession.isPresent()) {
//                logger.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "sessionId not valid", "");
//
//            } else if (optAdminSession.isPresent()) {
//                logger.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "sessionId valid for admin_session", "");
//                //Session session = optSession.get();
//
//                AdministratorSession admin = optAdminSession.get();
//
//                expiryTime = optAdminSession.get().getExpiry();
//
//                username = optAdminSession.get().getUsername();
//
//            } else if (optClientSession.isPresent()) {
//
//            } else if (optCustomerSession.isPresent()) {
//
//            }
//
//            if (null != expiryTime && null != username) {
//                long diff = 0;
//                try {
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                    Date currentTime = sdf.parse(DateTimeUtil.currentTimestamp());
//                    diff = expiryTime.getTime() - currentTime.getTime();
//                } catch (ParseException e) {
//                    logger.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "error calculating time to session expiry", "");
//                }
//                logger.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "time to session expiry: " + diff + "ms", "");
//                if (0 < diff) {
//                    MySQLUserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(username);
//
//                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
//                            userDetails, null, userDetails.getAuthorities());
//                    usernamePasswordAuthenticationToken
//                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//
//                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
//                } else {
//                    logger.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "session expired", "");
//                }
//            }
//        }
//        chain.doFilter(request, response);
    }

}
