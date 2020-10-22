package eu.europeana.set.web.model;

/**
 * This method implements a custom filter for serialization of positive values
 * only. For the open sets with minimal profile we set the value to -1 so that
 * the total will not be serialized
 */
public class PositiveIntegerFilter {

    public PositiveIntegerFilter() {
	super();
    }

    @Override
    public boolean equals(Object other) {
	// Trick required to be compliant with the Jackson Custom attribute processing
	if (other == null) {
	    return true;
	}

	//avoid critical sonar cube issue
	if(!(other instanceof Number)) {
	    //true means filter out	
	    return true;
	}
	
	Integer value = (Integer) other;
	return value < 0;
    }

    @Override
    public int hashCode() {
	return super.hashCode();
    }
}