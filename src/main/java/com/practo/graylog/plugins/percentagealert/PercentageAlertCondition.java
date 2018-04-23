package com.practo.graylog.plugins.percentagealert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.graylog2.Configuration;
import org.graylog2.alerts.AbstractAlertCondition;
import org.graylog2.indexer.results.ResultMessage;
import org.graylog2.indexer.results.SearchResult;
import org.graylog2.indexer.searches.Searches;
import org.graylog2.indexer.searches.SearchesConfig;
import org.graylog2.indexer.searches.Sorting;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.MessageSummary;
import org.graylog2.plugin.Tools;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.BooleanField;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.NumberField;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.indexer.searches.timeranges.AbsoluteRange;
import org.graylog2.plugin.streams.Stream;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class PercentageAlertCondition extends AbstractAlertCondition {

   private final Searches searches;
   private String query;
   private Period period;
   private int threshold;
   private Logger logger = LoggerFactory.getLogger(PercentageAlertCondition.class);

   public interface Factory extends AlertCondition.Factory {
      @Override
      PercentageAlertCondition create(Stream stream, @Assisted("id") String id, DateTime createdAt,
            @Assisted("userid") String creatorUserId, Map<String, Object> parameters,
            @Assisted("title") @Nullable String title);

      @Override
      PercentageAlertCondition.Config config();

      @Override
      PercentageAlertCondition.Descriptor descriptor();
   }

   public static class Config implements AlertCondition.Config {
      public Config() {
      }

      @Override
      public ConfigurationRequest getRequestedConfiguration() {
         final ConfigurationRequest configurationRequest = ConfigurationRequest.createWithFields(
               new TextField("query", "Api success param", "", "param to validate api success e.g: resposne:200",
                     ConfigurationField.Optional.NOT_OPTIONAL),
               new TextField("period", "Period", "PT24H",
                     "Duration window, period in ISO8601 format e.g. \"P1M\" is a one-month duration and \"PT10M\" is a ten-minutes duration",
                     ConfigurationField.Optional.NOT_OPTIONAL),
               new NumberField("threshold", "Error percentage", 3,
                     "Error Percentage which will trigger an alarm, if its breached with in the duration window",
                     ConfigurationField.Optional.NOT_OPTIONAL));
         configurationRequest.addFields(AbstractAlertCondition.getDefaultConfigurationFields());

         return configurationRequest;
      }
   }

   public static class Descriptor extends AlertCondition.Descriptor {
      public Descriptor() {
         super("Percentage Alert Condition", "https://github.com/practo",
               "Trigger an alaram if percentage limit breached");
      }
   }

   @AssistedInject
   public PercentageAlertCondition(Searches searches, Configuration configuration, @Assisted Stream stream,
         @Nullable @Assisted("id") String id, @Assisted DateTime createdAt, @Assisted("userid") String creatorUserId,
         @Assisted Map<String, Object> parameters, @Assisted("title") @Nullable String title) {
      super(stream, id, PercentageAlertCondition.class.getName(), createdAt, creatorUserId, parameters, title);
      this.searches = searches;
      try {
         this.query = (String) parameters.get("query");
         this.period = Period.parse((String) parameters.get("period"));
         this.threshold = (Integer) parameters.get("threshold");
      } catch (Exception e) {
         logger.error(e.getMessage());
      }
   }

   @Override
   public String getDescription() {
      StringBuilder builder = new StringBuilder();
      builder.append("[query=");
      builder.append(query);
      builder.append(", period=");
      builder.append(period);
      builder.append(", threshold=");
      builder.append(threshold);
      builder.append("]");
      return builder.toString();
   }

   @Override
   public CheckResult runCheck() {
      if (query != null) {
         int requestBacklogSize = getBacklog();
         
         final SearchesConfig searchesConfigAll = SearchesConfig.builder() //
        		 .query("") //
                 .filter("streams:" + stream.getId()) //
                 .fields(Arrays.stream(new String[] { Message.FIELD_TIMESTAMP, Message.FIELD_MESSAGE })
                       .collect(Collectors.toList())) //
                 .range(AbsoluteRange.create(Tools.nowUTC().minus(period), Tools.nowUTC())) //
                 .limit(threshold) //
                 .offset(0) //
                 .sorting(Sorting.DEFAULT).build();
           SearchResult searchResultAll = searches.search(searchesConfigAll);
           
           
         final SearchesConfig searchesConfigQuery = SearchesConfig.builder() //
               .query(query) //
               .filter("streams:" + stream.getId()) //
               .fields(Arrays.stream(new String[] { Message.FIELD_TIMESTAMP, Message.FIELD_MESSAGE })
                     .collect(Collectors.toList())) //
               .range(AbsoluteRange.create(Tools.nowUTC().minus(period), Tools.nowUTC())) //
               .limit(threshold) //
               .offset(0) //
               .sorting(Sorting.DEFAULT).build();
         SearchResult searchResultQuery = searches.search(searchesConfigQuery);
         
         long totalLog = searchResultAll.getTotalResults();
         long errorLog = totalLog - searchResultQuery.getTotalResults();
         
         System.out.println("Totle log ----->"+totalLog);
         System.out.println("Error log ----->"+errorLog);
         float singleUnit = totalLog/100f;
         int errorPercentage = (int)(errorLog/singleUnit);
         // found matched messages more than given threshold
         
         System.out.println("Error percentage ------>"+errorPercentage);
         boolean trigger = false;
         StringBuilder stringBuffer = new StringBuilder();
         
            if (errorPercentage > threshold) {
               trigger = true;
               stringBuffer.append("Found ").append(errorLog).append(" messages from `")
                     .append(query).append("` which were exceeding the (").append(threshold).append("%)  threshold in last ")
                     .append(PeriodFormat.getDefault().print(period));
            }
        
         List<MessageSummary> summariesList = new ArrayList<>();
         if (trigger && requestBacklogSize > 0) {
            List<ResultMessage> results = searchResultQuery.getResults();
            int resultSize = results.size();
            for (int i = 0; i < requestBacklogSize && i < resultSize; i++) {
               ResultMessage resultMessage = results.get(i);
               Message message = resultMessage.getMessage();
               summariesList.add(new MessageSummary(resultMessage.getIndex(), message));
            }
         }
         return new CheckResult(trigger, this, stringBuffer.toString(), Tools.nowUTC(), summariesList);
      }
      return new NegativeCheckResult();
   }

}
