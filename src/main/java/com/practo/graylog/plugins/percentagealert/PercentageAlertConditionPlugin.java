package com.practo.graylog.plugins.percentagealert;

import org.graylog2.plugin.Plugin;
import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.PluginModule;

import java.util.Collection;
import java.util.Collections;

public class PercentageAlertConditionPlugin implements Plugin {
    @Override
    public PluginMetaData metadata() {
        return new PercentageAlertConditionMetaData();
    }

    @Override
    public Collection<PluginModule> modules () {
        return Collections.<PluginModule>singletonList(new PercentageAlertConditionModule());
    }
}
