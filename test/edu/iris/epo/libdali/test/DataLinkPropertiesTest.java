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

package edu.iris.epo.libdali.test;

import java.util.Properties;

import edu.iris.epo.libdali.DataLinkClient;
import edu.iris.epo.libdali.DataLinkConst;
import edu.iris.epo.libdali.DataLinkProperties;
import edu.iris.epo.libdali.DataLinkPropertiesKey;

public class DataLinkPropertiesTest implements DataLinkConst, Runnable {
    public static void main(String[] args) {
        new DataLinkPropertiesTest().run();
    }

    @Override
    public void run() {
        DataLinkClient dlc = new DataLinkClient(
                DataLinkClientTest.class.getSimpleName(), null);
        try {
            DataLinkPropertiesKey key;
            String value;
            DataLinkProperties dlprops = dlc.getDataLinkProperties();
            Properties props = dlprops.getProperties();
            
            key = DataLinkPropertiesKey.ADDRESS;
            value = "myhost";
            dlprops.setProperty(key, value);
            dlc.init();
            value = ":bogus";
            dlprops.setProperty(key, value);
            dlc.init();
            value = ":1234";
            dlprops.setProperty(key, value);
            System.err.println(props.toString());
            dlc.init();
            value = "myhost:bogus";
            dlprops.setProperty(key, value);
            dlc.init();
            value = "myhost:1234";
            dlprops.setProperty(key, value);
            dlc.init();
            
            key = DataLinkPropertiesKey.IOTIMEOUT;
            value = "bogus";
            dlprops.setProperty(key, value);
            dlc.init();            
        } finally {
            dlc.close();
        }
    }
}
