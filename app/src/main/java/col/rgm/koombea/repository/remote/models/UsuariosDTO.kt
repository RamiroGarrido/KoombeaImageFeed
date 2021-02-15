package col.rgm.koombea.repository.remote.models

import com.google.gson.annotations.SerializedName

data class UsuariosDTO (
    @SerializedName("data") var data: List<Usuarios>
        )
data class Usuarios(
    @SerializedName("uid") var uid: String,
    @SerializedName("name") var name: String,
    @SerializedName("email") var email: String,
    @SerializedName("profile_pic") var profilePic: String,
    @SerializedName("post") var post: Post
)
data class Post(
    @SerializedName("id") var id: Int,
    @SerializedName("date") var date: String,
    @SerializedName("pics") var pics: List<String>
)