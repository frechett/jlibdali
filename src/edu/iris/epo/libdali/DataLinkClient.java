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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 * DataLink Client.
 * 
 * @author kevin
 */
public class DataLinkClient implements Closeable, DataLinkConst {
    /** DataLink return value */
    public enum DL_RETVAL {
        /** No error, success */
        _NO_ERROR,
        /** End Of File */
        EOF,
        /** Invalid header length */
        INVALID_HEADER_LEN,
        /** Invalid synchronization values (does not start with "DL") */
        INVALID_SYNC,
        /** Invalid argument supplied */
        INVALIDARG,
        /** Invalid response */
        INVALIDRESP,
        /** No data in non-blocking mode */
        NO_DATA,
        /**
         * No DataLink ID was found in the response
         * ({@link DataLinkConst#DATALINK_ID}
         */
        NO_DATALINK_ID,
        /** No socket */
        NO_SOCKET,
        /** No synchronization bytes or not enough (requires 3) */
        NO_SYNC,
        /** Error while receiving data */
        RECV_ERROR,
        /** Error while sending data */
        SEND_ERROR,
        /** Socket Timeout */
        SOCKET_TIMEOUT,
        /** Error, connection in streaming mode */
        STREAMING_ERROR;

        /**
         * Get the error number.
         * 
         * @return the error number.
         */
        public int getErrNum() {
            return ordinal();
        }

        /**
         * Determines if this return value is an error.
         * 
         * @return true if error, false otherwise.
         */
        public boolean isError() {
            return this != _NO_ERROR && this != NO_DATA;
        }
    }

    private static final AtomicBoolean first = new AtomicBoolean();

    /** Jlibdali version */
    public static final String VERSION = "1.0.2017.124";

