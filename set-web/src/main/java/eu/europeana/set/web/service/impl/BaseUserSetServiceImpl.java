package eu.europeana.set.web.service.impl;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.europeana.set.mongo.service.PersistentUserSetService;

public abstract class BaseUserSetServiceImpl {

	@Resource
	PersistentUserSetService mongoPersistance;

	Logger logger = LogManager.getLogger(getClass());


	protected PersistentUserSetService getMongoPersistence() {
		return mongoPersistance;
	}

	public void setMongoPersistance(PersistentUserSetService mongoPersistance) {
		this.mongoPersistance = mongoPersistance;
	}
	
	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public PersistentUserSetService getMongoPersistance() {
		return mongoPersistance;
	}

}
