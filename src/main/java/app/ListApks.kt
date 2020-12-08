package app

import com.google.api.services.androidpublisher.AndroidPublisher
import com.google.common.base.Preconditions
import com.google.common.base.Strings
import org.apache.commons.logging.LogFactory
import java.io.IOException
import java.security.GeneralSecurityException

object ListApks {
    private val log = LogFactory.getLog(ListApks::class.java)
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            Preconditions.checkArgument(
                !Strings.isNullOrEmpty(ApplicationConfig.PACKAGE_NAME),
                "ApplicationConfig.PACKAGE_NAME cannot be null or empty!"
            )

            // Create the API service.
            val service: AndroidPublisher = AndroidPublisherHelper.init(
                ApplicationConfig.APPLICATION_NAME, ApplicationConfig.SERVICE_ACCOUNT_EMAIL
            )
            val edits = service.edits()

            // Create a new edit to make changes.
            val editRequest = edits
                .insert(
                    ApplicationConfig.PACKAGE_NAME,
                    null
                    /** no content  */
                )
            val appEdit = editRequest.execute()


            // Get a list of apks.
            val apksResponse = edits
                .apks()
                .list(
                    ApplicationConfig.PACKAGE_NAME,
                    appEdit.id
                ).execute()

//            TracksListResponse trackResponse = edits.tracks()
//                    .get(ApplicationConfig.PACKAGE_NAME, appEdit.getId(), "internal")
//                    .list(ApplicationConfig.PACKAGE_NAME,
//                            appEdit.getId()).execute();

            // Print the apk info.
            for (apk in apksResponse.apks) {
                println(
                    String.format(
                        "Version: %d - Binary sha1: %s", apk.versionCode,
                        apk.binary.sha1
                    )
                )
            }
        } catch (ex: IOException) {
            log.error("Exception was thrown while updating listing", ex)
        } catch (ex: GeneralSecurityException) {
            log.error("Exception was thrown while updating listing", ex)
        }
    }
}