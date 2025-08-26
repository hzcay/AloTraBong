package com.alotrabong.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.alotrabong")
public class AlotrabongApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlotrabongApplication.class, args);
	}

}
