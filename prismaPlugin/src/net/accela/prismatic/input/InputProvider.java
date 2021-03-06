package net.accela.prismatic.input;

import net.accela.prismatic.input.lanterna.actions.InputEvent;

import java.io.IOException;

/**
 * Objects implementing this interface can read character streams
 * and transform them into {@code Key} objects which can be read in a FIFO manner.
 */
public interface InputProvider {
    /**
     * Returns the next {@code Key} off the input queue or null if there is no more input events available.
     * Note, this method call is <b>not</b> blocking,
     * it returns null immediately if there is nothing on the input stream.
     *
     * @return Key object which represents a keystroke coming in through the input stream
     * @throws java.io.IOException Propagated error if the underlying stream gave errors
     * @see #readInput()
     */
    InputEvent pollInput() throws IOException;

    /**
     * Returns the next {@code Key} off the input queue or blocks until one is available.
     * Note, this method call is blocking and you can call {@link #pollInput()}
     * for the non-blocking version.
     *
     * @return Key object which represents a keystroke coming in through the input stream
     * @throws java.io.IOException Propagated error if the underlying stream gave errors
     * @see #pollInput()
     */
    InputEvent readInput() throws IOException;
}