    /**
     * Close the data destination quietly ignoring any I/O exceptions.
     * 
     * @param closeable
     *            the data destination or null if none.
     */
    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ex) {
        }
    }

    /**
     * Get the program version text.
     * 
     * @return the program version text.
     */
    public static String getVersion() {
        return "Jlibdali version " + VERSION;
    }

    private SocketAddress address;
    private int bytesread;
    private String clientid;
    private final DLPacket dlpacket = new DLPacket();
    private final DataLinkProperties dlprops;
    private boolean initFlag;
    private int iotimeout;
    private InputStream is;
    private final IDataLinkLogger logger;
    private String logprefix;
    private int maxpktsize;
    private OutputStream os;
    private byte[] readBuffer = new byte[MAXPACKETSIZE];
    private String readText;
    private int resp_size;
    private String resp_status;
    private String resp_value;
    private byte[] sendBuffer = new byte[MAXPACKETSIZE];
    private int sendBuflen;
    private float serverproto;
    private Socket socket;
    private boolean streamingEndFlag;
    private boolean streamingFlag;
    private volatile boolean terminateFlag;
    private boolean writeperm;

    /**
     * Create the DataLink client.
     * 
     * @param progname
     *            the program name, usually the simple name of the main class.
     * @param logger
     *            the logger or null for the default.
     */
    public DataLinkClient(String progname, IDataLinkLogger logger) {
        // ensure address is never null
        address = DATALINK_ADDRESS;
        iotimeout = DATALINK_IOTIMEOUT;
        dlprops = new DataLinkProperties();
        setLogprefix();
        if (logger == null) {
            logger = DataLinkUtils.createLogger();
        }
        this.logger = logger;
        clientid = DataLinkUtils.genClientid(progname);
        sendBuffer[sendBuflen++] = 'D';
        sendBuffer[sendBuflen++] = 'L';
        if (first.compareAndSet(false, true)) {
            logger.log(Level.INFO, DataLinkClient.getVersion());
        }
    }

    private void addBuffer(byte b) {
        sendBuffer[sendBuflen++] = b;
    }

    private void addBuffer(int b) {
        sendBuffer[sendBuflen++] = (byte) b;
    }

    private void addBuffer(String s) {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            addBuffer((byte) s.charAt(i));
        }
    }

    private final void clearBuffer() {
        sendBuflen = 2;
    }

    /**
     * Close the connection to a DataLink server.
     */
    @Override
    public void close() {
        terminateFlag = true;
        closeQuietly(socket);
        initFlag = false;
        os = null;
        is = null;
        socket = null;
    }

    /**
     * Collect a packet streaming from the DataLink server.
     * <p>
     * If the <code>endflag</code> is true the ENDSTREAM command is sent which
     * instructs the server to stop streaming packets; a client must continue
     * collecting packets until {@link #getPacket().getDatasize()} returns
     * <code>0</code> in order to get any packets that were in-the-air when
     * ENDSTREAM was requested.
     * 
     * @param endflag
     *            true to end, false otherwise.
     * @param blockflag
     *            true to block until data is available, false otherwise.
     * @return the DataLink return value.
     */
    public DL_RETVAL collect(boolean endflag, boolean blockflag) {
        dlpacket.clear();
        if (socket == null) {
            log(Level.WARNING, "collect: no socket");
            return DL_RETVAL.NO_SOCKET;
        }
        DL_RETVAL retVal;
        if (!streamingFlag) {
            if (!streamingEndFlag) {
                if (!endflag) {
                    // If not streaming and end not requested send the STREAM
                    // command
                    String header = "STREAM";
                    retVal = sendpacket(header, null, 0, false);
                    if (retVal.isError()) {
                        log(Level.WARNING,
                                "collect: problem sending STREAM command");
                        return retVal;
                    }
                    streamingEndFlag = false;
                    streamingFlag = true;
                    log(Level.INFO, "collect: STREAM command sent to server");
                } else {
                    log(Level.WARNING,
                            "collect: Connection is not in streaming mode, cannot continue");
                    return DL_RETVAL.STREAMING_ERROR;
                }
            }
        } else {
            // If streaming and end is requested send the ENDSTREAM command
            if (endflag) {
                String header = "ENDSTREAM";
                retVal = sendpacket(header, null, 0, false);
                if (retVal.isError()) {
                    log(Level.WARNING,
                            "collect: problem sending ENDSTREAM command");
                    return retVal;
                }
                streamingEndFlag = true;
                streamingFlag = false;
                log(Level.INFO, "collect: ENDSTREAM command sent to server");
            }
        }

        for (;;) {
            if (terminateFlag) {
                retVal = DL_RETVAL._NO_ERROR;
                break;
            }
            retVal = recvheader(blockflag);
            if (retVal == DL_RETVAL.NO_DATA) {
                break;
            }
            if (terminateFlag) {
                retVal = DL_RETVAL._NO_ERROR;
                break;
            }
            if (!retVal.isError()) {
                retVal = readPacket();
                break;
            } else if (retVal != DL_RETVAL.SOCKET_TIMEOUT) {
                break;
            }
        }
        return retVal;
    }

    /**
     * Connect to a DataLink server.
     * 
     * @return true if success, false otherwise.
     */
    public boolean connect() {
        if (!initFlag) {
            init();
        }
        final Socket socket = new Socket();
        try {
            socket.connect(address, iotimeout);
            socket.setSoTimeout(iotimeout);
            is = socket.getInputStream();
            os = socket.getOutputStream();
            this.socket = socket;
            return true;
        } catch (Exception ex) {
            log(Level.WARNING, "connect: %s", getMessage(ex));
            closeQuietly(socket);
            return false;
        }
    }

    /**
     * Send the ID command to the DataLink server and Verify DataLink signature
     * in server response.
     * 
     * @return the DataLink return value.
     */
    public DL_RETVAL exchangeIDs() {
        if (socket == null) {
            log(Level.WARNING, "exchangeIDs: no socket");
            return DL_RETVAL.NO_SOCKET;
        }
        // Sanity check that connection is not in streaming mode
        if (streamingFlag) {
            log(Level.WARNING,
                    "exchangeIDs: Connection in streaming mode, cannot continue");
            return DL_RETVAL.STREAMING_ERROR;
        }
        String header = String.format("ID %s", clientid);
        DL_RETVAL retVal = sendpacket(header, null, 0, true);
        // Check for errors
        if (retVal.isError()) {
            return retVal;
        }
        String respstr = getReadText();
        // Verify DataLink signature in server response
        if (!DATALINK_ID.regionMatches(true, 0, respstr, 0,
                DATALINK_ID.length())) {
            log(Level.WARNING, "exchangeIDs: Unrecognized server ID: %11.11s",
                    respstr);
            return DL_RETVAL.NO_DATALINK_ID;
        }

        /*
         * Search for capabilities flags in server ID by looking for "::" The
         * expected format of the complete server ID is:
         * "ID DataLink <optional text> <:: optional capability flags>"
         */
        String s = "::";
        int index = respstr.indexOf(s);
        if (index > 0 && index < respstr.length() - s.length()) {
            s = respstr.substring(3, index).trim();
            log(Level.FINER, "exchangeIDs: ID (%s)", s);

            int endIndex;
            int fromIndex = index + 1;

            s = "DLPROTO:";
            index = respstr.indexOf(s, fromIndex);
            if (index > 0) {
                endIndex = respstr.indexOf(' ', index + s.length());
                if (endIndex < 0) {
                    endIndex = respstr.length();
                }
                s = respstr.substring(index + s.length(), endIndex);
                try {
                    serverproto = Float.parseFloat(s);
                    log(Level.FINER, "exchangeIDs: DLPROTO (%.1f)",
                            serverproto);
                } catch (Exception ex) {
                    log(Level.WARNING,
                            "exchangeIDs: could not parse protocol version from DLPROTO flag: %s",
                            s);
                }
            }

            s = "PACKETSIZE:";
            index = respstr.indexOf(s, fromIndex);
            if (index > 0) {
                endIndex = respstr.indexOf(' ', index + s.length());
                if (endIndex < 0) {
                    endIndex = respstr.length();
                }
                s = respstr.substring(index + s.length(), endIndex);
                try {
                    maxpktsize = Integer.parseInt(s);
                    log(Level.FINER, "exchangeIDs: PACKETSIZE (%d)",
                            maxpktsize);
                } catch (Exception ex) {
                    log(Level.WARNING,
                            "exchangeIDs: could not parse protocol version from PACKETSIZE flag: %s",
                            s);
                }
            }

            s = "WRITE";
            index = respstr.indexOf(s, fromIndex);
            if (index > 0) {
                writeperm = true;
                log(Level.FINER, "exchangeIDs: WRITE");
            }
        } else {
            log(Level.FINER, "exchangeIDs: (%s)", respstr);
        }
        return DL_RETVAL._NO_ERROR;
    }

    /**
     * Get the number of bytes read.
     * 
     * @return the number of bytes read.
     */
    public int getBytesread() {
        return bytesread;
    }

    /**
     * Get the data link properties.
     * 
     * @return the data link properties.
     */
    public DataLinkProperties getDataLinkProperties() {
        return dlprops;
    }

    /**
     * Request information from the DataLink server.
     * 
     * @param infotype
     *            the INFO type to request.
     * @param infomatch
     *            an optional match pattern.
     * @return the DataLink return value.
     */
    public DL_RETVAL getinfo(String infotype, String infomatch) {
        if (socket == null) {
            log(Level.WARNING, "getinfo: no socket");
            return DL_RETVAL.NO_SOCKET;
        }
        // Sanity check that connection is not in streaming mode
        if (streamingFlag) {
            log(Level.WARNING,
                    "getinfo: Connection in streaming mode, cannot continue");
            return DL_RETVAL.STREAMING_ERROR;
        }
        if (infotype == null) {
            log(Level.WARNING, "getinfo: no INFO type");
            return DL_RETVAL.INVALIDARG;
        }
        String header = String.format("INFO %s %s", infotype,
                (infomatch != null) ? infomatch : "");
        DL_RETVAL retVal = sendpacket(header, null, 0, true);
        if (retVal.isError()) {
            return retVal;
        }
        retVal = handlereply(getReadText());
        if (retVal.isError()) {
            return retVal;
        }
        if (resp_status.equals("INFO")) {
            if (!resp_value.equals(infotype)) {
                log(Level.WARNING,
                        "getinfo: requested type %s but received type %s",
                        infotype, resp_value);
            }
            return DL_RETVAL._NO_ERROR;
        } else if (resp_status.startsWith("ERROR")) {
            String respstr = getReadText();
            log(Level.WARNING, "getinfo: %s", respstr);
        } else {
            retVal = DL_RETVAL.INVALIDRESP;
        }
        retVal = DL_RETVAL.INVALIDRESP;
        return retVal;
    }

    /**
     * Get the maximum packet size for server.
     * 
     * @return the maximum packet size for server.
     */
    public int getMaxPktSize() {
        return maxpktsize;
    }

    private String getMessage(Exception ex) {
        String msg = ex.getMessage();
        if (msg == null) {
            msg = ex.toString();
        }
        return msg;
    }

    /**
     * Get the last packet read.
     * 
     * @return the last packet read.
     */
    public DLPacket getPacket() {
        return dlpacket;
    }

    /**
     * Get the read buffer.
     * 
     * @return the read buffer.
     */
    public byte[] getReadBuffer() {
        return readBuffer;
    }

    /**
     * Get the bytes in the read buffer as text.
     * 
     * @return the bytes in the read buffer as text.
     */
    public String getReadText() {
        String s = readText;
        if (s != null) {
            return s;
        }
        s = new String(readBuffer, 0, bytesread, ASCII);
        readText = s;
        return s;
    }

    /**
     * @return the <code>resp_value</code> as a long or
     *         <code>Long.MIN_VALUE</code> if none.
     */
    public long getReponseValueLong() {
        try {
            return Long.parseLong(resp_value);
        } catch (Exception ex) {
        }
        return Long.MIN_VALUE;
    }

    /**
     * @return the <code>resp_value</code>
     */
    public String getResponseValue() {
        return resp_value;
    }

    /**
     * Gets the server version of the DataLink protocol.
     * 
     * @return the server version of the DataLink protocol.
     */
    public float getServerProto() {
        return serverproto;
    }

    /**
     * Handle the server reply to a command. This method sets the
     * <code>resp_size</code>, <code>resp_status</code> and
     * <code>resp_value</code> values if successful.
     * 
     * @param respstr
     *            the response string.
     * @return the DataLink return value.
     */
    private DL_RETVAL handlereply(String respstr) {
        try {
            // status, value, size
            String ra[] = respstr.split(RE_WS, 3);
            if (ra.length == 3) {
                resp_status = ra[0];
                resp_value = ra[1];
                resp_size = Integer.parseInt(ra[2]);
                if (resp_size > 0 && resp_size < MAXPACKETSIZE) {
                    return recvdata(resp_size, true);
                }
            }
        } catch (Exception ex) {
        }
        log(Level.WARNING, "handlereply: Unable to parse reply header: '%s'",
                respstr);
        return DL_RETVAL.INVALIDRESP;
    }

    /**
     * Initialize the client. This method should be called after setting the
     * properties and before calling <code>connect</code>. If the
     * <code>connect</code> method is called and this method has not already
     * been called it will be called prior to connecting.
     * 
     * @return true if success, false if errors.
     */
    public boolean init() {
        initFlag = true;
        boolean successFlag = true;

        int index;
        DataLinkPropertiesKey key;
        String value;

        key = DataLinkPropertiesKey.ADDRESS;
        value = dlprops.getProperty(key);
        try {
            String hostname;
            int port;
            index = value.indexOf(DATALINK_ADDRESS_SEP);
            // if only host name is specified
            if (index < 0) {
                hostname = value;
                port = DATALINK_PORT;
            } else { // host name and port are specified
                hostname = value.substring(0, index);
                if (hostname.isEmpty()) {
                    hostname = DATALINK_HOSTNAME;
                }
                port = Integer.parseInt(value.substring(index + 1));
            }
            SocketAddress address;
            if (!DATALINK_HOSTNAME.equals(hostname) || DATALINK_PORT != port) {
                address = new InetSocketAddress(hostname, port);
            } else {
                address = DATALINK_ADDRESS;
            }
            if (!this.address.equals(address)) {
                this.address = address;
                log(Level.INFO, "init: %s (%s)", key, address.toString());
                setLogprefix();
            }
        } catch (Exception ex) {
            successFlag = false;
            log(Level.WARNING, "init: invalid %s (%s)", key, value);
        }

        key = DataLinkPropertiesKey.IOTIMEOUT;
        value = dlprops.getProperty(key);
        try {
            int iotimeout = Integer.parseInt(value);
            if (this.iotimeout != iotimeout) {
                this.iotimeout = iotimeout;
                log(Level.INFO, "init: %s (%d)", key, iotimeout);
            }
        } catch (Exception ex) {
            successFlag = false;
            log(Level.WARNING, "init: invalid %s (%s)", key, value);
        }

        return successFlag;
    }

    /**
     * Determines if this client is streaming.
     * 
     * @return true if streaming, false otherwise.
     */
    public boolean isStreaming() {
        return streamingFlag;
    }

    /**
     * Checks if there is write permission.
     * 
     * @return true if there is write permission, otherwise false.
     */
    public boolean isWritePerm() {
        return writeperm;
    }

    private void log(Level level, String format, Object... args) {
        logger.log(level, logprefix + String.format(format, args));
    }

    /**
     * Set the packet match parameters for a connection.
     * <p>
     * Send new match pattern to server or reset matching. An empty
     * <code>matchpattern</code> sent to the server which resets the client
     * matching setting.
     * <p>
     * The packet match pattern limits which packets are sent to the client in
     * streaming mode, this is the mode used for #collect() requests.
     * <p>
     * The <code>getReponseValueLong()</code> may be called to determine the
     * number of currently matched streams on success.
     * 
     * @param matchpattern
     *            the match pattern.
     * @return the DataLink return value.
     * @see #getReponseValueLong()
     */
    public DL_RETVAL match(String matchpattern) {
        if (socket == null) {
            log(Level.WARNING, "match: no socket");
            return DL_RETVAL.NO_SOCKET;
        }
        // Sanity check that connection is not in streaming mode
        if (streamingFlag) {
            log(Level.WARNING,
                    "match: Connection in streaming mode, cannot continue");
            return DL_RETVAL.STREAMING_ERROR;
        }
        final int packetlen = matchpattern.length();
        final byte[] packet = matchpattern.getBytes(ASCII);
        final String header = String.format("MATCH %d", packetlen);
        log(Level.INFO, "match: header=\"%s\"", header);
        DL_RETVAL retVal = sendpacket(header, packet, packetlen, true);
        if (!retVal.isError()) {
            retVal = handlereply(getReadText());
        }
        return retVal;
    }

    /**
     * Position the client read position.
     * <p>
     * The <code>getReponseValueLong()</code> may be called to determine the
     * packet ID on success on success.
     * 
     * @param pktid
     *            the Packet ID to set position to,
     *            <code>DATALINK_POSITION_EARLIEST</code> for the earliest
     *            position or <code>DATALINK_POSITION_LATEST</code for the
     *            latest position.
     * @param pkttime
     *            the Packet time for the specified packet ID in microseconds.
     * @return the DataLink return value.
     * @see #DATALINK_POSITION_EARLIEST, #DATALINK_POSITION_LATEST
     * @see #getReponseValueLong()
     */
    public DL_RETVAL position(long pktid, long pkttime) {
        if (socket == null) {
            log(Level.WARNING, "position: no socket");
            return DL_RETVAL.NO_SOCKET;
        }
        // Sanity check that connection is not in streaming mode
        if (streamingFlag) {
            log(Level.WARNING,
                    "position: Connection in streaming mode, cannot continue");
            return DL_RETVAL.STREAMING_ERROR;
        }
        final String header;
        if (pktid == DATALINK_POSITION_EARLIEST) {
            header = "POSITION SET EARLIEST";
        } else if (pktid == DATALINK_POSITION_LATEST) {
            header = "POSITION SET LATEST";
        } else {
            header = String.format("POSITION SET %d %d", pktid, pkttime);
        }
        log(Level.INFO, "position: header=\"%s\"", header);
        DL_RETVAL retVal = sendpacket(header, null, 0, true);
        if (!retVal.isError()) {
            retVal = handlereply(getReadText());
        }
        return retVal;
    }

    /**
     * Position the client read position based on data time.
     * <p>
     * Set the client read position to the first packet with a data end time
     * after the specified data time.
     * <p>
     * The <code>getReponseValueLong()</code> may be called to determine the
     * packet ID on success on success.
     * 
     * @param datatime
     *            the data time in microseconds.
     * @return the DataLink return value.
     * @see #getReponseValueLong()
     */
    public DL_RETVAL positionAfter(long datatime) {
        if (socket == null) {
            log(Level.WARNING, "positionAfter: no socket");
            return DL_RETVAL.NO_SOCKET;
        }
        // Sanity check that connection is not in streaming mode
        if (streamingFlag) {
            log(Level.WARNING,
                    "positionAfter: Connection in streaming mode, cannot continue");
            return DL_RETVAL.STREAMING_ERROR;
        }
        String header = String.format("POSITION AFTER %d", datatime);
        log(Level.INFO, "positionAfter: header=\"%s\"", header);
        DL_RETVAL retVal = sendpacket(header, null, 0, true);
        if (!retVal.isError()) {
            retVal = handlereply(getReadText());
        }
        return retVal;
    }

    /**
     * Request a specific packet from the server.
     * 
     * @param pktid
     *            the Packet ID to request.
     * @return the DataLink return value.
     */
    public DL_RETVAL read(long pktid) {
        if (socket == null) {
            log(Level.WARNING, "read: no socket");
            return DL_RETVAL.NO_SOCKET;
        }
        // Sanity check that connection is not in streaming mode
        if (streamingFlag) {
            log(Level.WARNING,
                    "read: Connection in streaming mode, cannot continue");
            return DL_RETVAL.STREAMING_ERROR;
        }
        DL_RETVAL retVal;
        if (pktid > 0) {
            String header = String.format("READ %d", pktid);
            log(Level.INFO, "read: header=\"%s\"", header);
            retVal = sendpacket(header, null, 0, true);
        } else {
            retVal = recvheader(true);
        }
        if (!retVal.isError()) {
            retVal = readPacket();
        }
        return retVal;
    }

    /**
     * Read a packet.
     * 
     * @return the DataLink return value.
     */
    private DL_RETVAL readPacket() {
        DL_RETVAL retVal = DL_RETVAL.INVALIDRESP;
        final String respstr = getReadText();
        String prefix = "PACKET ";
        if (respstr.startsWith(prefix)) {
            if (dlpacket.parse(respstr.substring(prefix.length()))) {
                int readlen = dlpacket.getDatasize();
                retVal = recvdata(readlen, true);
                if (!retVal.isError() && bytesread != readlen) {
                    log(Level.WARNING, "read: problem receiving packet data");
                    retVal = DL_RETVAL.RECV_ERROR;
                }
            }
        } else if (respstr.startsWith("ERROR")) {
            retVal = handlereply(respstr);
        } else if (respstr.equals("ENDSTREAM")) {
            if (streamingEndFlag) {
                streamingEndFlag = false;
                retVal = DL_RETVAL._NO_ERROR;
            }
        }
        if (retVal.isError()) {
            log(Level.WARNING, "read: Unable to parse reply header: '%s'",
                    respstr);
        }
        return retVal;
    }

    /**
     * Receive arbitrary data from a DataLink server.
     * 
     * @param readlen
     *            the number of bytes to read.
     * @param blockflag
     *            true to block until data is available, false otherwise.
     * @return the DataLink return value.
     */
    private DL_RETVAL recvdata(int readlen, boolean blockflag) {
        readText = null;
        bytesread = 0;
        int nrecv = 0;
        try {
            // Recv until readlen bytes have been read
            while (bytesread < readlen) {
                if (!blockflag && is.available() == 0) {
                    return DL_RETVAL.NO_DATA;
                }
                if ((nrecv = is.read(readBuffer, bytesread,
                        readlen - bytesread)) < 0) {
                    return DL_RETVAL.EOF;
                }
                // Update byte count and offset
                bytesread += nrecv;
            }
        } catch (SocketTimeoutException ex) {
            if (!streamingFlag) {
                log(Level.WARNING, "recvdata: socket timeout %s",
                        getMessage(ex));
            }
            return DL_RETVAL.SOCKET_TIMEOUT;
        } catch (Exception ex) {
            log(Level.WARNING, "recvdata: %d %d %s", readlen, nrecv,
                    getMessage(ex));
            return DL_RETVAL.RECV_ERROR;
        }
        return DL_RETVAL._NO_ERROR;
    }

    /**
     * Receive DataLink packet header.
     * 
     * @param blockflag
     *            true to block until data is available, false otherwise.
     * @return the DataLink return value.
     */
    private DL_RETVAL recvheader(boolean blockflag) {
        int len = 3;
        DL_RETVAL retVal = recvdata(len, blockflag);
        if (retVal.isError() || retVal == DL_RETVAL.NO_DATA) {
            return retVal;
        }
        if (bytesread != len) {
            return DL_RETVAL.NO_SYNC;
        }
        // Test synchronization bytes
        if (readBuffer[0] != 'D' || readBuffer[1] != 'L') {
            log(Level.WARNING, "recvheader: No DataLink packet detected");
            return DL_RETVAL.INVALID_SYNC;
        }
        // 3rd byte is the header length
        len = readBuffer[2];
        if (len < 0 || len > readBuffer.length) {
            log(Level.WARNING, "recvheader: Invalid header length: %d", len);
            return DL_RETVAL.INVALID_HEADER_LEN;
        }
        retVal = recvdata(len, true);
        if (!retVal.isError() && bytesread != len) {
            retVal = DL_RETVAL.RECV_ERROR;
            log(Level.WARNING, "recvheader: %d %d", len, bytesread);
        }
        return retVal;
    }

    /**
     * Set the packet reject parameters for a connection
     * <p>
     * Send new reject pattern to server or reset rejecting. If the
     * <code>rejectpattern</code> is empty the server will reset the client
     * rejecting setting.
     * <p>
     * The packet reject pattern limits which packets are sent to the client in
     * streaming mode, this is the mode used for <code>collect()<code> requests.
     * 
     * @param rejectpattern
     *            Reject regular expression
     * @return the DataLink return value.
     */
    public DL_RETVAL reject(String rejectpattern) {
        if (socket == null) {
            log(Level.WARNING, "reject: no socket");
            return DL_RETVAL.NO_SOCKET;
        }
        // Sanity check that connection is not in streaming mode
        if (streamingFlag) {
            log(Level.WARNING,
                    "reject: Connection in streaming mode, cannot continue");
            return DL_RETVAL.STREAMING_ERROR;
        }
        final int packetlen = rejectpattern.length();
        final byte[] packet = rejectpattern.getBytes(ASCII);
        final String header = String.format("REJECT %d", packetlen);
        log(Level.INFO, "reject: header=\"%s\"", header);
        DL_RETVAL retVal = sendpacket(header, packet, packetlen, true);
        if (!retVal.isError()) {
            retVal = handlereply(getReadText());
        }
        return retVal;
    }

    /**
     * Send arbitrary data to a DataLink server.
     * 
     * @param buffer
     *            the data.
     * @param off
     *            the start offset in the data.
     * @param len
     *            the number of bytes to write.
     * @return the DataLink return value.
     */
    private DL_RETVAL senddata(byte[] buffer, int off, int len) {
        try {
            os.write(buffer, off, len);
            return DL_RETVAL._NO_ERROR;
        } catch (Exception ex) {
            log(Level.WARNING, "senddata: error sending data: %s",
                    getMessage(ex));
            return DL_RETVAL.SEND_ERROR;
        }
    }

    /**
     * Create and send a DataLink packet.
     * 
     * @param header
     *            the DataLink packet header.
     * @param packet
     *            the packet data buffer to send.
     * @param packetlen
     *            the Length of data in bytes to send from the packet data
     *            buffer.
     * @param ack
     *            if true process acknowledgement, false otherwise.
     * @return the DataLink return value.
     */
    private DL_RETVAL sendpacket(String header, byte[] packet, int packetlen,
            boolean ack) {
        final int headerlen = header.length();
        // Sanity check that the header is not too large or zero
        if (headerlen > MAXHEADERLEN || headerlen == 0) {
            log(Level.WARNING, "sendpacket: packet header size is invalid: %d",
                    headerlen);
            return DL_RETVAL.INVALIDARG;
        }
        clearBuffer();
        addBuffer(header.length());
        addBuffer(header);
        DL_RETVAL retVal = senddata(sendBuffer, 0, sendBuflen);
        if (!retVal.isError() && packet != null && packetlen > 0) {
            retVal = senddata(packet, 0, packetlen);
        }
        if (!retVal.isError() && ack) {
            retVal = recvheader(true);
        }
        return retVal;
    }

    private final void setLogprefix() {
        logprefix = String.format("[%s] DL_", address.toString());
    }

    /**
     * Set the terminate parameter of a DataLink connection.
     */
    public void terminate() {
        if (!terminateFlag) {
            log(Level.INFO, "terminate: Terminating connection");
            terminateFlag = true;
        }
    }

    /**
     * Send a packet to the DataLink server.
     * 
     * @param packet
     *            the packet data buffer to send.
     * @param packetlen
     *            the Length of data in bytes to send from the packet data
     *            buffer.
     * @param streamid
     *            the stream ID of packet.
     * @param datastart
     *            the Unix/POSIX epoch start time in microseconds.
     * @param dataend
     *            the Unix/POSIX epoch end time in microseconds.
     * @param ack
     *            if true request acknowledgement, false otherwise.
     * @return the DataLink return value.
     */
    public DL_RETVAL write(byte[] packet, int packetlen, IStreamid streamid,
            long datastart, long dataend, boolean ack) {
        if (socket == null) {
            log(Level.WARNING, "write: no socket");
            return DL_RETVAL.NO_SOCKET;
        }
        // Sanity check that connection is not in streaming mode
        if (streamingFlag) {
            log(Level.WARNING,
                    "write: Connection in streaming mode, cannot continue");
            return DL_RETVAL.STREAMING_ERROR;
        }
        /*
         * Sanity check that packet data is not larger than max packet size if
         * known
         */
        if (maxpktsize > 0 && packetlen > maxpktsize) {
            log(Level.WARNING,
                    "write: Packet length (%d) greater than max packet size (%d)",
                    packetlen, maxpktsize);
            return DL_RETVAL.INVALIDARG;
        }
        // Create packet header with command:
        // "WRITE streamid hpdatastart hpdataend flags size"
        String header =
                String.format("WRITE %s %d %d %s %d", streamid.getText(),
                        datastart, dataend, (ack) ? "A" : "N", packetlen);
        log(Level.FINE, "write: header=\"%s\"", header);
        DL_RETVAL retVal = sendpacket(header, packet, packetlen, ack);
        return retVal;
    }
}
