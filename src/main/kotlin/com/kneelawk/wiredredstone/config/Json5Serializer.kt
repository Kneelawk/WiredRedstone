/*
 * Copyright 2022 QuiltMC
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
 */

package com.kneelawk.wiredredstone.config

import org.quiltmc.config.api.Config
import org.quiltmc.config.api.MarshallingUtils
import org.quiltmc.config.api.MarshallingUtils.MapEntryConsumer
import org.quiltmc.config.api.Serializer
import org.quiltmc.config.api.annotations.Comment
import org.quiltmc.config.api.exceptions.ConfigParseException
import org.quiltmc.config.api.values.*
import org.quiltmc.config.impl.tree.TrackedValueImpl
import org.quiltmc.json5.JsonReader
import org.quiltmc.json5.JsonToken
import org.quiltmc.json5.JsonWriter
import java.io.*

object Json5Serializer : Serializer {
    override fun getFileExtension(): String = "json5"

    @Throws(IOException::class)
    private fun serialize(writer: JsonWriter, value: Any?) {
        when {
            value is Int -> writer.value(value as Int?)
            value is Long -> writer.value(value as Long?)
            value is Float -> writer.value(value as Float?)
            value is Double -> writer.value(value as Double?)
            value is Boolean -> writer.value(value as Boolean?)
            value is String -> writer.value(value as String?)
            value is ValueList<*> -> {
                writer.beginArray()

                for (v in value) {
                    serialize(writer, v)
                }

                writer.endArray()
            }
            value is ValueMap<*> -> {
                writer.beginObject()

                for ((key, value1) in value) {
                    writer.name(key)
                    serialize(writer, value1)
                }

                writer.endObject()
            }
            value is ConfigSerializableObject<*> -> serialize(writer, value.representation)
            value == null -> writer.nullValue()
            value.javaClass.isEnum -> writer.value((value as Enum<*>).name)
            else -> throw ConfigParseException()
        }
    }

    @Throws(IOException::class)
    private fun serialize(writer: JsonWriter, node: ValueTreeNode) {
        for (comment in node.metadata(Comment.TYPE)) {
            writer.comment(comment)
        }

        if (node is ValueTreeNode.Section) {
            writer.name(node.key().lastComponent)
            writer.beginObject()

            for (child in node) {
                serialize(writer, child)
            }

            writer.endObject()
        } else {
            val trackedValue = node as TrackedValue<*>
            val defaultValue = trackedValue.defaultValue

            // Add comments for all possible enum values
            if (defaultValue.javaClass.isEnum) {
                val options = StringBuilder("options: ")
                val enumConstants = defaultValue.javaClass.enumConstants
                val enumConstantsLength = enumConstants.size

                for ((i, o) in enumConstants.withIndex()) {
                    options.append(o)

                    if (i < enumConstantsLength - 1) {
                        options.append(", ")
                    }
                }

                writer.comment(options.toString())
            }

            for (constraint in trackedValue.constraints()) {
                writer.comment(constraint.representation)
            }

            if (defaultValue !is CompoundConfigValue<*>) {
                writer.comment("default: $defaultValue")
            }

            writer.name(node.key().lastComponent)
            serialize(writer, trackedValue.realValue)
        }
    }

    @Throws(IOException::class)
    override fun serialize(config: Config, to: OutputStream) {
        val writer = JsonWriter.json5(OutputStreamWriter(to))

        for (comment in config.metadata(Comment.TYPE)) {
            writer.comment(comment)
        }

        writer.beginObject()

        for (node in config.nodes()) {
            this.serialize(writer, node)
        }

        writer.endObject()
        writer.close()
    }

    @Suppress("unchecked_cast")
    override fun deserialize(config: Config, from: InputStream) {
        try {
            val reader = JsonReader.json5(InputStreamReader(from))

            val values = parseObject(reader)

            for (value in config.values()) {
                var m: Map<String, Any?> = values

                for (i in 0 until value.key().length()) {
                    val k = value.key().getKeyComponent(i)

                    if (m.containsKey(k) && i != value.key().length() - 1) {
                        m = m[k] as Map<String, Any?>
                    } else if (m.containsKey(k)) {
                        (value as TrackedValueImpl<Any?>).setValue(
                            MarshallingUtils.coerce<Map<String, *>, Any>(
                                m[k], value.defaultValue
                            ) { map: Map<String, *>, entryConsumer: MapEntryConsumer ->
                                map.forEach(entryConsumer::put)
                            }, false
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun parseObject(reader: JsonReader): Map<String, Any?> {
        reader.beginObject()

        val obj: MutableMap<String, Any?> = LinkedHashMap()

        while (reader.hasNext() && reader.peek() == JsonToken.NAME) {
            obj[reader.nextName()] = parseElement(reader)
        }

        reader.endObject()

        return obj
    }

    @Throws(IOException::class)
    fun parseArray(reader: JsonReader): List<Any?> {
        reader.beginArray()

        val array: MutableList<Any?> = ArrayList()

        while (reader.hasNext() && reader.peek() != JsonToken.END_ARRAY) {
            array.add(parseElement(reader))
        }

        reader.endArray()

        return array
    }

    @Throws(IOException::class)
    private fun parseElement(reader: JsonReader): Any? {
        return when (reader.peek()) {
            JsonToken.END_ARRAY -> throw ConfigParseException("Unexpected end of array")
            JsonToken.BEGIN_OBJECT -> parseObject(reader)
            JsonToken.BEGIN_ARRAY -> parseArray(reader)
            JsonToken.END_OBJECT -> throw ConfigParseException("Unexpected end of object")
            JsonToken.NAME -> throw ConfigParseException("Unexpected name")
            JsonToken.STRING -> reader.nextString()
            JsonToken.NUMBER -> reader.nextNumber()
            JsonToken.BOOLEAN -> reader.nextBoolean()
            JsonToken.NULL -> {
                reader.nextNull()
                null
            }
            JsonToken.END_DOCUMENT -> throw ConfigParseException("Unexpected end of file")
            else -> throw ConfigParseException("Encountered unknown JSON token")
        }
    }
}
