package com.example.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.*

object PrayerTimeCalculator {

    // Default Aleppo GPS coordinates
    const val DEFAULT_LATITUDE = 36.2021
    const val DEFAULT_LONGITUDE = 37.1343
    const val DEFAULT_TIMEZONE = 3.0 // UTC + 3

    enum class CalculationMethod(val fajrAngle: Double, val ishaAngle: Double) {
        EGYPTIAN(19.5, 17.5),
        UMM_AL_QURA(18.5, 90.0), // Note: Umm Al-Qura uses 90 min after Maghrib (handled split)
        MWL(18.0, 17.0),
        ISNA(15.0, 15.0),
        KARACHI(18.0, 18.0)
    }

    enum class AsrSchool(val shadowRatio: Double) {
        SHAFII(1.0),
        HANAFI(2.0)
    }

    data class PrayerTimes(
        val fajr: LocalDateTime,
        val sunrise: LocalDateTime,
        val dhuhr: LocalDateTime,
        val asr: LocalDateTime,
        val maghrib: LocalDateTime,
        val isha: LocalDateTime
    )

    fun calculateTimes(
        date: LocalDate,
        latitude: Double = DEFAULT_LATITUDE,
        longitude: Double = DEFAULT_LONGITUDE,
        timezone: Double = DEFAULT_TIMEZONE,
        method: CalculationMethod = CalculationMethod.EGYPTIAN,
        school: AsrSchool = AsrSchool.SHAFII
    ): PrayerTimes {
        val year = date.year
        val month = date.monthValue
        val day = date.dayOfMonth

        val julianDate = dateToJulianDate(year, month, day) - longitude / (360.0 * 24.0)
        val d = julianDate - 2451545.0

        // Solar parameters
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2.0 * g)))

        val obliquity = 23.439 - 0.00000036 * d
        val declination = Math.toDegrees(asin(sin(Math.toRadians(obliquity)) * sin(Math.toRadians(l))))
        val ra = Math.toDegrees(atan2(cos(Math.toRadians(obliquity)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))) / 15.0
        val eqTime = (q / 15.0) - fixHour(ra)

        // Solar mid-day (Dhuhr)
        val midDay = fixHour(12.0 + timezone - longitude / 15.0 - eqTime)

        // Sunrise and Sunset times
        val sunriseHA = hourAngle(-0.833, latitude, declination)
        val sunriseTime = fixHour(midDay - sunriseHA)
        val sunsetTime = fixHour(midDay + sunriseHA)

        // Fajr
        val fajrHA = hourAngle(-method.fajrAngle, latitude, declination)
        val fajrTime = fixHour(midDay - fajrHA)

        // Asr calculation
        val latRad = Math.toRadians(latitude)
        val decRad = Math.toRadians(declination)
        val diffAngle = abs(latRad - decRad)
        val asrAngleRad = atan(1.0 / (school.shadowRatio + tan(diffAngle)))
        val asrAngle = Math.toDegrees(asrAngleRad)
        val asrHA = hourAngle(asrAngle - 90.0, latitude, declination)
        val asrTime = fixHour(midDay + asrHA)

        // Maghrib
        val maghribTime = sunsetTime

        // Isha
        val ishaTime = if (method == CalculationMethod.UMM_AL_QURA) {
            fixHour(maghribTime + 1.5) // Umm al-Qura is strictly 90 mins (1.5 hours) after Maghrib
        } else {
            val ishaHA = hourAngle(-method.ishaAngle, latitude, declination)
            fixHour(midDay + ishaHA)
        }

        return PrayerTimes(
            fajr = doubleToDateTime(date, fajrTime),
            sunrise = doubleToDateTime(date, sunriseTime),
            dhuhr = doubleToDateTime(date, midDay),
            asr = doubleToDateTime(date, asrTime),
            maghrib = doubleToDateTime(date, maghribTime),
            isha = doubleToDateTime(date, ishaTime)
        )
    }

    private fun dateToJulianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2.0 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun hourAngle(angle: Double, latitude: Double, declination: Double): Double {
        val latRad = Math.toRadians(latitude)
        val decRad = Math.toRadians(declination)
        val angleRad = Math.toRadians(angle)
        
        val cosHA = (sin(angleRad) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
        if (cosHA < -1.0 || cosHA > 1.0) {
            return 12.0 // Return maximum limit if unattainable
        }
        return Math.toDegrees(acos(cosHA)) / 15.0
    }

    private fun fixAngle(angle: Double): Double {
        var a = angle % 360.0
        if (a < 0) a += 360.0
        return a
    }

    private fun fixHour(hour: Double): Double {
        var h = hour % 24.0
        if (h < 0) h += 24.0
        return h
    }

    private fun doubleToDateTime(date: LocalDate, timeDouble: Double): LocalDateTime {
        if (timeDouble.isNaN() || timeDouble.isInfinite()) {
            return date.atTime(12, 0)
        }
        var totalMinutes = (timeDouble * 60.0).roundToInt()
        var h = totalMinutes / 60
        val m = totalMinutes % 60
        
        var d = date
        if (h >= 24) {
            h -= 24
            d = d.plusDays(1)
        } else if (h < 0) {
            h += 24
            d = d.minusDays(1)
        }
        return d.atTime(h, m)
    }
}
