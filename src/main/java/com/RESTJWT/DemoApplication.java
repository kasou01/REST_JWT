package com.RESTJWT;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication()
public class DemoApplication {

	public static void main(String[] args) {
		ApplicationContext applicationContext =SpringApplication.run(DemoApplication.class, args);
	}

}
