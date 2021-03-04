package com.dojo.codeexecution.execution;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicClassLoader extends ClassLoader {

    final static Logger logger = LoggerFactory.getLogger(DynamicClassLoader.class);

    public DynamicClassLoader(ClassLoader parent) {
        super(parent);
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        if(!RequestExecutor.participantClassName.equals(name))
            return super.loadClass(name);

        final String fullFilePath = "file:" + RequestExecutor.participantClassPath + ".class";
        try {
            URL url = new URL(fullFilePath);
            URLConnection connection = url.openConnection();
            InputStream input = connection.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int data = input.read();

            while(data != -1) {
                buffer.write(data);
                data = input.read();
            }

            input.close();

            byte[] classData = buffer.toByteArray();

            return defineClass(RequestExecutor.participantClassName,
                    classData, 0, classData.length);

        } catch (MalformedURLException e) {
            logger.error("Malformed url " + fullFilePath + ". Error Message : " + e.getMessage());
        } catch (IOException e) {
            logger.error("Could not read file : " + e.getMessage());
        }

        return null;
    }
}
