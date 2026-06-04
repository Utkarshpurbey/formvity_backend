package com.example.uttuCodes.formvity.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ErrorResponse extends BaseResponse {

    private String error;
    private String errorMessage;

    public ErrorResponse(int status, String error, String errorMessage) {
        super(status);
        this.error = error;
        this.errorMessage = errorMessage;
    }

    public static ErrorResponse of(HttpStatus httpStatus, String error, String errorMessage) {
        return new ErrorResponse(httpStatus.value(), error, errorMessage);
    }
}
