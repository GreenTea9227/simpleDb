package mysqlconnect;

public class NoConnection extends RuntimeException {
    public NoConnection() {
        super();
    }

    public NoConnection(String message) {
        super(message);
    }

    public NoConnection(Throwable cause) {
        super(cause);
    }
}
