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

import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import zowie.fractals.FractalController;

public class MandelPropsAdaptor implements IPropertySource
{
	private FractalController controller;

	public MandelPropsAdaptor( FractalController controller )
	{
		this.controller = controller;
	}

	private static PropertyDescriptor  cornerXProp = new PropertyDescriptor( "corner.x", "X" ),
		cornerYProp = new PropertyDescriptor( "corner.y", "Y" ), 
		dimsXProp = new PropertyDescriptor( "window.width", "Width" ), 
		dimsYProp = new PropertyDescriptor( "windows.heigh", "Height" ),
		itersProp = new TextPropertyDescriptor( "iters", "Iterations" );

	private static final IPropertyDescriptor[] propDescriptors = {
		cornerXProp, cornerYProp, dimsXProp, dimsYProp, itersProp
	};

	static {
		cornerXProp.setCategory( "Corner" );
		cornerYProp.setCategory( "Corner" );
		dimsXProp.setCategory( "Dimensions" );
		dimsYProp.setCategory( "Dimensions" );
		itersProp.setValidator( new ICellEditorValidator() {
			
			/** Return null iff value is a valid integer String */
			public String isValid(Object value)
			{
				try {
					int iters = Integer.parseInt( value.toString() );
					if( iters > 10 )
						return null;
					return value + " must be more than 10";
				} catch( NumberFormatException ex ) {
					return value  + " is not a number";
				}
			}
		} );
	}

	public Object getEditableValue()
	{
		return controller;
	}

	public IPropertyDescriptor[] getPropertyDescriptors()
	{
		return propDescriptors;
	}

	public Object getPropertyValue(Object id)
	{
		if( id.equals( cornerXProp.getId() ) ) return controller.getModel().getCorner().getReal();
		if( id.equals( cornerYProp.getId() ) ) return controller.getModel().getCorner().getImaginary();

		if( id.equals( dimsXProp.getId() ) ) return controller.getModel().getDimensions().getX();
		if( id.equals( dimsYProp.getId() ) ) return controller.getModel().getDimensions().getY();

		if( id.equals( itersProp.getId() ) ) return controller.getModel().getIters() + "";

		throw new Error( "Unknown property id " + id );
	}

	public boolean isPropertySet( Object id )
	{
		if( id.equals( itersProp.getId() ) )
			return controller.getModel().getIters() != FractalEditor.DEFAULT_ITERS;
		
		for( IPropertyDescriptor desc : propDescriptors )
			if( desc.getId().equals( id ) )
				return true;
		return false;
	}

	public void resetPropertyValue(Object id)
	{
		if( id.equals( itersProp.getId() ) ) 
			setPropertyValue( id, FractalEditor.DEFAULT_ITERS );
	}

	public void setPropertyValue(Object id, Object value)
	{
		if( id.equals( itersProp.getId() ) ) 
		{
			controller.getModel().setIters( Integer.parseInt( value.toString() ) );
			controller.calculate(); // Get the controller to redraw
		}
	}
}
