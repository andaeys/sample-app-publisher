package app

import com.charleskorn.kaml.Yaml
import okhttp3.internal.io.FileSystem
import okio.buffer
import java.io.File
import java.nio.file.Paths

object ArgsTest {

    @JvmStatic
    fun main(args: Array<String>) {
        val currentDir = Paths.get("").toAbsolutePath()
        val settingsFile = File("$currentDir${File.separator}settings.yaml")

        val source = FileSystem.SYSTEM.source(settingsFile).buffer()
        val settingsString = source.readUtf8()

        val settings = Yaml.default.decodeFromString(Settings.serializer(), settingsString)

        println("email: ${settings.serviceAccountEmail} | track: ${settings.track}")
    }
}