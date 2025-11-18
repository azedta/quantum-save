package com.quantumsave.quantum_save;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class QuantumSaveApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuantumSaveApplication.class, args);
	}

}
