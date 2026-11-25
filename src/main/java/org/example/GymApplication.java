package org.example;

import org.example.config.WebConfig;
import org.example.dto.request.TrainerRegistrationRequest;
import org.example.dto.response.RegistrationResponse;
import org.example.facade.GymFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan(
        basePackages = "org.example",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = WebConfig.class
        )
)
@PropertySource("classpath:application.properties")
public class GymApplication {

    private static final Logger logger = LoggerFactory.getLogger(GymApplication.class);

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(GymApplication.class);

        GymFacade gymFacade = context.getBean(GymFacade.class);

        TrainerRegistrationRequest newTrainer = new TrainerRegistrationRequest(
                "Oleksii", "Hryshko", "Java Development");

        RegistrationResponse savedTrainer = gymFacade.createTrainer(newTrainer);

        logger.info("Created Trainer in Main: Username={}", savedTrainer.getUserName());

        context.close();
    }
}