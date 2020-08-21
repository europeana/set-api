package eu.europeana.set.client.web;

import java.io.IOException;

import org.springframework.http.ResponseEntity;

import eu.europeana.set.client.BaseUserSetApi;
import eu.europeana.set.client.exception.TechnicalRuntimeException;

public class WebUserSetApiImpl extends BaseUserSetApi implements WebUserSetApi {

	@Override
	public ResponseEntity<String> createUserSet(String set, String profile) {

		ResponseEntity<String> res;
		try {
			res = apiConnection.createUserSet(set, profile);
		} catch (IOException e) {
			throw new TechnicalRuntimeException(
					"Exception occured when invoking the UserSetJsonApi createUserSet method", e);
		}

		return res;
	}
	
	@Override
	public ResponseEntity<String> deleteUserSet(String identifier) {
		ResponseEntity<String> res;
		try {
			res = apiConnection.deleteUserSet(identifier);
		} catch (IOException e) {
			throw new TechnicalRuntimeException(
					"Exception occured when invoking the UserSetJsonApi deleteUserSet method", e);
		}

		return res;
	}

	@Override
	public ResponseEntity<String> getUserSet(String identifier, String profile) {

		ResponseEntity<String> res;
		try {
			res = apiConnection.getUserSet(identifier, profile);
		} catch (IOException e) {
			throw new TechnicalRuntimeException(
					"Exception occured when invoking the UserSetJsonApi getUserSet method", e);
		}

		return res;
	}

	@Override
	public ResponseEntity<String> updateUserSet(String identifier, String set, String profile) {
		ResponseEntity<String> res;
		try {
			res = apiConnection.updateUserSet(identifier, set, profile);
		} catch (IOException e) {
			throw new TechnicalRuntimeException(
					"Exception occured when invoking the UserSetJsonApi updateUserSet method", e);
		}

		return res;
	}

}