package org.example;

import org.example.facade.GymFacade;
import org.example.model.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan(basePackages = "org.example")
@PropertySource("classpath:application.properties")
public class GymApplication {

    private static final Logger logger = LoggerFactory.getLogger(GymApplication.class);

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(GymApplication.class);

        GymFacade gymFacade = context.getBean(GymFacade.class);

        Trainer newTrainer = new Trainer("Oleksii", "Hryshko", true, "Java Development");
        Trainer savedTrainer = gymFacade.createTrainer(newTrainer);

        logger.info("Created Trainer in Main: Username={}", savedTrainer.getUserName());

        context.close();
    }
}
