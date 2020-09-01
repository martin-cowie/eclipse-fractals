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

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorActionBarContributor;

public class FractalEditorContrib extends EditorActionBarContributor
{

	private CopyAction copyHandler;
	private FractalEditor editor;

	public void init(IActionBars bars, IWorkbenchPage page)
	{
		super.init(bars, page);
		bars.setGlobalActionHandler( ActionFactory.COPY.getId(), copyHandler ); 		
	}

	public void setActiveEditor(IEditorPart targetEditor)
	{
		super.setActiveEditor(targetEditor);
		editor = (FractalEditor)targetEditor;
		copyHandler.setEditor( editor );
	}
	
	public FractalEditorContrib()
	{
		copyHandler = new CopyAction();
	}
	
	

}
