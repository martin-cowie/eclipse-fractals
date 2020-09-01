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

public class Dimensions extends Pair<Double>
{
	public Double getX() { return x; };
	public Double getY() { return y; };
	

	public Dimensions(Double x, Double y)
	{
		super(x, y);
	}
	
	public Dimensions( Pair<Double> pair )
	{
		super( pair );
	}

//	//TODO: These two are never used
//	public Dimensions divideBy(double factor)
//	{
//		return new Dimensions( this.x / factor, this.y / factor );
//	}
//	
//	public Dimensions multiplyBy(double factor)
//	{
//		return new Dimensions( this.x * factor, this.y * factor );
//	}
}
