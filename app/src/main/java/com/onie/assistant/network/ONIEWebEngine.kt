package com.onie.assistant.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class ONIEWebEngine {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .callTimeout(45, TimeUnit.SECONDS)
        .build()

    suspend fun process(input: String): String =
        withContext(Dispatchers.IO) {

            Log.e(
                "ONIE_TRACE",
                "WEB: Processing: $input"
            )

            val encoded =
                URLEncoder.encode(input, "UTF-8")

            /*
             * First Internet source:
             * DuckDuckGo Instant Answer API.
             */
            val url =
                "https://api.duckduckgo.com/" +
                "?q=$encoded" +
                "&format=json" +
                "&no_html=1" +
                "&skip_disambig=0"

            val request =
                Request.Builder()
                    .url(url)
                    .get()
                    .header(
                        "User-Agent",
                        "ONIE/0.1 Android Assistant"
                    )
                    .build()

            client.newCall(request).execute().use { response ->

                Log.e(
                    "ONIE_TRACE",
                    "WEB: HTTP ${response.code}"
                )

                val body =
                    response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    throw Exception(
                        "Internet search failed with HTTP ${response.code}"
                    )
                }

                val result =
                    parseDuckDuckGo(
                        body,
                        input
                    )

                if (result != null) {
                    return@withContext result
                }

                /*
                 * DuckDuckGo didn't provide a useful
                 * instant answer.
                 *
                 * Try Wikipedia as a second source.
                 */
                val wikipedia =
                    searchWikipedia(input)

                if (wikipedia != null) {
                    return@withContext wikipedia
                }

                return@withContext (
                    "I searched the Internet for \"$input\", " +
                    "but I couldn't find enough reliable information " +
                    "to answer it."
                )
            }
        }

    /*
     * ONIE handles basic arithmetic itself.
     *
     * Internet is still required because ONIEBrain
     * checks connectivity before reaching this method.
     */
    fun tryCalculate(input: String): String? {

        val cleaned =
            input
                .lowercase()
                .replace("what is", "")
                .replace("calculate", "")
                .replace("equals", "")
                .replace("?", "")
                .trim()

        if (
            !cleaned.matches(
                Regex("[0-9+\\-*/().%\\s]+")
            )
        ) {
            return null
        }

        if (
            !cleaned.any { it.isDigit() } ||
            !cleaned.any {
                it == '+' ||
                it == '-' ||
                it == '*' ||
                it == '/' ||
                it == '%'
            }
        ) {
            return null
        }

        return try {

            val result =
                evaluateExpression(cleaned)

            formatNumber(result)

        } catch (e: Throwable) {

            Log.e(
                "ONIE_TRACE",
                "CALCULATOR ERROR",
                e
            )

            null
        }
    }

    /*
     * Recursive-descent arithmetic parser.
     *
     * Supports:
     * 25 + 37
     * 25 * 16
     * (25 + 5) * 2
     * 100 / 4
     * 20 % 3
     */
    private fun evaluateExpression(
        expression: String
    ): Double {

        class Parser(
            private val text: String
        ) {

            private var position = 0

            private fun skipSpaces() {
                while (
                    position < text.length &&
                    text[position].isWhitespace()
                ) {
                    position++
                }
            }

            private fun match(char: Char): Boolean {

                skipSpaces()

                if (
                    position < text.length &&
                    text[position] == char
                ) {
                    position++
                    return true
                }

                return false
            }

            fun parse(): Double {

                val value =
                    parseExpression()

                skipSpaces()

                if (position != text.length) {
                    throw IllegalArgumentException(
                        "Unexpected character"
                    )
                }

                return value
            }

            private fun parseExpression(): Double {

                var value =
                    parseTerm()

                while (true) {

                    value =
                        when {
                            match('+') ->
                                value + parseTerm()

                            match('-') ->
                                value - parseTerm()

                            else ->
                                return value
                        }
                }
            }

            private fun parseTerm(): Double {

                var value =
                    parseFactor()

                while (true) {

                    value =
                        when {
                            match('*') ->
                                value * parseFactor()

                            match('/') ->
                                value / parseFactor()

                            match('%') ->
                                value % parseFactor()

                            else ->
                                return value
                        }
                }
            }

            private fun parseFactor(): Double {

                skipSpaces()

                if (match('+')) {
                    return parseFactor()
                }

                if (match('-')) {
                    return -parseFactor()
                }

                if (match('(')) {

                    val value =
                        parseExpression()

                    if (!match(')')) {
                        throw IllegalArgumentException(
                            "Missing closing parenthesis"
                        )
                    }

                    return value
                }

                skipSpaces()

                val start =
                    position

                while (
                    position < text.length &&
                    (
                        text[position].isDigit() ||
                        text[position] == '.'
                    )
                ) {
                    position++
                }

                if (start == position) {
                    throw IllegalArgumentException(
                        "Expected number"
                    )
                }

                return text
                    .substring(start, position)
                    .toDouble()
            }
        }

        return Parser(expression).parse()
    }

    private fun formatNumber(
        value: Double
    ): String {

        if (
            value.isNaN() ||
            value.isInfinite()
        ) {
            return "The calculation produced an invalid result."
        }

        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            value.toString()
        }
    }

    private fun parseDuckDuckGo(
        body: String,
        question: String
    ): String? {

        if (body.isBlank()) {
            return null
        }

        val json =
            JSONObject(body)

        /*
         * Direct answer.
         */
        val answer =
            json.optString("Answer")

        if (answer.isNotBlank()) {
            return answer
        }

        /*
         * Abstract answer.
         */
        val abstractText =
            json.optString("AbstractText")

        if (abstractText.isNotBlank()) {

            val source =
                json.optString("AbstractSource")

            return if (source.isNotBlank()) {
                "$abstractText\n\nSource: $source"
            } else {
                abstractText
            }
        }

        /*
         * Related topics.
         */
        val related =
            json.optJSONArray("RelatedTopics")

        if (
            related != null &&
            related.length() > 0
        ) {

            val results =
                mutableListOf<String>()

            for (
                i in 0 until
                    minOf(related.length(), 5)
            ) {

                val item =
                    related.optJSONObject(i)
                        ?: continue

                val text =
                    item.optString("Text")

                if (text.isNotBlank()) {
                    results.add(text)
                }
            }

            if (results.isNotEmpty()) {

                return buildString {

                    append(
                        "I found these Internet results for \"$question\":"
                    )

                    results.forEachIndexed { index, result ->

                        append("\n\n")
                        append(index + 1)
                        append(". ")
                        append(result)
                    }
                }
            }
        }

        /*
         * Heading gives us a weak indication that
         * DuckDuckGo recognized the subject.
         */
        val heading =
            json.optString("Heading")

        if (heading.isNotBlank()) {

            return (
                "I found information about $heading, " +
                "but the available Internet result did not " +
                "contain enough detail to answer reliably."
            )
        }

        return null
    }

    private fun searchWikipedia(
        question: String
    ): String? {

        return try {

            val encoded =
                URLEncoder.encode(
                    question,
                    "UTF-8"
                )

            val url =
                "https://en.wikipedia.org/api/rest_v1/page/summary/$encoded"

            Log.e(
                "ONIE_TRACE",
                "WEB: Trying Wikipedia"
            )

            val request =
                Request.Builder()
                    .url(url)
                    .get()
                    .header(
                        "User-Agent",
                        "ONIE/0.1 Android Assistant"
                    )
                    .build()

            client.newCall(request).execute().use { response ->

                if (!response.isSuccessful) {
                    return null
                }

                val body =
                    response.body?.string().orEmpty()

                if (body.isBlank()) {
                    return null
                }

                val json =
                    JSONObject(body)

                val type =
                    json.optString("type")

                if (type == "https://mediawiki.org/wiki/HyperSwitch/errors/not_found") {
                    return null
                }

                val extract =
                    json.optString("extract")

                if (extract.isBlank()) {
                    return null
                }

                val title =
                    json.optString("title")

                if (title.isNotBlank()) {
                    "$title\n\n$extract"
                } else {
                    extract
                }
            }

        } catch (e: Throwable) {

            Log.e(
                "ONIE_TRACE",
                "WIKIPEDIA ERROR",
                e
            )

            null
        }
    }
}
