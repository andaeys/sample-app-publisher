package app

@kotlinx.serialization.Serializable
data class Settings(
    val appName: String,
    val packageName: String,
    val serviceAccountEmail: String,
    val track: String,
    val releaseName: String,
    val releaseNotes: String,
    val releasePriority: Int
)