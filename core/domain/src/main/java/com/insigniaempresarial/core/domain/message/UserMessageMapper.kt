package com.insigniaempresarial.core.domain.message

import com.insigniaempresarial.core.common.UserMessageKey

/** Maps [UserMessageKey] values to localized user-facing text (implemented in :app). */
interface UserMessageMapper {
    fun map(key: UserMessageKey): String
}
