
# MARC Bibliographic data processing library for Java

This is a Java library for processing bibliographic data in the following formats:

- ISO 2709/Z39.2
- MARC (USMARC, MARC 21, MARC XML)
- MarcXchange (ISO 25577:2013)
- UNIMARC
- MAB (MAB2, MAB XML)
- dialects of MARC (Aleph Sequential, Pica, SISIS format)

The motivation of this library is to transport bibliographic data into XML or JSON based formats,
with the focus on european/german application environment.

The most known and widespread bibliographic data format is MARC, which stands for "machine readable cataloging"
and was developed by the Library of Congress 1968. Inspired by the success of MARC, several other formats, mostly based
on MARC, were developed in the 1970s, some very similar, some with significant differences. Most notable
is the UNIMARC format, developed by IFLA.

MARC does not offer the features of XML or JSON, it is not a document format
or a format for the Web. MARC is stream-based "structured data", composed of fields in sequential order,
and was targeted to write records on magnetic tape.
Today, magnetic tape data distribution service is history. Also, file distribution via FTP, common in the 1990s,
does not fit well into a highly linked and sophisticated  information infrastructure like the Semantic Web.

This library offers the first step in the complex procedure to move MARC data into computer applications of today,
by writing MARC fields to XML or JSON formats. More steps would include the generation of
graph structures (RDF triples) by processing MARC records in context, but that is not part of this package.

The library provides a fluent interface and a rich set of input streams, content handlers and listeners.
Provided are writers for XML, stylesheet transformations (MODS), and a JSON writer for
key/value-oriented JSON, suitable for indexing into Elasticsearch. Indexing into Elasticsearch is not
part of this package.

### ISO 2709 to MarcXchange

Here is a code example for reading from an ISO 2709 stream and writing into a MarcXchange collection.

```
try (MarcXchangeWriter writer = new MarcXchangeWriter(out)) {
    Marc.builder()
            .setInputStream(in)
            .setCharset(Charset.forName("ANSEL"))
            .setMarcListener(writer)
            .build()
            .writeCollection();
}
```

### MARC to MODS

Here is an example to create MODS from an ISO 2709 stream

```
Marc marc = Marc.builder()
        .setInputStream(marcInputStream)
        .setCharset(Charset.forName("ANSEL"))
        .setSchema(MARC21_FORMAT)
        .build();
StringWriter sw = new StringWriter();
Result result = new StreamResult(sw);
System.setProperty("http.agent", "Java Agent");
marc.transform(new URL("http://www.loc.gov/standards/mods/v3/MARC21slim2MODS3.xsl"), result);
```

### MARC to Aleph sequential

And here is an example showing how records in "Aleph Sequential") can be parsed
and written into a MarcXchange collection:

```
try (MarcXchangeWriter writer = new MarcXchangeWriter(out, true)
        .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)) {
    Marc marc = Marc.builder()
            .setInputStream(in)
            .setCharset(StandardCharsets.UTF_8)
            .setMarcListener(writer)
            .build();
    marc.wrapIntoCollection(marc.aleph());
}
```

### MARC in Elasticsearch

Another example, writing compressed Elasticsearch bulk format JSON from an ANSEL MARC input stream:

```
MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
// normalize ANSEL diacritics
marcValueTransformers.setMarcValueTransformer(value -> Normalizer.normalize(value, Normalizer.Form.NFC));
// split at 10000 records, select Elasticsearch bulk format, set buffer size 65536, gzip compress = true
try (MarcJsonWriter writer = new MarcJsonWriter("bulk%d.jsonl.gz", 10000,
        MarcJsonWriter.Style.ELASTICSEARCH_BULK, 65536, true)
        .setIndex("testindex", "testtype")) {
    writer.setMarcValueTransformers(marcValueTransformers);
    Marc.builder()
            .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
            .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
            .setInputStream(in)
            .setCharset(Charset.forName("ANSEL"))
            .setMarcListener(writer)
            .build()
            .writeCollection();

}
```

where the result can be indexed by a simple bash script using `curl`, because our JSON
format is compatible to Elasticsearch JSON (which is a key/value format serializable JSON).

