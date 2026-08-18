package com.dark.jobai.service

import android.content.Context
import android.net.Uri
import android.util.Log
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor

/**
 * PdfExtractorService - Extracts text and information from PDF resumes
 */
class PdfExtractorService {

    companion object {
        private const val TAG = "PdfExtractor"
    }

    /**
     * Extract full text from PDF
     */
    fun extractText(context: Context, uri: Uri): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val pdfReader = PdfReader(inputStream)
            val pdfDocument = PdfDocument(pdfReader)

            val textBuilder = StringBuilder()

            for (pageNum in 1..pdfDocument.numberOfPages) {
                val page = pdfDocument.getPage(pageNum)
                val text = PdfTextExtractor.getTextFromPage(page)
                textBuilder.append(text)
                textBuilder.append("\n")
            }

            pdfDocument.close()
            pdfReader.close()
            inputStream?.close()

            textBuilder.toString()
        } catch (e: Exception) {
            Log.e(TAG, "PDF extraction failed", e)
            ""
        }
    }

    /**
     * Extract name from resume text
     */
    fun extractName(text: String): String {
        val lines = text.lines().filter { it.isNotBlank() }

        for (i in 0 until minOf(5, lines.size)) {
            val line = lines[i].trim()
            val words = line.split(" ")

            if (words.size in 2..4 &&
                line.matches(Regex("^[A-Za-z\\s.]+$")) &&
                !line.contains("@") &&
                !line.lowercase().contains("resume") &&
                !line.lowercase().contains("curriculum") &&
                !line.lowercase().contains("cv")
            ) {
                return line
            }
        }
        return ""
    }

    /**
     * Extract email from resume text
     */
    fun extractEmail(text: String): String {
        val emailRegex = Regex("[\\w.+-]+@[\\w-]+\\.[\\w.-]+")
        val match = emailRegex.find(text)
        return match?.value ?: ""
    }

    /**
     * Extract phone from resume text
     */
    fun extractPhone(text: String): String {
        val phoneRegex = Regex("(?:(?:\\+?\\d{1,3})?[-.\\s]?)?(?:\\(?\\d{3}\\)?[-.\\s]?)?\\d{3}[-.\\s]?\\d{4}")
        val match = phoneRegex.find(text)
        return match?.value?.trim() ?: ""
    }

    /**
     * Extract location from resume text
     */
    fun extractLocation(text: String): String {
        val cities = listOf(
            "Bengaluru", "Bangalore", "Mumbai", "Delhi", "Noida",
            "Gurgaon", "Gurugram", "Pune", "Hyderabad", "Chennai",
            "Kolkata", "Ahmedabad", "Jaipur", "Remote", "India"
        )

        for (city in cities) {
            if (text.contains(city, ignoreCase = true)) {
                return city
            }
        }
        return ""
    }

    /**
     * Extract skills from resume text
     */
    fun extractSkills(text: String): List<String> {
        val skills = mutableListOf<String>()
        val techSkills = listOf(
            "Kotlin", "Java", "Android", "Flutter", "React Native", "Swift",
            "iOS", "JavaScript", "TypeScript", "React", "Node.js", "Python",
            "Django", "Flask", "Spring Boot", "Firebase", "AWS", "Azure",
            "GCP", "Docker", "Kubernetes", "SQL", "MySQL", "PostgreSQL",
            "MongoDB", "REST API", "GraphQL", "Git", "CI/CD", "Jetpack Compose",
            "MVVM", "MVP", "Clean Architecture", "Coroutines", "RxJava",
            "Dagger", "Hilt", "Retrofit", "OkHttp", "HTML", "CSS",
            "PHP", "Laravel", "Vue.js", "Angular", ".NET", "Unity"
        )

        val lowerText = text.lowercase()

        for (skill in techSkills) {
            if (lowerText.contains(skill.lowercase())) {
                skills.add(skill)
            }
        }

        return skills.distinct()
    }

    /**
     * Extract experience years from resume text
     */
    fun extractExperienceYears(text: String): String {
        val patterns = listOf(
            Regex("(\\d+)\\s*\\+\\s*years?", RegexOption.IGNORE_CASE),
            Regex("(\\d+)\\s*-\\s*(\\d+)\\s*years?", RegexOption.IGNORE_CASE),
            Regex("minimum\\s+(\\d+)\\s*years?", RegexOption.IGNORE_CASE),
            Regex("at\\s+least\\s+(\\d+)\\s*years?", RegexOption.IGNORE_CASE),
            Regex("(\\d+)\\s*years?\\s*of\\s*experience", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                return if (match.groupValues.size > 2 && match.groupValues[2].isNotEmpty()) {
                    "${match.groupValues[1]}-${match.groupValues[2]}"
                } else {
                    match.groupValues[1]
                }
            }
        }
        return ""
    }

    /**
     * Extract all information from resume
     */
    fun extractAllInfo(context: Context, uri: Uri): ResumeInfo {
        val text = extractText(context, uri)

        return ResumeInfo(
            name = extractName(text),
            email = extractEmail(text),
            phone = extractPhone(text),
            location = extractLocation(text),
            skills = extractSkills(text),
            experienceYears = extractExperienceYears(text),
            fullText = text
        )
    }
}

/**
 * Resume extracted information data class
 */
data class ResumeInfo(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val skills: List<String> = emptyList(),
    val experienceYears: String = "",
    val fullText: String = ""
)