package com.coxtunes.mapbox

import android.content.Context
import android.location.Address
import android.location.Geocoder
import java.io.IOException
import java.util.*

object GpsUtils {
    fun getAddressFromCurrentLocation(
        context: Context?,
        latitude: Double?,
        longtitude: Double?
    ): List<Address?> {
        val geocoder: Geocoder
        var addresses: List<Address?> = ArrayList()
        geocoder = Geocoder(context, Locale.getDefault())
        try {
            addresses = geocoder.getFromLocation(
                latitude!!,
                longtitude!!,
                1
            ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return addresses
    }
}