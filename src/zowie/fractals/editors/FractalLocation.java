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

import org.eclipse.ui.IMemento;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.NavigationLocation;

import zowie.fractals.FractalCoords;

/**
 * An object to stick into the Location History service 
 */
public class FractalLocation extends NavigationLocation
{
	private FractalCoords coords;

	public FractalLocation( FractalEditor editor, FractalCoords coords )
	{
		super( editor );
		this.coords = coords;
	}

	public boolean mergeInto( INavigationLocation other )
	{
		return ( other instanceof FractalLocation ) ? this.equals( other ) : false;
	}

	public void restoreLocation()
	{
		((FractalEditor)getEditorPart()).getController().zoomTo( coords.getCorner(), coords.getDimensions() );
	}

	public void restoreState(IMemento memento)
	{
		try {
			coords = FractalCoords.fromXML( memento.getTextData() );
		} catch ( Exception e) {
			throw new Error( e );
		}
	}

	public void saveState(IMemento memento)
	{
		memento.putTextData( coords.toXML() );
	}

	public void update()
	{
		coords = ((FractalEditor)getEditorPart()).getModel().getCoords();
	}
}
