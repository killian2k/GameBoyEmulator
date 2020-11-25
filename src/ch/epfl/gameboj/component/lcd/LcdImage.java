package ch.epfl.gameboj.component.lcd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable class representing an image of the Gameboy.
 */
public final class LcdImage {
	private final int height;
	private final int width;
	private final List<LcdImageLine> lines;
	
	/**
	 * Constructor of the immutable class.
	 * @param height is the height of the gameboy's image.
	 * @param width is the width of the gameboy's image.
	 * @param l is the list of all the lines of the image.
	 */
	public LcdImage(int height, int width, List<LcdImageLine> l){
		this.height = height;
		this.width = width;
		lines = Collections.unmodifiableList(new ArrayList<>(l));
	}
	
	/**
	 * Getter of gameboy's height.
	 * @return the height in pixel
	 */
	public int height() {
		return height;
	}
	
	/**
	 * Getter of gameboy's width.
	 * @return the widthin pixel
	 */
	public int width() {
		return width;
	}
	
	/**
	 * Get the int color of the pixel.
	 * @param x : X-axis coordinated.
	 * @param y : Y-axis coordinated.
	 * @return the integer corresponding to the color (2 bits used).
	 */
	public int get(int x,int y) {
		Objects.checkIndex(x, width);
		Objects.checkIndex(y, height);
		return lines.get(y).getColor(x);
	}
	
	/**
	 * {@link java.lang.Object#equals(Object)}
	 */
	public boolean equals(Object that) {
		if(!(that instanceof LcdImage))
			return false;
		LcdImage im2 = (LcdImage)that;
		return height == im2.height && width == im2.width && lines.equals(im2.lines); 
	}
	
	/**
	 * {@link java.lang.Object#hashCode()}
	 */
	public int hashCode() {
		return Objects.hash(lines, height, width);
	}
	
	/**
	 * Intern class to have a builder of LcdImage.
	 */
	public static final class Builder {
		private final int height;
		private final int width;
		private List<LcdImageLine> lines;
		
		/**
		 * Constructor of the builder.
		 * @param height is the height in pixel of the future image.
		 * @param width is the width in pixel of the future image.
		 */
		public Builder(int height, int width) {
			this.height = height;
			this.width = width;
			lines = new ArrayList<>(Collections.nCopies(height, LcdImageLine.newEmptyLine(width)));
		}
		
		/**
		 * Set a specific line of the image
		 * @param index is the index of the line to set.
		 * @param l is the new line we want to allocate.
		 * @return the builder itself.
		 * @throws IllegalStateException if the builder has already built an instance.
		 * @throws NullPointerException if the new line given is null.
		 * @throws IndexOutOfBoundsException if the index of the line to set is not valid.
		 */
		public Builder setLine(int index, LcdImageLine l) {
			if(lines == null)
				throw new IllegalStateException();
			if(l == null)
				throw new NullPointerException();
			lines.set(index, l);
			return this;
		}
		
		/**
		 * build the lcdImage from the builder.
		 * @return The lcdImage corresponding to the builder.
		 * @throws IllegalStateException if the builder has already built an instance.
		 */
		public LcdImage build() {
			if(lines == null)
				throw new IllegalStateException();
			LcdImage im = new LcdImage(height, width, lines);
			lines = null;
			return im;
		}
	}
}
