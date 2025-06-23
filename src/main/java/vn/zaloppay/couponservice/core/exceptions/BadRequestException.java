package vn.zaloppay.couponservice.core.exceptions;

public class BadRequestException extends  RuntimeException{

    public BadRequestException(String message) {
        super(message);
    }

}
