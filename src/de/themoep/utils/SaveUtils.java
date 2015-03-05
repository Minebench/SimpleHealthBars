package de.themoep.utils;

import java.io.*;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HashMap Java SaveUtils
 * Copyright (C) 2015 Max Lee (https://github.com/Phoenix616/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.* 
 */

public class SaveUtils {

    File folder;
    Logger logger;

    public SaveUtils(File folder, Logger logger) {
        this.folder = folder;
        this.logger = logger;
    }

    /**
     * Writes a Hashmap to a file
     * @param object The Hashmap to write
     * @param outputFile The file to write to
     */
    public void writeMap(Object object, String outputFile) {
        try
        {
            File file = new File(folder.getPath() + folder.pathSeparator + outputFile);
            if (!file.isFile()) {
                if(!file.createNewFile()){
                    throw new IOException("Error creating new file: " + file.getPath());
                }
            }
            FileOutputStream fileOut = new FileOutputStream(file.getPath());
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(object);
            out.close();
            fileOut.close();
            logger.fine("Serialized data is saved in " + file.getPath());
        }catch(IOException i)
        {
            i.printStackTrace();
        }
    }

    /**
     * Reads a Hashmap from a file
     * @param inputFile The file to read from
     * @return An Object which is a HashMap<Object,Object>
     */
    @SuppressWarnings("unchecked")
    public Object readMap(String inputFile) {
        HashMap<Object, Object> map = new HashMap<Object,Object>();
        File file = new File(folder.getPath() + folder.pathSeparator + inputFile);
        if (!file.isFile()) {
            logger.log(Level.INFO, "No file found in " + file.getPath());
            try {
                if(!file.createNewFile())
                {
                    throw new IOException("Error while creating new file: " + file.getPath());
                } else {
                    writeMap(map, inputFile);
                    logger.log(Level.INFO, "New file created in " + file.getPath());
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            FileInputStream fileIn = new FileInputStream(file.getPath());
            ObjectInputStream in = new ObjectInputStream(fileIn);
            map = (HashMap<Object, Object>) in.readObject();
            in.close();
            fileIn.close();
            logger.log(Level.INFO, "Sucessfully loaded cooldown.map.");
        }catch(IOException i)
        {
            logger.log(Level.WARNING, "No saved Map found in " + inputFile);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return map;
    }
}
