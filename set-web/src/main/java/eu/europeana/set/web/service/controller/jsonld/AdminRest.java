package eu.europeana.set.web.service.controller.jsonld;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import eu.europeana.api.commons.definitions.exception.ApiWriteLockException;
import eu.europeana.api.commons.nosql.entity.ApiWriteLock;
import eu.europeana.api.commons.nosql.service.ApiWriteLockService;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.api.commons.web.model.vocabulary.Operations;
import eu.europeana.api2.utils.JsonWebUtils;
import eu.europeana.set.web.config.UserSetI18nConstants;
import eu.europeana.set.web.exception.request.RequestValidationException;
import eu.europeana.set.web.model.SetOperationResponse;
import eu.europeana.set.web.service.controller.BaseRest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "Set Admin Rest", hidden = true)
public class AdminRest extends BaseRest {
  
  Logger adminLogger = LogManager.getLogger(getClass());
  
  @Resource(name = "set_db_apilockService")
  private ApiWriteLockService writeLockService; 
  
  public ApiWriteLockService getApiWriteLockService() {
  return writeLockService;
  }
  
  @PostMapping(value = "/set/admin/lock", produces = { HttpHeaders.CONTENT_TYPE_JSON_UTF8,
      HttpHeaders.CONTENT_TYPE_JSONLD_UTF8 })
  @ApiOperation(value = "Lock write operations. Authorization required.", nickname = "lockWriteOperations", response = Void.class)
  public ResponseEntity<String> lockWriteOperations(
      HttpServletRequest request) throws HttpException, ApiWriteLockException {

    //checks also write lock and returns also 423 if application is locked
    verifyWriteAccess(Operations.WRITE_LOCK, request);
  
    ApiWriteLock activeLock = getApiWriteLockService().lock(ApiWriteLock.LOCK_WRITE_TYPE);
    //verify if the lock is really active
    boolean isLocked = isLocked(activeLock);
    
    SetOperationResponse response;
    response = new SetOperationResponse("admin", "/set/admin/lock");
    response.setStatus(isLocked ? "Server is now locked for changes" : "Unable to set lock");
    response.success = isLocked;
    if(activeLock!=null) {
      response.setSince(activeLock.getStarted());
    }
    
    if(adminLogger.isInfoEnabled()) {
      adminLogger.info("Lock write operations result: {}", response.getStatus());
    }
  
    String jsonStr = JsonWebUtils.toJson(response);
    HttpStatus httpStatus = response.success ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
    
    return buildResponse(jsonStr, httpStatus);
  }

  private boolean isLocked(ApiWriteLock activeLock) {
    return activeLock != null && activeLock.getStarted() != null && activeLock.getEnded() == null;
  }

  @DeleteMapping(value = "/set/admin/lock", produces = {
      HttpHeaders.CONTENT_TYPE_JSON_UTF8, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8 })
  @ApiOperation(value = "Unlock write operations", nickname = "unlockWriteOperations", response = Void.class)
  public ResponseEntity<String> unlockWriteOperations(
      HttpServletRequest request) throws HttpException, ApiWriteLockException {
    
    //allows write_unlock even when application is locked
    verifyWriteAccess(Operations.WRITE_UNLOCK, request);
  
    SetOperationResponse response;
    response = new SetOperationResponse("admin", "/set/admin/lock");
  
    ApiWriteLock activeLock = getApiWriteLockService().getLastActiveLock(ApiWriteLock.LOCK_WRITE_TYPE);
    if (activeLock != null && activeLock.getEnded() == null && ApiWriteLock.LOCK_WRITE_TYPE.equals(activeLock.getName())) {
        getApiWriteLockService().unlock(activeLock);
        ApiWriteLock lock = getApiWriteLockService().getLastActiveLock(ApiWriteLock.LOCK_WRITE_TYPE);
        if (lock == null) {
          response.setStatus("Server is now unlocked for changes");
          response.success = true;
          response.setSince(activeLock.getStarted());
          response.setEnd(activeLock.getEnded());
        } else {
          response.setStatus("Unlocking write operations failed");
          response.success = false;
        }
    } else {
      throw new RequestValidationException(UserSetI18nConstants.LOCK_NOT_IN_EFFECT, new String[] {});
    }
    
    if(adminLogger.isInfoEnabled()) {
      adminLogger.info("Unlock write operations result: {}", response.getStatus());
    }
  
    String jsonStr = JsonWebUtils.toJson(response);
    HttpStatus httpStatus = response.success ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
    return buildResponse(jsonStr, httpStatus);
  }

}
