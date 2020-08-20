package io.xream.sqli.api;

import io.xream.sqli.core.builder.condition.InCondition;

import java.util.List;

/**
 * @Author Sim
 */
public interface QueryForCache {
    <T> List<T> in(InCondition inCondition);
}
