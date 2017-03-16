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

import java.util.logging.Level;

public interface IDataLinkLogger {
    /**
     * Log a message.
     * <p>
     * If the logger is currently enabled for the given message level then the
     * given message is forwarded to all the registered output Handler objects.
     * <p>
     * 
     * @param level
     *            One of the message level identifiers, e.g., SEVERE
     * @param msg
     *            The string message (or a key in the message catalog)
     */
    public void log(Level level, String msg);

    /**
     * Log a message.
     * <p>
     * If the logger is currently enabled for the given message level then the
     * given message is forwarded to all the registered output Handler objects.
     * <p>
     * 
     * @param level
     *            One of the message level identifiers, e.g., SEVERE
     * @param msg
     *            The string message (or a key in the message catalog)
     * @param thrown
     *            Throwable associated with log message
     */
    public void log(Level level, String msg, Throwable thrown);
}
