package net.sf.jrtps.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

import net.sf.jrtps.message.Message;
import net.sf.jrtps.transport.RTPSByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageReader {
    private static final Logger logger = LoggerFactory.getLogger(MessageReader.class);
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        MessageReader mr = new MessageReader();
        LinkedList<File> fileList = new LinkedList<>();
        logger.debug("{} arguments", args.length);
        
        for (int i = 0; i < args.length; i++) {
            logger.debug("Processing argument {}", args[i]);
            
            File f = new File(args[i]);
            if (f.exists()) {
                if (f.isFile()) {
                    fileList.add(f);
                }
                else if (f.isDirectory()) {
                    File[] files = f.listFiles();
                    for (File _f : files) {
                        fileList.add(_f);
                    }
                }
            }
        }
        
        for (File f : fileList) {
            mr.readMessage(f);
        }
    }

    
    private void readMessage(File f) throws FileNotFoundException, IOException {    
        logger.info("Reading file {}", f);
        
        RTPSByteBuffer bb = new RTPSByteBuffer(new FileInputStream(f));
        Message m = new Message(bb);
        
        logger.info("{}", m);
    }
}
