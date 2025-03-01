package com.tistory.kmmoon.frankit.common.annotation;

import java.util.function.Supplier;

public class Tx {
    private static TxRunner txRunner;

    public static void initialize(TxRunner runner) {
        txRunner = runner;
    }

    public static <T> T masterTx(Supplier<T> function) {
        return txRunner.runTx(function);
    }

    public static <T> T readOnlyTx(Supplier<T> function) {
        return txRunner.runReadOnly(function);
    }
}
