package vn.zaloppay.couponservice.presenter.entities;

import lombok.Value;

@Value
public class ApiResponse {

   String message;

    boolean success;

    Object data;

    public static ApiResponse success(Object data, String message) {
        return new ApiResponse(message, true, data);
    }

    public static ApiResponse error(String message) {
        return new ApiResponse(message, false, null);
    }

}
