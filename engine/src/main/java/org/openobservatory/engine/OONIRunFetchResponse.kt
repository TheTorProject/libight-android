package org.openobservatory.engine

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Date
import java.util.HashMap

data class OONIRunFetchResponse(
    @JvmField
    val archived: Boolean,
    @JvmField
    @SerializedName("descriptor_creation_time")
    val creationTime: Date,
    @JvmField
    @SerializedName("translation_creation_time")
    val translationCreationTime: Date,
    @JvmField
    val descriptor: OONIRunDescriptor
) : Serializable

data class OONIRunDescriptor(
    val author: String,
    val description: String,
    @SerializedName("description_intl")
    val descriptionIntl: HashMap<String, String>,
    val icon: String,
    val color: String,
    val animation: String,
    val name: String,
    @SerializedName("name_intl")
    val nameIntl: HashMap<String, String>,
    @SerializedName("short_description")
    val shortDescription: String,
    @SerializedName("short_description_intl")
    val shortDescriptionIntl: HashMap<String, String>,
    val nettests: List<OONIRunNettest>
) : Serializable

class OONIRunNettest(
    @SerializedName("test_name")
    val name: String,
    val inputs: List<String>
) : Serializable
