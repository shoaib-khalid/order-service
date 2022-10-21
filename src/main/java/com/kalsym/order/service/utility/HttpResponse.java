package com.kalsym.order.service.utility;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import com.kalsym.order.service.model.Error;
import com.kalsym.order.service.model.ErrorCode;
import com.kalsym.order.service.model.repository.ErrorCodeRepository;

/**
 *
 * @author Sarosh
 */
@Getter
@Setter
@JsonInclude(Include.NON_NULL)
@NoArgsConstructor
@ToString
public class HttpResponse {

    public HttpResponse(String requestUri) {
        this.timestamp = DateTimeUtil.currentTimestamp();
        this.path = requestUri;
    }

    private String timestamp;
    private int status;
    private String error;
    private String message;
    private Object data;
    private String path;
    private String errorCode;
    
    /**
     * *
     * Sets success and message as reason phrase of provided status.
     *
     * @param status
     */
    public void setSuccessStatus(HttpStatus status) {
        this.status = status.value();
        this.message = status.getReasonPhrase();
    }

    /**
     * *
     * Sets status and custom message.
     *
     * @param status
     */
    public void setErrorStatus(HttpStatus status) {
        this.status = status.value();
        this.error = status.getReasonPhrase();
    }

    /**
     * *
     * Sets status and custom message.
     *
     * @param status
     * @param message
     */
    public void setErrorStatus(HttpStatus status, String message) {
        this.status = status.value();
        this.error = message;
    }
    
    
    /**
     * *
     * Sets status and custom message.
     * @param modules
     * @param errorCategory
     * @param status
     * @param error
     * @param errorCodeRepository
     */
    public void setStatus(String modules, String errorCategory, HttpStatus status, Error error, ErrorCodeRepository errorCodeRepository) {
        Optional<ErrorCode> errorCodeOpt = errorCodeRepository.findByModulesAndErrorCategoryAndErrorCode(modules, errorCategory, error.errorCode);
        this.status = status.value();
        if (errorCodeOpt.isPresent()) {
            this.message = errorCodeOpt.get().getErrorMessage();
            this.errorCode = modules+"-"+errorCategory+"-"+errorCodeOpt.get().getErrorCode();
        }
    }
}
