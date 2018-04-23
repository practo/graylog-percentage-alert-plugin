package com.practo.graylog.plugins.percentagealert;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.ServerStatus;
import org.graylog2.plugin.Version;

public class PercentageAlertConditionMetaData implements PluginMetaData {
   private static final String PLUGIN_PROPERTIES = "com.praco.graylog.plugins.graylog-plugin-percentage-alert-condition/graylog-plugin.properties";

   @Override
   public String getUniqueId() {
      return PercentageAlertCondition.class.getName();
   }

   @Override
   public String getName() {
      return "Percentage Alert Plugin";
   }

   @Override
   public String getAuthor() {
      return "Practo";
   }

   @Override
   public URI getURL() {
      return URI.create("https://github.com/practo");
   }

   @Override
   public Version getVersion() {
      return Version.fromPluginProperties(getClass(), PLUGIN_PROPERTIES, "version", Version.from(1, 0, 0, "unknown"));
   }

   @Override
   public String getDescription() {
      return "Alert plugin that triggers an alarm when given percentage limit was breached";
   }

   @Override
   public Version getRequiredVersion() {
      return Version.fromPluginProperties(getClass(), PLUGIN_PROPERTIES, "graylog.version", Version.from(2, 3, 0));
   }

   @Override
   public Set<ServerStatus.Capability> getRequiredCapabilities() {
      return Collections.emptySet();
   }
}
