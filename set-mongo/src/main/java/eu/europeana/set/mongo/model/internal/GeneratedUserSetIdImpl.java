package eu.europeana.set.mongo.model.internal;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

/**
 * This class is used to generate the UserSet IDs in form of europeanaId/UserSetId, where the 
 * UserSet numbers are generated using sequencies 
 * 
 * @author GrafR
 *
 */
@Entity(value="userSetIdGenerator", noClassnameStored=true)
public class GeneratedUserSetIdImpl {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = -4554805997975526594L;
	
	@Id
	private String id;
	private Long userSetId;
	
	public Long getUserSetId() {
		return userSetId;
	}

	public void setUserSetId(Long userSetId) {
		this.userSetId = userSetId;
	}

	public static final String SEQUENCE_COLUMN_NAME = "userSetId";
	
	/**
	 * This constructor must be use only by morphia 
	 */
	protected GeneratedUserSetIdImpl(){
		
	}
	
	/**
	 * 
	 * @param provider
	 * @param identifier - must be a long number
	 */
	public GeneratedUserSetIdImpl(String provider, String identifier){
		this(provider, Long.valueOf(identifier));
	}
	
	public GeneratedUserSetIdImpl(String provider, Long userSetId){
		this.id=provider;
		this.userSetId = userSetId;
	}
	
	public String getIdentifier() {
		return ""+getUserSetId();
	}

	public void setIdentifier(String identifier) {
		setUserSetId(Long.valueOf(identifier));
	}
	
}
