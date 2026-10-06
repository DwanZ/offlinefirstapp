package com.insigniaempresarial.core.data.mapper

import com.insigniaempresarial.core.database.entity.BudgetEntity
import com.insigniaempresarial.core.database.entity.CategoryEntity
import com.insigniaempresarial.core.domain.model.Budget
import com.insigniaempresarial.core.domain.model.Category

fun CategoryEntity.toDomain() = Category(id = id, name = name, iconKey = iconKey)

fun BudgetEntity.toDomain() = Budget(
    id = id,
    categoryId = categoryId,
    limitCents = limitCents,
    periodStartEpochMs = periodStartEpochMs,
    periodEndEpochMs = periodEndEpochMs,
)
