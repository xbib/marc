/*
   Copyright 2016 Jörg Prante

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.xbib.marc.tools;

import org.xbib.marc.Marc;
import org.xbib.marc.xml.MarcXchangeWriter;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.stream.StreamResult;

/**
 * Command line tool.
 */
public class MarcTool {

    private static final Logger logger = Logger.getLogger(MarcTool.class.getName());

    private String mode = null;
    private String input = null;
    private String output = null;
    private String charset = "UTF-8";
    private String schema = null;
    private String stylesheet = null;
    private String result = null;

    public static void main(String[] args) {
        MarcTool marcTool = new MarcTool();
        marcTool.parse(args);
        System.exit(marcTool.run());
    }

    private void parse(String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            switch (args[i]) {
                case "--mode": {
                    mode = args[i + 1];
                    break;
                }
                case "--out": {
                    output = args[i + 1];
                    break;
                }
                case "--charset": {
                    charset = args[i + 1];
                    break;
                }
                case "--in": {
                    input = args[i + 1];
                    break;
                }
                case "--schema": {
                    schema = args[i + 1];
                    break;
                }
                case "--stylesheet": {
                    stylesheet = args[i + 1];
                    break;
                }
                case "--result": {
                    result = args[i + 1];
                    break;
                }
                default: {
                    break;
                }
            }
        }
        if (input == null && args.length > 0) {
            input = args[args.length - 1];
        }
    }

    private int run() {
        Objects.requireNonNull(input);
        Objects.requireNonNull(output);
        Objects.requireNonNull(charset);
        if (mode == null) {
            mode = "marc2xml";
        }
        if ("marc2xml".equals(mode)) {
            try (InputStream in = Files.newInputStream(Paths.get(input));
                 MarcXchangeWriter writer = new MarcXchangeWriter(Files.newBufferedWriter(Paths.get(output)), true)) {
                Marc.Builder builder = Marc.builder()
                        .setInputStream(in)
                        .setCharset(Charset.forName(charset))
                        .setMarcListener(writer);
                if (schema != null && stylesheet != null && result != null) {
                    System.setProperty("http.agent", "Java Agent");
                    builder.setSchema(schema).build().transform(new URL(stylesheet),
                                    new StreamResult(Files.newBufferedWriter(Paths.get(result))));
                } else {
                    builder.build().writeCollection();
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                return 1;
            }
            return 0;
        } else {
            String help = "Usage: " + getClass().getName()
                    + " --mode [marc2xml] set operation mode\n"
                    + " --input <path> \n"
                    + " --output <path> \n"
                    + " --charset <name> \n"
                    + " --schema [MARC21|MarcXchange] \n"
                    + " --stylesheet <URL> \n"
                    + " --result <path> \n";
            logger.log(Level.INFO, help);
            return 0;
        }
    }
}
