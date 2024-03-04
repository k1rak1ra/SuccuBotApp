package net.k1ra.succubotapp.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Module
@InstallIn(ViewModelComponent::class, FragmentComponent::class)
object GsonProvider {

    @Provides
    fun provideGson() : Gson {
        return GsonBuilder().registerTypeAdapter(
            LocalDateTime::class.java,
            JsonDeserializer { json, _, _ ->
                val instant = Instant.ofEpochMilli(json.asJsonPrimitive.asLong)
                LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
            }).registerTypeAdapter(
            LocalDateTime::class.java,
            JsonSerializer { date: LocalDateTime, _, _ ->
                JsonPrimitive(date.toInstant(ZoneOffset.UTC).toEpochMilli())
            }).registerTypeAdapter(
            LocalTime::class.java,
            JsonDeserializer { json, _, _ ->
                val tacc = DateTimeFormatter.ISO_TIME.parse(json.asJsonPrimitive.asString)
                LocalTime.from(tacc)
            }).registerTypeAdapter(
            LocalTime::class.java,
            JsonSerializer { time: LocalTime, _, _ ->
                JsonPrimitive(time.format(DateTimeFormatter.ISO_TIME))
            }).create()
    }
}