package com.rf.AIquantum;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;

/**
 * @author zzf
 */
@EnableJpaRepositories(basePackages = {"com.rf.AIquantum"})
@SpringBootApplication(scanBasePackages = {"com.rf.AIquantum"})
@EnableJpaAuditing
@EnableTransactionManagement
@EnableScheduling
@Slf4j
public class AIquantumApplication {

    static Logger logger = LoggerFactory.getLogger(AIquantumApplication.class);
    @Autowired
    static Environment environment;
    @Value("${spring.profiles.active}")
    static String profile;


    public static void main(String[] args) {

        SpringApplication.run(AIquantumApplication.class, args);

    }


    /**
     * 让Spring管理JPAQueryFactory
     *
     * @param entityManager
     * @return
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }


}
