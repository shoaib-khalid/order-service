package com.kalsym.order.service;

import com.kalsym.order.service.utility.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import com.kalsym.order.service.model.repository.CustomRepositoryImpl;

/**
 *
 * @author 7cu
 */
@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories(repositoryBaseClass = CustomRepositoryImpl.class)

public class OrderServiceApplication implements CommandLineRunner {

    static {
        System.setProperty("spring.jpa.hibernate.naming.physical-strategy", "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");
        /**
         * To escape SQL reserved keywords
         */
        System.setProperty("hibernate.globally_quoted_identifiers", "true");
    }

    public static String VERSION;
    public static String ASSETURL ;
    
    @Autowired
    private Environment env;

    public static void main(String... args) {
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, "main", "Staring order-service...");
        SpringApplication.run(OrderServiceApplication.class, args);
    }

    @Value("${build.version:not-known}")
    String version;
    
    @Value("${asset.service.URL:https://assets.symplified.it}")
    String assetServiceUrl ;

    @Bean
    CommandLineRunner lookup(ApplicationContext context) {
        return args -> {
            VERSION = version;
            ASSETURL = assetServiceUrl;
            
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, "lookup", "[v{}][{}] {}", VERSION, "", "\n"
                    + "               _                                      _          \n"
                    + "              | |                                    (_)         \n"
                    + "  ___  _ __ __| | ___ _ __ ______ ___  ___ _ ____   ___  ___ ___ \n"
                    + " / _ \\| '__/ _` |/ _ \\ '__|______/ __|/ _ \\ '__\\ \\ / / |/ __/ _ \\\n"
                    + "| (_) | | | (_| |  __/ |         \\__ \\  __/ |   \\ V /| | (_|  __/\n"
                    + " \\___/|_|  \\__,_|\\___|_|         |___/\\___|_|    \\_/ |_|\\___\\___|\n"
                    + "                                                                 \n"
                    + " :: com.kalsym ::              (v" + VERSION + ")");
        };
    }

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @Override
    public void run(String... args) throws Exception {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
