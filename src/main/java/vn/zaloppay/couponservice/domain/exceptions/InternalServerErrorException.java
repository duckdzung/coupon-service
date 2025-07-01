package vn.zaloppay.couponservice.domain.exceptions;

public class InternalServerErrorException extends RuntimeException{

    public InternalServerErrorException(String message) {
        super(message);
    }

}
