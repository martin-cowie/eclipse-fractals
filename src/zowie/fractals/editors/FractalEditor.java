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
package zowie.fractals.editors;

import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

import org.eclipse.core.filebuffers.manipulation.ContainerCreator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.INavigationLocationProvider;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import zowie.fractals.Activator;
import zowie.fractals.FractalController;
import zowie.fractals.FractalCoords;
import zowie.fractals.ImagePanel;
import zowie.fractals.ImageTools;
import zowie.fractals.Mandelbrot;

/**
 * TODO; Allow change of iterations - encode iterations into FractalCoords
 * FIXME: Implement the selection service - http://www.eclipse.org/articles/Article-WorkbenchSelections/article.html
 * http://www.eclipse.org/articles/Article-Properties-View/properties-view.html
 * TODO: Copy & paste, that works at least in XP and MacOS
 * TODO: Fix file association, so that .fractal.png is possible
 * LOW: Fix zoom-outs caused by perspective changes (only possible from Eclipse 3.3 onwards)
 * LOW: The Julia set: http://hewgill.com/chaos-and-fractals/c13_julia.java
 * LOW: Better use of colours
 * Hosted it on http://www.zowie.org.uk/Zowie.Toys (Done using SFTP to Penfold, and rsync from there)
 */

public class FractalEditor extends EditorPart implements INavigationLocationProvider 
{
	public static final String 
		EDITOR_ID = "zowie.fractals.editors.FractalEditor",
		SUFFIX = ".fractal"; // LOW: Find an alternative to hard coding this

	/** Display surface */
	private ImagePanel canvas;

	/** Raw fractal engine */
	private Mandelbrot model;
	
	/** Does this document need saving? */
	private boolean dirty = false;

	private Composite parent;

	private FractalController controller;

	public static int DEFAULT_ITERS = 100;

	public FractalEditor() { }

	@Override
	public void dispose() 
	{
		controller.dispose();
		super.dispose();
	}

	public void doSaveAs()
	{
		SaveAsDialog dialog= new SaveAsDialog( parent.getShell() );

		dialog.setOriginalName( "myMandel" + SUFFIX );
		dialog.create();
		if (dialog.open() == Window.CANCEL)
			return;

		//FIXME: Not all of the resulting suffix is affixed by default

		IPath filePath= dialog.getResult();
		if (filePath == null)
			return;

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IFile file = workspace.getRoot().getFile( filePath );

		IProgressMonitor monitor = getProgressMonitor();
		doSave( monitor, file );
		
		// Most important step - to tie this editor with the new file
		setInput( new FileEditorInput( file  ) );
		setPartName( file.getName() );
	}

	
	public void doSave(IProgressMonitor monitor )
	{
		IEditorInput input = getEditorInput();
		if( input instanceof FractalEditorInput )
			doSaveAs();
		else {
			IFile file = ((FileEditorInput)getEditorInput()).getFile();
			doSave( monitor, file );
		}
	}

	
	/**
	 * Common 'save this file' functionality between 'Save' and 'Save As'
	 * @param monitor
	 * @param file
	 */
	public void doSave(IProgressMonitor monitor, IFile file )
	{
		try
		{
			InputStream pngStream = new ByteArrayInputStream( renderPng( canvas, model ) );
			
			if( file. exists() )
				file.setContents( pngStream, true, true, monitor );
			else {
				ContainerCreator creator = new ContainerCreator(file.getWorkspace(), file.getParent().getFullPath());
				creator.createContainer(new SubProgressMonitor(monitor, 1000));
				file.create( pngStream, false, new SubProgressMonitor(monitor, 1000));
			}
			setDirty( false );
		} catch( Exception ex ) {
			Activator.getDefault().logException( ex );
		}

	}

	public void init(IEditorSite site, IEditorInput input) throws PartInitException
	{
		setSite( site );
		setInput( input );
	}

	public boolean isDirty()
	{
		return dirty;
	}
	
	public void setDirty( boolean dirty )
	{
		if( this.dirty != dirty )
		{
			this.dirty = dirty; // Important to set the value *before* firing the event
			firePropertyChange( PROP_DIRTY );
		}
	}

	public boolean isSaveAsAllowed()
	{
		return true;
	}

	@Override
	public void createPartControl(Composite parent)
	{		
		this.parent = parent;

		canvas = new ImagePanel( parent ) {
			public void handleResize(Point size)
			{
				controller.calculate();
			}
		};
		
		IEditorInput input = getEditorInput();

		FractalCoords homeView = null;
		if( input instanceof FileEditorInput ) 
			try {
				homeView = loadCoords( (FileEditorInput)input );
				setPartName( getEditorInput().getName() );
			} catch ( Exception ex ) {
				Activator.getDefault().logException( ex );
			}
		else if( input instanceof FractalEditorInput )
			homeView = ((FractalEditorInput)input).getCoords();
		else
			throw new Error( "Unexpected object type \"" + input.getClass().getName() + "\"" );

		model = new Mandelbrot( homeView );

		// Wire up the controller (consider the ImagePanel the view, and the fractal as the model)
		controller = new FractalController( canvas, model, this );
		controller.calculate();
		
		// Wire this editor to the Properties view
		getSite().setSelectionProvider( controller );
	}

