package com.dark.jobai.service

import android.content.Context
import android.net.Uri
import android.util.Log
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import java.util.Locale

/**
 * PdfExtractorService
 *
 * Extracts useful information from text-based PDF resumes.
 *
 * Important:
 * - Existing ResumeInfo fields are preserved.
 * - Missing information returns an empty string/list.
 * - User can edit extracted information before saving.
 * - This service does not upload anything or modify the original PDF.
 */
class PdfExtractorService {

    companion object {
        private const val TAG = "PdfExtractor"
    }

    /**
     * Extract full text from PDF.
     *
     * Returns empty string if:
     * - File cannot be opened
     * - PDF is invalid
     * - PDF is image-only/scanned
     * - Extraction fails
     */
    fun extractText(context: Context, uri: Uri): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return ""

            inputStream.use { stream ->

                PdfReader(stream).use { reader ->

                    PdfDocument(reader).use { pdfDocument ->

                        val textBuilder = StringBuilder()

                        for (pageNum in 1..pdfDocument.numberOfPages) {
                            val page = pdfDocument.getPage(pageNum)

                            val text = PdfTextExtractor.getTextFromPage(page)

                            if (text.isNotBlank()) {
                                textBuilder.append(text.trim())
                                textBuilder.append("\n")
                            }
                        }

                        textBuilder.toString().trim()
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "PDF extraction failed", e)
            ""
        }
    }

    // ═════════════════════════════════════════════
    // NAME
    // ═════════════════════════════════════════════

    fun extractName(text: String): String {

        val lines = cleanLines(text)

        if (lines.isEmpty()) return ""

        val ignoredWords = setOf(
            "resume",
            "curriculum",
            "vitae",
            "cv",
            "profile",
            "summary",
            "objective",
            "contact",
            "experience",
            "education",
            "skills",
            "projects",
            "developer",
            "engineer",
            "professional"
        )

        // Usually name appears near the top.
        // Check first 15 meaningful lines instead of only 5.
        for (line in lines.take(15)) {

            val normalized = line
                .replace(Regex("\\s+"), " ")
                .trim()

            val words = normalized.split(" ")

            val lower = normalized.lowercase(Locale.ROOT)

            if (
                words.size in 2..4 &&
                normalized.length in 3..60 &&
                normalized.matches(Regex("^[A-Za-z][A-Za-z .'-]*$")) &&
                !normalized.contains("@") &&
                !normalized.any { it.isDigit() } &&
                ignoredWords.none { lower.contains(it) }
            ) {
                return normalized
            }
        }

        return ""
    }

    // ═════════════════════════════════════════════
    // EMAIL
    // ═════════════════════════════════════════════

    fun extractEmail(text: String): String {

        val emailRegex = Regex(
            """\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}\b"""
        )

        return emailRegex
            .find(text)
            ?.value
            ?.trim()
            ?: ""
    }

    // ═════════════════════════════════════════════
    // PHONE
    // ═════════════════════════════════════════════

    fun extractPhone(text: String): String {

        val phoneRegex = Regex(
            """(?<!\d)(?:\+?\d{1,3}[\s.-]?)?(?:\(?\d{3,4}\)?[\s.-]?)?\d{3,4}[\s.-]?\d{3,4}(?!\d)"""
        )

        val candidates = phoneRegex.findAll(text)
            .map { it.value.trim() }
            .filter { candidate ->

                val digits = candidate.filter { it.isDigit() }

                // Phone should generally have 10-15 digits.
                if (digits.length !in 10..15) return@filter false

                // Reject obvious years.
                if (digits.length == 4 &&
                    digits.toIntOrNull() in 1900..2100
                ) {
                    return@filter false
                }

                true
            }
            .toList()

        return candidates.firstOrNull() ?: ""
    }

    // ═════════════════════════════════════════════
    // LOCATION
    // ═════════════════════════════════════════════

