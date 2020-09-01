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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import zowie.fractals.FractalCoords;

public class FractalEditorInput implements IEditorInput
{
	protected FractalCoords coords;
	
	public FractalCoords getCoords() { return coords; }

	public FractalEditorInput( FractalCoords coords )
	{
		this.coords = coords;
	}

	public boolean exists()
	{
		return false; //LOW: return true if the file has been saved
	}

	public ImageDescriptor getImageDescriptor()
	{
        return ImageDescriptor.getMissingImageDescriptor();
	}

	public String getName()
	{
		return coords.toString();
	}

	public IPersistableElement getPersistable()
	{
		return null; //LOW: Return a persistable set of coords
	}

	public String getToolTipText()
	{
		return coords.toString();
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter)
	{
		return null;
	}

}
