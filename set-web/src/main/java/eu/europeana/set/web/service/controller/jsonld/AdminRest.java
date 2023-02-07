package eu.europeana.set.web.service.controller.jsonld;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.api2.utils.JsonWebUtils;
import eu.europeana.set.definitions.exception.ApiWriteLockException;
import eu.europeana.set.mongo.model.internal.PersistentApiWriteLock;
import eu.europeana.set.mongo.service.PersistentApiWriteLockService;
import eu.europeana.set.web.exception.authorization.UserAuthorizationException;
import eu.europeana.set.web.model.SetOperationResponse;
import eu.europeana.set.web.model.vocabulary.SetOperations;
import eu.europeana.set.web.service.controller.BaseRest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "Set Admin Rest", hidden = true)
public class AdminRest extends BaseRest {
  
  Logger logger = LogManager.getLogger(getClass());
  
  @Resource(name = "set_db_apilockService")
  private PersistentApiWriteLockService writeLockService; 
  
  public PersistentApiWriteLockService getApiWriteLockService() {
  return writeLockService;
  }
  
  @RequestMapping(value = "/admin/lock", method = RequestMethod.POST, produces = { HttpHeaders.CONTENT_TYPE_JSON_UTF8,
      HttpHeaders.CONTENT_TYPE_JSONLD_UTF8 })
  @ApiOperation(value = "Lock write operations. Authorization required.", nickname = "lockWriteOperations", response = java.lang.Void.class)
  public ResponseEntity<String> lockWriteOperations(
      HttpServletRequest request) throws UserAuthorizationException, HttpException, ApiWriteLockException {

    verifyWriteAccess(SetOperations.WRITE_LOCK, request);
  
    // get last active lock check if start date is correct and end date does
    // not exist
    PersistentApiWriteLock activeLock = getApiWriteLockService().getLastActiveLock("lockWriteOperations");
    //if already locked, an exception is thrown in verifyWriteAccess
    boolean isLocked = false;
    if(activeLock == null) {
        activeLock = getApiWriteLockService().lock("lockWriteOperations");
    } 
    
    isLocked = isLocked(activeLock);
    
    SetOperationResponse response;
    response = new SetOperationResponse("admin", "/admin/lock");
  
    response.setStatus(isLocked ? "Server is now locked for changes" : "Unable to set lock");
    response.success = isLocked;
  
    String jsonStr = JsonWebUtils.toJson(response, null);
    HttpStatus httpStatus = response.success ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
    logger.info("Lock write operations result: " + jsonStr + "(HTTP status: " + httpStatus.toString() + ")");
    return buildResponse(jsonStr, httpStatus);
  }

  private boolean isLocked(PersistentApiWriteLock activeLock) {
    return activeLock != null && activeLock.getStarted() != null && activeLock.getEnded() == null;
  }

  @RequestMapping(value = "/admin/unlock", method = RequestMethod.POST, produces = {
      HttpHeaders.CONTENT_TYPE_JSON_UTF8, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8 })
  @ApiOperation(value = "Unlock write operations", nickname = "unlockWriteOperations", response = java.lang.Void.class)
  public ResponseEntity<String> unlockWriteOperations(
      HttpServletRequest request) throws UserAuthorizationException, HttpException, ApiWriteLockException {

    verifyWriteAccess(SetOperations.WRITE_UNLOCK, request);
  
    SetOperationResponse response;
    response = new SetOperationResponse("admin", "/admin/unlock");
  
    PersistentApiWriteLock activeLock = getApiWriteLockService().getLastActiveLock("lockWriteOperations");
    if (activeLock != null && activeLock.getName().equals("lockWriteOperations") && activeLock.getEnded() == null) {
        getApiWriteLockService().unlock(activeLock);
        PersistentApiWriteLock lock = getApiWriteLockService().getLastActiveLock("lockWriteOperations");
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
  
    String jsonStr = JsonWebUtils.toJson(response, null);
    HttpStatus httpStatus = response.success ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
    logger.info("Unlock write operations result: " + jsonStr + "(HTTP status: " + httpStatus.toString() + ")");
    return buildResponse(jsonStr, httpStatus);
  }

}
