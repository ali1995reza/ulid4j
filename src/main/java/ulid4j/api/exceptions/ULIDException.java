package ulid4j.api.exceptions;

public class ULIDException extends RuntimeException {

    public ULIDException() {
        super();
    }

    public ULIDException(String e) {
        super(e);
    }

    public ULIDException(Throwable e) {
        super(e);
    }
}
