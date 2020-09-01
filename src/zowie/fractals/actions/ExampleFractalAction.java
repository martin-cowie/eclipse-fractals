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
package zowie.fractals.actions;

import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import zowie.fractals.FractalCoords;
import zowie.fractals.editors.FractalEditor;
import zowie.fractals.editors.FractalEditorInput;

public class ExampleFractalAction implements IWorkbenchWindowPulldownDelegate
{
	private Menu menu;
	
	public ExampleFractalAction() { /* do nothing */ }

	public void run(IAction action) 
	{
		newEditor( FractalCoords.presets.get( "Classic" ) );
	}

	public void selectionChanged(IAction action, ISelection selection) { /* do nothing */ }


	public void dispose() 
	{
		if( menu != null )
			menu.dispose();
	}

	public void init(IWorkbenchWindow window) 
	{
	}

	public Menu getMenu(Control parent)
	{
		if( menu == null ) menu = createMenu( parent );
		return menu;
	}

	private Menu createMenu( Control parent )
	{
		Menu result = new Menu( parent );
		
		for( final Map.Entry<String,FractalCoords> entry : FractalCoords.presets.entrySet() )
		{
			MenuItem item = new MenuItem( result, SWT.NONE ); 
			item.setText( entry.getKey() );
			item.addSelectionListener( new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e)
				{					
					newEditor( entry.getValue() );
				}				
			});
		}
		return result;
	}
	
	protected void newEditor( FractalCoords coords )
	{
		try {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			FractalEditor editor;
			editor = (FractalEditor)page.openEditor( new FractalEditorInput( coords ), FractalEditor.EDITOR_ID );
			editor.setDirty( true );
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
}