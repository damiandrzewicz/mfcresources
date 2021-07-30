package de.atos.solumversion.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LineFileReader {

    public interface FileLineCallback{
        boolean processLine(String line);
    }

    static public void read(File file, FileLineCallback callback) {

        if(!file.exists()){
            throw new IllegalArgumentException(String.format("Resource file not exists: [%s]", file.getPath()));
        }

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line = bufferedReader.readLine();
            while(line != null){
                if(!callback.processLine(line)){
                    break;
                }
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
