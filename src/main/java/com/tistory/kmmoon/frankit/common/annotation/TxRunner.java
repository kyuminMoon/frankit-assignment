package com.tistory.kmmoon.frankit.common.annotation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Component
public class TxRunner {
    @Transactional
    public <T> T runTx(Supplier<T> block) {
        return block.get();
    }

    @ReadOnlyTransactional
    public <T> T runReadOnly(Supplier<T> block) {
        return block.get();
    }
}