	/**
	 * Create an image of a PNG file in a byte buffer.
	 * Fractal coordinates are stored in as XML in a tEXt metadata node
	 * @param canvas
	 * @return
	 * @throws IOException 
	 * @throws IOException
	 */
	private static byte[] renderPng( ImagePanel canvas, Mandelbrot fractal ) throws IOException 
	{
		RenderedImage image = ImageTools.convert( canvas.getImage().getImageData() );			
		Iterator<ImageWriter> iterator = ImageIO.getImageWritersBySuffix( "png" );
		
		if(!iterator.hasNext()) throw new Error( "No image writer for PNG" );
		
		ImageWriter imagewriter = iterator.next();
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		imagewriter.setOutput( ImageIO.createImageOutputStream( bytes ) );
		
		// Create, populate & attach metadata ... The following is the favoured way of doing it
		IIOMetadata metadata = imagewriter.getDefaultImageMetadata( new ImageTypeSpecifier( image ), null);
		String formatNames[] = metadata.getMetadataFormatNames();
	
		for (String formatName : formatNames) 
		{
			Node node = metadata.getAsTree(formatName);
			if( formatName.contains( "png" ) ) 
			{
				IIOMetadataNode tEXtEntry = new IIOMetadataNode("tEXtEntry");
				tEXtEntry.setAttribute( "keyword", "fractalCoords" );
				tEXtEntry.setAttribute( "value", fractal.getCoords().toXML() );
				tEXtEntry.setAttribute( "compression", "none" );
	
				IIOMetadataNode tEXt = new IIOMetadataNode("tEXt");
				tEXt.appendChild(tEXtEntry);
				node.appendChild(tEXt);
				metadata.setFromTree(formatName, node);
			}
		}
		imagewriter.write(new IIOImage(image, null, metadata));
	
		bytes.flush();
		imagewriter.dispose();
		return bytes.toByteArray();
	}

	/**
	 * Extract the <code>fractalCoords</code> metadata from the given file
	 * @param input
	 * @return
	 * @throws CoreException 
	 * @throws IOException 
	 */
	private FractalCoords loadCoords( FileEditorInput input ) throws IOException, CoreException
	{
		Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("png");
		ImageReader reader = (ImageReader)readers.next();

		System.out.println( "Loading " + input.getFile() );
		ImageInputStream is = ImageIO.createImageInputStream( input.getFile().getContents() );
		reader.setInput( is );
		
		IIOMetadata metadata = reader.getImageMetadata( 0 );
		
		String fmtName = metadata.getNativeMetadataFormatName();		
		Element rootNode = (Element)metadata.getAsTree( fmtName );
		is.close();

		// Drill into //tEXt/tEXtEntry elements
		NodeList entries = ( (Element)rootNode.getElementsByTagName( "tEXt" ).item( 0 ) ).getChildNodes();
		for( int i=0; i<entries.getLength(); i++ )
		{
			Element textElem = (Element)entries.item( i );
			String keyword = textElem.getAttribute( "keyword" );
			if( keyword.equals( "fractalCoords" ) )
				try {
					return FractalCoords.fromXML( textElem.getAttribute( "value" ) );
				} catch ( Exception e ) {
					throw new IOException( input.getName() + " contains invalid fractalCoords metadata" );
				}
		}
		throw new IOException( input.getName() + " does not contain fractalCoords metadata" );
	}

	public void setFocus()
	{
		canvas.setFocus();
		controller.fireSelectionChanged();
	}

	/** swiped from  AbstractTextEditor */
	protected IProgressMonitor getProgressMonitor() {

		IProgressMonitor pm= null;

		IStatusLineManager manager= getStatusLineManager();
		if (manager != null)
			pm= manager.getProgressMonitor();

		return pm != null ? pm : new NullProgressMonitor();
	}

	private IStatusLineManager getStatusLineManager() {

		IEditorActionBarContributor contributor= getEditorSite().getActionBarContributor();
		if (!(contributor instanceof EditorActionBarContributor))
			return null;

		IActionBars actionBars= ((EditorActionBarContributor) contributor).getActionBars();
		if (actionBars == null)
			return null;

		return actionBars.getStatusLineManager();
	}
	
	// Navigation control ======================

	public INavigationLocation createEmptyNavigationLocation() 
	{
		return new FractalLocation( this, model.getCoords() );
	}

	public INavigationLocation createNavigationLocation() 
	{
		return new FractalLocation( this, model.getCoords() );
	}

	public Mandelbrot getModel() { return model; }
	public FractalController getController() { return controller; }
	public ImagePanel getCanvas() { return canvas; }

	
	// Clipboard handling ========================
	
	private Clipboard clipboard;

	public Clipboard getClipboard()
	{
		if( clipboard == null )
			clipboard = new Clipboard(getSite().getShell().getDisplay());
		return clipboard;
	}

}
