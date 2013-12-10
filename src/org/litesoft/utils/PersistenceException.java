package org.litesoft.utils;

public class PersistenceException extends RuntimeException {
    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public PersistenceException(Throwable cause) {
        super(cause);
    }

    private String mAugmentedMessage = null;

    public String getAugmentedMessage() {
        return mAugmentedMessage;
    }

    public void setAugmentedMessage(String pAugmentedMessage) {
        mAugmentedMessage = pAugmentedMessage;
    }

    /**
     * Returns the detail message string of this throwable.
     *
     * @return the detail message string of this <tt>Throwable</tt> instance
     *         (which may be <tt>null</tt>).
     */
    @Override
    public String getMessage() {
        String zMessage = super.getMessage();
        if (mAugmentedMessage != null) {
            zMessage += mAugmentedMessage;
        }
        return zMessage;
    }
}
