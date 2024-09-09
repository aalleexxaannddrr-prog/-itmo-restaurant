package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

@SpringBootApplication
public class ItmoRestaurantApplication {

	private static final Logger logger = LoggerFactory.getLogger(ItmoRestaurantApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ItmoRestaurantApplication.class, args);

		// Логирование пути к текущему классу
		try {
			String classPath = Paths.get(ItmoRestaurantApplication.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
			logger.info("Current class path: " + classPath);
		} catch (URISyntaxException e) {
			logger.error("Failed to get class path", e);
		}

		// Логирование пути к папке resources
		File resourceFolder = new File("src/main/resources");
		logger.info("Resources folder path: " + resourceFolder.getAbsolutePath());
	}
}
