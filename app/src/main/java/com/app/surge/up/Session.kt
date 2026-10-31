package com.app.surge.up

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

object Session {
    private lateinit var prefs: SharedPreferences

    fun init(c: Context) {
        prefs = c.applicationContext.getSharedPreferences("surge_session", Context.MODE_PRIVATE)
    }

    var id: String
        get() = prefs.getString("id", "") ?: ""
        set(v) {
            prefs.edit().putString("id", v).apply()
        }

    var name: String
        get() = prefs.getString("name", "") ?: ""
        set(v) {
            prefs.edit().putString("name", v).apply()
        }

    var lastNotif: String
        get() = prefs.getString("lastNotif", "") ?: ""
        set(v) {
            prefs.edit().putString("lastNotif", v).apply()
        }

    var char: String
        get() = prefs.getString("char", "PUNK") ?: "PUNK"
        set(v) {
            prefs.edit().putString("char", v).apply()
        }

    var bio: String
        get() = prefs.getString("bio", "") ?: ""
        set(v) {
            prefs.edit().putString("bio", v).apply()
        }

    var ava: String
        get() = prefs.getString("ava", "") ?: ""
        set(v) {
            prefs.edit().putString("ava", v).apply()
        }

    var music: Boolean
        get() = prefs.getBoolean("music", true)
        set(v) {
            prefs.edit().putBoolean("music", v).apply()
        }

    /** Buat akun baru: ambil ID numerik, simpan ke players/{id}. */
    suspend fun register(nick: String): Boolean {
        val n = Fb.nextId() ?: return false
        var clean = nick.trim()
        if (clean.length > 14) clean = clean.substring(0, 14)
        if (clean.isEmpty()) clean = "Player"
        val body = JSONObject()
            .put("name", clean)
            .put("created", JSONObject().put(".sv", "timestamp"))
            .toString()
        if (!Fb.put(Fb.GAME, "players/" + n, body)) return false
        id = n.toString()
        name = clean
        return true
    }
}
