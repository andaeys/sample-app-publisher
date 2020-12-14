package app

import com.charleskorn.kaml.Yaml
import com.google.api.client.http.AbstractInputStreamContent
import com.google.api.client.http.FileContent
import com.google.api.services.androidpublisher.model.LocalizedText
import com.google.api.services.androidpublisher.model.Track
import com.google.api.services.androidpublisher.model.TrackRelease
import com.google.common.base.Preconditions
import com.google.common.base.Strings
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

    /**
     * Track for uploading the apk, can be 'alpha', beta', 'production' or
     * 'rollout'.
     */
    private const val TRACK_ALPHA = "alpha"
    private const val TRACK_INTERNAL = "internal"

    //change these param
    private const val RELEASE_NAME = "Internal base build 36 aab - low"
    private const val RELEASE_NOTES ="Internal base build 36 aab - low"
    private const val RELEASE_PRIORITY = 3

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val currentDir = Paths.get("").toAbsolutePath()
            val settingsFile = File("$currentDir${File.separator}settings.yaml")

            val source = FileSystem.SYSTEM.source(settingsFile).buffer()
            val settingsString = source.readUtf8()

            val settings = Yaml.default.decodeFromString(Settings.serializer(), settingsString)

            println("email: ${settings.serviceAccountEmail} | track: ${settings.track}")

            Preconditions.checkArgument(!Strings.isNullOrEmpty(ApplicationConfig.PACKAGE_NAME),
                    "ApplicationConfig.PACKAGE_NAME cannot be null or empty!")

            // Create the API service.
            val service = AndroidPublisherHelper.init(
                    ApplicationConfig.APPLICATION_NAME, ApplicationConfig.SERVICE_ACCOUNT_EMAIL)
            val edits = service.edits()

            // Create a new edit to make changes to your listing.
            val editRequest = edits
                    .insert(ApplicationConfig.PACKAGE_NAME,
                            null
                            /** no content  */)
            val edit = editRequest.execute()
            val editId = edit.id
            log.info(String.format("Created edit with id: %s", editId))

            // Upload new apk to developer console
//            final String apkPath = BasicUploadApk.class
//                    .getResource(ApplicationConfig.APK_FILE_PATH)
//                    .toURI().getPath();
            val aabFilePath = File(ApplicationConfig.APK_FILE_PATH)
                    .toURI().path
            val aabFile: AbstractInputStreamContent = FileContent(AndroidPublisherHelper.MIME_TYPE_AAB, File(aabFilePath))
            val uploadRequest = edits
                    .bundles()
                    .upload(ApplicationConfig.PACKAGE_NAME,
                            editId,
                            aabFile)
//            val apk = uploadRequest.execute()
            val versionCode = 29898L
//            val versionCode = apk.versionCode.toLong())
//            log.info(String.format("Version code %d has been uploaded",
//                    apk.versionCode))

            // Assign apk to alpha track.
            val apkVersionCodes: MutableList<Long> = ArrayList()
            apkVersionCodes.add(java.lang.Long.valueOf(versionCode))
            val updateTrackRequest = edits
                    .tracks()
                    .update(ApplicationConfig.PACKAGE_NAME,
                            editId,
                            TRACK_INTERNAL,
                            Track()
                                    .setTrack(TRACK_INTERNAL)
                                    .setReleases(listOf(
                                            TrackRelease()
                                                    .setName(RELEASE_NAME)
                                                    .setVersionCodes(apkVersionCodes)
                                                    .setStatus("completed")
                                                    .setInAppUpdatePriority(RELEASE_PRIORITY)
                                                    .setReleaseNotes(listOf(
                                                            LocalizedText()
                                                                    .setLanguage("en-US")
                                                                    .setText(RELEASE_NOTES))))))
//            val updatedTrack = updateTrackRequest.execute()
//            log.info(String.format("Track %s has been updated.", updatedTrack.track))

            // Commit changes for edit.
            val commitRequest = edits.commit(ApplicationConfig.PACKAGE_NAME, editId)
//            val appEdit = commitRequest.execute()
//            log.info(String.format("App edit with id %s has been comitted", appEdit.id))
        } catch (ex: IOException) {
            log.error("Excpetion was thrown while uploading apk to alpha track", ex)
        } catch (ex: GeneralSecurityException) {
            log.error("Excpetion was thrown while uploading apk to alpha track", ex)
        }
    }
}