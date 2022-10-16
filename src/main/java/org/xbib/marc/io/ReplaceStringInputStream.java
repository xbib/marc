/**
 *  Copyright 2016-2022 Jörg Prante <joergprante@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache License 2.0</a>
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.xbib.marc.io;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * A string replacing input stream.
 */
public class ReplaceStringInputStream extends FixedTokenReplacementInputStream {

    /**
     * Create string replacing input stream.
     * @param in the underlying input stream
     * @param token the token
     * @param fixedValue the fixed value
     */
    public ReplaceStringInputStream(InputStream in, String token, String fixedValue) {
        super(in, token, new FixedStringValueTokenHandler(fixedValue));
    }

    /**
     * The stream token handler for this replacement input stream.
     */
    private static class FixedStringValueTokenHandler implements StreamTokenHandler {

        private final String value;

        /**
         * Create stream token handler.
         * @param value value
         */
        FixedStringValueTokenHandler(String value) {
            this.value = value;
        }

        @Override
        public InputStream processToken(String token) {
            return new ByteArrayInputStream(value.getBytes(StandardCharsets.ISO_8859_1));
        }
    }
}
