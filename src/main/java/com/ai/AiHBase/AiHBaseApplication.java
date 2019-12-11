package com.ai.AiHBase;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.ai.controller","com.ai.service"})
@MapperScan(basePackages = {"com.ai.dao"})
public class AiHBaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiHBaseApplication.class, args);
	}

}
