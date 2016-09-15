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
package org.xbib.marc;

/**
 * ISO/DIS 25577 MarcXchange constants.
 */
public interface MarcXchangeConstants {

    String MARCXCHANGE_V1_NS_URI = "info:lc/xmlns/marcxchange-v1";

    String MARCXCHANGE_V2_NS_URI = "info:lc/xmlns/marcxchange-v2";

    String MARCXCHANGE_V2_0_SCHEMA_LOCATION = "http://www.loc.gov/standards/iso25577/marcxchange-2-0.xsd";

    // related (strict superset)

    String MARC21_SCHEMA_URI = "http://www.loc.gov/MARC21/slim";

    String MARC21_SCHEMA_LOCATION = "http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd";

    // element names

    String COLLECTION = "collection";

    String RECORD = "record";

    String LEADER = "leader";

    String CONTROLFIELD = "controlfield";

    String DATAFIELD = "datafield";

    String SUBFIELD = "subfield";

    // attribute names

    String TAG_ATTRIBUTE = "tag";

    String IND_ATTRIBUTE = "ind";

    String CODE_ATTRIBUTE = "code";

    String FORMAT_ATTRIBUTE = "format";

    String TYPE_ATTRIBUTE = "type";

    // formats

    String MARCXCHANGE_FORMAT = "MarcXchange";

    String MARC21_FORMAT = "MARC21";

    // types

    String BIBLIOGRAPHIC_TYPE = "Bibliographic";

    String HOLDINGS_TYPE = "Holdings";

}
