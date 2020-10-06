package com.coxtunes.mapbox

import android.location.Location
import android.util.Log
import com.mapbox.mapboxsdk.geometry.LatLng
import java.text.DecimalFormat

object DistanceCalculations {
    fun CalculationByDistance(StartP: LatLng, EndP: LatLng): Double {
        val Radius = 6371 // radius of earth in Km
        val lat1 = StartP.latitude
        val lat2 = EndP.latitude
        val lon1 = StartP.longitude
        val lon2 = EndP.longitude
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = (Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + (Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2)))
        val c = 2 * Math.asin(Math.sqrt(a))
        val valueResult = Radius * c
        val km = valueResult / 1
        val newFormat = DecimalFormat("####")
        val kmInDec = Integer.valueOf(newFormat.format(km))
        val meter = valueResult % 1000
        val meterInDec = Integer.valueOf(newFormat.format(meter))
        Log.i(
            "Radius Value", "" + valueResult + "   KM Value " + kmInDec
                    + " Meter Value   " + meterInDec
        )
        return Radius * c
    }

    fun checkForArea(rad: Int, fromPosition: LatLng, toPosition: LatLng): Boolean {
        val locationA = Location("point A")
        locationA.latitude = fromPosition.latitude
        locationA.longitude = fromPosition.longitude
        val locationB = Location("point B")
        locationB.latitude = toPosition.latitude
        locationB.longitude = toPosition.longitude
        val distance = locationA.distanceTo(locationB).toInt()
        return distance / 1000 <= rad
    }

    fun checklocationInFixedarea(
        currentlocationlat: Double,
        currentlocationlong: Double,
        checklocationlat: Double,
        checklocationlong: Double,
        rangeinmeter: Int
    ): Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(
            currentlocationlat,
            currentlocationlong,
            checklocationlat,
            checklocationlong,
            results
        )
        val distanceInMeters = results[0]
        val isWithinrange = distanceInMeters < rangeinmeter
        return isWithinrange
    }

}