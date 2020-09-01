/*
This file is part of Zowie.Fractals
Copyright 2009 Martin Cowie

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package zowie.fractals;


import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Canvas;

/**
 * An object to calculate the Mandelbrot set of complex numbers, and render them to an ImagePanel
 * Inspired by  http://en.wikipedia.org/wiki/Mandelbrot_set#For_programmers
 *
 * @author COWIEM
 */
public class Mandelbrot extends FractalCoords
{
	public Mandelbrot( FractalCoords coords )
	{
		super( coords );
	}

	public Mandelbrot(Complex corner, Dimensions dimensions, int iters )
	{
		super( corner, dimensions, iters );
	}

	public void setCoords( Complex corner, Dimensions dimensions )
	{
		this.corner = new Complex( corner );
		this.dimensions = new Dimensions( dimensions );
	}

	/** Get the current view coordinates */
	public FractalCoords getCoords()
	{
		return new FractalCoords( corner, dimensions, iters );
	}

	/**
	 * Render the set & display it on the aforegiven ImagePanel.
	 * Adjust the complex coordinates to fix the aspect ratio of the ImagePanel
	 * @return 
	 */
	public Image calculate( Canvas canvas )
	{
		Point cDimensions = canvas.getSize();
		if( cDimensions.x ==0 && cDimensions.y == 0 ) return null;

		// Adjust the fractal space to fit the viewport
		//FIXME: adjusting these coordinates, causes 'zoom-out' problems after a few perspective changes
		if( isTall( cDimensions ) )
		{
			// Make the mandelspace taller & move the corner up 1/2 the extra space
			double y = dimensions.y;
			dimensions.y *= ( (double)cDimensions.y / (double)cDimensions.x ) / ( dimensions.y / dimensions.x );
			corner.y -= (dimensions.y - y) /2;
		} else {
			double x = dimensions.x;
			dimensions.x *= ( (double)cDimensions.x / (double)cDimensions.y ) / ( dimensions.x / dimensions.y );
			corner.x -= (dimensions.x -x) /2;
		}

		// Prepare the raw image LOW: get to the bottom of colorDepth
		ImageData iData = new ImageData( cDimensions.x, cDimensions.y, 8, new PaletteData( palette ) );

		// Render each point
		for( int y=0; y< cDimensions.y; y++ )
			for( int x=0; x< cDimensions.x; x++ )
			{
				// Retain the aspect ratio of the virtual co-ords, by extending it to fit the viewport
				double mapX = ((x * dimensions.x )/ cDimensions.x ) + corner.x;
				double mapY = ((y * dimensions.y )/ cDimensions.y ) + corner.y;
				iData.setPixel( x, y, calculatePixel( mapX, mapY ) );
			}

		// Drop the image onto the canvas
		return new Image( canvas.getDisplay(), iData );
	}

	/**
	 * Calculate the given pixel for the Mandelbrot set
	 * @param x, the real number
	 * @param y, the imaginary number
	 */
	private int calculatePixel( double x, double y )
	{
		double origX = x, origY = y;
		int i =0;

		for ( i=0; x*x + y*y <= (2*2)  &&  i < iters; i++ )
		{
			// Z = Z^2 + C, where Z and C are complex numbers
			// LOW: Maybe move this code into Complex.java
			double xtemp = x*x - y*y + origX;
			y = 2*x*y + origY;
			x = xtemp;
		}

		// Return the offset into the palette array 
		return i == iters 
			? BLACK // Black
			: ( i * BLACK ) / iters ; // A colour
	}

	private static final int BLACK = 200;
//	private static final int HSV_BASE = 240;
	
	/** The palette used by the raw image */
	private static RGB palette[];

	static {
		palette = new RGB[BLACK+1];		
		palette[BLACK] = new RGB( 0,0,0 );

//		// Compose a Rainbow of colours, starting at blue
//		for( int i=0; i< BLACK; i++ )
//		{
//			float hue = ( ( ( i*360 ) / palette.length ) + HSV_BASE ) % 360; 
//			palette[i] = new RGB( hue, 1f, 1f );		
//		}

		for (int i = 0, c = 255 ; i < 50 ; i++, c -= 5 )
			palette[i] = new RGB( 0, 255 - c,   c  );

		for( int i = 50, c = 255 ; i < 100 ; i++, c-= 5 )
			palette[i] = new RGB( 255 - c, 255, 0 );

		for (int i = 100, c = 255 ; i < 150 ; i++, c-= 5 )
			palette[i] = new RGB( c, c, 255 - c );

		for (int i = 150, c = 255 ; i < 200 ; i++, c-= 5 )
			palette[i] = new RGB( 255 - c, 0, 255 );

	}

	private static boolean isTall( Point dim )
	{
		return dim.y > dim.x;
	}
	
}
