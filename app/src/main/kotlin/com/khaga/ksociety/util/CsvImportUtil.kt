package com.khaga.ksociety.util

import com.khaga.ksociety.model.Member
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Parses a CSV file into a list of [Member] objects.
 *
 * Expected column order (header row optional):
 *   name, phone, contribution, amt_borrowed, fees, join_date
 *
 * Rules:
 *  - Lines starting with '#' are treated as comments and skipped.
 *  - A header row is auto-detected if the first non-comment line contains "name".
 *  - Only [name] is required; all other fields are optional and default to 0 / current month.
 *  - Currency values must be whole-rupee integers (e.g. 2000, not 2000.50).
 *  - join_date should be in "Mon YYYY" format (e.g. "Jan 2025"). Blanks default to current month.
 */
object CsvImportUtil {

    data class ParseResult(
        val members: List<Member>,
        val skipped: List<String>   // human-readable error lines
    )

    private val currentMonth: String
        get() = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date())

    fun parse(csvContent: String, fundId: Long): ParseResult {
        val members  = mutableListOf<Member>()
        val skipped  = mutableListOf<String>()

        val lines = csvContent.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("#") }

        if (lines.isEmpty()) {
            skipped.add("File is empty or contains only comments.")
            return ParseResult(emptyList(), skipped)
        }

        // Auto-detect and skip header row
        val dataLines = if (lines.first().lowercase().startsWith("name")) {
            lines.drop(1)
        } else {
            lines
        }

        if (dataLines.isEmpty()) {
            skipped.add("No data rows found (only a header row).")
            return ParseResult(emptyList(), skipped)
        }

        dataLines.forEachIndexed { idx, line ->
            val row = idx + 2   // human-readable row number (1-based, accounting for header)
            val cols = splitCsvLine(line)

            val name = cols.getOrNull(0)?.trim().orEmpty()
            if (name.isBlank()) {
                skipped.add("Row $row: 'name' is required — skipped.")
                return@forEachIndexed
            }

            val phone        = cols.getOrNull(1)?.trim().orEmpty()
            val contribution = cols.getOrNull(2)?.trim()?.toLongOrNull() ?: 0L
            val amtBorrowed  = cols.getOrNull(3)?.trim()?.toLongOrNull() ?: 0L
            val fees         = cols.getOrNull(4)?.trim()?.toLongOrNull() ?: 0L
            val joinDate     = cols.getOrNull(5)?.trim()?.ifBlank { currentMonth } ?: currentMonth

            members.add(
                Member(
                    name         = name,
                    phone        = phone,
                    avatar       = Member.generateAvatar(name),
                    contribution = contribution,
                    amtBorrowed  = amtBorrowed,
                    fees         = fees,
                    joinDate     = joinDate,
                    fundId       = fundId
                )
            )
        }

        return ParseResult(members, skipped)
    }

    /** Splits a CSV line respecting quoted fields (e.g. "Kumar, Ravi"). */
    private fun splitCsvLine(line: String): List<String> {
        val result  = mutableListOf<String>()
        val current = StringBuilder()
        var inQuote = false
        for (ch in line) {
            when {
                ch == '"'        -> inQuote = !inQuote
                ch == ',' && !inQuote -> { result.add(current.toString()); current.clear() }
                else             -> current.append(ch)
            }
        }
        result.add(current.toString())
        return result
    }

    /** Returns the template CSV text bundled in assets as a plain String. */
    fun templateText(): String = buildString {
        appendLine("name,phone,contribution,amt_borrowed,fees,join_date")
        appendLine("Ravi Kumar,9876543210,2000,10000,500,Jan 2025")
        appendLine("Sunita Devi,9123456789,2000,0,500,Jan 2025")
        appendLine("Mohan Rao,,2000,0,500,Jan 2025")
    }
}
