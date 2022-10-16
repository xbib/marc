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

import java.io.Closeable;
import java.io.IOException;
import java.util.stream.Stream;

/**
 * A chunk stream.
 * @param <S> the separator type
 * @param <D> the data type
 */
public interface ChunkStream<S, D> extends Closeable {

    /**
     * Return a stream of chunks.
     * @return a stream of chunks
     */
    Stream<Chunk<S, D>> chunks();

    /**
     * Reads a single chunk from stream.
     * @return a single chunk.
     * @throws IOException if chunk read fails
     */
    Chunk<S, D> readChunk() throws IOException;
}
