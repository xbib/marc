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
package org.xbib.marc;

public class LightweightMarcRecordAdapter extends MarcRecordAdapter {

    public LightweightMarcRecordAdapter(MarcRecordListener marcRecordListener) {
        super(marcRecordListener, false);
        this.builder = Marc.builder().lightweightRecord();
    }

    @Override
    public void endRecord() {
        super.endRecord();
        builder = Marc.builder().lightweightRecord();
    }
}
