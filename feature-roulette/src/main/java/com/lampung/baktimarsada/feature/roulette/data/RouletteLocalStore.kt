package com.lampung.baktimarsada.feature.roulette.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lampung.baktimarsada.feature.roulette.model.RouletteHistoryItem
import com.lampung.baktimarsada.feature.roulette.model.RouletteLocalSnapshot
import com.lampung.baktimarsada.feature.roulette.model.RouletteNameSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

@Singleton
class RouletteLocalStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val snapshot: Flow<RouletteLocalSnapshot> = dataStore.data.map { preferences ->
        RouletteLocalSnapshot(
            names = decodeNames(preferences[rouletteNamesKey]),
            history = decodeHistory(preferences[rouletteHistoryKey]),
            source = preferences[rouletteSourceKey]
                ?.let { value -> runCatching { RouletteNameSource.valueOf(value) }.getOrNull() }
                ?: RouletteNameSource.MANUAL,
            lastUpdatedMillis = preferences[rouletteUpdatedAtKey]
        )
    }

    suspend fun replaceNames(
        names: List<String>,
        source: RouletteNameSource
    ) {
        val sanitizedNames = sanitizeNames(names)
        val updatedAt = System.currentTimeMillis()
        dataStore.edit { preferences ->
            preferences[rouletteNamesKey] = encodeNames(sanitizedNames)
            preferences[rouletteSourceKey] = source.name
            preferences[rouletteUpdatedAtKey] = updatedAt
        }
    }

    suspend fun appendHistory(winnerName: String) {
        val updatedAt = System.currentTimeMillis()
        dataStore.edit { preferences ->
            val currentHistory = decodeHistory(preferences[rouletteHistoryKey])
            val updatedHistory = buildList {
                add(
                    RouletteHistoryItem(
                        id = updatedAt.toString(),
                        winnerName = winnerName,
                        drawnAtMillis = updatedAt
                    )
                )
                addAll(currentHistory.take(MAX_HISTORY_COUNT - 1))
            }
            preferences[rouletteHistoryKey] = encodeHistory(updatedHistory)
            preferences[rouletteUpdatedAtKey] = updatedAt
        }
    }

    suspend fun clearHistory() {
        dataStore.edit { preferences ->
            preferences[rouletteHistoryKey] = encodeHistory(emptyList())
            preferences[rouletteUpdatedAtKey] = System.currentTimeMillis()
        }
    }

    private fun sanitizeNames(names: List<String>): List<String> {
        return names
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private fun encodeNames(names: List<String>): String {
        return JSONArray(names).toString()
    }

    private fun decodeNames(rawValue: String?): List<String> {
        if (rawValue.isNullOrBlank()) return emptyList()
        return runCatching {
            val jsonArray = JSONArray(rawValue)
            List(jsonArray.length()) { index -> jsonArray.optString(index).trim() }
                .filter { it.isNotBlank() }
        }.getOrDefault(emptyList())
    }

    private fun encodeHistory(history: List<RouletteHistoryItem>): String {
        val jsonArray = JSONArray()
        history.forEach { item ->
            jsonArray.put(
                JSONObject()
                    .put("id", item.id)
                    .put("winnerName", item.winnerName)
                    .put("drawnAtMillis", item.drawnAtMillis)
            )
        }
        return jsonArray.toString()
    }

    private fun decodeHistory(rawValue: String?): List<RouletteHistoryItem> {
        if (rawValue.isNullOrBlank()) return emptyList()
        return runCatching {
            val jsonArray = JSONArray(rawValue)
            List(jsonArray.length()) { index ->
                val item = jsonArray.getJSONObject(index)
                RouletteHistoryItem(
                    id = item.optString("id", index.toString()),
                    winnerName = item.optString("winnerName"),
                    drawnAtMillis = item.optLong("drawnAtMillis")
                )
            }.filter { it.winnerName.isNotBlank() }
        }.getOrDefault(emptyList())
    }

    private companion object {
        const val MAX_HISTORY_COUNT = 50
        val rouletteNamesKey = stringPreferencesKey("roulette_names")
        val rouletteHistoryKey = stringPreferencesKey("roulette_history")
        val rouletteSourceKey = stringPreferencesKey("roulette_source")
        val rouletteUpdatedAtKey = longPreferencesKey("roulette_updated_at")
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
