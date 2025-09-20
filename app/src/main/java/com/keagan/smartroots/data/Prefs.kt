package com.keagan.smartroots.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import com.keagan.smartroots.model.Note
import java.util.UUID
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first

private const val STORE_NAME = "smartroots_prefs"
private val Context.dataStore by preferencesDataStore(STORE_NAME)

private object Keys {
    val THEME_LIGHT = booleanPreferencesKey("theme_light")
    val LANG_TAG = stringPreferencesKey("lang_tag")

    // Notes
    val NOTES_TEXT = stringPreferencesKey("notes_text") // legacy single field (kept)
    val NOTES_JSON = stringPreferencesKey("notes_json") // list of Note objects

    // Planner selections
    val SELECTED_FODDER = stringPreferencesKey("selected_fodder") // JSON array of strings
    val SELECTED_VEG = stringPreferencesKey("selected_veg")       // JSON array of strings

    // Harvest tracker
    val HARVEST_JSON = stringPreferencesKey("harvest_batches")    // JSON array of objects
}

class Prefs(private val context: Context) {

    // ---------- Theme / language ----------
    val themeIsLight: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.THEME_LIGHT] ?: false }

    suspend fun setThemeLight(value: Boolean) {
        context.dataStore.edit { it[Keys.THEME_LIGHT] = value }
    }

    val languageTag: Flow<String> =
        context.dataStore.data.map { it[Keys.LANG_TAG] ?: "en" }

    suspend fun setLanguageTag(tag: String) {
        context.dataStore.edit { it[Keys.LANG_TAG] = tag }
        val locales = LocaleListCompat.forLanguageTags(tag)
        AppCompatDelegate.setApplicationLocales(locales)
    }

    // ---------- Notes ----------
    val notes: Flow<List<Note>> = context.dataStore.data.map { prefs ->
        parseNotes(prefs[Keys.NOTES_JSON].orEmpty())
    }

    suspend fun addNote(title: String, body: String, imageUris: List<String>) {
        context.dataStore.edit { prefs ->
            val note = JSONObject().apply {
                put("id", UUID.randomUUID().toString())
                put("title", title)
                put("body", body)
                put("createdAt", System.currentTimeMillis())
                put("imageUris", JSONArray(imageUris))
            }
            val arr = JSONArray(prefs[Keys.NOTES_JSON].orEmpty().ifEmpty { "[]" })
            arr.put(note)
            prefs[Keys.NOTES_JSON] = arr.toString()
        }
    }

    suspend fun deleteNote(id: String) {
        context.dataStore.edit { prefs ->
            val arr = JSONArray(prefs[Keys.NOTES_JSON].orEmpty().ifEmpty { "[]" })
            val out = JSONArray()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                if (o.optString("id") != id) out.put(o)
            }
            prefs[Keys.NOTES_JSON] = out.toString()
        }
    }

    private fun parseNotes(raw: String): List<Note> {
        if (raw.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val imgs = o.optJSONArray("imageUris") ?: JSONArray()
                    val uris = List(imgs.length()) { j -> imgs.getString(j) }
                    add(
                        Note(
                            id = o.optString("id"),
                            title = o.optString("title"),
                            body = o.optString("body"),
                            createdAt = o.optLong("createdAt"),
                            imageUris = uris
                        )
                    )
                }
            }
        } catch (_: Throwable) { emptyList() }
    }

    // Legacy single string (if still used anywhere)
    val notesText: Flow<String> = context.dataStore.data.map { it[Keys.NOTES_TEXT] ?: "" }
    suspend fun saveNotes(text: String) {
        context.dataStore.edit { it[Keys.NOTES_TEXT] = text }
    }

    // ---------- Planner selections ----------
    val selectedFodder: Flow<List<String>> =
        context.dataStore.data.map { prefs -> readStringArray(prefs[Keys.SELECTED_FODDER]) }

    val selectedVeg: Flow<List<String>> =
        context.dataStore.data.map { prefs -> readStringArray(prefs[Keys.SELECTED_VEG]) }

    suspend fun saveSelectedCrops(tent: String, crops: List<String>) {
        context.dataStore.edit { prefs ->
            val arr = JSONArray()
            crops.forEach { arr.put(it) }
            if (tent == "fodder") prefs[Keys.SELECTED_FODDER] = arr.toString()
            else prefs[Keys.SELECTED_VEG] = arr.toString()
        }
    }

    private fun readStringArray(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i -> arr.getString(i) }
        } catch (_: Throwable) { emptyList() }
    }

    // ---------- Harvest tracker ----------
    data class HarvestBatch(
        val id: String,
        val tent: String,              // "veg" | "fodder"
        val crop: String,
        val startEpoch: Long,          // millis
        val daysToHarvest: Int,
        val status: String = "active", // "active" | "harvested" | "paused"
        val batchName: String? = null
    )

    val harvestBatches: Flow<List<HarvestBatch>> =
        context.dataStore.data.map { prefs -> parseBatches(prefs[Keys.HARVEST_JSON].orEmpty()) }

    suspend fun addHarvestBatch(b: HarvestBatch) {
        context.dataStore.edit { prefs ->
            val arr = JSONArray(prefs[Keys.HARVEST_JSON].orEmpty().ifEmpty { "[]" })
            arr.put(JSONObject().apply {
                put("id", b.id)
                put("tent", b.tent)
                put("crop", b.crop)
                put("startEpoch", b.startEpoch)
                put("daysToHarvest", b.daysToHarvest)
                put("status", b.status)
                put("batchName", b.batchName)
            })
            prefs[Keys.HARVEST_JSON] = arr.toString()
        }
    }

    suspend fun updateBatchStatus(id: String, newStatus: String) {
        context.dataStore.edit { prefs ->
            val arr = JSONArray(prefs[Keys.HARVEST_JSON].orEmpty().ifEmpty { "[]" })
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                if (o.optString("id") == id) {
                    o.put("status", newStatus)
                    break
                }
            }
            prefs[Keys.HARVEST_JSON] = arr.toString()
        }
    }

    suspend fun removeBatch(id: String) {
        context.dataStore.edit { prefs ->
            val arr = JSONArray(prefs[Keys.HARVEST_JSON].orEmpty().ifEmpty { "[]" })
            val out = JSONArray()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                if (o.optString("id") != id) out.put(o)
            }
            prefs[Keys.HARVEST_JSON] = out.toString()
        }
    }

    private fun parseBatches(raw: String): List<HarvestBatch> {
        if (raw.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                HarvestBatch(
                    id = o.optString("id"),
                    tent = o.optString("tent"),
                    crop = o.optString("crop"),
                    startEpoch = o.optLong("startEpoch"),
                    daysToHarvest = o.optInt("daysToHarvest"),
                    status = o.optString("status", "active"),
                    batchName = o.optString("batchName", null)
                )
            }
        } catch (_: Throwable) { emptyList() }
    }
}
