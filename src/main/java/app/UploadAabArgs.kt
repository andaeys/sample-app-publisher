package app

import com.charleskorn.kaml.Yaml
import com.google.api.client.http.AbstractInputStreamContent
import com.google.api.client.http.FileContent
import com.google.api.services.androidpublisher.AndroidPublisher
import com.google.api.services.androidpublisher.model.LocalizedText
import com.google.api.services.androidpublisher.model.Track
import com.google.api.services.androidpublisher.model.TrackRelease
import okhttp3.internal.io.FileSystem
import okio.buffer
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import java.security.GeneralSecurityException
import java.util.*

object UploadAabArgs {
    private val log: Log = LogFactory.getLog(UploadAabArgs::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        val currentDir = Paths.get("").toAbsolutePath()
        val settingsFile = File("$currentDir${File.separator}settings.yaml")

        val source = FileSystem.SYSTEM.source(settingsFile).buffer()
        val settingsString = source.readUtf8()

        val settings = Yaml.default.decodeFromString(Settings.serializer(), settingsString)

        println("track: ${settings.track}")
        println("priority: ${settings.releasePriority}")
        println("release name: ${settings.releaseName}")
        println("release notes: ${settings.releaseNotes}")
        try {
            val p12File = File("$currentDir${File.separator}key.p12")
            val p12FilePath  = p12File.absolutePath
            // Create the API service.
            val service: AndroidPublisher = AndroidPublisherHelper.init(
                applicationName = ApplicationConfig.APPLICATION_NAME,
                serviceAccountEmail = settings.serviceAccountEmail,
                filePath = p12FilePath
            )
            val edits = service.edits()

            // Create a new edit to make changes to your listing.
            val editRequest = edits
                .insert(ApplicationConfig.PACKAGE_NAME,
                    null
                    /** no content  */)
            val edit = editRequest.execute()
            val editId = edit.id
            log.info(String.format("Created edit with id: %s", editId))

            val aabFile = File("$currentDir${File.separator}aab").listFiles()?.firstOrNull()
            if (aabFile == null) {
                println("aab file not found")
                return
            }
            val aabFilePath = aabFile.absolutePath
            print("aab file: $aabFilePath ext: ${aabFile.extension}")
            val aabFileStream: AbstractInputStreamContent = FileContent(AndroidPublisherHelper.MIME_TYPE_AAB, File(aabFilePath))
            val uploadRequest = edits
                    .bundles()
                    .upload(ApplicationConfig.PACKAGE_NAME,
                            editId,
                            aabFileStream)
            val apk = uploadRequest.execute()
//            val versionCode = 29898L
            val versionCode = apk.versionCode.toLong()
            log.info(String.format("Version code %d has been uploaded",
                    apk.versionCode))

            // Assign apk to alpha track.
            val apkVersionCodes: MutableList<Long> = ArrayList()
            apkVersionCodes.add(java.lang.Long.valueOf(versionCode))
            val updateTrackRequest = edits
                    .tracks()
                    .update(ApplicationConfig.PACKAGE_NAME,
                            editId,
                            settings.track,
                            Track()
                                    .setTrack(settings.track)
                                    .setReleases(listOf(
                                            TrackRelease()
                                                    .setName(settings.releaseName)
                                                    .setVersionCodes(apkVersionCodes)
                                                    .setStatus("completed")
                                                    .setInAppUpdatePriority(settings.releasePriority)
                                                    .setReleaseNotes(listOf(
                                                            LocalizedText()
                                                                    .setLanguage("en-US")
                                                                    .setText(settings.releaseNotes))))))
            val updatedTrack = updateTrackRequest.execute()
            log.info(String.format("Track %s has been updated.", updatedTrack.track))

            // Commit changes for edit.
            val commitRequest = edits.commit(ApplicationConfig.PACKAGE_NAME, editId)
            val appEdit = commitRequest.execute()
            log.info(String.format("App edit with id %s has been comitted", appEdit.id))
        } catch (ex: IOException) {
            log.error("Excpetion was thrown while uploading apk to alpha track", ex)
        } catch (ex: GeneralSecurityException) {
            log.error("Excpetion was thrown while uploading apk to alpha track", ex)
        }
    }
}