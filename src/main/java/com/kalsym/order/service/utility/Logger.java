package com.kalsym.order.service.utility;

import org.slf4j.LoggerFactory;

/**
 *
 * @author Sarosh
 */
public class Logger {

    public static final org.slf4j.Logger application = LoggerFactory.getLogger("application");
    
    
     public static final org.slf4j.Logger cdr = LoggerFactory.getLogger("cdr");

    public static String pattern = "[v{}][{}] {}";
}
