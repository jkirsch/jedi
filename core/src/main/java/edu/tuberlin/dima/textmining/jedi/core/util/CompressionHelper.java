package edu.tuberlin.dima.textmining.jedi.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.zip.GZIPInputStream;

/**
 *
 * Detects if the stream is of type GZIP and decorates it -  otherwise pass through
 * From http://stackoverflow.com/questions/4818468/how-to-check-if-inputstream-is-gzipped
 *
 *
 */
public class CompressionHelper {

    /**
     * Detects if the stream is of type GZIP and decorates it -  otherwise pass through
     * From http://stackoverflow.com/questions/4818468/how-to-check-if-inputstream-is-gzipped
     *
     * @param input inputstream
     * @return GZIPInputStream or inputstream
     * @throws java.io.IOException in case of errors
     */
    public static InputStream getDecompressionStream(InputStream input) throws IOException {
        PushbackInputStream pb = new PushbackInputStream(input, 2); //we need a pushbackstream to look ahead
        byte[] signature = new byte[2];
        int read = pb.read(signature);//read the signature
        if(read < 2) {
            throw new IllegalStateException("Could not determine the type of file");
        }
        pb.unread(signature); //push back the signature to the stream
        if (signature[0] == (byte) 0x1f && signature[1] == (byte) 0x8b) //check if matches standard gzip magic number
            return new GZIPInputStream(pb);
        else
            return pb;
    }
}
