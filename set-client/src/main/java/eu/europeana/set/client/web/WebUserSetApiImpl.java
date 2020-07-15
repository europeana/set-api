package eu.europeana.set.client.web;

import java.io.IOException;

import org.springframework.http.ResponseEntity;

import eu.europeana.set.client.BaseUserSetApi;
import eu.europeana.set.client.exception.TechnicalRuntimeException;

public class WebUserSetApiImpl extends BaseUserSetApi implements WebUserSetApi {

	@Override
	public ResponseEntity<String> createUserSet(String wskey, String set) {

		ResponseEntity<String> res;
		try {
			res = apiConnection.createUserSet(wskey, set);
		} catch (IOException e) {
			throw new TechnicalRuntimeException(
					"Exception occured when invoking the UserSetJsonApi createUserSet method", e);
		}

		return res;
	}
	
	@Override
	public ResponseEntity<String> deleteUserSet(String wskey, String identifier) {
		ResponseEntity<String> res;
		try {
			res = apiConnection.deleteUserSet(wskey, identifier);
		} catch (IOException e) {
			throw new TechnicalRuntimeException(
					"Exception occured when invoking the UserSetJsonApi deleteUserSet method", e);
		}

		return res;
	}

	@Override
	public ResponseEntity<String> getUserSet(String wskey, String identifier) {

		ResponseEntity<String> res;
		try {
			res = apiConnection.getUserSet(wskey, identifier);
		} catch (IOException e) {
			throw new TechnicalRuntimeException(
					"Exception occured when invoking the UserSetJsonApi getUserSet method", e);
		}

		return res;
	}

	@Override
	public ResponseEntity<String> updateUserSet(String wskey, String identifier, String set) {
		ResponseEntity<String> res;
		try {
			res = apiConnection.updateUserSet(wskey, identifier, set);
		} catch (IOException e) {
			throw new TechnicalRuntimeException(
					"Exception occured when invoking the UserSetJsonApi updateUserSet method", e);
		}

		return res;
	}

}