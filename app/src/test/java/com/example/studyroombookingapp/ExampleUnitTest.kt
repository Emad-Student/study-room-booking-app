package com.example.studyroombookingapp

import org.junit.Test
import org.junit.Assert.*

class ReservationValidationTest {

    // Helper functions extracted from app logic for testing
    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && email.contains("@") && email.contains(".")
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    private fun doPasswordsMatch(password: String, confirm: String): Boolean {
        return password == confirm
    }

    private fun isValidDate(date: String): Boolean {
        return date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))
    }

    private fun isValidTime(time: String): Boolean {
        return time.matches(Regex("\\d{2}:\\d{2}"))
    }

    private fun isEndTimeAfterStart(start: String, end: String): Boolean {
        return end > start
    }

    private fun isValidReservation(room: String, date: String, start: String, end: String): Boolean {
        return room.isNotBlank() && isValidDate(date) && isValidTime(start) && isValidTime(end) && isEndTimeAfterStart(start, end)
    }

    // === Email Validation Tests ===
    @Test
    fun validEmail_returnsTrue() {
        assertTrue(isValidEmail("test@test.com"))
    }

    @Test
    fun emptyEmail_returnsFalse() {
        assertFalse(isValidEmail(""))
    }

    @Test
    fun emailWithoutAt_returnsFalse() {
        assertFalse(isValidEmail("testtest.com"))
    }

    // === Password Validation Tests ===
    @Test
    fun validPassword_returnsTrue() {
        assertTrue(isValidPassword("123456"))
    }

    @Test
    fun shortPassword_returnsFalse() {
        assertFalse(isValidPassword("123"))
    }

    @Test
    fun exactSixChars_returnsTrue() {
        assertTrue(isValidPassword("abcdef"))
    }

    // === Password Match Tests ===
    @Test
    fun matchingPasswords_returnsTrue() {
        assertTrue(doPasswordsMatch("password", "password"))
    }

    @Test
    fun mismatchedPasswords_returnsFalse() {
        assertFalse(doPasswordsMatch("password", "different"))
    }

    // === Date Validation Tests ===
    @Test
    fun validDate_returnsTrue() {
        assertTrue(isValidDate("2026-06-11"))
    }

    @Test
    fun invalidDateFormat_returnsFalse() {
        assertFalse(isValidDate("11-06-2026"))
    }

    @Test
    fun emptyDate_returnsFalse() {
        assertFalse(isValidDate(""))
    }

    // === Time Validation Tests ===
    @Test
    fun validTime_returnsTrue() {
        assertTrue(isValidTime("09:00"))
    }

    @Test
    fun invalidTimeFormat_returnsFalse() {
        assertFalse(isValidTime("9am"))
    }

    // === Time Range Tests ===
    @Test
    fun endAfterStart_returnsTrue() {
        assertTrue(isEndTimeAfterStart("09:00", "11:00"))
    }

    @Test
    fun endBeforeStart_returnsFalse() {
        assertFalse(isEndTimeAfterStart("11:00", "09:00"))
    }

    @Test
    fun sameStartAndEnd_returnsFalse() {
        assertFalse(isEndTimeAfterStart("09:00", "09:00"))
    }

    // === Full Reservation Validation Tests ===
    @Test
    fun validReservation_returnsTrue() {
        assertTrue(isValidReservation("Room A", "2026-06-11", "09:00", "11:00"))
    }

    @Test
    fun emptyRoomName_returnsFalse() {
        assertFalse(isValidReservation("", "2026-06-11", "09:00", "11:00"))
    }

    @Test
    fun invalidDateInReservation_returnsFalse() {
        assertFalse(isValidReservation("Room A", "bad-date", "09:00", "11:00"))
    }
}
