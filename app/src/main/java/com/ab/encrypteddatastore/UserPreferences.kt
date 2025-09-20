package com.ab.encrypteddatastore

import android.util.Base64
import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class UserPreferences(
    val token: String? = null
)


object  UserPreferencesSerializer : Serializer<UserPreferences> {
    override val defaultValue: UserPreferences
        get() = UserPreferences()

    override suspend fun readFrom(input: InputStream): UserPreferences {
        val encryptedBytes = withContext(Dispatchers.IO) {
            input.use {
                it.readBytes()

            }
        }
        val encryptedBytesDecoded = Base64.decode(encryptedBytes, Base64.DEFAULT)
        val decryptedBytes = Crypto.decrypt(encryptedBytesDecoded)
        return Json.decodeFromString(decryptedBytes.decodeToString())
    }

    override suspend fun writeTo(
        t: UserPreferences,
        output: OutputStream
    ) {
       val json = Json.encodeToString(t)
        val bytes = json.toByteArray()
        val encryptedBytes = Crypto.encrypt(bytes)
        val encryptedBase64 = Base64.encode(encryptedBytes, Base64.DEFAULT)
        withContext(Dispatchers.IO) {
            output.use {
                it.write(encryptedBase64)
            }
        }
    }

}