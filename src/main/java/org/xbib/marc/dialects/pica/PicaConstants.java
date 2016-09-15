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
package org.xbib.marc.dialects.pica;

import org.xbib.marc.MarcXchangeConstants;

/**
 *
 */
public interface PicaConstants extends MarcXchangeConstants {

    String PICAXML_NAMESPACE = "http://www.oclcpica.org/xmlns/ppxml-1.0";

    String PICAXML_PREFIX = "ppxml";

    String SRW_PICAXML_NAMESPACE = "info:srw/schema/5/picaXML-v1.0";

    // tags

    String GLOBAL_TAG = "global";

    String SUBF_TAG = "subf";

    // attributes

    String OPACFLAG = "opacflag";

    String STATUS = "status";

    String ID_ATTRIBUTE = "id";

    String OCC = "occ";

    String OCCURENCE_ATTRIBUTE = "occurrence";
}