    fun extractLocation(text: String): String {

        val lines = cleanLines(text)

        val locationLabels = listOf(
            "location",
            "address",
            "based in",
            "city"
        )

        // First try labelled location.
        for (line in lines) {

            val lower = line.lowercase(Locale.ROOT)

            if (locationLabels.any { lower.startsWith(it) }) {

                val value = line
                    .substringAfter(":")
                    .trim()

                if (value.isNotBlank()) {
                    return value
                }
            }
        }

        // Fallback: known cities/countries.
        val locations = listOf(
            "New York",
            "San Francisco",
            "Los Angeles",
            "Chicago",
            "Seattle",
            "Boston",
            "Austin",
            "Bengaluru",
            "Bangalore",
            "Mumbai",
            "Delhi",
            "New Delhi",
            "Noida",
            "Gurgaon",
            "Gurugram",
            "Pune",
            "Hyderabad",
            "Chennai",
            "Kolkata",
            "Ahmedabad",
            "Jaipur",
            "Remote",
            "India",
            "United States",
            "USA"
        )

        for (location in locations) {
            if (text.contains(location, ignoreCase = true)) {
                return location
            }
        }

        return ""
    }

    // ═════════════════════════════════════════════
    // SKILLS
    // ═════════════════════════════════════════════

    fun extractSkills(text: String): List<String> {

        val techSkills = listOf(
            "Kotlin",
            "Java",
            "Android",
            "Flutter",
            "React Native",
            "Swift",
            "iOS",
            "JavaScript",
            "TypeScript",
            "React",
            "Node.js",
            "Python",
            "Django",
            "Flask",
            "Spring Boot",
            "Firebase",
            "AWS",
            "Azure",
            "GCP",
            "Docker",
            "Kubernetes",
            "SQL",
            "MySQL",
            "PostgreSQL",
            "MongoDB",
            "REST API",
            "GraphQL",
            "Git",
            "CI/CD",
            "Jetpack Compose",
            "MVVM",
            "MVP",
            "Clean Architecture",
            "Coroutines",
            "RxJava",
            "Dagger",
            "Hilt",
            "Retrofit",
            "OkHttp",
            "HTML",
            "CSS",
            "PHP",
            "Laravel",
            "Vue.js",
            "Angular",
            ".NET",
            "Unity",
            "C++",
            "C#",
            "Go",
            "Rust",
            "Machine Learning",
            "Data Science",
            "TensorFlow",
            "PyTorch",
            "Figma",
            "Jira",
            "Agile",
            "Scrum"
        )

        val foundSkills = mutableListOf<String>()

        for (skill in techSkills) {

            val escapedSkill = Regex.escape(skill)

            val regex = Regex(
                """(?<![A-Za-z0-9+#.])$escapedSkill(?![A-Za-z0-9+#.])""",
                RegexOption.IGNORE_CASE
            )

            if (regex.containsMatchIn(text)) {
                foundSkills.add(skill)
            }
        }

        return foundSkills.distinct()
    }

    // ═════════════════════════════════════════════
    // EXPERIENCE
    // ═════════════════════════════════════════════

    fun extractExperienceYears(text: String): String {

        val patterns = listOf(

            Regex(
                """(\d+(?:\.\d+)?)\s*[-–]\s*(\d+(?:\.\d+)?)\s*(?:\+)?\s*years?""",
                RegexOption.IGNORE_CASE
            ),

            Regex(
                """(\d+(?:\.\d+)?)\s*\+\s*years?""",
                RegexOption.IGNORE_CASE
            ),

            Regex(
                """(?:minimum|at least)\s+(\d+(?:\.\d+)?)\s*years?""",
                RegexOption.IGNORE_CASE
            ),

            Regex(
                """(\d+(?:\.\d+)?)\s*years?\s*(?:of)?\s*experience""",
                RegexOption.IGNORE_CASE
            )
        )

        for (pattern in patterns) {

            val match = pattern.find(text) ?: continue

            return if (match.groupValues.size > 2 &&
                match.groupValues[2].isNotEmpty()
            ) {
                "${match.groupValues[1]}-${match.groupValues[2]}"
            } else {
                match.groupValues[1]
            }
        }

        return ""
    }

    // ═════════════════════════════════════════════
    // JOB TITLE
    // ═════════════════════════════════════════════

    fun extractJobTitle(text: String): String {

        val lines = cleanLines(text)

        val titleKeywords = listOf(
            "software engineer",
            "software developer",
            "android developer",
            "mobile developer",
            "frontend developer",
            "backend developer",
            "full stack developer",
            "full-stack developer",
            "data scientist",
            "data analyst",
            "product manager",
            "project manager",
            "ui ux designer",
            "ui/ux designer",
            "devops engineer",
            "qa engineer",
            "qa tester",
            "intern",
            "developer",
            "engineer",
            "designer"
        )

        for (line in lines.take(15)) {

            val lower = line.lowercase(Locale.ROOT)

            if (titleKeywords.any { lower.contains(it) } &&
                line.length <= 80
            ) {
                return line
            }
        }

        return ""
    }

