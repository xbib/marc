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
package org.xbib.marc.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class TestUtil {

    public static <T extends Exception> T assertException(Class<T> type,
                                                          String message,
                                                          Runnable runnable) {
        return assertException(type, message, adapt(runnable));
    }

    public static <T extends Exception> T assertException(Class<T> type,
                                                          String message,
                                                          RunnableEx runnable) {
        T exception = assertException(type, runnable);
        assertEquals("exception message", message, exception.getMessage());
        return exception;
    }

    public static <T extends Exception> T assertException(Class<T> type, Runnable runnable) {
        return assertException(type, adapt(runnable));
    }

    public static <T extends Exception> T assertException(Class<T> type, RunnableEx runnable) {
        T exception = catchException(runnable, type);
        assertNotNull("Expected exception: " + type.getName(), exception);
        return exception;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Exception> T catchException(RunnableEx runnable, Class<T> type) {
        try {
            runnable.run();
            return null;
        } catch (Exception exception) {
            if (type.isAssignableFrom(exception.getClass())) {
                return (T) exception;
            }
            String message = "Unexpected exception: " + exception.getMessage();
            throw new RuntimeException(message, exception);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T serializeAndDeserialize(T instance) throws Exception {
        return (T) deserialize(serialize(instance));
    }

    public static byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new ObjectOutputStream(outputStream).writeObject(object);
        return outputStream.toByteArray();
    }

    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        return new ObjectInputStream(inputStream).readObject();
    }

    private static RunnableEx adapt(final Runnable runnable) {
        return new RunnableEx() {
            public void run() {
                runnable.run();
            }
        };
    }

    public interface RunnableEx {
        void run() throws Exception;
    }

}
