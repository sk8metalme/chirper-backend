package com.chirper.infrastructure.config;

import com.chirper.domain.repository.IFollowRepository;
import com.chirper.domain.repository.ITweetRepository;
import com.chirper.domain.service.AuthenticationService;
import com.chirper.domain.service.FollowService;
import com.chirper.domain.service.TimelineService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DomainServiceConfig
 * Domain層のサービスをSpring Beanとして定義する設定クラス
 * Domain層はSpring依存を持たないため、Infrastructure層で@Beanとして登録
 */
@Configuration
public class DomainServiceConfig {

    @Bean
    public AuthenticationService authenticationService(
        @Value("${jwt.secret}") String jwtSecret,
        @Value("${jwt.expiration-seconds:3600}") long jwtExpirationSeconds
    ) {
        return new AuthenticationService(jwtSecret, jwtExpirationSeconds);
    }

    @Bean
    public TimelineService timelineService(ITweetRepository tweetRepository) {
        return new TimelineService(tweetRepository);
    }

    @Bean
    public FollowService followService(IFollowRepository followRepository) {
        return new FollowService(followRepository);
    }
}
