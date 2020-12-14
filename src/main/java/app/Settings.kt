package app

@kotlinx.serialization.Serializable
data class Settings(
    val serviceAccountEmail: String,
    val track: String
)