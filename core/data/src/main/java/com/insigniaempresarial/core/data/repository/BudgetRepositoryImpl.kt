package com.insigniaempresarial.core.data.repository

import com.insigniaempresarial.core.data.mapper.toDomain
import com.insigniaempresarial.core.database.InsigniaDatabase
import com.insigniaempresarial.core.domain.model.Budget
import com.insigniaempresarial.core.domain.model.Category
import com.insigniaempresarial.core.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BudgetRepositoryImpl(
    private val db: InsigniaDatabase,
) : BudgetRepository {
    override fun observeCategories(): Flow<List<Category>> =
        db.categoryDao().observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeBudgets(): Flow<List<Budget>> =
        db.budgetDao().observeAll().map { list -> list.map { it.toDomain() } }
}
