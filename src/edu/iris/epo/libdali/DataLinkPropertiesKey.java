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

/**
 * DataLink properties key
 * 
 * @author kevin
 */
public enum DataLinkPropertiesKey implements DataLinkConst {
    /**
     * Address of the DataLink server in following format:
     * <p>
     * [host][:][port]
     * <p>
     * if host is omitted (e.g. ":16000"), the default host is used
     * <p>
     * if :port is omitted (e.g. "localhost"), the default port is used
     */
    ADDRESS(DATALINK_ADDRESS_TEXT),
    /**
     * Timeout for network I/O operations (milliseconds)
     */
    IOTIMEOUT(DATALINK_IOTIMEOUT);

    private final String defValue;
    private final String propkey;

    DataLinkPropertiesKey(Object defValue) {
        propkey = "DL_" + name();
        this.defValue = defValue.toString();
    }

    /**
     * Get the default value.
     * 
     * @return the default value.
     */
    public String getDefaultValue() {
        return defValue;
    }

    /**
     * Get the property key.
     * 
     * @return the property key.
     */
    public String getPropKey() {
        return propkey;
    }

    @Override
    public String toString() {
        return propkey;
    }
}
