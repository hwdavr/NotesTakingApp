package com.example.notesapp.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NotesApiService {
    @GET("v1/items")
    suspend fun listItems(
        @Query("type") type: String? = null,
        @Query("parentId") parentId: String? = null,
        @Query("rootOnly") rootOnly: Boolean? = null,
        @Query("q") query: String? = null,
        @Query("sinceVersion") sinceVersion: Long? = null,
        @Query("includeDeleted") includeDeleted: Boolean = true
    ): List<ApiItem>

    @GET("v1/items/{itemID}")
    suspend fun getItem(@Path("itemID") itemId: String): ApiItem

    @POST("v1/folders")
    suspend fun createFolder(@Body request: CreateFolderRequest): ApiItem

    @POST("v1/notes")
    suspend fun createNote(@Body request: CreateNoteRequest): ApiItem

    @PATCH("v1/items/{itemID}/rename")
    suspend fun renameItem(@Path("itemID") itemId: String, @Body request: RenameItemRequest): MutationResultDto

    @PATCH("v1/items/{itemID}/move")
    suspend fun moveItem(@Path("itemID") itemId: String, @Body request: MoveItemRequest): MutationResultDto

    @PATCH("v1/items/{itemID}/reorder")
    suspend fun reorderItem(@Path("itemID") itemId: String, @Body request: ReorderItemRequest): MutationResultDto

    @PATCH("v1/notes/{itemID}/content")
    suspend fun updateNoteContent(
        @Path("itemID") itemId: String,
        @Body request: UpdateNoteContentRequest
    ): MutationResultDto

    @PATCH("v1/items/{itemID}/favorite")
    suspend fun favoriteItem(@Path("itemID") itemId: String, @Body request: FavoriteItemRequest): MutationResultDto

    @HTTP(method = "DELETE", path = "v1/items/{itemID}", hasBody = true)
    suspend fun deleteItem(@Path("itemID") itemId: String, @Body request: DeleteItemRequest): MutationResultDto

    @GET("v1/notes/{itemID}/shares")
    suspend fun listNoteShares(@Path("itemID") itemId: String): List<NoteShareDto>

    @POST("v1/notes/{itemID}/shares")
    suspend fun createNoteShare(
        @Path("itemID") itemId: String,
        @Body request: CreateNoteShareRequest
    ): NoteShareDto

    @PATCH("v1/notes/{itemID}/shares/{shareID}")
    suspend fun updateNoteShare(
        @Path("itemID") itemId: String,
        @Path("shareID") shareId: String,
        @Body request: UpdateNoteShareRequest
    ): NoteShareDto

    @HTTP(method = "DELETE", path = "v1/notes/{itemID}/shares/{shareID}", hasBody = false)
    suspend fun deleteNoteShare(
        @Path("itemID") itemId: String,
        @Path("shareID") shareId: String
    )
}
