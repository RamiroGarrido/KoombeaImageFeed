package col.rgm.koombea.repository.remote

import col.rgm.koombea.repository.remote.models.UsuariosDTO
import retrofit2.Response
import retrofit2.http.GET

interface IWebServices {
    @GET("posts")
    suspend fun getUsersList(): Response<UsuariosDTO>
}