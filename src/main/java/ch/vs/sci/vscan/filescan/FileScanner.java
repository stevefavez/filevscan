package ch.vs.sci.vscan.filescan;

import ch.vs.sci.vscan.clamav.ClamAVClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Created by Steve Favez on 20.09.2017.
 * FileScanner Ressource.
 *      a file scanner provide a Get /health- Ping (is available or not) - and clamd process's up or not
 *      a file scanner Post-> allow to check a given file- get a scan result.
 */
@RestController
@RequestMapping("/api/filescanner")
public class FileScanner {

    private final static Logger LOGGER = LoggerFactory.getLogger( FileScanner.class ) ;


    @Autowired
    ClamAVClient clamAVClient;

    /**
     * @return Clamd status.
     */
    @RequestMapping(method = RequestMethod.GET, path = "health")
    public Ping ping()  {
        try {
            return Ping.PingBuilder.aPing().withClamavAvailable(clamAVClient.ping()).withTime(System.currentTimeMillis()).build() ;
        } catch (IOException e) {
            LOGGER.error("unable to ping clamav daemon", e);
            return Ping.PingBuilder.aPing().withClamavAvailable(false).withTime(System.currentTimeMillis()).build() ;
        }
    }

    /**
     * @return Clamd scan result
     */
    @RequestMapping(method = RequestMethod.POST)
    public  FileScanResult scanFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            String name = file.getOriginalFilename() ;
            if (name == null) {
                name = file.getName() ;
            }
            LOGGER.info( "scanning file - {}", name );
            byte[] r = new byte[0];
            r = clamAVClient.scan(file.getInputStream());
            FileScanResult result =  FileScanResult.FileScanResultBuilder.aFileScanResult().withClean(clamAVClient.isCleanReply(r)).withFileName(name).withClamdMessage( clamAVClient.scanResultMessage(r) ).build() ;
            return result ;
        } else {
            throw new IllegalArgumentException("empty file");
        }
    }

}
