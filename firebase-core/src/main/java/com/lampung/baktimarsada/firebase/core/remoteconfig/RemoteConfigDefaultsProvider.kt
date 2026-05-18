package com.lampung.baktimarsada.firebase.core.remoteconfig

import android.content.Context
import android.content.res.XmlResourceParser
import com.lampung.baktimarsada.firebase.core.R
import dagger.hilt.android.qualifiers.ApplicationContext
import org.xmlpull.v1.XmlPullParser
import javax.inject.Inject
import javax.inject.Singleton

interface RemoteConfigDefaultsProvider {
    val defaultsResId: Int
    fun getAll(): Map<String, String>
    fun contains(key: String): Boolean
    fun getString(key: String): String?
}

@Singleton
class XmlRemoteConfigDefaultsProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : RemoteConfigDefaultsProvider {

    override val defaultsResId: Int = R.xml.firebase_remote_config_defaults

    private val defaults: Map<String, String> by lazy {
        parseDefaults(context.resources.getXml(defaultsResId))
    }

    override fun getAll(): Map<String, String> = defaults

    override fun contains(key: String): Boolean = defaults.containsKey(key)

    override fun getString(key: String): String? = defaults[key]

    private fun parseDefaults(parser: XmlResourceParser): Map<String, String> {
        val values = linkedMapOf<String, String>()
        var currentKey: String? = null
        var currentValue: String? = null
        var currentTag: String? = null

        parser.use {
            while (it.eventType != XmlPullParser.END_DOCUMENT) {
                when (it.eventType) {
                    XmlPullParser.START_TAG -> currentTag = it.name
                    XmlPullParser.TEXT -> {
                        when (currentTag) {
                            "key" -> currentKey = it.text
                            "value" -> currentValue = it.text
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (it.name == "entry") {
                            val key = currentKey?.trim().orEmpty()
                            if (key.isNotBlank()) {
                                values[key] = currentValue?.trim().orEmpty()
                            }
                            currentKey = null
                            currentValue = null
                        }
                        currentTag = null
                    }
                }
                it.next()
            }
        }
        return values
    }
}
