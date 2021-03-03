package eu.europeana.set.definitions.model.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.vocabulary.VisibilityTypes;

/**
 * Europeana Sets API Specification
 *
 * @author GrafR
 * Modified by Srishti Singh 2-2-2021
 */
public class BaseUserSet extends BasePageInfo implements UserSet {

    // EDM Collection Profile

    // Type of user set : #UserSetTypes
    private String type;

    // Visibility of user set : #VisibilityTypes
    private String visibility;

    // Name of user set
    private Map<String, String> title;

    // A summary of the content and topics of the set
    private Map<String, String> description;

    /** Contains query URI to items
     * The existence of the isDefinedBy determines
     * whether it is an open set or closed set.
     */
    private String isDefinedBy;

    /** Terms that describe the overall topical content
     * of the objects in the collection
     */
    private List<String> subject;

    /** List of the users that have the editor role and contributed to the creation of this set 
     * of the Entity user sets
     */
    private List<String> contributor;

    /** Indicates whether the set is generated by a User as opposed to more
     * traditional collections combing from e.g. Data Providers.
     * For the time being will be always set to true
     */
    private boolean ugc;

    // Provenance information

    /**
     * A reference to the user agent that gathers objects together following
     * implicit or explicit criteria or accrual policy.
     */
    private Agent creator;

    /**
     * The time at which the Set was created by the user. The value must be a
     * literal expressed as xsd:dateTime with the UTC timezone expressed as "Z".
     */
    private Date created;

    /**
     * The time at which the Set was modified, after creation. The value must be a
     * literal expressed as xsd:dateTime with the UTC timezone expressed as "Z".
     */
    private Date modified;

    /** Ordered Collections from Activity Streams
     * For EDM Collection class
     */

    // The items that should be part of the Set (as a rdf:List)
    private List<String> items;

    // ID sequentially generated by a database
    private String identifier;


    public String getIdentifier() {
	return identifier;
    }

    public void setIdentifier(String sequenceIdentifier) {
	this.identifier = sequenceIdentifier;
    }

    @Override
    public String getType() {
	return type;
    }

    @Override
    public void setType(String type) {
	this.type = type;
    }

    @Override
    public Map<String, String> getTitle() {
	return title;
    }

    @Override
    public void setTitle(Map<String, String> title) {
	this.title = title;
    }

    @Override
    public Map<String, String> getDescription() {
	return description;
    }

    @Override
    public void setDescription(Map<String, String> description) {
	this.description = description;
    }

    @Override
    public List<String> getSubject() {
	return subject;
    }

    @Override
    public void setSubject(List<String> subject) {
	this.subject = subject;
    }

    @Override
    public List<String> getContributor() {
        return contributor;
    }

    @Override
    public void setContributor(List<String> contributor) {
        this.contributor = contributor;
    }

    public boolean isUgc() {
        return ugc;
    }

    public void setUgc(boolean ugc) {
        this.ugc = ugc;
    }

    @Override
    public Agent getCreator() {
	return creator;
    }

    @Override
    public void setCreator(Agent creator) {
	this.creator = creator;
    }

    @Override
    public Date getCreated() {
	return created;
    }

    @Override
    public void setCreated(Date created) {
	this.created = created;
    }

    @Override
    public Date getModified() {
	return modified;
    }

    @Override
    public void setModified(Date modified) {
	this.modified = modified;
    }

    @Override
    public List<String> getItems() {
	return items;
    }

    @Override
    public void setItems(List<String> items) {
	this.items = items;
    }

    @Override
    public String getIsDefinedBy() {
	return this.isDefinedBy;
    }

    @Override
    public void setIsDefinedBy(String isDefinedBy) {
	this.isDefinedBy = isDefinedBy;
    }

    public boolean isOpenSet() {
	if (getIsDefinedBy() != null) {
	    return true;
    }
	return false;
    }

    @Override
    public String getVisibility() {
	return this.visibility;
    }

    @Override
    public void setVisibility(String visibility) {
	this.visibility = visibility;
    }

    @Override
    public boolean isPrivate() {
	return VisibilityTypes.PRIVATE.getJsonValue().equals(getVisibility());
    }

    @Override
    public boolean isPublic() {
	return VisibilityTypes.PUBLIC.getJsonValue().equals(getVisibility());
    }

    @Override
    public boolean isPublished() {
	return VisibilityTypes.PUBLISHED.getJsonValue().equals(getVisibility());
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder("\t### User set ###\n");

        if (getType() != null)
            res.append("\t\t").append("user set type:").append(getType()).append("\n");
        if (getTitle() != null)
            res.append("\t\t" + "name:").append(getTitle()).append("\n");
        if (getItems() != null)
            res.append("\t\t").append("number of items:").append(getItems().size()).append("\n");
        return res.toString();
    }
}