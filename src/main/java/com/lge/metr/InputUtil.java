package com.lge.metr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;

public class InputUtil {

    public static String readAllText(File file) {
        return readAllText(file, Charset.forName("UTF8"));
    }

    public static String readAllText(File file, Charset charset) {
        try {
            return readAllText(new FileInputStream(file), charset);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public static String readAllText(InputStream stream, Charset charset) {
        if (stream == null)
            return null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                stream, charset));
        StringWriter writer = new StringWriter();
        copyAll(reader, writer);
        return writer.toString();
    }

    public static void copyAll(Reader reader, Writer writer) {
        try {
            char[] data = new char[4096]; // copy in chunks of 4K
            int count;
            while ((count = reader.read(data)) >= 0)
                writer.write(data, 0, count);

            reader.close();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}