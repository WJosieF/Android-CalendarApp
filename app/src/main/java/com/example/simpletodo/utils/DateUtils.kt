package com.example.simpletodo.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth  // 确保这行存在
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateUtils {

    // 将LocalDate转换为数据库查询用的字符串格式
    fun LocalDate.toDatabaseDate(): String {
        return format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    // 将LocalDateTime转换为数据库查询用的字符串格式
    fun LocalDateTime.toDatabaseDate(): String {
        return format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    // 获取YearMonth的数据库查询格式（如：2024-01）
    fun YearMonth.toDatabaseFormat(): String {
        return format(DateTimeFormatter.ofPattern("yyyy-MM"))
    }

    // 将数据库日期字符串转换为LocalDate
    fun String.toLocalDate(): LocalDate {
        return LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)
    }

    // 获取一个月的所有日期
    fun getDaysInMonth(yearMonth: YearMonth): List<LocalDate> {
        val daysInMonth = mutableListOf<LocalDate>()
        val firstDayOfMonth = yearMonth.atDay(1)
        val lastDayOfMonth = yearMonth.atEndOfMonth()

        // 添加当月的所有日期
        var currentDate = firstDayOfMonth
        while (!currentDate.isAfter(lastDayOfMonth)) {
            daysInMonth.add(currentDate)
            currentDate = currentDate.plusDays(1)
        }

        return daysInMonth
    }

    // 获取某个月的第一天是星期几（1=星期一，7=星期日）
    fun getFirstDayOfWeek(yearMonth: YearMonth): Int {
        return yearMonth.atDay(1).dayOfWeek.value
    }

    // 获取月份显示名称（如：2024年1月）
    fun getMonthDisplayName(yearMonth: YearMonth): String {
        return "${yearMonth.year}年${yearMonth.monthValue}月"
    }

    // 获取日期显示名称（如：1月15日 星期一）
    fun getDateDisplayName(date: LocalDate): String {
        val monthDay = date.format(DateTimeFormatter.ofPattern("M月d日"))
        val weekDay = getChineseWeekDay(date)
        return "$monthDay $weekDay"
    }

    // 获取中文星期几
    private fun getChineseWeekDay(date: LocalDate): String {
        return when (date.dayOfWeek.value) {
            1 -> "星期一"
            2 -> "星期二"
            3 -> "星期三"
            4 -> "星期四"
            5 -> "星期五"
            6 -> "星期六"
            7 -> "星期日"
            else -> ""
        }
    }

    // 检查两个日期是否是同一天
    fun isSameDay(date1: LocalDate?, date2: LocalDate?): Boolean {
        if (date1 == null || date2 == null) return false
        return date1 == date2
    }

    // 检查一个日期是否在今天
    fun isToday(date: LocalDate): Boolean {
        return date == LocalDate.now()
    }
}