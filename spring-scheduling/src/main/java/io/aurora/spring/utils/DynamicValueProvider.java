package io.aurora.spring.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.StringUtils;

public class DynamicValueProvider {

	private static final Logger log = LoggerFactory.getLogger(DynamicValueProvider.class);
	
	protected ScriptEngineManager mgr = new ScriptEngineManager();
	protected ScriptEngine engineJs = mgr.getEngineByName("JavaScript");
	   
	private Map<String,Long> mapper = new ConcurrentHashMap<String,Long>();
	
	private Object lock = new Object();
		
	public Values useScript(String script) {
		return new Values(script);
	}
	
	public void reset() {
		synchronized (lock) {
			mapper = new ConcurrentHashMap<String,Long>();
		}
	}
	
	public void addParam(String key,Long value) {
		mapper.put(key, value);
	}
	
	
	@Async
	public void addAvg(String context,Long value) {
		Long count = mapper.get(context+"-count");
		if(count == null) {
			count =new Long(0);
		}
		synchronized (lock) {
			Long avg = mapper.get(context+"-avg");
			if(avg == null) {
				avg =new Long(3);
			}
			avg = ((avg * count) + value) / (count+1);
			count++;
			mapper.put(context+"-count",count);
			mapper.put(context+"-avg",avg);
		}
	}
	
	public class Values {
		private String script;
		private Values(String script) {
			this.script = script;
		}
		
		public  Long getDelay(Long defaultValueInMS) {
			if(!StringUtils.isEmpty(script)) {
				return getDelayFromScript(defaultValueInMS);
			}
			return getDelayCalc(defaultValueInMS);
		}
		
		public  Long getDelayCalc(Long defaultValueInMs) {
			return defaultValueInMs;
		}
		
		//TODO
		private  Long getDelayFromScript(Long defaultValueInMS) {
			
			try {
				ScriptContext context = new SimpleScriptContext();
				mapper.forEach((k,v) -> {
					if(v >= 0) {
						engineJs.put(k, v);
						log.trace("Params : {} , {}" , k,v);
					}
				});
				
				Object result = engineJs.eval(script, context);
				log.debug("Got result from Script: {}" , result);
			    return (long) Double.parseDouble(result.toString());
			
			} catch (ScriptException e) {
				log.error("Not able to evaluate script {}",script,e);
			}catch (Exception e) {
				log.error("Error while evaluate script {}",script,e);
			}
			return defaultValueInMS;
		}
		
	}
}
