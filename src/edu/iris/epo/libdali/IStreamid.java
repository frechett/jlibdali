/**
 *    Copyright (C) 2017 IRIS (http://www.iris.edu/hq/).
 *    
 *    All inquiries should be sent to John Taber <taber@iris.edu>.
 *    
 *    This file is part of Jlibdali.
 *
 *    Jlibdali is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Jlibdali is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Jlibdali.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.iris.epo.libdali;

public interface IStreamid {
    /**
     * Get the stream identifier text.
     * 
     * @return the stream identifier text for the stream in the composite form:
     *         "W_X_Y_Z/TYPE" where the underscores and slash separate the
     *         components.
     */
    String getText();

    /** Get the Type component */
    String getType();

    /** Get the W component */
    String getW();

    /** Get the X component */
    String getX();

    /** Get the Y component */
    String getY();

    /** Get the Z component */
    String getZ();
}
