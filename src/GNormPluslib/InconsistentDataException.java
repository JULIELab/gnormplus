package GNormPluslib;

public class InconsistentDataException extends RuntimeException {
    private String docId;

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public InconsistentDataException() {
    }

    public InconsistentDataException(String message) {
        super(message);
    }

    public InconsistentDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public InconsistentDataException(Throwable cause) {
        super(cause);
    }

    public InconsistentDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
