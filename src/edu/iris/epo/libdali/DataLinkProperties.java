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

import java.util.Properties;

public class DataLinkProperties {
    /** The default properties */
    private static final Properties DEFAULT_PROPERTIES;
    static {
        final Properties props = new Properties();
        for (DataLinkPropertiesKey key : DataLinkPropertiesKey.values()) {
            props.put(key.getPropKey(), key.getDefaultValue());
        }
        DEFAULT_PROPERTIES = props;
    }

    /** The properties */
    private final Properties props;

    /**
     * Create the properties with the default values.
     */
    public DataLinkProperties() {
        props = new Properties(DEFAULT_PROPERTIES);
    }

    /**
     * Get the properties.
     * 
     * @return the properties.
     */
    public Properties getProperties() {
        return props;
    }

    /**
     * Get the property with the specified key.
     * 
     * @param key
     *            the property key.
     * @return the value with the specified key value.
     */
    public String getProperty(DataLinkPropertiesKey key) {
        return props.getProperty(key.getPropKey());
    }

    /**
     * Set the property with the specified key.
     * 
     * @param key
     *            the property key.
     * @param value
     *            the value or null for the default value.
     */
    public void setProperty(DataLinkPropertiesKey key, String value) {
        if (value == null) {
            props.remove(key.getPropKey());
        } else {
            props.setProperty(key.getPropKey(), value);
        }
    }
}
