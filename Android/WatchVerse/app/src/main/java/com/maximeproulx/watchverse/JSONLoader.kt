package com.maximeproulx.watchverse

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object JSONLoader {

    fun loadUniverse(
        context: Context,
        fileName: String
    ): Universe {

        val jsonText = context.assets
            .open(fileName)
            .bufferedReader()
            .use { it.readText() }

        val root = JSONObject(jsonText)

        val moviesArray = root.getJSONArray("movies")
        val movies = mutableListOf<Movie>()

        for (i in 0 until moviesArray.length()) {

            val movieObject = moviesArray.getJSONObject(i)

            movies.add(
                Movie(
                    id = movieObject.getString("id"),
                    title = movieObject.getString("title"),
                    poster = movieObject.getString("poster"),
                    year = movieObject.getInt("year"),
                    runtime = movieObject.getString("runtime"),
                    synopsis = movieObject.getString("synopsis"),
                    director = movieObject.getString("director"),
                    genres = jsonArrayToStringList(
                        movieObject.getJSONArray("genres")
                    ),
                    tags = jsonArrayToStringList(
                        movieObject.optJSONArray("tags")
                            ?: JSONArray()
                    ),
                    isWatched = movieObject.optBoolean(
                        "isWatched",
                        false
                    ),
                    isSkipped = movieObject.optBoolean(
                        "isSkipped",
                        false
                    )
                )
            )
        }

        return Universe(
            id = root.getString("id"),
            title = root.getString("title"),
            subtitle = root.optString("subtitle"),
            fullTitle = root.optString("fullTitle"),
            description = root.optString("description"),
            logo = root.optString("logo"),
            banner = root.optString("banner"),
            poster = root.optString("poster"),
            filters = jsonArrayToStringList(
                root.optJSONArray("filters")
                    ?: JSONArray()
            ),
            movies = movies
        )
    }

    private fun jsonArrayToStringList(
        jsonArray: JSONArray
    ): List<String> {

        val list = mutableListOf<String>()

        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.getString(i))
        }

        return list
    }

    fun loadUniverseSummaries(
        context: Context
    ): List<UniverseSummary> {

        val jsonText = context.assets
            .open("universes.json")
            .bufferedReader()
            .use { it.readText() }

        val root = JSONArray(jsonText)
        val universes = mutableListOf<UniverseSummary>()

        for (i in 0 until root.length()) {

            val universeObject = root.getJSONObject(i)

            universes.add(
                UniverseSummary(
                    id = universeObject.getString("id"),
                    state = universeObject.getString("state"),
                    file = universeObject.getString("file")
                )
            )
        }

        return universes
    }

    fun loadActiveUniverse(
        context: Context
    ): Universe {

        val summaries = loadUniverseSummaries(context)

        val activeSummary = summaries.first {
            it.state == "available"
        }

        return loadUniverse(
            context = context,
            fileName = "${activeSummary.file}.json"
        )
    }
}