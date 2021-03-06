package app

import com.google.api.client.http.AbstractInputStreamContent
import com.google.api.client.http.FileContent
import com.google.common.base.Preconditions
import com.google.common.base.Strings
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import java.io.File
import java.io.IOException
import java.security.GeneralSecurityException

object UploadApkArgs {
    private val log: Log = LogFactory.getLog(UploadApkArgs::class.java)

    /**
     * Track for uploading the apk, can be 'alpha', beta', 'production' or
     * 'rollout'.
     */
    private const val TRACK_ALPHA = "alpha"
    private const val TRACK_INTERNAL = "internal"

    //change these param
    private const val RELEASE_NAME = "Internal base build 36 - high"
    private const val RELEASE_NOTES ="Internal base build 36 - high"
    private const val RELEASE_PRIORITY = 5

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val releasePriority = args[0].toInt()
            val releaseName = args[2]

            log.info("priority: $releasePriority | name: $releaseName")
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
//            val edit = editRequest.execute()
//            val editId = edit.id
//            log.info(String.format("Created edit with id: %s", editId))

            // Upload new apk to developer console
//            final String apkPath = BasicUploadApk.class
//                    .getResource(ApplicationConfig.APK_FILE_PATH)
//                    .toURI().getPath();
            val apkPath = File(ApplicationConfig.APK_FILE_PATH)
                    .toURI().path
            val apkFile: AbstractInputStreamContent = FileContent(AndroidPublisherHelper.MIME_TYPE_APK, File(apkPath))
            log.info("apk file: $apkPath")
//            val uploadRequest = edits
//                    .apks()
//                    .upload(ApplicationConfig.PACKAGE_NAME,
//                            editId,
//                            apkFile)
//            val apk = uploadRequest.execute()
//            log.info(String.format("Version code %d has been uploaded",
//                    apk.versionCode))
//
//            // Assign apk to alpha track.
//            val apkVersionCodes: MutableList<Long> = ArrayList()
//            apkVersionCodes.add(java.lang.Long.valueOf(apk.versionCode.toLong()))
//            val updateTrackRequest = edits
//                    .tracks()
//                    .update(ApplicationConfig.PACKAGE_NAME,
//                            editId,
//                            TRACK_INTERNAL,
//                            Track()
//                                    .setTrack(TRACK_INTERNAL)
//                                    .setReleases(listOf(
//                                            TrackRelease()
//                                                    .setName(RELEASE_NAME)
//                                                    .setVersionCodes(apkVersionCodes)
//                                                    .setStatus("completed")
//                                                    .setInAppUpdatePriority(RELEASE_PRIORITY)
//                                                    .setReleaseNotes(listOf(
//                                                            LocalizedText()
//                                                                    .setLanguage("en-US")
//                                                                    .setText(RELEASE_NOTES))))))
//            val updatedTrack = updateTrackRequest.execute()
//            log.info(String.format("Track %s has been updated.", updatedTrack.track))
//
//            // Commit changes for edit.
//            val commitRequest = edits.commit(ApplicationConfig.PACKAGE_NAME, editId)
//            val appEdit = commitRequest.execute()
//            log.info(String.format("App edit with id %s has been comitted", appEdit.id))
        } catch (ex: IOException) {
            log.error("Excpetion was thrown while uploading apk to alpha track", ex)
        } catch (ex: GeneralSecurityException) {
            log.error("Excpetion was thrown while uploading apk to alpha track", ex)
        }
    }
}