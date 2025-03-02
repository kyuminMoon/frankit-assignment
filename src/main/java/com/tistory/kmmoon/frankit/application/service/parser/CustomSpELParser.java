package com.tistory.kmmoon.frankit.application.service.parser;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class CustomSpELParser {

    private CustomSpELParser() {
    }

    public static Object getDynamicValue(final String[] parameterNames, final Object[] args, final String key) {
        final ExpressionParser expressionParser = new SpelExpressionParser();
        final StandardEvaluationContext standardEvaluationContext = new StandardEvaluationContext();

        for (int i = 0; i < parameterNames.length; i++) {
            standardEvaluationContext.setVariable(parameterNames[i], args[i]);
        }

        return expressionParser.parseExpression(key).getValue(standardEvaluationContext, Object.class);
    }
}
