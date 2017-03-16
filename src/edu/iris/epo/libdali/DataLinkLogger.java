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
import java.util.logging.Logger;

/**
 * The default DataLink logger.
 * 
 * @author kevin
 */
public class DataLinkLogger implements IDataLinkLogger {
    private final Logger logger;

    /**
     * Create the DataLink logger.
     */
    public DataLinkLogger() {
        this(Logger.getGlobal());
    }

    /**
     * Create the DataLink logger.
     * 
     * @param logger
     *            the logger.
     */
    public DataLinkLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void log(Level level, String msg) {
        logger.log(level, msg);
    }

    @Override
    public void log(Level level, String msg, Throwable thrown) {
        logger.log(level, msg, thrown);
    }
}
