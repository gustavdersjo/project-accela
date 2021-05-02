package net.accela.prisma.terminal.old;

import net.accela.prisma.gui.geometry.Point;
import net.accela.prisma.gui.geometry.Size;
import net.accela.prisma.sequence.ANSIUtils;
import net.accela.prisma.sequence.SGRAttribute;
import net.accela.server.AccelaAPI;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public abstract class Terminal implements TerminalSafeMethods {
    //
    // Constants
    //

    // Constants - Size
    public final static Size DEFAULT_SIZE = new Size(80, 24);

    // Constants - Charset
    public final static Charset UTF8_CHARSET = StandardCharsets.UTF_8;
    public final static Charset ASCII_CHARSET = StandardCharsets.US_ASCII;
    public final static Charset IBM437_CHARSET = Charset.forName("IBM437");
    public final static Charset DEFAULT_CHARSET = UTF8_CHARSET;
    public final static List<Charset> DEFAULT_SUPPORTED_CHARSETS = new ArrayList<>() {{
        add(Terminal.UTF8_CHARSET);
        add(Terminal.IBM437_CHARSET);
        add(Terminal.ASCII_CHARSET);
    }};

    // Constants - Colors
    public final static boolean DEFAULT_AIXTERM_COLOR_SUPPORT = true;
    public final static boolean DEFAULT_TABLE_COLOR_SUPPORT = true;
    public final static boolean DEFAULT_TRUE_COLOR_SUPPORT = true;
    public final static boolean DEFAULT_ICE_COLOR_SUPPORT = false;

    public final static boolean DEFAULT_FONT_CHANGE_SUPPORT = false;

    //
    // Flags
    //

    // Size
    protected @NotNull Size terminalSize = DEFAULT_SIZE;

    // Charset
    /**
     * Order matters. Preferred {@link Charset}s go first.
     */
    protected final @NotNull List<@NotNull Charset> supportedCharsets = DEFAULT_SUPPORTED_CHARSETS;
    protected @NotNull Charset charset = DEFAULT_CHARSET;

    // Colors
    protected boolean supportsAixtermColor = DEFAULT_AIXTERM_COLOR_SUPPORT;
    protected boolean supportsTableColor = DEFAULT_TABLE_COLOR_SUPPORT;
    protected boolean supportsTrueColor = DEFAULT_TRUE_COLOR_SUPPORT;
    protected boolean supportsIceColor = DEFAULT_ICE_COLOR_SUPPORT;

    protected boolean supportsFontChange = DEFAULT_FONT_CHANGE_SUPPORT;

    public Terminal() {
    }

    //
    // Getters
    //

    @Override
    public @NotNull Size getSize() {
        return terminalSize;
    }

    @Override
    public boolean supportsAixtermColor() {
        return supportsAixtermColor;
    }

    @Override
    public boolean supportsTableColor() {
        return supportsTableColor;
    }

    @Override
    public boolean supportsTrueColor() {
        return supportsTrueColor;
    }

    @Override
    public boolean supportsIceColor() {
        return supportsIceColor;
    }

    @Override
    public @NotNull List<@NotNull Charset> getSupportedCharsets() {
        return supportedCharsets;
    }

    @Override
    public @NotNull Charset getCharset() {
        return charset;
    }

    public boolean supportsFontChange() {
        return supportsFontChange;
    }

    //
    // Setters
    //

    public void setSize(@NotNull Size size) {
        terminalSize = size;
    }

    public void supportsAixtermColor(boolean bool) {
        supportsAixtermColor = bool;
    }

    public void supportsTableColor(boolean bool) {
        supportsTableColor = bool;
    }

    public void supportsTrueColor(boolean bool) {
        supportsTrueColor = bool;
    }

    public void supportsIceColor(boolean bool) {
        supportsIceColor = bool;
    }

    public void setCharset(@NotNull Charset charset) {
        if (supportedCharsets.contains(charset)) {
            AccelaAPI.getLogger().log(Level.INFO, "Changing charset to " + charset.name());
            this.charset = charset;
        } else {
            throw new UnsupportedCharsetException(charset.name());
        }
    }

    public void addCharsetSupport(@NotNull Charset charset) {
        synchronized (supportedCharsets) {
            if (!supportedCharsets.contains(charset)) supportedCharsets.add(charset);
        }
    }

    public void removeCharsetSupport(@NotNull Charset charset) {
        supportedCharsets.remove(charset);
    }

    public void supportsFontChange(boolean bool) {
        supportsFontChange = bool;
    }

    //
    // Output
    //

    public abstract void print(char chr);

    public abstract void print(@NotNull CharSequence str);

    public abstract void printLn(char chr);

    public abstract void printLn(@NotNull CharSequence str);

    /**
     * Moves the text cursor. 0-indexed.
     * @param point 0-indexed instead of ANSIs normal 1-based indexing.
     */
    public void setCursorPosition(@NotNull Point point){
        setCursorPosition(point.getX(), point.getY());
    }

    /**
     * Moves the text cursor. 0-indexed.
     * @param x X. 0-indexed instead of ANSIs normal 1-based indexing.
     * @param y Y. 0-indexed instead of ANSIs normal 1-based indexing.
     */
    public void setCursorPosition(int x, int y) {
        print(ANSIUtils.CSI + (y + 1) + ';' + (x + 1) + 'H');
    }

    public void resetAllSGRs() {
        print(ANSIUtils.makeSGRSequence(SGRAttribute.RESET));
    }

    public void enableSGR(@NotNull SGRAttribute attribute) {

    }
}