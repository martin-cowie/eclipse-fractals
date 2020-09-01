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

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** A tuple of a corner & dimensions values */
public class FractalCoords 
{
	protected Complex corner;
	protected Dimensions dimensions;
	protected int iters;


	public static final Map<String, FractalCoords> presets = new HashMap<String, FractalCoords>() {
		private static final long serialVersionUID = 6019209667366221635L;
		{
			put( "Classic", new FractalCoords( new Complex( -2.5, -1.35 ), new Dimensions( 3.6, 2.7 ), 100 ) );
			put( "Scientific American", new FractalCoords( new Complex( -0.58, -1.24 ), new Dimensions( 0.9, 0.66  ) , 100));
			put( "Bumps 1", new FractalCoords( new Complex(-1.41, -0.145 ), new Dimensions( 0.19, 0.12 ) , 100 ) );
			put( "Classic Whorl 1", new FractalCoords( new Complex(-0.22434423124559996, -0.6612661830521369), new Dimensions(0.014845101125826116, 0.016150164961063578) , 100 ) );
			put( "Pinwheel 4", new FractalCoords( new Complex( 0.27525331649616824, -0.610073738723913 ), new Dimensions( 0.008454899010592636, 0.008548842332932556 ), 100 ) );
		}
	};
	
	public FractalCoords( FractalCoords other )
	{
		this.corner = other.corner;
		this.dimensions = other.dimensions;
		this.iters = other.iters;
	}

	public FractalCoords( Complex corner, Dimensions dimensions, int iters)
	{
		this.corner = corner;
		this.dimensions = dimensions;
		this.iters = iters;
	}

	public String toString()
	{
		return String.format( "{corner: %s dimensions: %s}", corner, dimensions );
	}
	
	public boolean equals( FractalCoords obj )
	{
		if( !( obj instanceof FractalCoords ) )
			return false;
		FractalCoords other = (FractalCoords)obj;
		return this.corner.equals( other.corner ) && this.dimensions.equals( other.dimensions );
	}
		
	public Complex getCorner() { return corner; }
	public Dimensions getDimensions() { return dimensions; }
	public int getIters() { return iters; }
	
	public void setCorner(Complex corner) { this.corner = corner; }
	public void setDimensions(Dimensions dimensions) { this.dimensions = dimensions; }
	public void setIters(int iters) { this.iters = iters; }
	
	public String toXML()
	{
		try {
			DocumentBuilder builder;
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	        return toXML( builder.newDocument() );
		} catch (ParserConfigurationException e) {
			throw new Error( e );
		}
	}
	
	public String toXML( Document doc )
	{        
        Element rootElem = doc.createElement( "fractal" );
        rootElem.setAttribute( "algorithm", "mandelbrot" );
        rootElem.setAttribute( "iterations", iters + "" );
        doc.appendChild( rootElem );

        Element cornerElem = doc.createElement( "corner" ),
        	dimensionsElem = doc.createElement( "dimensions" );

        cornerElem.setAttribute( "x", corner.x + "" );
        cornerElem.setAttribute( "y", corner.y + "" );
        
        dimensionsElem.setAttribute( "x", dimensions.x + "" );
        dimensionsElem.setAttribute( "y", dimensions.y + "" );
        
        rootElem.appendChild( cornerElem );
        rootElem.appendChild( dimensionsElem );
        
        // Serialise the DOM to a String
        try {
			return Common.toString( doc );
		} catch (TransformerException e) {
			throw new Error( e );
		}
	}
	
	public static FractalCoords fromXML( String xmlStr ) throws SAXException, IOException, ParserConfigurationException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating( false );
        
        Document doc = factory.newDocumentBuilder().parse( new InputSource( new StringReader( xmlStr ) ) );
        Element rootElem = doc.getDocumentElement();
        int iters = Integer.parseInt( rootElem.getAttribute( "iterations" ) );
        
        return new FractalCoords( 
    		new Complex( fromXML( (Element)rootElem.getElementsByTagName( "corner" ).item( 0 )  ) ), 
    		new Dimensions( fromXML( (Element)rootElem.getElementsByTagName( "dimensions" ).item( 0 ) ) ), 
    		iters
        );
	}

	private static Pair<Double> fromXML( Element elem )
	{
		return new Pair<Double>(
			Double.parseDouble( elem.getAttribute( "x" ) ),
			Double.parseDouble( elem.getAttribute( "y" ) ) 
		);
	}

}
