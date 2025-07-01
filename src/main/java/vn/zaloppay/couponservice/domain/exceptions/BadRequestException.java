package vn.zaloppay.couponservice.domain.exceptions;

public class BadRequestException extends  RuntimeException{

    public BadRequestException(String message) {
        super(message);
    }

}
