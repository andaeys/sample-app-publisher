package app

class ApplicationConfig {

    companion object {
        /**
         * Specify the name of your application. If the application name is
         * `null` or blank, the application will log a warning. Suggested
         * format is "MyCompany-Application/1.0".
         */
        val APPLICATION_NAME = "Quipper"

        /**
         * Specify the package name of the app.
         */
        val PACKAGE_NAME = "com.quipper.school.assignment"

        /**
         * Authentication.
         *
         *
         * Installed application: Leave this string empty and copy or
         * edit resources/client_secrets.json.
         *
         *
         *
         * Service accounts: Enter the service
         * account email and add your key.p12 file to the resources directory.
         *
         */
        val SERVICE_ACCOUNT_EMAIL = "learn-android@api-4918360869960997146-390636.iam.gserviceaccount.com"


        /**
         * Specify the apk file path of the apk to upload, i.e. /resources/your_apk.apk
         *
         *
         * This needs to be set for running [BasicUploadApk] and [UploadApkWithListing]
         * samples.
         *
         */
        val APK_FILE_PATH =
            "/Users/quipperindonesia/Workspace/android-play-publisher-api/v3/test-publish/src/main/resources/base_35.apk"
    }

    private fun ApplicationConfig() {
        // no instance
    }


}