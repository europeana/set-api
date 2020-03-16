package eu.europeana.set.web.service.controller.exception;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.ControllerAdvice;

import eu.europeana.api.commons.config.i18n.I18nService;
import eu.europeana.api.commons.service.authorization.AuthorizationService;
import eu.europeana.api.commons.web.controller.exception.AbstractExceptionHandlingController;
import eu.europeana.api.commons.web.model.ApiResponse;
import eu.europeana.set.web.model.UserSetOperationResponse;

@ControllerAdvice
public class GlobalExceptionHandler extends AbstractExceptionHandlingController {

	@Resource
	I18nService i18nService;

	protected I18nService getI18nService() {
		return i18nService;
	}
	
	@Override
	public ApiResponse buildErrorResponse(String errorMessage, String action, String apiKey) {

		UserSetOperationResponse response = new UserSetOperationResponse(apiKey, action);
		response.success = false;
		response.error = errorMessage;
		return response;
	}
	
	@Override
	protected ApiResponse getErrorReport(String apiKey, String action, Throwable th, boolean includeErrorStack) {
		UserSetOperationResponse response = (UserSetOperationResponse) super.getErrorReport(apiKey, action, th, includeErrorStack);
		return response;
	}

	@Override
	protected AuthorizationService getAuthorizationService() {
		// TODO Auto-generated method stub
		return null;
	}

}
