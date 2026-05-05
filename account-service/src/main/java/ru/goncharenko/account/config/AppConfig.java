package ru.goncharenko.account.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"ru.goncharenko.bankclient.reactive", "ru.goncharenko.bankclient.common"})
public class AppConfig {
}
