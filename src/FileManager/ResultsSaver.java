/*
 * GPLv3
 */
package FileManager;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author xiao
 */
public class ResultsSaver {
    private final PrintWriter pt;
    
    public ResultsSaver(String fileName) throws FileNotFoundException, IOException{
        pt=new PrintWriter(new FileWriter(fileName));
    }
    public void appendln(String line){
        pt.println(line);
    }
    public void close(){
        pt.close();
    }
    
}
