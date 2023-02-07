package eu.europeana.set.web.model.vocabulary;

import eu.europeana.api.commons.definitions.vocabulary.Role;
import eu.europeana.api.commons.web.model.vocabulary.Operations;

public enum Roles implements Role {

	ANONYMOUS(new String[]{Operations.RETRIEVE}),
	USER(new String[]{Operations.RETRIEVE, Operations.CREATE, Operations.DELETE, Operations.UPDATE}),
	EDITOR(new String[]{Operations.RETRIEVE, Operations.CREATE, Operations.DELETE, Operations.UPDATE}),
	PUBLISHER(new String[]{Operations.RETRIEVE, Operations.CREATE, Operations.DELETE, Operations.UPDATE, SetOperations.PUBLISH}),
	ADMIN(new String[]{Operations.RETRIEVE, Operations.CREATE, Operations.DELETE, Operations.UPDATE, SetOperations.PUBLISH, Operations.ADMIN_ALL, SetOperations.ADMIN_REINDEX, SetOperations.WRITE_LOCK, SetOperations.WRITE_UNLOCK});
	
	String[] operations;
	
	Roles (String[] operations){
		this.operations = operations;
	}
	
	public String[] getOperations() {
		return operations;
	}
	
	@Override
	public String[] getPermissions() {
	    return getOperations();
	}
	
	@Override
	public String getName() {
	    return this.name();
	}
	
	/**
	 * This method returns the api specific Role for the given role name
	 * 
	 * @param name the name of user role 
	 * @return the user role
	 */
	public static Role getRoleByName(String name) {
	    Role userRole = null;
	    for(Roles role : Roles.values()) {
			if(role.name().equalsIgnoreCase(name)) {
			    userRole = role;
			    break;
			}
	    }
	    return userRole;
	}
}
