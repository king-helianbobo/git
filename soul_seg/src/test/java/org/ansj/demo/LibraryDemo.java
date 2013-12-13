package org.ansj.demo;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;

import org.soul.treeSplit.IOUtil;
import org.soul.treeSplit.Library;


public class LibraryDemo {
    public static void main(String[] args) throws IOException {
        
        StreamTokenizer st = new StreamTokenizer(IOUtil.getReader("/home/ansj/workspace/ansj_seg/License.txt",IOUtil.UTF8)) ;
        while(st.nextToken()!=-1){
            System.out.println(st.sval);
        }
    }
}
