package app

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.DataStoreFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.androidpublisher.AndroidPublisher
import com.google.api.services.androidpublisher.AndroidPublisherScopes
import com.google.common.base.Preconditions
import com.google.common.base.Strings
import org.apache.commons.logging.LogFactory
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.security.GeneralSecurityException


object AndroidPublisherHelper {
    private val log = LogFactory.getLog(
        AndroidPublisherHelper::class.java
    )
    const val MIME_TYPE_APK = "application/vnd.android.package-archive"
    const val MIME_TYPE_AAB = "application/octet-stream"

    /** Path to the private key file (only used for Service Account auth).  */
    private const val SRC_RESOURCES_KEY_P12 = "src/main/resources/key.p12"

    /**
     * Path to the client secrets file (only used for Installed Application
     * auth).
     */
    private const val RESOURCES_CLIENT_SECRETS_JSON = "/src/main/resources/client_secrets.json"

    /**
     * Directory to store user credentials (only for Installed Application
     * auth).
     */
    private const val DATA_STORE_SYSTEM_PROPERTY = "user.home"
    private const val DATA_STORE_FILE = ".store/android_publisher_api"
    private val DATA_STORE_DIR = File(System.getProperty(DATA_STORE_SYSTEM_PROPERTY), DATA_STORE_FILE)

    /** Global instance of the JSON factory.  */
    private val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()

    /** Global instance of the HTTP transport.  */
    private var HTTP_TRANSPORT: HttpTransport? = null

    /** Installed application user ID.  */
    private const val INST_APP_USER_ID = "user"

    /**
     * Global instance of the [DataStoreFactory]. The best practice is to
     * make it a single globally shared instance across your application.
     */
    private var dataStoreFactory: FileDataStoreFactory? = null
    @Throws(GeneralSecurityException::class, IOException::class)
    private fun authorizeWithServiceAccount(serviceAccountEmail: String): Credential {
        log.info(String.format("Authorizing using Service Account: %s", serviceAccountEmail))

        // Build service account credential.
        return GoogleCredential.Builder()
            .setTransport(HTTP_TRANSPORT)
            .setJsonFactory(JSON_FACTORY)
            .setServiceAccountId(serviceAccountEmail)
            .setServiceAccountScopes(setOf(AndroidPublisherScopes.ANDROIDPUBLISHER))
            .setServiceAccountPrivateKeyFromP12File(File(SRC_RESOURCES_KEY_P12))
            .build()
    }

    /**
     * Authorizes the installed application to access user's protected data.
     *
     * @throws IOException
     * @throws GeneralSecurityException
     */
    @Throws(IOException::class)
    private fun authorizeWithInstalledApplication(): Credential {
        log.info("Authorizing using installed application")

        // load client secrets
        val clientSecrets = GoogleClientSecrets.load(
            JSON_FACTORY,
            InputStreamReader(
                AndroidPublisherHelper::class.java
                    .getResourceAsStream(RESOURCES_CLIENT_SECRETS_JSON)
            )
        )
        // Ensure file has been filled out.
        checkClientSecretsFile(clientSecrets)
        dataStoreFactory = FileDataStoreFactory(DATA_STORE_DIR)

        // set up authorization code flow
        val flow = GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT,
            JSON_FACTORY, clientSecrets, setOf(AndroidPublisherScopes.ANDROIDPUBLISHER)
        )
            .setDataStoreFactory(dataStoreFactory).build()
        // authorize
        return AuthorizationCodeInstalledApp(
            flow, LocalServerReceiver()
        ).authorize(INST_APP_USER_ID)
    }

    /**
     * Ensure the client secrets file has been filled out.
     *
     * @param clientSecrets the GoogleClientSecrets containing data from the
     * file
     */
    private fun checkClientSecretsFile(clientSecrets: GoogleClientSecrets) {
        if (clientSecrets.details.clientId.startsWith("[[INSERT")
            || clientSecrets.details.clientSecret.startsWith("[[INSERT")
        ) {
            log.error(
                "Enter Client ID and Secret from "
                        + "APIs console into resources/client_secrets.json."
            )
            System.exit(1)
        }
    }
    /**
     * Performs all necessary setup steps for running requests against the API.
     *
     * @param applicationName the name of the application: com.example.app
     * @param serviceAccountEmail the Service Account Email (empty if using
     * installed application)
     * @return the {@Link AndroidPublisher} service
     * @throws GeneralSecurityException
     * @throws IOException
     */
    /**
     * Performs all necessary setup steps for running requests against the API
     * using the Installed Application auth method.
     *
     * @param applicationName the name of the application: com.example.app
     * @return the {@Link AndroidPublisher} service
     */
    @Throws(IOException::class, GeneralSecurityException::class)
    internal fun init(
        applicationName: String?,
        serviceAccountEmail: String? = null
    ): AndroidPublisher {
        Preconditions.checkArgument(
            !Strings.isNullOrEmpty(applicationName),
            "applicationName cannot be null or empty!"
        )

        // Authorization.
        newTrustedTransport()
        val credential = if (serviceAccountEmail == null || serviceAccountEmail.isEmpty()) {
            authorizeWithInstalledApplication()
        } else {
            authorizeWithServiceAccount(serviceAccountEmail)
        }

        val httpRequestInitializer = HttpRequestInitializer { httpRequest ->
            httpRequest.connectTimeout = 3 * 60000 // 3 minutes connect timeout
            httpRequest.readTimeout = 3 * 60000 // 3 minutes read timeout
        }

        // Set up and return API client.
        return AndroidPublisher.Builder(
            HTTP_TRANSPORT, JSON_FACTORY, credential
        ).setApplicationName(applicationName)
//            .setHttpRequestInitializer(httpRequestInitializer)
            .build()
    }

    @Throws(GeneralSecurityException::class, IOException::class)
    private fun newTrustedTransport() {
        if (null == HTTP_TRANSPORT) {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
        }
    }
}