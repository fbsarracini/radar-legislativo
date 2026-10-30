package br.com.radarlegislativo.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PARLAMENTARES = "parlamentares";
    public static final String PROPOSICOES = "proposicoes";

    /** As APIs públicas são lentas e têm limite de uso: cache de 6h evita martelar o servidor. */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(PARLAMENTARES, PROPOSICOES);
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofHours(6))
                .maximumSize(20_000));
        return manager;
    }
}
