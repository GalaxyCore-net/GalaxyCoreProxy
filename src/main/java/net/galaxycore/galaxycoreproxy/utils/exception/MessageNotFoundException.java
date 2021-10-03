package net.galaxycore.galaxycoreproxy.utils.exception;

public class MessageNotFoundException extends RuntimeException {

    public MessageNotFoundException(String s) {
        super("I18N Key not found: " + s);
    }

}
