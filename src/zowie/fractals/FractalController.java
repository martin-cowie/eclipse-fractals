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

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tracker;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.views.properties.IPropertySource;

import zowie.fractals.editors.FractalEditor;
import zowie.fractals.editors.MandelPropsAdaptor;

/**
 * Implements the 'wiring' between a Fractal Calculator and an ImagePanel, 
 * to let the user zoom into and pan about the set 
 * @author COWIEM
 */
public class FractalController implements MouseListener, MouseMoveListener, Listener, ISelectionProvider, IAdaptable
{
	private Mandelbrot model;
	private ImagePanel canvas;
	private Tracker tracker;
	private Point panningPoint = null;
	private Cursor handCursor = null;
	private FractalEditor editor;
	private static final String VERSION;
	
	static {
		VERSION = Activator.getDefault().getBundle().getHeaders().get( org.osgi.framework.Constants.BUNDLE_VERSION ).toString();
	}
	

	public FractalController( final ImagePanel canvas, Mandelbrot mandelbrot, FractalEditor editor )
	{
		busyPointer = new Cursor( canvas.getDisplay(), SWT.CURSOR_WAIT );
		handCursor = new Cursor( canvas.getDisplay(), SWT.CURSOR_SIZEALL );


		tracker = new Tracker( canvas, SWT.RESIZE );
		tracker.setStippled( true );
		canvas.addMouseListener( this );
		canvas.addMouseMoveListener( this );
		canvas.addListener( SWT.MouseWheel, this );
		this.model = mandelbrot;
		this.canvas = canvas;
		this.editor = editor;
		
		canvas.setMenu( new Menu( canvas.getShell(), SWT.POP_UP ) );
		MenuItem whatsThis = new MenuItem( canvas.getMenu(), SWT.PUSH );
		whatsThis.setText( "What is this?" );
		whatsThis.addSelectionListener( new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				try {
					URL wikiURL = new URL( "http://en.wikipedia.org/wiki/Mandelbrot_Set" );
					openBrowser( wikiURL, wikiURL.toString(), null );
				} catch( Exception ex ) {
					throw new Error( ex );
				}
			}
		});
		MenuItem propItem = new MenuItem( canvas.getMenu(), SWT.PUSH );
		propItem.setText( "Properties ..." );
		propItem.addSelectionListener( new SelectionAdapter() {
			
			final static String propID = "org.eclipse.ui.views.PropertySheet";
			public void widgetSelected(SelectionEvent e)
			{
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView( propID );
				} catch (CoreException e1) {
					throw new Error( e1 );
				}
			}
		});
		
		MenuItem aboutItem = new MenuItem( canvas.getMenu(), SWT.PUSH );
		aboutItem.setText( "About ..." );
		aboutItem.addSelectionListener( new SelectionAdapter() {
			final String aboutStr = "Zowie.Fractals\nWritten by Martin Cowie\nVersion " + VERSION;
			public void widgetSelected(SelectionEvent e)
			{
				MessageDialog.openInformation( canvas.getShell(), "About", aboutStr );
			}
		} );
	}
	
	public void dispose()
	{
		busyPointer.dispose();
		handCursor.dispose();
	}
	
	public Mandelbrot getModel() { return model; }
	public ImagePanel getCanvas() { return canvas; }

	public void mouseDoubleClick( MouseEvent e )
	{
		scrollZoom( true, new Point( e.x, e.y ) );
	}

	public void mouseUp( MouseEvent ev )
	{
		if( ev.button == 2 )
		{
			panningPoint = null;		
			canvas.setCursor( null ); // default mouse pointer
			recordCoords();
		}
	}

	public void mouseDown( MouseEvent ev )
	{
		if( ev.button == 1 )
		{
			tracker.setRectangles( new Rectangle[] { new Rectangle( ev.x, ev.y, 0, 0 ) } );
			if( !tracker.open() ) return;
	
			Rectangle zoomRect = tracker.getRectangles()[0]; //NB: this automatically rights negative rectangles - thankfully
			if( zoomRect.height < 2 || zoomRect.width < 2 ) return; // Prevent single clicks causing a zoom
			Complex vCorner = model.getCorner();
			Dimensions vDimensions = model.getDimensions();
			
			// Scale the rectangle to complex coords					
			Complex newCorner = new Complex(vCorner);
			Dimensions newDimensions = new Dimensions(vDimensions);
			Point size = canvas.getSize();
			
			// Calculate the new corner LOW: Centralise these scaling calculations
			newCorner.x += ( zoomRect.x * vDimensions.x ) / size.x;
			newCorner.y += ( zoomRect.y * vDimensions.y ) / size.y;
	
			// Calculate the new width/height
			newDimensions.x = ( zoomRect.width * vDimensions.x ) / size.x;
			newDimensions.y = ( zoomRect.height * vDimensions.y ) / size.y;
	
			zoomTo( newCorner, newDimensions );
		}
		if( ev.button == 2 )
		{
			canvas.setCursor( handCursor );
			panningPoint = new Point( ev.x, ev.y );
		}
	}
	
	/**
	 * Tell location control that we're moving
	 */
	private void recordCoords() 
	{
		IWorkbenchPage page = editor.getSite().getPage();
        page.getNavigationHistory().markLocation( editor );
	}

	public void mouseMove( MouseEvent ev )
	{
		if( panningPoint == null ) return;

		Point now = new Point( ev.x, ev.y );
		Point delta = new Point( panningPoint.x - now.x, panningPoint.y - now.y );
		Point cDimensions = canvas.getSize();
		
		Dimensions vDimensions = model.getDimensions();
		Complex newCorner = new Complex( model.getCorner() );
		newCorner.x += (delta.x * vDimensions.x) / cDimensions.x;   
		newCorner.y += (delta.y * vDimensions.y) / cDimensions.y;
		zoomTo( newCorner, model.getDimensions(), false );

		panningPoint = now;
	}
	
	/** Handle mouse wheel events */
	public void handleEvent(Event event)
	{
		scrollZoom( event.count > 0, new Point( event.x, event.y ) ); //NB: All mouse wheel events seem to be in powers of three
	}

	/**
	 * Zoom in, or out, by the given number. Used by the scroll-wheel & double clicks
	 * @param i the zoom factor
	 * @param point the location of the mouse, relative to the Composite
	 */
	private void scrollZoom( boolean zoomIn, Point point )
	{		
		Point cSize = canvas.getSize();

		Point cDimensions = zoomIn? 
			new Point( cSize.y /2, cSize.y /2 ): 
			new Point( cSize.y *2, cSize.y *2 );
			
		Point cCorner = new Point( point.x - cDimensions.x /2, point.y - cDimensions.y /2 );
		Rectangle zoomRect = new Rectangle( cCorner.x, cCorner.y, cDimensions.x, cDimensions.y ); 			
		
		// Translate the rectangle into a corner and dimensions pair
		FractalCoords coords = model.getCoords();
		Complex newCorner = new Complex( coords.corner );
		newCorner.x += ( zoomRect.x * coords.dimensions.x ) / cSize.x;
		newCorner.y += ( zoomRect.y * coords.dimensions.y ) / cSize.y;

		// Calculate the new width/height
		Dimensions newDimensions = new Dimensions( coords.dimensions );
		newDimensions.x = ( zoomRect.width * coords.dimensions.x ) / cSize.x;
		newDimensions.y = ( zoomRect.height * coords.dimensions.y ) / cSize.y;

		zoomTo( newCorner, newDimensions );
	}

	public void zoomTo( Complex corner, Dimensions dimensions ) 
	{
		zoomTo( corner, dimensions, true );
	}
	
	/**
	 * Execute a change in coordinates, and record the new location
	 * @param coords
	 */
	public void zoomTo( Complex corner, Dimensions dimensions, boolean recordStep ) 
	{
		model.setCoords( corner, dimensions );
		canvas.setCursor( busyPointer ); 
		canvas.setImage( model.calculate( canvas ) );
		if( recordStep )
		{
			recordCoords();
			fireSelectionChanged();
		}
		editor.setDirty( true );
		canvas.setCursor( null ); 
	}
	
	private Cursor busyPointer;
	
	/** Recalculate the Mandelbrot set view */
	public void calculate()
	{
		canvas.setCursor( busyPointer ); 
		Image img = model.calculate( canvas );
		if( img != null ) canvas.setImage( img );
		canvas.setCursor( null );
	}

	public void openBrowser( URL url, String name, String toolTip )
	{
		try {
			final int style = IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR | IWorkbenchBrowserSupport.STATUS | IWorkbenchBrowserSupport.PERSISTENT;
			final IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser( style, "TRACKER", name, toolTip );
			browser.openURL(url);
		} catch( Exception ex ) {
			Activator.getDefault().logException( ex );
		}
	}

	// Selection handling ====================
	// .. necessary for the properties handling.
	
	private ListenerList selectionChangedListeners = new ListenerList();
	
	public void addSelectionChangedListener(ISelectionChangedListener listener)
	{
		selectionChangedListeners.add( listener );
	}

	public ISelection getSelection()
	{
		return new StructuredSelection( this );
	}

	public void removeSelectionChangedListener( ISelectionChangedListener listener )
	{
		selectionChangedListeners.remove( listener );
	}

	public void setSelection(ISelection selection)
	{
		/* do nothing */
	}
	
	/**
	 * Fire a selection change event
	 */
	public void fireSelectionChanged()
	{
		final SelectionChangedEvent event = new SelectionChangedEvent( this, new StructuredSelection( this  ) );
        Object[] listeners = selectionChangedListeners.getListeners();
        for (int i = 0; i < listeners.length; ++i) 
        {
            final ISelectionChangedListener l = (ISelectionChangedListener) listeners[i];
            SafeRunnable.run(new SafeRunnable() {
                public void run() {
                    l.selectionChanged( event );
                }
            });
        }
	}
	
	@SuppressWarnings("unchecked")
	public Object getAdapter( Class adapter )
	{
		return adapter.equals( IPropertySource.class ) 
			? new MandelPropsAdaptor( this ) 
			: null;
	}

}
