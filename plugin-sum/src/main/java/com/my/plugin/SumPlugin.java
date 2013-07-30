package com.my.plugin;

public class SumPlugin implements Plugin {
    @Override
    public Integer perform(Integer param1, Integer param2) {
        return param1 + param2;
    }
}