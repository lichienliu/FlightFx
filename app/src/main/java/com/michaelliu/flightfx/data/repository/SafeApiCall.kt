package com.michaelliu.flightfx.data.repository

import com.michaelliu.flightfx.util.AppError
import com.michaelliu.flightfx.util.AppResult
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(block: suspend () -> T): AppResult<T> =
    try {
        AppResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        AppResult.Failure(AppError.Server(e.code(), e))
    } catch (e: IOException) {
        AppResult.Failure(AppError.Network(e))
    } catch (e: SerializationException) {
        AppResult.Failure(AppError.Parse(e))
    } catch (e: Exception) {
        AppResult.Failure(AppError.Unknown(e))
    }
