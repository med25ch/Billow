package com.tamersarioglu.flowpay.data.database

import androidx.room.TypeConverter
import com.tamersarioglu.flowpay.data.database.subcription.SubscriptionCategory
import java.time.LocalDate
import java.time.LocalDateTime


class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? =
        dateString?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? = dateTime?.toString()

    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? =
        dateTimeString?.let { LocalDateTime.parse(it) }

    @TypeConverter
    fun fromBillingInterval(interval: BillingInterval): String = interval.name

    @TypeConverter
    fun toBillingInterval(intervalName: String): BillingInterval =
        BillingInterval.valueOf(intervalName)

    @TypeConverter
    fun fromSubscriptionCategory(category: SubscriptionCategory): String = category.name

    @TypeConverter
    fun toSubscriptionCategory(categoryName: String): SubscriptionCategory =
        SubscriptionCategory.valueOf(categoryName)

}