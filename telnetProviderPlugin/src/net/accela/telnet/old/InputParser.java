package net.accela.telnet.old;

import net.accela.prisma.terminal.old.PrismaWM;
import net.accela.prisma.terminal.old.PointEvent;
import net.accela.prisma.terminal.old.InputEventDecoder;
import net.accela.prisma.textgfx.deprecated.CSISequence;
import net.accela.prisma.terminal.old.Terminal;
import net.accela.prisma.textgfx.deprecated.Crayon;
import net.accela.prisma.terminal.old.InputEvent;
import net.accela.prisma.gui.drawabletree.NodeNotFoundException;
import net.accela.prisma.gui.geometry.Rect;
import net.accela.prisma.gui.geometry.Size;
import net.accela.server.AccelaAPI;
import net.accela.server.plugin.Plugin;
import net.accela.telnet.old.TelnetSessionServer;
import net.accela.telnet.session.TelnetSession;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Parses input and relays it to the sessions WindowManager, either as an Event or through an OutputStream,
 * depending on which one is preferred.
 */
// FIXME: 11/9/20 Make this into a separate thread to prevent freezes
//  note, this is no longer that big of an issue as callEvent
//  is now multithreaded and offhands tasks to thread.
//  Still worth looking into however.
public class InputParser {
    final TelnetSession session;
    final Plugin plugin;
    final TelnetSessionServer sessionServer;
    final InputEventDecoder ansiParser;
    volatile CountDownLatch latch;

    public enum Mode {
        NORMAL,
        DETECT_SIZE,
        DETECT_UNICODE
    }

    @NotNull
    volatile Mode mode = Mode.NORMAL;

    public InputParser(@NotNull TelnetSession session, @NotNull TelnetSessionServer sessionServer) {
        this.session = session;
        this.sessionServer = sessionServer;
        this.plugin = session.getCreator().getPlugin();
        this.ansiParser = new InputEventDecoder(session.getCreator().getPlugin());
    }

    public void processDecoded(@NotNull String decoded) {
        // Try to turn it into an InputEvent
        InputEvent inputEvent = ansiParser.parse(decoded);
        if (inputEvent == null) return;

        // Send or parse the InputEvent
        switch (mode) {
            case NORMAL:
                // Check if the session has a WindowManager
                PrismaWM windowManager = session.getWindowManager();
                if (windowManager != null) {
                    session.getLogger().log(Level.INFO, "Calling event " + inputEvent);
                    AccelaAPI.getPluginManager().callEvent(inputEvent, windowManager.getBroadcastChannel());
                } else {
                    session.getLogger().log(Level.WARNING, "Engine down when trying to call an event via PluginManager");
                }
                break;
            case DETECT_SIZE:
                // If we got something, check if it's a PointEvent
                if (inputEvent instanceof PointEvent) {
                    PointEvent PointInputEvent = (PointEvent) inputEvent;

                    // Update the size
                    session.getTerminal().setSize(
                            new Size(PointInputEvent.getPoint().getX(), PointInputEvent.getPoint().getY())
                    );

                    // Change mode back to normal and unlock
                    reset();
                }
                break;
            case DETECT_UNICODE:
                // If we got something, check if it's a PointEvent.
                if (inputEvent instanceof PointEvent) {
                    PointEvent PointInputEvent = (PointEvent) inputEvent;

                    // See https://ryobbs.com/doku/terminal.php for info on the logic behind this.
                    // I probably butchered the idea... but my implementation seems to work *shrug*
                    if (PointInputEvent.getPoint().getX() == 7) {
                        session.getTerminal().addCharsetSupport(Terminal.UTF8_CHARSET);
                    } else {
                        session.getTerminal().removeCharsetSupport(Terminal.UTF8_CHARSET);
                    }

                    session.getLogger().log(Level.INFO, "point: " + PointInputEvent);

                    // Change mode back to normal and unlock
                    reset();
                }
                break;

        }
    }

    void reset() {
        // Change mode back to normal and unlock
        mode = Mode.NORMAL;

        // Count down
        latch.countDown();
        latch = null;
    }

    @NotNull
    public CountDownLatch updateTerminalSize() throws IOException {
        // New latch
        latch = new CountDownLatch(1);

        // Change mode
        mode = Mode.DETECT_SIZE;

        // Form a request
        String request =
                /* Move the cursor to the very bottom of the lowlevel */
                CSISequence.CSI_STRING + "255B" +
                        /* Move the cursor to the very right of the lowlevel */
                        CSISequence.CSI_STRING + "255C" +
                        /* Ask for the cursor location */
                        CSISequence.CSI_STRING + "6n";

        // Send the request
        sessionServer.writeToClient(request.getBytes(session.getTerminal().getCharset()));

        return latch;
    }

    public void builtinUpdateTerminalSize() throws IOException, InterruptedException, NodeNotFoundException {
        System.out.println("Updating lowlevel size");

        CountDownLatch latch = updateTerminalSize();

        // Wait for the detection to complete, or time out
        latch.await(1000, TimeUnit.MILLISECONDS);

        System.out.println("Updated lowlevel size! New value: " + session.getTerminal().getSize());

        // Clear and redraw
        sessionServer.writeToClient(CSISequence.CLR_STRING.getBytes(session.getTerminal().getCharset()));
        PrismaWM windowManager = session.getWindowManager();
        if (windowManager != null) windowManager.paint(new Rect(session.getTerminal().getSize()));
    }

    @NotNull
    public CountDownLatch updateUnicodeSupport() throws IOException {
        // New latch
        latch = new CountDownLatch(1);

        // Change to UTF-8 temporarily so that we can perform this detection
        session.getTerminal().setCharset(Terminal.UTF8_CHARSET);

        // Change mode
        mode = Mode.DETECT_UNICODE;

        // Form a request
        String request =
                /* Clear to reset the cursor */
                CSISequence.CLR_STRING +
                        /* Black FG and BG so that the characters aren't visible. Makes for a cleaner look */
                        new Crayon().blackBg(true).blackBg(false) +
                        /* Print three unicode characters that are 3 bytes in UTF-8 encoding */
                        "㐸惵㲒" +
                        /* Request to know where the cursor is now */
                        CSISequence.CSI_STRING + "6n";

        // Send the request
        sessionServer.writeToClient(request.getBytes(session.getTerminal().getCharset()));

        return latch;
    }

    public void builtinUpdateUnicodeSupport() throws IOException, InterruptedException, NodeNotFoundException {
        System.out.println("Updating lowlevel size");

        CountDownLatch latch = updateUnicodeSupport();

        latch.await(1000, TimeUnit.MILLISECONDS);

        // Change the charset back to normal
        // If we DON'T support unicode, change the charset so that
        // CP437/IBM437 extended characters works.
        // If we DO support unicode, don't change anything, it's good as-is already.
        boolean unicodeSupport = true;
        if (!session.getTerminal().getSupportedCharsets().contains(Terminal.UTF8_CHARSET)) {
            session.getTerminal().setCharset(Terminal.IBM437_CHARSET);
            unicodeSupport = false;
        }

        System.out.println("Updated unicode support! New value: " + unicodeSupport);

        // Clear and redraw
        sessionServer.writeToClient(CSISequence.CLR_STRING.getBytes(session.getTerminal().getCharset()));
        PrismaWM windowManager = session.getWindowManager();
        if (windowManager != null) windowManager.paint(new Rect(session.getTerminal().getSize()));
    }
}