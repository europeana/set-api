package eu.europeana.set.client.web;

import java.io.IOException;

import org.springframework.http.ResponseEntity;

import eu.europeana.set.client.BaseUserSetApi;
import eu.europeana.set.client.exception.TechnicalRuntimeException;

public class WebUserSetApiImpl extends BaseUserSetApi implements WebUserSetApi {

	@Override
	public ResponseEntity<String> createUserSet(String wskey, String set, String userToken) {

		ResponseEntity<String> res;
		try {
			res = apiConnection.createUserSet(wskey, set, userToken);
		} catch (IOException e) {
			throw new TechnicalRuntimeException(
					"Exception occured when invoking the UserSetJsonApi createUserSet method", e);
		}

		return res;
	}
	
	@Override
	public ResponseEntity<String> deleteUserSet(String wskey, String identifier, String userToken) {
		ResponseEntity<String> res;
		try {
			res = apiConnection.deleteUserSet(wskey, identifier, userToken);
		} catch (IOException e) {
			throw new TechnicalRuntimeException(
					"Exception occured when invoking the UserSetJsonApi deleteUserSet method", e);
		}

		return res;
	}

	@Override
	public ResponseEntity<String> getUserSet(String wskey, String provider, String identifier) {

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
	public ResponseEntity<String> updateUserSet(String wskey, String identifier, String set,
			String userToken) {
		ResponseEntity<String> res;
		try {
			res = apiConnection.updateUserSet(wskey, identifier, set, userToken);
		} catch (IOException e) {
			throw new TechnicalRuntimeException(
					"Exception occured when invoking the UserSetJsonApi updateUserSet method", e);
		}

		return res;
	}

}
