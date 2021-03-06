/*
 * Copyright (C) 2017-2019 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */

import { all, put, takeEvery } from 'redux-saga/effects';
import processOrtResultData from './processOrtResultData';

export function* loadOrtResultData() {
    yield put({ type: 'APP::SHOW_LOAD_VIEW' });
    yield put({ type: 'APP::LOADING_ORT_RESULT_DATA_START' });

    // Parse JSON report data embedded in HTML page
    const ortResultDataNode = document.querySelector('script[id="ort-report-data"]');
    const ortResultDataTxt = ortResultDataNode ? ortResultDataNode.textContent : undefined;

    if (!ortResultDataTxt || ortResultDataTxt.trim().length === 0) {
        yield put({ type: 'APP::SHOW_NO_REPORT' });
    } else {
        const ortResultData = JSON.parse(ortResultDataTxt);
        yield put({ type: 'APP::LOADING_ORT_RESULT_DATA_DONE', payload: ortResultData });
        yield put({ type: 'APP::LOADING_PROCESS_ORT_RESULT_DATA_START' });
    }
}
export function* watchProcessOrtResultData() {
    yield takeEvery('APP::LOADING_PROCESS_ORT_RESULT_DATA_START', processOrtResultData);
}

export function* watchLoadOrtResultData() {
    yield takeEvery('APP::LOADING_START', loadOrtResultData);
}

// single entry point to start all Sagas at once
export default function* rootSaga() {
    yield all([
        watchLoadOrtResultData(),
        watchProcessOrtResultData()
    ]);
}
