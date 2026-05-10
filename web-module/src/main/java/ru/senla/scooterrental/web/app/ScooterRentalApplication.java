package ru.senla.scooterrental.web.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = "ru.senla.scooterrental")
@EntityScan(basePackages = "ru.senla.scooterrental")
public class ScooterRentalApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScooterRentalApplication.class, args);
    }
}
