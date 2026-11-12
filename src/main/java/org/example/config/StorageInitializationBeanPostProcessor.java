package org.example.config;

import org.example.model.Trainee;
import org.example.model.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class StorageInitializationBeanPostProcessor implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(StorageInitializationBeanPostProcessor.class);
    private static final String TRAINER_STORAGE = "trainerStorage";
    private static final String TRAINEE_STORAGE = "traineeStorage";

    @Value("${data.file.path}")
    private String dataFilePath;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (TRAINER_STORAGE.equals(beanName)) {
            initializeTrainers(bean);
        } else if (TRAINEE_STORAGE.equals(beanName)) {
            initializeTrainees(bean);
        }

        return bean;
    }

    private void initializeTrainers(Object bean) {
        Map<Long, Trainer> storage = storage(bean, TRAINER_STORAGE);
        readRows().forEach(columns -> {
            if ("TRAINER".equalsIgnoreCase(columns.get(0))) {
                Trainer trainer = new Trainer(
                        columns.get(2),
                        columns.get(3),
                        Boolean.parseBoolean(columns.get(4)),
                        columns.get(5));
                trainer.setUserId(Long.valueOf(columns.get(1)));
                setCredentials(trainer.getUser(), columns);
                storage.put(trainer.getUserId(), trainer);
            }
        });
        logger.info("Initialized {} trainers from {}", storage.size(), dataFilePath);
    }

    private void initializeTrainees(Object bean) {
        Map<Long, Trainee> storage = storage(bean, TRAINEE_STORAGE);
        readRows().forEach(columns -> {
            if ("TRAINEE".equalsIgnoreCase(columns.get(0))) {
                Trainee trainee = new Trainee(
                        columns.get(2),
                        columns.get(3),
                        Boolean.parseBoolean(columns.get(4)),
                        LocalDate.parse(columns.get(6)),
                        columns.get(7));
                trainee.setUserId(Long.valueOf(columns.get(1)));
                setCredentials(trainee.getUser(), columns);
                storage.put(trainee.getUserId(), trainee);
            }
        });
        logger.info("Initialized {} trainees from {}", storage.size(), dataFilePath);
    }

    @SuppressWarnings("unchecked")
    private <T> Map<Long, T> storage(Object bean, String beanName) {
        if (!(bean instanceof Map<?, ?>)) {
            throw new BeanInitializationException(
                    "Bean '" + beanName + "' must be a Map but was " + bean.getClass().getName());
        }
        return (Map<Long, T>) bean;
    }

    private List<List<String>> readRows() {
        try (var lines = Files.lines(Path.of(dataFilePath))) {
            return lines.map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .skip(1)
                    .map(this::parseCsvLine)
                    .peek(this::validateRow)
                    .toList();
        } catch (IOException | RuntimeException exception) {
            logger.error("Could not initialize storage from {}", dataFilePath, exception);
            throw new BeanInitializationException(
                    "Could not initialize storage from " + dataFilePath, exception);
        }
    }

    private List<String> parseCsvLine(String line) {
        var columns = new java.util.ArrayList<String>();
        var value = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (character == '"') {
                quoted = !quoted;
            } else if (character == ',' && !quoted) {
                columns.add(value.toString().trim());
                value.setLength(0);
            } else {
                value.append(character);
            }
        }
        if (quoted) {
            throw new IllegalArgumentException("Unclosed quoted CSV field: " + line);
        }
        columns.add(value.toString().trim());
        return columns;
    }

    private void validateRow(List<String> columns) {
        if (columns.size() < 10
                || (!"TRAINER".equalsIgnoreCase(columns.get(0))
                && !"TRAINEE".equalsIgnoreCase(columns.get(0)))) {
            throw new IllegalArgumentException(
                    "Each data row must contain a TRAINER or TRAINEE record and 10 columns");
        }
    }

    private void setCredentials(org.example.model.User user, List<String> columns) {
        user.setUserName(columns.get(8));
        user.setPassword(columns.get(9));
    }
}