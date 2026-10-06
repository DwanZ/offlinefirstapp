package com.insigniaempresarial.core.domain.message

import com.insigniaempresarial.core.common.AppError

/** Maps typed [AppError] values to localized user-facing text (implemented in :app). */
interface ErrorMessageMapper {
    fun map(error: AppError): String
}
