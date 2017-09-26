package ch.vs.sci.vscan.clamav;

/**
 * Created by Steve Favez on 20.09.2017.
 */
public class ClamAVSizeLimitException extends RuntimeException  {
    private static final long serialVersionUID = 1L;

    public ClamAVSizeLimitException(String msg) {
        super(msg);
    }
}
