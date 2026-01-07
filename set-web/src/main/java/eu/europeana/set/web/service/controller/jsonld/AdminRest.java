package eu.europeana.set.web.service.controller.jsonld;

import eu.europeana.api.commons_sb3.definitions.http.HttpHeaders;
import eu.europeana.api.commons_sb3.error.EuropeanaI18nApiException;
import eu.europeana.api.commons_sb3.error.config.ErrorConfig;
import  jakarta.annotation.Resource;
import  jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import eu.europeana.api.commons_sb3.definitions.oauth.exception.ApiWriteLockException;
import eu.europeana.api.commons_sb3.definitions.oauth.Operations;
import eu.europeana.api.commons_sb3.nosql.entity.ApiWriteLock;
import eu.europeana.api.commons_sb3.nosql.service.ApiWriteLockService;
import eu.europeana.api2.utils.JsonWebUtils;
import eu.europeana.set.web.model.SetOperationResponse;
import eu.europeana.set.web.service.controller.BaseRest;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Collections;

@RestController
@Hidden
@Tag(name = "Set Admin Rest")
public class AdminRest extends BaseRest {
  
  Logger adminLogger = LogManager.getLogger(getClass());
  
  @Resource(name = "set_db_apilockService")
  private ApiWriteLockService writeLockService;
  
  public ApiWriteLockService getApiWriteLockService() {
  return writeLockService;
  }
  
  @PostMapping(value = "/set/admin/lock", produces = { HttpHeaders.CONTENT_TYPE_JSON_UTF8,
      HttpHeaders.CONTENT_TYPE_JSONLD_UTF8 })
  @Operation(description = "Lock write operations. Authorization required.", summary = "Lock Write Operations")
  public ResponseEntity<String> lockWriteOperations(
      HttpServletRequest request) throws ApiWriteLockException, EuropeanaI18nApiException {

    //checks also write lock and returns also 423 if application is locked
    verifyWriteAccess(Operations.WRITE_LOCK, request);
  
    ApiWriteLock activeLock = getApiWriteLockService().lock(ApiWriteLock.LOCK_WRITE_TYPE);
    //verify if the lock is really active
    boolean isLocked = isLocked(activeLock);
    
    HttpStatus httpStatus = isLocked ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
    SetOperationResponse response = new SetOperationResponse("admin",
            "/set/admin/lock",
            isLocked ? "Server is now locked for changes" : "Unable to set lock",
            isLocked,
            httpStatus.value());

    if (activeLock != null) {
      response.setSince(activeLock.getStarted());
    }
    
    if (adminLogger.isInfoEnabled()) {
      adminLogger.info("Lock write operations result: {}", response.getStatus());
    }
    String jsonStr = JsonWebUtils.toJson(response);
    return buildResponse(jsonStr, httpStatus);
  }

  private boolean isLocked(ApiWriteLock activeLock) {
    return activeLock != null && activeLock.getStarted() != null && activeLock.getEnded() == null;
  }

  @DeleteMapping(value = "/set/admin/lock", produces = {
      HttpHeaders.CONTENT_TYPE_JSON_UTF8, HttpHeaders.CONTENT_TYPE_JSONLD_UTF8 })
  @Operation(summary = "Unlock write operations")
  public ResponseEntity<String> unlockWriteOperations(
      HttpServletRequest request) throws EuropeanaI18nApiException, ApiWriteLockException {
    
    //allows write_unlock even when application is locked
    verifyWriteAccess(Operations.WRITE_UNLOCK, request);
  
    SetOperationResponse response = new SetOperationResponse("admin", "/set/admin/lock");

    ApiWriteLock activeLock = getApiWriteLockService().getLastActiveLock(ApiWriteLock.LOCK_WRITE_TYPE);
    if (activeLock != null && activeLock.getEnded() == null && ApiWriteLock.LOCK_WRITE_TYPE.equals(activeLock.getName())) {
        getApiWriteLockService().unlock(activeLock);
        ApiWriteLock lock = getApiWriteLockService().getLastActiveLock(ApiWriteLock.LOCK_WRITE_TYPE);
        if (lock == null) {
         response.setMessage("Server is now unlocked for changes");
         response.success = true;
         response.setStatus(HttpStatus.OK.value());
         response.setSince(activeLock.getStarted());
         response.setEnd(activeLock.getEnded());
        } else {
          response.setMessage("Unlocking write operations failed");
          response.success = false;
        }
    } else {
      throw new EuropeanaI18nApiException(null, null, null, ErrorConfig.NO_LOCK_IN_EFFECT, Collections.emptyList());
    }
    
    if (adminLogger.isInfoEnabled()) {
      adminLogger.info("Unlock write operations result: {}", response.getStatus());
    }
  
    String jsonStr = JsonWebUtils.toJson(response);
    HttpStatus httpStatus = response.success ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
    return buildResponse(jsonStr, httpStatus);
  }
}