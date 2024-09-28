package com.example.itmorestaurant;

import com.example.itmorestaurant.entities.RestaurantTable;
import com.example.itmorestaurant.entities.User;
import com.example.itmorestaurant.enums.Role;

import com.example.itmorestaurant.repository.RestaurantTableRepository;
import com.example.itmorestaurant.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Calendar;

@SpringBootApplication
public class ItmoRestaurantApplication implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(ItmoRestaurantApplication.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RestaurantTableRepository tableRepository;  // Репозиторий для работы с столами

	public static void main(String[] args) {
		SpringApplication.run(ItmoRestaurantApplication.class, args);

		try {
			String classPath = Paths.get(ItmoRestaurantApplication.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
			logger.info("Current class path: " + classPath);
		} catch (URISyntaxException e) {
			logger.error("Failed to get class path", e);
		}

		File resourceFolder = new File("src/main/resources");
		logger.info("Resources folder path: " + resourceFolder.getAbsolutePath());
	}

	@Override
	public void run(String... args) throws Exception {
		// Добавляем пользователя при запуске приложения
		if (!userRepository.findByEmail("kichmarev@list.ru").isPresent()) {
			User user = new User();
			user.setFirstname("Александр");
			user.setLastname("Кичмарев");
			user.setEmail("kichmarev@list.ru");
			Calendar calendar = Calendar.getInstance();
			calendar.set(1995, Calendar.JANUARY, 1);
			user.setDateOfBirth(calendar.getTime());
			user.setRole(Role.ADMIN);
			user.setPassword(new BCryptPasswordEncoder().encode("1"));



			User user1 = new User();
			user1.setFirstname("Александр");
			user1.setLastname("Кичмарев");
			user1.setEmail("kichmarev@list1.ru");
			user1.setDateOfBirth(calendar.getTime());
			user1.setRole(Role.USER);
			user1.setPassword(new BCryptPasswordEncoder().encode("1"));
			userRepository.save(user);
			userRepository.save(user1);
			logger.info("User kichmarev@list.ru created");
		} else {
			logger.info("User kichmarev@list.ru already exists");
		}

		// Добавляем несколько столов
		if (tableRepository.count() == 0) {
			RestaurantTable table1 = RestaurantTable.builder()
					.number(1)           // Номер стола
					.capacity(4)         // Вместимость стола
					.isAvailable(true)   // Статус доступности
					.build();
			RestaurantTable table2 = RestaurantTable.builder()
					.number(2)           // Номер стола
					.capacity(3)         // Вместимость стола
					.isAvailable(true)   // Статус доступности
					.build();
			RestaurantTable table3 = RestaurantTable.builder()
					.number(3)           // Номер стола
					.capacity(4)         // Вместимость стола
					.isAvailable(true)   // Статус доступности
					.build();

			tableRepository.save(table1);
			tableRepository.save(table2);
			tableRepository.save(table3);

			logger.info("Sample tables created");
		} else {
			logger.info("Tables already exist");
		}
	}
}
