package com.tinygc.numcheck.domain.repository

import com.tinygc.numcheck.domain.model.Company
import com.tinygc.numcheck.domain.model.Difficulty
import com.tinygc.numcheck.domain.model.NewsEvent

interface NewsRepository {
    suspend fun getTodaysNews(day: Int): List<NewsEvent>
    suspend fun generateRandomNews(
        companies: List<Company>,
        day: Int,
        difficulty: Difficulty
    ): List<NewsEvent>
    suspend fun saveNews(news: NewsEvent)
    suspend fun getNewsById(id: String): NewsEvent?
    suspend fun getNewsByDay(day: Int): List<NewsEvent>
    suspend fun deleteAllNews()
}