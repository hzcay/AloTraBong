package com.example.Alotrabong.controller;

import com.example.Alotrabong.dto.VndFormatter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThymeleafMoneyConfig {

    @Bean(name = "vnd")
    public VndFormatter vndFormatter() {
        return new VndFormatter();
    }
}
