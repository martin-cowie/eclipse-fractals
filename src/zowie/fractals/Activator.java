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

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin 
{
	// The plug-in ID
	public static final String PLUGIN_ID = "Zowie.Fractals";

	public static final String ICON_FORWARD = "forward.gif", ICON_BACK = "back.gif", ICON_HOME = "home_nav.gif", ICON_SAVE = "save_edit.gif";

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	protected void initializeImageRegistry(ImageRegistry reg)
	{
		Bundle bundle = Platform.getBundle( PLUGIN_ID );
		String[] IDs = new String[] { ICON_BACK, ICON_FORWARD, ICON_HOME, ICON_SAVE };
		
		for( int i=0; i< IDs.length; i++ )
		{
			URL url = FileLocator.find( bundle, new Path( "icons/" + IDs[i] ), null );
			if( url == null )
				this.logError( "Missing icon \"" + IDs[i] + "\"" );
			else
				reg.put( IDs[i], ImageDescriptor.createFromURL( url ) );
		}
	}
	
	public void logInfo( String msg )
	{
		getDefault().getLog().log( new Status( IStatus.INFO, PLUGIN_ID, IStatus.OK, msg, null ) );
	}
	
	public void logError( String msg )
	{
		getDefault().getLog().log( new Status( IStatus.ERROR, PLUGIN_ID, IStatus.OK, msg, null ) );
	}
	
	public void logException( Throwable thr )
	{
		getDefault().getLog().log( new Status( IStatus.INFO, PLUGIN_ID, IStatus.ERROR, thr.getMessage(), thr ) );

		//LOW: Can I detect which thread I'm running in?
		IStatus warning = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 1, thr.getLocalizedMessage(), null);
		ErrorDialog.openError( null, "Jupiter Environments", null, warning );
	}

}
