package com.example.paisapilot.data.local.converters;

import androidx.room.TypeConverter;
import com.example.paisapilot.data.local.SyncStatus;
import com.example.paisapilot.model.RecurringExpense;
import java.util.Date;

public class DataConverters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static String fromSyncStatus(SyncStatus status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public static SyncStatus toSyncStatus(String name) {
        return name == null ? null : SyncStatus.valueOf(name);
    }

    @TypeConverter
    public static String fromFrequency(RecurringExpense.Frequency frequency) {
        return frequency == null ? null : frequency.name();
    }

    @TypeConverter
    public static RecurringExpense.Frequency toFrequency(String name) {
        return name == null ? null : RecurringExpense.Frequency.valueOf(name);
    }
}
