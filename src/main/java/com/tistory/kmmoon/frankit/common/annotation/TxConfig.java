package com.tistory.kmmoon.frankit.common.annotation;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TxConfig {
    @Bean("txInitBean")
    public InitializingBean txInitialize(TxRunner txRunner) {
        return () -> Tx.initialize(txRunner);
    }
}
