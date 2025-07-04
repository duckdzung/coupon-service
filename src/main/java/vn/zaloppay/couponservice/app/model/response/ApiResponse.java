package vn.zaloppay.couponservice.app.model.response;

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
