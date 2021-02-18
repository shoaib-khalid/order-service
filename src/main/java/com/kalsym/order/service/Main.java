package com.kalsym.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

/**
 *
 * @author 7cu
 */
@SpringBootApplication
public class Main implements CommandLineRunner {

    private static Logger logger = LoggerFactory.getLogger("application");

    public static String VERSION;
    @Autowired
    private Environment env;

    public static void main(String... args){
        SpringApplication.run(Main.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
