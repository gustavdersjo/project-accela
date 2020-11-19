package net.accela.prisma.geometry;

import net.accela.prisma.geometry.exception.RectOutOfBoundsException;
import org.jetbrains.annotations.NotNull;

/**
 * A rectangle shape
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
    }

    public Rect(@NotNull Size size) {
        this.minX = 0;
        this.minY = 0;
        this.width = size.getWidth();
        this.height = size.getHeight();
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
    }

    public Rect(@NotNull Point point, @NotNull Size size) {
        this.minX = point.getX();
        this.minY = point.getY();
        this.width = size.getWidth();
        this.height = size.getHeight();
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
        if (getMinX() > getMaxX() || getMinY() > getMaxY()) {
            throw new RectOutOfBoundsException("negative points: " + this);
        }
        if (getWidth() < 1 || getHeight() < 1) {
            throw new RectOutOfBoundsException("no size: " + this);
        }
    }

    /**
     * @return A new Rect starting at (0,0), but with the same width and height as this one.
     */
    public Rect zero() {
        return new Rect(0, 0, width, height);
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
     * @return The X point of the lower right corner
     */
    public int getMaxX() {
        return minX + width - 1;
    }

    /**
     * @return The Y point of the lower right corner
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
    public @NotNull Size getSize() {
        return new Size(width, height);
    }

    /**
     * @return A {@link Point} representing the upper left corner
     */
    public @NotNull Point getStartPoint() {
        return new Point(minX, minY);
    }

    /**
     * @return A {@link Point} representing the lower right corner
     */
    public @NotNull Point getEndPoint() {
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
    public Point getCenterPoint() {
        return new Point(getCenterX(), getCenterY());
    }

    /**
     * @return true if the starting point of this {@link Rect} is negative
     */
    public boolean isNegative() {
        return minX < 0 || minY < 0;
    }

    /**
     * @return True if this fits inside the provided area
     */
    public final boolean fits(@NotNull Rect container) {
        return fits(container, this);
    }

    /**
     * @return True if item fits inside container
     */
    public static boolean fits(@NotNull Rect container, @NotNull Rect item) {
        //System.out.println(container + " vs " + item + " = " + result);
        return container.getMinX() <= item.getMinX() && container.getMinY() <= item.getMinY() &&
                container.getMaxX() >= item.getMaxX() && container.getMaxY() >= item.getMaxY();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Rect getBounds() {
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
     * {@inheritDoc}
     */
    public boolean contains(@NotNull Rect rect) {
        return this.contains(rect.minX, rect.minY, rect.width, rect.height);
    }

    public boolean contains(int X, int Y, int W, int H) {
        int w = this.width;
        int h = this.height;
        if ((w | h | W | H) < 0) {
            return false;
        } else {
            int x = this.minX;
            int y = this.minY;
            if (X >= x && Y >= y) {
                w += x;
                W += X;
                if (W <= X) {
                    if (w >= x || W > w) {
                        return false;
                    }
                } else if (w >= x && W > w) {
                    return false;
                }

                h += y;
                H += Y;
                if (H <= Y) {
                    return h < y && H <= h;
                } else return h < y || H <= h;
            } else {
                return false;
            }
        }
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
        /*
        if(rectA.equals(rectB)) return true;
        else return (rectA.minX < rectB.width && rectB.minX > rectA.width) &&
                (rectA.minY > rectB.height && rectB.minY < rectA.height);
        */
        int tw = rectA.width;
        int th = rectA.height;
        int rw = rectB.width;
        int rh = rectB.height;
        if (rw > 0 && rh > 0 && tw > 0 && th > 0) {
            int tx = rectA.minX;
            int ty = rectA.minY;
            int rx = rectB.minX;
            int ry = rectB.minY;
            rw += rx;
            rh += ry;
            tw += tx;
            th += ty;
            return (rw < rx || rw > tx) && (rh < ry || rh > ty) && (tw < tx || tw > rx) && (th < ty || th > ry);
        } else {
            return false;
        }
    }

    public Rect intersection(@NotNull Rect rect) {
        return intersection(this, rect);
    }

    public static Rect intersection(@NotNull Rect rectA, @NotNull Rect rectB) {
        Rect result;

        int x1 = Math.max(rectA.getMinX(), rectB.getMinX());
        int y1 = Math.max(rectA.getMinY(), rectB.getMinY());
        int x2 = Math.min(rectA.getMaxX(), rectB.getMaxX());
        int y2 = Math.min(rectA.getMaxY(), rectB.getMaxY());
        result = new Rect(x1, y1, x2 - x1 + 1, y2 - y1 + 1);

        /*
        int tx1 = rectA.minX;
        int ty1 = rectA.minY;
        int rx1 = rectB.minX;
        int ry1 = rectB.minY;
        int tx2 = tx1;
        tx2 += rectA.width;
        int ty2 = ty1;
        ty2 += rectA.height;
        int rx2 = rx1;
        rx2 += rectB.width;
        int ry2 = ry1;
        ry2 += rectB.height;

        if (tx1 < rx1) {
            tx1 = rx1;
        }

        if (ty1 < ry1) {
            ty1 = ry1;
        }

        if (tx2 > rx2) {
            tx2 = rx2;
        }

        if (ty2 > ry2) {
            ty2 = ry2;
        }

        tx2 -= tx1;
        ty2 -= ty1;

        result = new Rect(tx1, ty1, tx2, ty2);

         */

        //System.out.println("intersectingArea(" + rectA + ", " + rectB + ") = " + result);

        return result;
    }

    public Rect startPointAddition(@NotNull Rect addition) {
        return startPointAddition(this, addition);
    }

    public static Rect startPointAddition(@NotNull Rect relative, @NotNull Rect addition) {
        return new Rect(
                relative.minX + addition.minX,
                relative.minY + addition.minY,
                relative.width,
                relative.height
        );
    }

    public Rect startPointSubtraction(@NotNull Rect subtraction) {
        return startPointSubtraction(this, subtraction);
    }

    public static Rect startPointSubtraction(@NotNull Rect absolute, @NotNull Rect subtraction) {
        return new Rect(
                absolute.minX - subtraction.minX,
                absolute.minY - subtraction.minY,
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
    public String toString() {
        return this.getClass().getName() +
                "[" + minX + "," + minY + "," + width + "," + height + "]";
    }
}
