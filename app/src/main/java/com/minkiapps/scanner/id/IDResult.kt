package com.minkiapps.scanner.id

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDate

@Parcelize
data class IDResult(
        val idNumber : String,
        val issuingCountry : String,
        val givenNames : String,
        val sureName : String,
        val birthDate : LocalDate,
        val expirationDate : LocalDate,
        val nationality : String,
        val gender : String?,
        val nameNeedCorrection : Boolean,
        val scannedAddress : String?) : Parcelable