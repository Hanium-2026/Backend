package com.nevo.nevo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class NevoApplication {

	public static void main(String[] args) {
		SpringApplication.run(NevoApplication.class, args);
	}

}
