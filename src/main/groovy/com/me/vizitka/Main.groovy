package com.me.vizitka

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
public class Main {
	static void main(String[] args) {
		Data.init()
		SpringApplication.run(Main, args)
	}

}
