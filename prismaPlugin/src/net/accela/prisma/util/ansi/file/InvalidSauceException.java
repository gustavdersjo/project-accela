package net.accela.prisma.util.ansi.file;

public class InvalidSauceException extends RuntimeException {

    public InvalidSauceException(String reason) {
        super(reason);
    }

    public InvalidSauceException(Exception ex) {
        super(ex);
    }
}
