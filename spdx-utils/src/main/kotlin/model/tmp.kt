package com.here.ort.spdx.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.text.SimpleDateFormat
import java.util.*


// https://gist.github.com/nickrussler/7527851
fun toISO8601UTC(date: Date): String {
    val timeZone = TimeZone.getTimeZone("UTC")
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")

    dateFormat.setTimeZone(timeZone)

    return dateFormat.format(date)
}

fun main () {
    println("==== DEBUG ====")

    val mapperConfig: ObjectMapper.() -> Unit = {
        registerKotlinModule()

        registerModule(JavaTimeModule())
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        enable(SerializationFeature.INDENT_OUTPUT)
    }

    val spdxElem = SpdxAnnotationType.REVIEW
    val jsonMapper = ObjectMapper().apply(mapperConfig)
    val xmlMapper = XmlMapper().apply(mapperConfig)

    // xmlMapper.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);

    val xmlSerializedString = xmlMapper.writeValueAsString(spdxElem)
    println("XML Serialized String $xmlSerializedString")

    val jsonSerializedString = jsonMapper.writeValueAsString(spdxElem)
    println("JSON Serialized String $jsonSerializedString")

    val spdxFile1 = SpdxFile(
        id = "SPDXRef-DoapSource",
        name = "./src/org/spdx/parser/DOAPProject.java",
        licenseInfoFromFiles = listOf<String>("GPL-2.0", "MIT")
    )

//    val spdxFile1JsonString = xmlMapper.writeValueAsString(spdxFile1)
//    println("$spdxFile1JsonString")

    val spdxPkg1 = SpdxPackage(
        id = "package-1",
        name = "package-1",
        version = "0.1",
        files = listOf<SpdxFile>(spdxFile1)
    )

    val spdxPkg2 = SpdxPackage(
        id = "package-2",
        name = "package-2",
        version = "0.2"
    )

    val packages = listOf<SpdxPackage>(spdxPkg1, spdxPkg2)
    val files = emptyList<SpdxFile>()

    val spdxPkg1JsonString = xmlMapper.writeValueAsString(spdxPkg1)
    println("$spdxPkg1JsonString")

    val spdxDoc = SpdxDocument(
        id = "test-id",
        name = "test-name",
        licenseListVersion = "3.7",
        documentDescribes = SpdxDocumentDescribes(files = files, packages = packages)
    )



//    val spdxAnnotation = SpdxAnnotation(
//        annotator = "Person: Thomas Steenbergen (thomas.steenbergen@here.com)",
//        date = Date(),
//        type = SpdxAnnotationType.REVIEW,
//        comment = "testing 123"
//    )

    val spdxDocJsonString = xmlMapper.writeValueAsString(spdxDoc);
    println("$spdxDocJsonString")

    val spdxDocXmlString = jsonMapper.writeValueAsString(spdxDoc);
    println("$spdxDocXmlString")

    println("DONE")

}
