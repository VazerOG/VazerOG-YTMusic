package app.vazerog.patches.music.crossfade

import app.morphe.patcher.patch.resourcePatch

val vazerOGSettingsResourcePatch = resourcePatch(
    description = "Adds VazerOG settings menu to YouTube Music settings.",
) {
    execute {
        get("res/values/strings.xml").apply {
            writeText(
                readText().replace(
                    "</resources>",
                    """    <string name="vazerog_settings_title">VazerOG</string>
    <string name="vazerog_settings_summary">Crossfade and audio enhancements</string>
</resources>"""
                )
            )
        }

        get("AndroidManifest.xml").apply {
            writeText(
                readText().replace(
                    "</application>",
                    """        <activity
            android:name="app.template.extension.music.patches.VazerOGSettingsActivity"
            android:theme="@android:style/Theme.Material"
            android:exported="false" />
    </application>"""
                )
            )
        }

        get("res/xml/settings_headers.xml").apply {
            if (exists()) {
                var content = readText()
                val manifestContent = get("AndroidManifest.xml").readText()
                val packageName = Regex("""package="([^"]+)"""")
                    .find(manifestContent)?.groupValues?.get(1)
                    ?: "com.google.android.apps.youtube.music"

                val vazerOGPref = """    <Preference
        android:persistent="false"
        android:title="@string/vazerog_settings_title"
        android:summary="@string/vazerog_settings_summary"
        android:key="vazerog_settings"
        app:allowDividerAbove="false"
        app:allowDividerBelow="false">
        <intent
            android:targetPackage="$packageName"
            android:targetClass="app.template.extension.music.patches.VazerOGSettingsActivity" />
    </Preference>"""

                val playbackKey = """android:key="settings_header_playback""""
                val playbackIndex = content.indexOf(playbackKey)
                if (playbackIndex >= 0) {
                    val elementEnd = content.indexOf("/>", playbackIndex)
                    if (elementEnd >= 0) {
                        val insertAt = elementEnd + 2
                        content = content.substring(0, insertAt) +
                            "\n" + vazerOGPref +
                            content.substring(insertAt)
                    }
                } else {
                    content = content.replace(
                        "</PreferenceScreen>",
                        "$vazerOGPref\n</PreferenceScreen>"
                    )
                }
                writeText(content)
            }
        }
    }
}