```
#!/usr/bin/env bash
# This example file sends compressed JSON lines formatted files to Elasticsearch bulk endpoint
# It assumes the index settings and the mappings are already created and configured.

for f in bulk*.jsonl.gz; do
  curl -XPOST -H "Accept-Encoding: gzip" -H "Content-Encoding: gzip" \
   --data-binary @$f --compressed localhost:9200/_bulk
done
```

The result is a very basic MARC field based index, which is cumbersome to configure, search and analyze.
In upcoming projects, I will show how to turn MARC into semantic data with context,
and indexing such data makes much more sense and is also more fun.

By executing `curl localhost:9200/_search?pretty` the result can be examined.

![](https://github.com/xbib/marc/raw/master/src/docs/asciidoc/img/marcxchange-in-elasticsearch.png)

### Example: finding all ISSNs

This Java program scans through a MARC file, checks for ISSN values, and collects them in
JSON format (the library `org.xbib:content-core:1.0.7` is used for JSON formatting)

```
public void findISSNs() throws IOException {
    Map<String, List<Map<String, String>>> result = new TreeMap<>();
    // set up MARC listener
    MarcListener marcListener = new MarcFieldAdapter() {
        @Override
        public void field(MarcField field) {
            Collection<Map<String, String>> values = field.getSubfields().stream()
                    .filter(f -> matchISSNField(field, f))
                    .map(f -> Collections.singletonMap(f.getId(), f.getValue()))
                    .collect(Collectors.toList());
            if (!values.isEmpty()) {
                result.putIfAbsent(field.getTag(), new ArrayList<>());
                List<Map<String, String>> list = result.get(field.getTag());
                list.addAll(values);
                result.put(field.getTag(), list);
            }
        }
    };
    // read MARC file
    Marc.builder()
            .setInputStream(getClass().getResource("issns.mrc").openStream())
            .setMarcListener(marcListener)
            .build()
            .writeCollection();
    // collect ISSNs
    List<String> issns = result.values().stream()
            .map(l -> l.stream()
                    .map(m -> m.values().iterator().next())
                    .collect(Collectors.toList()))
            .flatMap(List::stream)
            .distinct()
            .collect(Collectors.toList());

    // JSON output
    XContentBuilder builder = contentBuilder().prettyPrint()
            .startObject();
    for (Map.Entry<String, List<Map<String, String>>> entry : result.entrySet()) {
        builder.field(entry.getKey(), entry.getValue());
    }
    builder.array("issns", issns);
    builder.endObject();

    logger.log(Level.INFO, builder.string());
}

private static boolean matchISSNField(MarcField field, MarcField.Subfield subfield) {
    switch (field.getTag()) {
        case "011": {
            return "a".equals(subfield.getId()) || "f".equals(subfield.getId());
        }
        case "421":
        case "451":
        case "452":
        case "488":
            return "x".equals(subfield.getId());
    }
    return false;
}
```

## Bibliographic character sets

Bibliographic character sets predate the era of Unicode. Before Unicode, characters sets were
scattered into several standards. Bibliographic standards were defined on several
bibliographic characters sets. Since Unicode, UTF-8 encoding has been accepted as
the de-facto standard, which fit into XML and JSON, but processing input data that was
created by using bibliographic standards still requires handling of ancient and exotic
encodings.

Because Java JDK does not provide  bibliographic character sets from before the Unicode era,
it must be extended by a  a bibliographic character set library.
it is recommended to use http://github.com/xbib/bibliographic-character-sets if the input data is encoded in ANSEL/Z39.47 or ISO 5426.

## Usage

The library can be used as a Gradle dependency

```
    "org.xbib:marc:2.8.0"
```

or as a Maven dependency

```
   <dependency>
     <groupId>org.xbib</groupId>
     <artifactId>marc</artifactId>
     <version>1.0.11</version>
   </dependency>
```

## Quick guide for using this project

First, install OpenJDK 8. If in doubt, I recommend SDKMan http://sdkman.io/ for easy installation.

Then clone the github repository

```
git clone https://github.com/xbib/marc
```

Then change directory into `marc` folder and enter

```
./gradlew test -Dtest.single=MarcFieldFilterTest
```

for executing the ISSN demo.

Gradle takes care of all the setup in the background.

There is also a Java program called `MarcTool` which is thought to run without Gradle

https://github.com/xbib/marc/blob/master/src/main/java/org/xbib/marc/tools/MarcTool.java

It could be extended to include a command for finding ISSNs (essentially, by copying the junit test code into the
`MarcTool` class, and wiring some suitable arguments into the code).

After

```
./gradlew assemble
```

there will find a file called marc-{version}.jar in the build/libs folder. To run this Java program,
the command would be something like

```
java -cp build/libs/marc-1.0.11.jar org.xbib.marc.tools.MarcTool
```

MarcTool is not perfect yet (it expects some arguments, if not present,
it will merely exit with an unfriendly `Exception in thread "main" java.lang.NullPointerException`).

To run the Java program as standalone program, including the JSON format as output, some more jar dependency files
must be on the runtime class path (e.g. `org.xbib:content-core:1.0.7`, `com.fasterxml.jackson.core:jackson-core:2.8.4`)

In Gradle, the exact dependencies for the JSON format in the junit test class `MarcFieldFilterTest`
can be found by executing the command

```
./gradlew dependencies
```

Then, see section `testRuntime`.

## Issues

The XSLT transformation is broken in Java 8u102. Please use Java 8u92 if there are
problems, or use Xerces/Xalan.

All contributions are welcome. Any bug reports, comments, or pull requests are welcome,
just open an issue at https://github.com/xbib/marc/issues

## MARC4J

This project was inspired by MARC4J, but is not related to MARC4J or makes reuse of the
source code. It is a completeley new implementation.

There is a MARC4J fork at https://github.com/ksclarke/freelib-marc4j where Kevin S. Clarke
implements modern Java features into the MARC4J code base.

For the curious, I tried to compile a feature comparison table to highlight some differences.
I am not very familiar with MARC4J, so I appreciate any hints, comments, or corrections.

Feature comparison of MARC4J to xbib MARC

|                                     | MARC4J                                                                                                         | xbib MARC                                                                                                                                                             |
|-------------------------------------|----------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------| 
| started by                          | Bas Peters                                                                                                     | Jörg Prante                                                                                                                                                           |
| Project start                       | 2001                                                                                                           | 2016                                                                                                                                                                  |
| Java                                | Java 5                                                                                                         | Java 17+                                                                                                                                                              |
| Build                               | Ant                                                                                                            | Gradle                                                                                                                                                                |
| Supported formats                   | ISO 2709/Z39.2,  MARC (USMARC, MARC 21, MARC XML), tries to parse MARC-like formats with a "permissive" parser | ISO 2709/Z39.2, MARC (USMARC, MARC 21, MARC XML), MarcXchange (ISO 25577:2013), UNIMARC, MAB (MAB2, MAB XML), dialects of MARC (Aleph Sequential, Pica, SISIS format) |
| Bibliographic character set support | builtin, auto-detectable                                                                                       | dynamically, via Java `Charset` API, no autodetection                                                                                                                 |
| Processing                          | iterator-based                                                                                                 | iterator-based, iterable-based, Java 8 streams for fields, records                                                                                                    |
| Transformations                     |                                                                                                                | on-the-fly, pattern-based filtering for tags/values, field key mapping, field value transformations                                                                   |
| Cleaning                            |                                                                                                                | substitute invalid characters with a pattern replacement input stream                                                                                                 |
| Statistics                          |                                                                                                                | can count tag/indicator/subfield combination occurences                                                                                                               |
| Concurrency support                 |                                                                                                                | can write to handlers record by record, provides a `MarcRecordAdapter` to turn MARC field events into record events                                                   | 
| JUnit test coverage                 |                                                                                                                | extensive testing over all MARC dialects, >80% code coverage                                                                                                          |
| Source Quality Profile              |                                                                                                                |                                                                                                                                                                       |
| Jar size                            | 447 KB (2.7.0)                                                                                                 | 150 KB (1.0.11), 194 KB (2.8.0)                                                                                                                                       |
| License                             | LGPL                                                                                                           | Apache                                                                                                                                                                |

# License

Copyright (C) 2016-2022 Jörg Prante

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
you may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
