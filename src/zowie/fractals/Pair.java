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

public class Pair<T>
{	
	protected T x,y;
	
	public Pair( Pair<T> other )
	{
		this.x = other.x;
		this.y = other.y;
	}
	
	public Pair(T x, T y)
	{
		this.x = x;
		this.y = y;
	}

	public String toString()
	{
		return String.format( "{x:%s y:%s}", ""+x, ""+y );
	}
	
	public boolean equals()
	{
		return x.equals( x ) && y.equals( y );
	}
}