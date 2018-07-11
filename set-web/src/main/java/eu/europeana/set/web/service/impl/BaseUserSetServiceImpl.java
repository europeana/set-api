package eu.europeana.set.web.service.impl;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.mongo.service.PersistentUserSetService;

public abstract class BaseUserSetServiceImpl {

	@Resource
	PersistentUserSetService mongoPersistance;

	Logger logger = Logger.getLogger(getClass());


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

	//TODO: EA-1148, there are no indexing requirements for now. this method should be removed, and the code used in the parent method
	protected UserSet updateAndReindex(PersistentUserSet persistentUserSet) {
		UserSet res = getMongoPersistence().update(persistentUserSet);
		return res;
	}
	
}
