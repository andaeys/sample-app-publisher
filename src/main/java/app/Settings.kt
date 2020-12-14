package app

@kotlinx.serialization.Serializable
data class Settings(
    val serviceAccountEmail: String,
    val track: String,
    val releaseName: String,
    val releaseNotes: String,
    val releasePriority: Int
)