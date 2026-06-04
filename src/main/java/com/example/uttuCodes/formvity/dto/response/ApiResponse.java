package com.example.uttuCodes.formvity.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ApiResponse<T> extends BaseResponse {

    private T data;
    private String message;

    public ApiResponse(int status, T data, String message) {
        super(status);
        this.data = data;
        this.message = message;
    }

    public static <T> ApiResponse<T> of(HttpStatus httpStatus, T data, String message) {
        return new ApiResponse<>(httpStatus.value(), data, message);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return of(HttpStatus.OK, data, "Success");
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return of(HttpStatus.OK, data, message);
    }

    public static <T> ApiResponse<T> created(T data) {
        return of(HttpStatus.CREATED, data, "Created");
    }


    public static <T> ApiResponse<T> created(T data, String message) {
        return of(HttpStatus.CREATED, data, message);
    }
}
