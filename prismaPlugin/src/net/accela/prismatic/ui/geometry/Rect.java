package net.accela.prismatic.ui.geometry;

import net.accela.prismatic.ui.geometry.exception.RectOutOfBoundsException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a rectangular shape
 */
public class Rect implements Shape {
    private final int minX, minY, width, height;

    //
    // Constructors and factory methods
    //

    public Rect() {
        this.minX = 0;
        this.minY = 0;
        this.width = 1;
        this.height = 1;

        validate();
    }

    public Rect(@NotNull Size size) {
        this.minX = 0;
        this.minY = 0;
        this.width = size.getWidth();
        this.height = size.getHeight();

        validate();
    }

    public Rect(int width, int height) throws RectOutOfBoundsException {
        this.minX = 0;
        this.minY = 0;
        this.width = width;
        this.height = height;

        validate();
    }

    public Rect(@NotNull Point point) {
        this.minX = point.getX();
        this.minY = point.getY();
        this.width = 1;
        this.height = 1;

        validate();
    }

    public Rect(@NotNull Point point, @NotNull Size size) {
        this.minX = point.getX();
        this.minY = point.getY();
        this.width = size.getWidth();
        this.height = size.getHeight();

        validate();
    }

    public Rect(int minX, int minY, int width, int height) throws RectOutOfBoundsException {
        this.minX = minX;
        this.minY = minY;
        this.width = width;
        this.height = height;

        validate();
    }

    public Rect(@NotNull Point start, @NotNull Point end) throws RectOutOfBoundsException {
        this.minX = start.getX();
        this.minY = start.getY();
        this.width = end.getX() - start.getX() + 1;  // +1 since we're using 0-based coordinates
        this.height = end.getY() - start.getY() + 1; // +1 since we're using 0-based coordinates

        validate();
    }

    /**
     * Validates this {@link Rect}.
     *
     * @throws RectOutOfBoundsException If it's not a valid area
     */
    private void validate() throws RectOutOfBoundsException {
        if (width < 1 || height < 1) {
            throw new RectOutOfBoundsException("Bad dimensions \n" + this);
        }
    }

    /**
     * @return A new {@link Rect} with the same width and height as this before, but starting at {@link Point}(0,0).
     */
    @NotNull
    public final Rect zero() {
        return new Rect(0, 0, width, height);
    }

    /**
     * @return A new {@link Rect} starting at the same point as before, but in a new {@link Size}.
     */
    @NotNull
    public final Rect resize(@NotNull Size size) {
        Point startPoint = getStartPoint();
        return new Rect(startPoint.getX(), startPoint.getY(), size.getWidth(), size.getHeight());
    }

    //
    // Getters, calculations and utilities
    //

    /**
     * @return The X point of the upper left corner
     */
    public int getMinX() {
        return minX;
    }

    /**
     * @return The Y point of the upper left corner
     */
    public int getMinY() {
        return minY;
    }

    /**
     * @return The X point of the advanced right corner
     */
    public int getMaxX() {
        return minX + width - 1;
    }

    /**
     * @return The Y point of the advanced right corner
     */
    public int getMaxY() {
        return minY + height - 1;
    }

    /**
     * @return The width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return The height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return The width multiplied with the height
     */
    public int getCapacity() {
        return getWidth() * getHeight();
    }

    /**
     * @return A {@link Size} representing this {@link Rect}.
     */
    @NotNull
    public Size getSize() {
        return new Size(width, height);
    }

    /**
     * @return A {@link Point} representing the upper left corner
     */
    @NotNull
    public Point getStartPoint() {
        return new Point(minX, minY);
    }

    /**
     * @return A {@link Point} representing the advanced right corner
     */
    @NotNull
    public Point getEndPoint() {
        return new Point(width, height);
    }

    /**
     * @return The X point of the center
     */
    public int getCenterX() {
        return this.getMinX() + this.getWidth() / 2;
    }

    /**
     * @return The Y point of the center
     */
    public int getCenterY() {
        return this.getMinY() + this.getHeight() / 2;
    }

    /**
     * @return A {@link Point} representing the center of this {@link Rect}
     */
    @NotNull
    public Point getCenterPoint() {
        return new Point(getCenterX(), getCenterY());
    }

