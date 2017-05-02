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
 * DataLink packet
 * 
 * @author kevin
 */
public class DLPacket implements DataLinkConst {
    /** Data end time in microseconds */
    private long dataend;

    /** Data size in bytes */
    private int datasize;

    /** Data start time in microseconds */
    private long datastart;

    /** Packet ID */
    private long pktid;

    /** Packet time in microseconds */
    private long pkttime;

    /** Stream ID */
    private String streamid;

    /**
     * Clear the DataLink packet.
     */
    public void clear() {
        dataend = 0;
        datasize = 0;
        pktid = 0;
        pkttime = 0;
        streamid = null;
    }

    /**
     * @return the dataend in microseconds
     */
    public long getDataend() {
        return dataend;
    }

    /**
     * @return the datasize
     */
    public int getDatasize() {
        return datasize;
    }

    /**
     * @return the datastart in microseconds
     */
    public long getDatastart() {
        return datastart;
    }

    /**
     * @return the pktid
     */
    public long getPktid() {
        return pktid;
    }

    /**
     * @return the pkttime in microseconds
     */
    public long getPkttime() {
        return pkttime;
    }

    /**
     * @return the streamid
     */
    public String getStreamid() {
        return streamid;
    }

    /**
     * @return true if this packet is empty, false otherwise.
     */
    public boolean isEmpty() {
        return datasize == 0;
    }

    /**
     * Parse the text which contains the <code>streamid</code>,
     * <code>pktid</code>, <code>pkttime</code>, <code>datastart</code>,
     * <code>dataend</code> and <code>datasize</code>.
     * 
     * @param s
     *            the text.
     * @return true if successful, false otherwise.
     */
    public boolean parse(String s) {
        try {
            String ra[] = s.split(RE_WS, 6);
            if (ra.length == 6) {
                String streamid = ra[0];
                final long spktid = Long.parseLong(ra[1]);
                final long spkttime = Long.parseLong(ra[2]);
                final long datastart = Long.parseLong(ra[3]);
                final long sdataend = Long.parseLong(ra[4]);
                final int sdatasize = Integer.parseInt(ra[5]);
                if (sdatasize <= MAXPACKETSIZE) {
                    this.setStreamid(streamid);
                    this.setPktid(spktid);
                    this.setPktid(spkttime);
                    this.setDatastart(datastart);
                    this.setDataend(sdataend);
                    this.setDatasize(sdatasize);
                    return true;
                }
            }
        } catch (Exception ex) {
        }
        return false;
    }

    /**
     * @param dataend
     *            the dataend to set in microseconds
     */
    public void setDataend(long dataend) {
        this.dataend = dataend;
    }

    /**
     * @param datasize
     *            the datasize to set
     * @throws IllegalArgumentException
     *             if the data size is illegal.
     */
    public void setDatasize(int datasize) {
        if (datasize < 0 || datasize > MAXPACKETSIZE) {
            throw new IllegalArgumentException(
                    "Invalid data size (" + datasize + ")");
        }
        this.datasize = datasize;
    }

    /**
     * @param datastart
     *            the datastart to set in microseconds
     */
    public void setDatastart(long datastart) {
        this.datastart = datastart;
    }

    /**
     * @param pktid
     *            the pktid to set
     */
    public void setPktid(long pktid) {
        this.pktid = pktid;
    }

    /**
     * @param pkttime
     *            the pkttime to set in microseconds
     */
    public void setPkttime(long pkttime) {
        this.pkttime = pkttime;
    }

    /**
     * @param streamid
     *            the streamid to set
     */
    public void setStreamid(String streamid) {
        this.streamid = streamid;
    }

    @Override
    public String toString() {
        return streamid + ' ' + pktid + ' ' + pkttime + ' ' + datastart + ' ' +
                dataend + ' ' + datasize;
    }
}
