/*
 * Copyright (C) 2019 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

package com.here.ort.spdx.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

/**
 * A class to denote the type of [SPDXFile].
 */
@JacksonXmlRootElement(
    localName = "spdx:fileType"
)
enum class SpdxFileType {
    /**
     * The file is associated with a specific application type (MIME type of application e.g. .exe).
     */
    APPLICATION,

    /**
     * Indicates the file is an archive file.
     */
    ARCHIVE,

    /**
     * The file is associated with an audio file (MIME type of audio, e.g. .mp3).
     */
    AUDIO,

    /**
     * Indicates the file is not a text file.
     */
    BINARY,

    /**
     * The file serves as documentation.
     */
    DOCUMENTATION,

    /**
     * The file is associated with an picture image file (MIME type of image, e.g. .jpg, .gif).
     */
    IMAGE,

    /**
     * Indicates the file is not a source, archive or binary file.
     */
    OTHER,

    /**
     * Indicates the file is a source code file.
     */
    SOURCE,

    /**
     * The file is an SPDX document.
     */
    SPDX,

    /**
     * The file is a human readable text file (MIME type of text).
     */
    TEXT,

    /**
     * The file is associated with a video file (MIME type of video, e.g. .avi, .mkv, .mp4)
     */
    VIDEO;
}
