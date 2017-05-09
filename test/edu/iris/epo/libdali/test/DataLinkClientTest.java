package edu.iris.epo.libdali.test;

import edu.iris.epo.libdali.DLPacket;
import edu.iris.epo.libdali.DataLinkClient;
import edu.iris.epo.libdali.DataLinkConst;
import edu.iris.epo.libdali.DataLinkUtils;
import edu.iris.epo.libdali.IStreamid;
import edu.iris.epo.libdali.DataLinkClient.DL_RETVAL;

/**
 * DataLink Client Test
 * 
 * @author kevin
 */
public class DataLinkClientTest implements DataLinkConst, Runnable {
    public static void main(String[] args) {
        DataLinkClientTest test = new DataLinkClientTest();
        if (args.length > 0) {
            test.read_pktids = new long[args.length];
            for (int index = 0; index < args.length; index++) {
                test.read_pktids[index] = Long.parseLong(args[index]);
            }
        }
        test.run();
    }

    private boolean ack = true;
    private boolean blockflag = false;
    private boolean collectFlag = true;
    private long dataend;
    private long datastart;
    private boolean endflag;
    private boolean getInfoFlag = false;
    private int maxpktsize;
    private byte[] packet;
    private int packetlen;
    private long[] read_pktids;
    private String rejectpattern;
    private String stream_matchpattern;
    // packet ID, 0 for after, DATALINK_POSITION_EARLIEST,
    // DATALINK_POSITION_LATEST
    private long stream_pktid = DATALINK_POSITION_EARLIEST;
    private long stream_pkttime = 0;
    private IStreamid streamid;
    private boolean writeFlag = false;

    @Override
    public void run() {
        DataLinkClient dlc = new DataLinkClient(
                DataLinkClientTest.class.getSimpleName(), null);

        if (writeFlag) {
            packetlen = SLINKPACKETSIZE;
            packet = new byte[packetlen];
            datastart = System.currentTimeMillis() * MS_PER_MICROSECOND;
            dataend = datastart + 60 * 1000 * MS_PER_MICROSECOND;
            // IU_ANMO_00_BHZ/MSEED
            streamid = DataLinkUtils.createStreamid("IU", "ANMO", "00", "BHZ");
            for (int i = 0; i < packetlen; i++) {
                packet[i] = (byte) i;
            }
        }

        try {
            if (dlc.connect()) {
                DL_RETVAL retVal = runNow(dlc);
                if (retVal.isError()) {
                    System.err.println(retVal);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            dlc.close();
        }
    }

    private DL_RETVAL runNow(final DataLinkClient dlc) {
        String s;
        String infotype = "STATUS";
        String infomatch = null;
        DL_RETVAL retVal;

        if ((retVal = dlc.exchangeIDs()).isError()) {
            return retVal;
        }
        if (!dlc.isWritePerm() && writeFlag) {
            System.err.println("Write is not permitted");
            writeFlag = false;
            packetlen = 0;
            packet = null;
        }
        maxpktsize = dlc.getMaxPktSize();

        if (getInfoFlag) {
            if (!dlc.getinfo(infotype, infomatch).isError()) {
                s = dlc.getReadText();
                System.out.printf("getinfo header:\n\"%s\" (%d)\n", s,
                        s.length());
            }
        }

        if (streamid != null && packet != null && packet.length >= packetlen &&
                datastart > 0 && dataend > datastart) {
            if (packetlen > maxpktsize) {
                System.err.printf(
                        "Cannot write packet, packet length %d is larger than maximum %d\n",
                        packetlen, maxpktsize);
            } else {
                dlc.write(packet, packetlen, streamid, datastart, dataend, ack);
                if (ack) {
                    s = dlc.getReadText();
                    System.out.printf("write header:\n\"%s\" (%d)\n", s,
                            s.length());
                }
            }
        }

        if (rejectpattern != null) {
            if ((retVal = dlc.reject(rejectpattern)).isError()) {
                return retVal;
            }
            s = dlc.getReadText();
            System.out.printf("reject header:\n\"%s\" (%d)\n", s, s.length());
        }

        if (stream_matchpattern != null) {
            if ((retVal = dlc.match(stream_matchpattern)).isError()) {
                return retVal;
            }
            s = dlc.getReadText();
            System.out.printf("match %d streams, header:\n\"%s\" (%d)\n",
                    dlc.getReponseValueLong(), s, s.length());
        }

        if (read_pktids != null && read_pktids.length != 0) {
            for (long read_pktid : read_pktids) {
                if ((retVal = dlc.read(read_pktid)).isError()) {
                    return retVal;
                }
                System.out.printf("read %d: %s\n", read_pktid, dlc.getPacket());
            }
        }

        if (collectFlag) {
            if (stream_pktid == 0) {
                if ((retVal = dlc.positionAfter(stream_pkttime)).isError()) {
                    return retVal;
                }
                s = dlc.getReadText();
                System.out.printf("positionAfter header:\n\"%s\" (%d)\n", s,
                        s.length());
            } else {
                if ((retVal = dlc.position(stream_pktid, stream_pkttime))
                        .isError()) {
                    return retVal;
                }
                s = dlc.getReadText();
                System.out.printf("position header:\n\"%s\" (%d)\n", s,
                        s.length());
            }

            DLPacket dlpacket;
            while ((!(retVal = dlc.collect(endflag, blockflag)).isError())) {
                if ((dlpacket = dlc.getPacket()).getDatasize() == 0) {
                    System.out.printf("No more data");
                    break;
                }
                if (!retVal.isError()) {
                    System.out.printf("read: %s\n", dlpacket);
                }
            }
        }
        return retVal;
    }
}
