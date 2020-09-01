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

public class Complex extends Pair<Double>
{
	public Complex( Double x, Double y )
	{
		super(x, y);
	}
	
	public Complex( Complex other )
	{
		super( other.x, other.y );
	}

	public Complex(Pair<Double> pair )
	{
		super( pair );
	}

	public Double getReal()
	{
		return x;
	}
	
	public Double getImaginary()
	{
		return y;
	}

	public Complex add( Pair<Double> other )
	{
		return new Complex( this.x + other.y, this.y + other.y );
	}

	public Complex subtract( Pair<Double> other )
	{
		return new Complex( this.x - other.y, this.y - other.y );
	}
}