package eu.europeana.set.web.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import eu.europeana.set.definitions.model.agent.impl.SoftwareAgent;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldType;

@JsonldType("http://schema.org/Agent")
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class WebSoftwareAgent extends SoftwareAgent{

	public WebSoftwareAgent(){
		super();
	}
}
