package com.heima.freemarker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class FreeMarkerApp {
    public static void main(String[] args) {
        SpringApplication.run(FreeMarkerApp.class);
    }

}
