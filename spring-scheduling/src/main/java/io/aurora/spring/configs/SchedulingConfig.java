package io.aurora.spring.configs;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import io.aurora.spring.utils.DynamicValueProvider;

@EnableScheduling
public class SchedulingConfig implements SchedulingConfigurer{

	private static final Logger log = LoggerFactory.getLogger(SchedulingConfig.class);
	
	@Value("${spring.schedular.initialDelay:0}")
	private long initialDelay;
	
	@Value("${spring.schedular.fixedDelay:60000}")
	private long fixedDelay;
	
	
	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		//To Support new Approach of Dynamic Scheduling
		taskRegistrar.addTriggerTask(new Runnable() {
	            @Override
	            public void run() {
	            		
	            	log.info("PROCESSING");
	            }
	        }, new Trigger() {

	        	@Override
	            public Date nextExecutionTime(TriggerContext triggerContext) {
	        		Date lastTime = triggerContext.lastScheduledExecutionTime();
	        		if(lastTime== null) {
	        			log.debug("Running for the first time");
						return new Date(System.currentTimeMillis() + initialDelay);
	        		}
	        	  long delayInMs = 	getDynamicDelay();
				  Date nextDate = new Date (lastTime.getTime() + delayInMs);
	              log.info("Next time to run is {}",nextDate);
	              return nextDate;
	            }
				
	        }
	        );
			
	}

	/**
	 * Logic to calculate the delay between each execution
	 * 
	 * For simplicity we have user 
	 *  fixedDelay 			: which can be inject from properties
	 *  calculatedDelay 	: which should be calculated as needed
	 * 
	 * @return
	 */
	Long getDynamicDelay() {
		long calculatedDelay = 10000L;
		return fixedDelay + calculatedDelay;
	}

	
	
	/**
	 * To calculate the dynamic delay value. We are using a Service to calculate the delay
	 * It provides 
	 * 	-	a bootstrap value feature which can be called from other service
	 *  -	an average calculator with reset functionality which might be useful
	 *  -	support for JavaScript dynamic value evaluation 
	 */
	
	
	@Autowired
	private DynamicValueProvider dynValueScheduler;
	
	Long getDynamicDelayNew() {
		return dynValueScheduler.useScript(null).getDelay(0L);
	}
	
	//Create a Service for the specific value provider
	@Bean
	public DynamicValueProvider dynValueScheduler() {
		return new DynamicValueProvider();
	}

}