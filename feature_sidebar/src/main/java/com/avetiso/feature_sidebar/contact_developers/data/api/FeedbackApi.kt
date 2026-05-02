package com.avetiso.feature_sidebar.contact_developers.data.api

import com.avetiso.feature_sidebar.SidebarConstants
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Сетевой контракт для отправки обратной связи через почтовый шлюз (например, Formspree).
 * Использование @Url позволяет гибко подставлять нужный эндпоинт без изменения базового URL всего приложения.
 */
interface FeedbackApi {

    @FormUrlEncoded
    @POST
    @Headers(SidebarConstants.Api.ACCEPT)
    suspend fun sendFeedback(
        @Url endpointUrl: String,
        @Field(SidebarConstants.Api.FIELD_EMAIL) email: String,
        @Field(SidebarConstants.Api.FIELD_MESSAGE) message: String
    ): Response<Unit>
}