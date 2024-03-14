package eu.europeana.set.mongo.model;

import java.util.Date;
import org.bson.types.ObjectId;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import eu.europeana.api.commons.nosql.entity.PersistentObject;
import eu.europeana.set.definitions.model.authentication.Application;
import eu.europeana.set.definitions.model.authentication.impl.BaseClientImpl;
import eu.europeana.set.mongo.model.internal.PersistentClient;

@Entity("client")
public class PersistentClientImpl extends BaseClientImpl implements PersistentClient, PersistentObject {

	@Embedded
	Application clientApplication;

	@Id
	private ObjectId id;
	
	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}
	
	public void setClientApplication(Application clientApplication) {
		this.clientApplication = clientApplication;
	}
	public Application getClientApplication() {
		return clientApplication;
	}
	
	public String toString() {
		return "PersistentClientImpl [" 
				+ "Id:" + getId() + ", " 
	            + "clientId:" + getClientId() + ", " 
	            + "authenticationConfigJson:" + getAuthenticationConfigJson() + "]";
	}

	@Override
	public Date getCreated() {
		return this.getCreationDate();
	}

	@Override
	public void setCreated(Date creationDate) {
		this.setCreationDate(creationDate);
	}


}