    /**
     * @return True if the starting point of this {@link Rect} is negative
     */
    public boolean hasNegativeStartPoint() {
        return minX < 0 || minY < 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public Rect getBounds() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public boolean contains(@NotNull Point point) {
        return this.contains(point.getX(), point.getY());
    }

    public boolean contains(int X, int Y) {
        int w = this.width;
        int h = this.height;
        if ((w | h) < 0) {
            return false;
        } else {
            int x = this.minX;
            int y = this.minY;
            if (X >= x && Y >= y) {
                w += x;
                h += y;
                return (w < x || w > X) && (h < y || h > Y);
            } else {
                return false;
            }
        }
    }

    /**
     * @return True if this fits inside the provided area
     */
    @Override
    public final boolean contains(@NotNull Rect container) {
        return contains(container, this);
    }

    /**
     * @return True if item fits inside container
     */
    public static boolean contains(@NotNull Rect container, @NotNull Rect item) {
        return container.getMinX() <= item.getMinX() && container.getMinY() <= item.getMinY() &&
                container.getMaxX() >= item.getMaxX() && container.getMaxY() >= item.getMaxY();
    }

    /**
     * {@inheritDoc}
     */
    public final boolean intersects(@NotNull Rect rect) {
        return intersects(this, rect);
    }

    /**
     * @return True if the areas overlap
     */
    public static boolean intersects(@NotNull Rect rectA, @NotNull Rect rectB) {
        return intersection(rectA, rectB) != null;
    }

    @Nullable
    public Rect intersection(@NotNull Rect rect) {
        return intersection(this, rect);
    }

    @Nullable
    public static Rect intersection(@NotNull Rect rectA, @NotNull Rect rectB) {
        int minX = Math.max(rectA.getMinX(), rectB.getMinX());
        int minY = Math.max(rectA.getMinY(), rectB.getMinY());
        int maxX = Math.min(rectA.getMaxX(), rectB.getMaxX());
        int maxY = Math.min(rectA.getMaxY(), rectB.getMaxY());

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;

        if (width < 1 || height < 1) {
            return null;
        } else {
            return new Rect(minX, minY, width, height);
        }
    }

    @NotNull
    public static Rect combine(@NotNull Rect rectA, @NotNull Rect rectB) {
        final int initialMinX, initialMinY, initialMaxX, initialMaxY;
        initialMinX = Math.min(rectA.getMinX(), rectB.getMinX());
        initialMinY = Math.min(rectA.getMinY(), rectB.getMinY());
        initialMaxX = Math.max(rectA.getMaxX(), rectB.getMaxX());
        initialMaxY = Math.max(rectA.getMaxY(), rectB.getMaxY());

        final int resultingMinX, resultingMinY, resultingMaxX, resultingMaxY;
        resultingMinX = Math.min(initialMinX, initialMaxX);
        resultingMinY = Math.min(initialMinY, initialMaxY);
        resultingMaxX = Math.max(initialMinX, initialMaxX);
        resultingMaxY = Math.max(initialMinY, initialMaxY);

        final int resultingWidth, resultingHeight;
        resultingWidth = resultingMaxX - resultingMinX + 1;
        resultingHeight = resultingMaxY - resultingMinY + 1;

        return new Rect(resultingMinX, resultingMinY, resultingWidth, resultingHeight);
    }

    @NotNull
    public Rect startPointAddition(@NotNull Point addition) {
        return startPointAddition(this, addition);
    }

    @NotNull
    public static Rect startPointAddition(@NotNull Rect relative, @NotNull Point addition) {
        return new Rect(
                relative.minX + addition.getX(),
                relative.minY + addition.getY(),
                relative.width,
                relative.height
        );
    }

    @NotNull
    public Rect startPointSubtraction(@NotNull Point subtraction) {
        return startPointSubtraction(this, subtraction);
    }

    @NotNull
    public static Rect startPointSubtraction(@NotNull Rect absolute, @NotNull Point subtraction) {
        return new Rect(
                absolute.minX - subtraction.getX(),
                absolute.minY - subtraction.getY(),
                absolute.width,
                absolute.height
        );
    }

    //
    // Object overrides
    //

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Rect) {
            Rect rect = (Rect) obj;
            return minX == rect.minX &&
                    minY == rect.minY &&
                    width == rect.width &&
                    height == rect.height;
        }
        return super.equals(obj);
    }

    @Override
    @NotNull
    public String toString() {
        return this.getClass().getName() +
                "[x1=" + minX + ",y1=" + minY + ",x2=" + getMaxX() + ",y2=" + getMaxY()
                + ",w=" + width + ",h=" + height + "]";
    }
}
