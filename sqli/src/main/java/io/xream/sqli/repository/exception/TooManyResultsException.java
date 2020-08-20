package io.xream.sqli.repository.exception;

public class TooManyResultsException extends RuntimeException {

    private static final long serialVersionUID = 5741842995845366081L;
    private String message;

    public TooManyResultsException(){
    }

    public TooManyResultsException(String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}