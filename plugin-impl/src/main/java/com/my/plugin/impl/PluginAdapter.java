package com.my.plugin.impl;

import com.my.plugin.Plugin;

public class PluginAdapter implements Plugin {
    @Override
    public Integer perform(Integer param1, Integer param2) {
        return param1 + param2;
    }
}