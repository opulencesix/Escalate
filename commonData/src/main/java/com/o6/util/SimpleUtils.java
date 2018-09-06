package com.o6.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.util.ResourceUtils;

public class SimpleUtils {

  public static String getStringFromFile(String fileName) throws IOException {
    File file = new File(fileName);
    FileInputStream fis = new FileInputStream(file);
    byte[] data = new byte[(int) file.length()];
    fis.read(data);
    fis.close();

    return new String(data, "UTF-8");
  }

  public static String getStringFromResource(Class clazz, String resourceName) throws IOException,
      URISyntaxException {

    InputStream in = clazz.getResourceAsStream("/" + resourceName); 
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    
    String response = new String();
    for (String line; (line = reader.readLine()) != null; response += line);
    
    return response;
  }

}
