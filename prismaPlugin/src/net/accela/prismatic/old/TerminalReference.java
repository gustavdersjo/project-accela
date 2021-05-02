package net.accela.prisma.terminal.old;

import net.accela.prisma.gui.geometry.Size;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.List;

public class TerminalReference implements TerminalSafeMethods {
    final Terminal terminal;

    public TerminalReference(@NotNull Terminal terminal) {
        this.terminal = terminal;
    }

    @Override
    public @NotNull Size getSize() {
        return terminal.getSize();
    }

    @Override
    public boolean supportsAixtermColor() {
        return terminal.supportsAixtermColor();
    }

    @Override
    public boolean supportsTableColor() {
        return terminal.supportsTableColor();
    }

    @Override
    public boolean supportsTrueColor() {
        return terminal.supportsTrueColor();
    }

    @Override
    public boolean supportsIceColor() {
        return terminal.supportsIceColor();
    }

    @Override
    public @NotNull List<@NotNull Charset> getSupportedCharsets() {
        return terminal.getSupportedCharsets();
    }

    @Override
    public @NotNull Charset getCharset() {
        return terminal.getCharset();
    }

    @Override
    public boolean supportsFontChange() {
        return terminal.supportsFontChange();
    }
}