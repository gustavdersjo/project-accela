package net.accela.prisma.property;

import net.accela.prisma.Drawable;
import net.accela.prisma.exception.NodeNotFoundException;
import net.accela.prisma.geometry.Rect;
import net.accela.prisma.geometry.Size;
import org.jetbrains.annotations.NotNull;

public interface SizeReadable {
    /**
     * @return The size of this {@link Drawable}.
     */
    @NotNull Size getSize() throws NodeNotFoundException;

    /**
     * @return The width of this {@link Drawable}.
     * @see Rect#getWidth()
     * @see Size#getWidth()
     */
    default int getWidth() throws NodeNotFoundException {
        return getSize().getWidth();
    }

    /**
     * @return The height of this {@link Drawable}.
     * @see Rect#getHeight()
     * @see Size#getHeight()
     */
    default int getHeight() throws NodeNotFoundException {
        return getSize().getHeight();
    }

    /**
     * @return The capacity (width * height) of this {@link Drawable}.
     * @see Rect#getCapacity()
     * @see Size#getCapacity()
     */
    default int getCapacity() throws NodeNotFoundException {
        return getSize().getCapacity();
    }
}
