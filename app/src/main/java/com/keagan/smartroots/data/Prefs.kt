package com.keagan.smartroots.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val STORE_NAME = "smartroots_prefs"
private val Context.dataStore by preferencesDataStore(STORE_NAME)

private object Keys {
    val THEME_LIGHT = booleanPreferencesKey("theme_light") // true=light, false=dark
    val LANG_TAG = stringPreferencesKey("lang_tag")         // "en", "af", "zu", ...
    val NOTES_TEXT = stringPreferencesKey("notes_text")     // demo notes blob
}

class Prefs(private val context: Context) {

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

    val notesText: Flow<String> =
        context.dataStore.data.map { it[Keys.NOTES_TEXT] ?: "" }

    suspend fun saveNotes(text: String) {
        context.dataStore.edit { it[Keys.NOTES_TEXT] = text }
    }
}
