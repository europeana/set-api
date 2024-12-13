package eu.europeana.set.client.model.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Class to parse the Web User Set pagination response.
 * For now, we only have the fields that are required by the IIIF presentation APi to generate gallery
 * @author Srishti Singh
 * @since 3 December 2024
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecordPreview {

	@JsonProperty("id")
	private String id;

	@JsonProperty("dcDescriptionLangAware")
	private Map<String, List<String>> description;

	@JsonProperty("dcTitleLangAware")
	private Map<String, List<String>> title;

	@JsonProperty("edmPreview")
	private List<String> edmPreview;

	public RecordPreview() {
		// for jackson
	}
	public RecordPreview(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Map<String, List<String>> getDescription() {
		return description;
	}

	public void addDescription(String key, List<String> values) {
		if (this.description.get(key) != null) {
			this.description.get(key).addAll(values);
		} else {
			this.description.put(key, values);
		}
	}

	public boolean hasDescription() {
		return description != null && !description.isEmpty();
	}

	public Map<String, List<String>> getTitle() {
		return title;
	}

	public void addTitle(String key, List<String> values) {
		if (this.title.get(key) != null) {
			this.title.get(key).addAll(values);
		} else {
			this.title.put(key, values);
		}
	}

	public boolean hasTitle() {
		return title != null && !title.isEmpty();
	}

	public List<String> getEdmPreview() {
		return edmPreview;
	}

	public void setEdmPreview(List<String> edmPreview) {
		this.edmPreview = edmPreview;
	}

	public boolean hasPreview() {
		return edmPreview != null && !edmPreview.isEmpty();
	}
}
