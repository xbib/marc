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
package org.xbib.marc.io;

import java.io.IOException;

/**
 * A Listener interface for capturing chunks.
 * @param <S> the separator type
 * @param <D> the datat type
 */
@FunctionalInterface
public interface ChunkListener<S, D> {
    /**
     * A chunk has arrived.
     * @param chunk the chunk
     * @throws IOException if chunk processing fails
     */
    void chunk(Chunk<S, D> chunk) throws IOException;
}
