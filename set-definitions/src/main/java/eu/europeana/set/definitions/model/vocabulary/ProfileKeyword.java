package eu.europeana.set.definitions.model.vocabulary;

public interface ProfileKeyword {

	/**
	 * Returns the value used in Linked Data profiles
	 * @return
	 */
	public String getHeaderValue();

	/**
	 * Returns the value used to indicate the profile in Prefer Header
	 * @return
	 */
	public String getPreferHeaderValue();

}
