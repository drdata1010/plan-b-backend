package com.planb.supportticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.planb.supportticket"
})
public class SupportTicketApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupportTicketApplication.class, args);
    }
}
