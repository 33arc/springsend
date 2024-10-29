package com.github.arc33.springsend.config;

import io.etcd.jetcd.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class EtcdBlacklistConfig {
    @Value("${etcd.endpoints}")
    private String etcdEndpoints;

    @Bean
    public Client etcdClient(){
        return Client.builder().endpoints(etcdEndpoints).build();
    }
}