    // ═════════════════════════════════════════════
    // LINKEDIN
    // ═════════════════════════════════════════════

    fun extractLinkedIn(text: String): String {

        val regex = Regex(
            """(?:https?://)?(?:www\.)?linkedin\.com/in/[A-Za-z0-9._-]+""",
            RegexOption.IGNORE_CASE
        )

        return regex.find(text)?.value ?: ""
    }

    // ═════════════════════════════════════════════
    // PORTFOLIO / WEBSITE
    // ═════════════════════════════════════════════

    fun extractWebsite(text: String): String {

        val regex = Regex(
            """https?://[^\s]+""",
            RegexOption.IGNORE_CASE
        )

        return regex.findAll(text)
            .map { it.value.trimEnd('.', ',', ')', ']') }
            .firstOrNull {
                !it.contains("linkedin.com", ignoreCase = true) &&
                        !it.contains("github.com", ignoreCase = true)
            }
            ?: ""
    }

    // ═════════════════════════════════════════════
    // EDUCATION
    // ═════════════════════════════════════════════

    fun extractEducation(text: String): String {

        val lines = cleanLines(text)

        val educationKeywords = listOf(
            "b.tech",
            "btech",
            "b.e.",
            "bachelor",
            "master",
            "m.tech",
            "mtech",
            "mca",
            "bca",
            "b.sc",
            "m.sc",
            "mba",
            "computer science",
            "information technology"
        )

        for (line in lines) {

            val lower = line.lowercase(Locale.ROOT)

            if (
                educationKeywords.any { lower.contains(it) } &&
                line.length <= 120
            ) {
                return line
            }
        }

        return ""
    }

    // ═════════════════════════════════════════════
    // CURRENT / LAST COMPANY
    // ═════════════════════════════════════════════

    fun extractCompany(text: String): String {

        val lines = cleanLines(text)

        val companyKeywords = listOf(
            "experience",
            "work experience",
            "professional experience"
        )

        val startIndex = lines.indexOfFirst { line ->
            companyKeywords.any {
                line.equals(it, ignoreCase = true)
            }
        }

        if (startIndex == -1) return ""

        // Look at the next few lines for company-like information.
        for (i in startIndex + 1 until minOf(startIndex + 8, lines.size)) {

            val line = lines[i]

            if (
                line.length in 2..100 &&
                !line.contains("@") &&
                !line.any { it.isDigit() } &&
                !line.equals("experience", ignoreCase = true)
            ) {
                return line
            }
        }

        return ""
    }

    // ═════════════════════════════════════════════
    // EXTRACT EVERYTHING
    // ═════════════════════════════════════════════

    fun extractAllInfo(
        context: Context,
        uri: Uri
    ): ResumeInfo {

        val text = extractText(context, uri)

        if (text.isBlank()) {
            return ResumeInfo()
        }

        return ResumeInfo(
            name = extractName(text),
            email = extractEmail(text),
            phone = extractPhone(text),
            location = extractLocation(text),
            skills = extractSkills(text),
            experienceYears = extractExperienceYears(text),
            jobTitle = extractJobTitle(text),
            linkedin = extractLinkedIn(text),
            website = extractWebsite(text),
            education = extractEducation(text),
            company = extractCompany(text),
            fullText = text
        )
    }

    // ═════════════════════════════════════════════
    // HELPERS
    // ═════════════════════════════════════════════

    private fun cleanLines(text: String): List<String> {

        return text.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { it.replace(Regex("\\s+"), " ") }
    }
}

/**
 * Resume extracted information.
 *
 * Existing fields preserved for backward compatibility.
 * New fields are optional and default to empty.
 */
data class ResumeInfo(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val skills: List<String> = emptyList(),
    val experienceYears: String = "",
    val fullText: String = "",

    // New extracted fields
    val jobTitle: String = "",
    val linkedin: String = "",
    val website: String = "",
    val education: String = "",
    val company: String = ""
)