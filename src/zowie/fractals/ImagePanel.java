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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;


/**
 * A Canvas that owns an Image, is smart enough to redraw itself, and notify interested parties on resize.
 * @author Martin Cowie
 */
public class ImagePanel extends Canvas
{
	private Image image = null;
	private Point lastSize = new Point( 0, 0 );

	public ImagePanel( Composite parent )
	{
		super( parent, SWT.NO_BACKGROUND  );
		this.addPaintListener( new PaintListener() {
			/** Redraw the Image - only iff there is one and if this is the last redraw event for a while */
			public void paintControl( PaintEvent ev )
			{
				if( ev.count != 0 ) return;

				Point size = getSize();
				if( !lastSize.equals( size ) )
				{
					handleResize( size );
					lastSize = size;
				} else 
					if( image != null ) ev.gc.drawImage( image, 0, 0 );
			}			
		} );
	}

	public void setImage( Image image )
	{
		this.image = image;
		this.lastSize = new Point( image.getBounds().width, image.getBounds().height );
		
		// Drop the image onto the canvas
		GC gc = new GC( this );
		gc.drawImage( image, 0, 0 );
		gc.dispose();		
	}
	
	public Image getImage()
	{
		return image;
	}

	/** Override this to be aware of a resize event 
	 * @param size */
	public void handleResize(Point size) { }

}
