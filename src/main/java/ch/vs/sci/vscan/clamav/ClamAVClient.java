package ch.vs.sci.vscan.clamav;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Created by Steve Favez on 20.09.2017.
 */
@Service
public class ClamAVClient {

    @Value("${clamd.host}")
    String hostname;

    @Value("${clamd.port}")
    int port;

    @Value("${clamd.timeout}")
    int timeout;

    // "do not exceed StreamMaxLength as defined in clamd.conf, otherwise clamd will reply with INSTREAM size limit exceeded and close the connection."
    private static final int CHUNK_SIZE = 2048;
    private static final int DEFAULT_TIMEOUT = 500;
    private static final int PONG_REPLY_LEN = 4;

    public ClamAVClient() {
    }


    /**
     * Run PING command to clamd to test it is responding.
     *
     * @return true if the server responded with proper ping reply.
     */
    public boolean ping() throws IOException {
        try (Socket s = new Socket(hostname, port); OutputStream outs = s.getOutputStream()) {
            s.setSoTimeout(timeout);
            outs.write(asBytes("zPING\0"));
            outs.flush();
            byte[] b = new byte[PONG_REPLY_LEN];
            InputStream inputStream = s.getInputStream();
            int copyIndex = 0;
            int readResult;
            do {
                readResult = inputStream.read(b, copyIndex, Math.max(b.length - copyIndex, 0));
                copyIndex += readResult;
            } while (readResult > 0);
            return Arrays.equals(b, asBytes("PONG"));
        }
    }

    /**
     * Streams the given data to the server in chunks. The whole data is not kept in memory.
     * This method is preferred if you don't want to keep the data in memory, for instance by scanning a file on disk.
     * Since the parameter InputStream is not reset, you can not use the stream afterwards, as it will be left in a EOF-state.
     * If your goal is to scan some data, and then pass that data further, consider using {@link #scan(byte[]) scan(byte[] in)}.
     * <p>
     * Opens a socket and reads the reply. Parameter input stream is NOT closed.
     *
     * @param is data to scan. Not closed by this method!
     * @return server reply
     */
    public byte[] scan(InputStream is) throws IOException {
        try (Socket s = new Socket(hostname, port); OutputStream outs = new BufferedOutputStream(s.getOutputStream())) {
            s.setSoTimeout(timeout);

            // handshake
            outs.write(asBytes("zINSTREAM\0"));
            outs.flush();
            byte[] chunk = new byte[CHUNK_SIZE];

            try (InputStream clamIs = s.getInputStream()) {
                // send data
                int read = is.read(chunk);
                while (read >= 0) {
                    // The format of the chunk is: '<length><data>' where <length> is the size of the following data in bytes expressed as a 4 byte unsigned
                    // integer in network byte order and <data> is the actual chunk. Streaming is terminated by sending a zero-length chunk.
                    byte[] chunkSize = ByteBuffer.allocate(4).putInt(read).array();

                    outs.write(chunkSize);
                    outs.write(chunk, 0, read);
                    if (clamIs.available() > 0) {
                        // reply from server before scan command has been terminated.
                        byte[] reply = assertSizeLimit(readAll(clamIs));
                        throw new IOException("Scan aborted. Reply from server: " + new String(reply, StandardCharsets.US_ASCII));
                    }
                    read = is.read(chunk);
                }

                // terminate scan
                outs.write(new byte[]{0, 0, 0, 0});
                outs.flush();
                // read reply
                return assertSizeLimit(readAll(clamIs));
            }
        }
    }

    /**
     * Scans bytes for virus by passing the bytes to clamav
     *
     * @param in data to scan
     * @return server reply
     **/
    public byte[] scan(byte[] in) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(in);
        return scan(bis);
    }

    /**
     * Interpret the result from a  ClamAV scan, and determine if the result means the data is clean
     *
     * @param reply The reply from the server after scanning
     * @return true if no virus was found according to the clamd reply message
     */
    public static boolean isCleanReply(byte[] reply) {
        final String r = scanResultMessage(reply) ;
        return (r.contains("OK") && !r.contains("FOUND"));
    }

    public static String scanResultMessage( byte[] reply) {
        return new String(reply, StandardCharsets.US_ASCII);
    }


    private byte[] assertSizeLimit(byte[] reply) {
        String r = new String(reply, StandardCharsets.US_ASCII);
        if (r.startsWith("INSTREAM size limit exceeded."))
            throw new ClamAVSizeLimitException("Clamd size limit exceeded. Full reply from server: " + r);
        return reply;
    }

    // byte conversion based on ASCII character set regardless of the current system locale
    private static byte[] asBytes(String s) {
        return s.getBytes(StandardCharsets.US_ASCII);
    }

    // reads all available bytes from the stream
    private static byte[] readAll(InputStream is) throws IOException {
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();

        byte[] buf = new byte[2000];
        int read = 0;
        do {
            read = is.read(buf);
            tmp.write(buf, 0, read);
        } while ((read > 0) && (is.available() > 0));
        return tmp.toByteArray();
    }
}
