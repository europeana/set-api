package eu.europeana.set.web.service.controller.jsonld;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.api2.utils.JsonWebUtils;
import eu.europeana.set.definitions.exception.ApiWriteLockException;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import eu.europeana.set.mongo.model.internal.PersistentApiWriteLock;
import eu.europeana.set.mongo.service.PersistentApiWriteLockService;
import eu.europeana.set.web.model.SetOperationResponse;
import eu.europeana.set.web.model.vocabulary.SetOperations;
import eu.europeana.set.web.service.controller.BaseRest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "Set Admin Rest", hidden = true)
public class AdminRest extends BaseRest {
  
  Logger adminLogger = LogManager.getLogger(getClass());
  
  @Resource(name = "set_db_apilockService")
  private PersistentApiWriteLockService writeLockService; 
  
  public PersistentApiWriteLockService getApiWriteLockService() {
  return writeLockService;
  }
  
  @PostMapping(value = "/admin/lock", produces = { HttpHeaders.CONTENT_TYPE_JSON_UTF8,
      HttpHeaders.CONTENT_TYPE_JSONLD_UTF8 })
  @ApiOperation(value = "Lock write operations. Authorization required.", nickname = "lockWriteOperations", response = Void.class)
  public ResponseEntity<String> lockWriteOperations(
      HttpServletRequest request) throws HttpException, ApiWriteLockException {

    verifyWriteAccess(SetOperations.WRITE_LOCK, request);
  
    // get last active lock check if start date is correct and end date does
    // not exist
    PersistentApiWriteLock activeLock = getApiWriteLockService().getLastActiveLock(WebUserSetModelFields.LOCK_WRITE_NAME);
    //if already locked, an exception is thrown in verifyWriteAccess
    boolean isLocked = false;
    if(activeLock == null) {
        activeLock = getApiWriteLockService().lock(WebUserSetModelFields.LOCK_WRITE_NAME);
    } 
    
    isLocked = isLocked(activeLock);
    
    SetOperationResponse response;
    response = new SetOperationResponse("admin", "/admin/lock");
  
    response.setStatus(isLocked ? "Server is now locked for changes" : "Unable to set lock");
    response.success = isLocked;
  
    String jsonStr = JsonWebUtils.toJson(response);
    HttpStatus httpStatus = response.success ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
    adminLogger.info("Lock write operations result: " + jsonStr + "(HTTP status: " + httpStatus.toString() + ")");
    return buildResponse(jsonStr, httpStatus);
  }

  private boolean isLocked(PersistentApiWriteLock activeLock) {
    return activeLock != null && activeLock.getStarted() != null && activeLock.getEnded() == null;
  }

  @PostMapping(value = "/admin/unlock", produces = {
      HttpHeaders.CONTENT_TYPE_JSON_UTF8, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8 })
  @ApiOperation(value = "Unlock write operations", nickname = "unlockWriteOperations", response = Void.class)
  public ResponseEntity<String> unlockWriteOperations(
      HttpServletRequest request) throws HttpException, ApiWriteLockException {

    verifyWriteAccess(SetOperations.WRITE_UNLOCK, request);
  
    SetOperationResponse response;
    response = new SetOperationResponse("admin", "/admin/unlock");
  
    PersistentApiWriteLock activeLock = getApiWriteLockService().getLastActiveLock(WebUserSetModelFields.LOCK_WRITE_NAME);
    if (activeLock != null && activeLock.getEnded() == null && WebUserSetModelFields.LOCK_WRITE_NAME.equals(activeLock.getName())) {
        getApiWriteLockService().unlock(activeLock);
        PersistentApiWriteLock lock = getApiWriteLockService().getLastActiveLock(WebUserSetModelFields.LOCK_WRITE_NAME);
        if (lock == null) {
        response.setStatus("Server is now unlocked for changes");
        response.success = true;
        } else {
        response.setStatus("Unlocking write operations failed");
        response.success = false;
        }
    } else {
        response.setStatus("No write lock in effect (remains unlocked)");
        response.success = true;
    }
  
    String jsonStr = JsonWebUtils.toJson(response);
    HttpStatus httpStatus = response.success ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
    adminLogger.info("Unlock write operations result: " + jsonStr + "(HTTP status: " + httpStatus.toString() + ")");
    return buildResponse(jsonStr, httpStatus);
  }

}