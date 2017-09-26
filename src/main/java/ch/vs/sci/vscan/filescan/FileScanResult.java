package ch.vs.sci.vscan.filescan;

import java.io.Serializable;

/**
 * Created by Steve Favez on 26.09.2017.
 * Result of a file scan.
 *   get the file name and if it's clean or not.
 */
public class FileScanResult implements Serializable {

    String fileName ;
    boolean clean ;
    String clamdMessage ;

    private FileScanResult() {}

    public boolean isClean() {
        return clean;
    }

    public String getFileName() {
        return fileName;
    }

    public String getClamdMessage() {
        return clamdMessage;
    }

    public static final class FileScanResultBuilder {
        String fileName ;
        boolean clean ;
        String clamdMessage ;

        private FileScanResultBuilder() {
        }

        public static FileScanResultBuilder aFileScanResult() {
            return new FileScanResultBuilder();
        }

        public FileScanResultBuilder withFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public FileScanResultBuilder withClean(boolean clean) {
            this.clean = clean;
            return this;
        }

        public FileScanResultBuilder withClamdMessage(String clamdMessage) {
            this.clamdMessage = clamdMessage;
            return this;
        }

        public FileScanResult build() {
            FileScanResult fileScanResult = new FileScanResult();
            fileScanResult.clean = this.clean;
            fileScanResult.fileName = this.fileName;
            fileScanResult.clamdMessage = this.clamdMessage;
            return fileScanResult;
        }
    }
}
