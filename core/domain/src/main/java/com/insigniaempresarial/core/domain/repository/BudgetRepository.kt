package com.insigniaempresarial.core.domain.repository

import com.insigniaempresarial.core.domain.model.Budget
import com.insigniaempresarial.core.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun observeCategories(): Flow<List<Category>>
    fun observeBudgets(): Flow<List<Budget>>
}
