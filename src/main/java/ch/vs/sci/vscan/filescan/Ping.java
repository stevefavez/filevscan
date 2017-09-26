package ch.vs.sci.vscan.filescan;

import java.io.Serializable;

/**
 * Created by Steve Favez on 26.09.2017.
 * Ping result to chech if service is available and if clamd process's up.
 */
public class Ping implements Serializable{

    long time ;
    boolean clamavAvailable ;

    private Ping() {}

    public long getTime() {
        return time;
    }

    public boolean isClamavAvailable() {
        return clamavAvailable;
    }


    public static final class PingBuilder {
        long time ;
        boolean clamavAvailable ;

        private PingBuilder() {
        }

        public static PingBuilder aPing() {
            return new PingBuilder();
        }

        public PingBuilder withTime(long time) {
            this.time = time;
            return this;
        }

        public PingBuilder withClamavAvailable(boolean clamavAvailable) {
            this.clamavAvailable = clamavAvailable;
            return this;
        }

        public Ping build() {
            Ping ping = new Ping();
            ping.clamavAvailable = this.clamavAvailable;
            ping.time = this.time;
            return ping;
        }
    }
}
