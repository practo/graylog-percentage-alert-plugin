package com.practo.graylog.plugins.percentagealert;

import java.util.Collections;
import java.util.Set;

import org.graylog2.plugin.PluginConfigBean;
import org.graylog2.plugin.PluginModule;

public class PercentageAlertConditionModule extends PluginModule {

   @Override
   public Set<? extends PluginConfigBean> getConfigBeans() {
      return Collections.emptySet();
   }

   @Override
   protected void configure() {
      addAlertCondition(PercentageAlertCondition.class.getCanonicalName(), PercentageAlertCondition.class,
            PercentageAlertCondition.Factory.class);
   }

}
