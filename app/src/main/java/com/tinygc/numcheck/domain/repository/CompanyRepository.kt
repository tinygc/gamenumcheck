package com.tinygc.numcheck.domain.repository

import com.tinygc.numcheck.domain.model.Company
import com.tinygc.numcheck.domain.model.CompanyCategory

interface CompanyRepository {
    suspend fun getAllCompanies(): List<Company>
    suspend fun getCompaniesByCategory(category: CompanyCategory): List<Company>
    suspend fun getCompanyBySymbol(symbol: String): Company?
    suspend fun getUnlockedCompanies(level: Int): List<Company>
    suspend fun getInitialCompanies(): List<Company>
    
    suspend fun loadCompaniesFromAssets(): List<Company>
}