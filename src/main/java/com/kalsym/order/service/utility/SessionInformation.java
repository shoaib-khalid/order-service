/*
 * Copyright (C) 2021 taufik
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.kalsym.order.service.utility;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.model.MySQLUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *
 * @author taufik
 */
public class SessionInformation {
    
    public static MySQLUserDetails getSessionInfo(String logprefix) {
        UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken)SecurityContextHolder.getContext().getAuthentication();
        MySQLUserDetails mysqlUserDetails = (MySQLUserDetails)userDetails.getPrincipal();
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Token userRole:" + mysqlUserDetails.getRole()+" ownerId:"+mysqlUserDetails.getOwnerId()+" isSuperUser:"+mysqlUserDetails.getIsSuperUser(), "");
        return   mysqlUserDetails;
    }
}
