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

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

// Imported JAR to copy images on Windows at least
import com.philschatz.swt.dnd.ImageDataTransfer;

import zowie.fractals.Activator;


/**
 * Object to look after copying a set of Fractal coordinates to the clipboard
 */
public class CopyAction extends Action
{
	private FractalEditor editor;


	public CopyAction()
	{
		super( "Copy" );
	}

	public void run()
	{
		try {
			editor.getClipboard().setContents(
				new Object[] { editor.getCanvas().getImage().getImageData() , editor.getModel().getCoords().toString(), },
				new Transfer[] { ImageDataTransfer.getInstance(), TextTransfer.getInstance(), }
			);
		} catch (SWTError error) {
			Activator.getDefault().logException( error );
		}
	}
	
	public FractalEditor getEditor() { return editor; }
	public void setEditor(FractalEditor editor) { this.editor = editor; }
}
