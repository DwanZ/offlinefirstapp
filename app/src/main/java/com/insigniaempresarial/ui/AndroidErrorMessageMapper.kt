package com.insigniaempresarial.ui

import android.content.Context
import com.insigniaempresarial.R
import com.insigniaempresarial.core.common.AppError
import com.insigniaempresarial.core.common.ValidationReason
import com.insigniaempresarial.core.domain.message.ErrorMessageMapper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidErrorMessageMapper @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ErrorMessageMapper {
    override fun map(error: AppError): String = when (error) {
        is AppError.Validation -> context.getString(
            when (error.reason) {
                ValidationReason.AmountCannotBeZero -> R.string.error_amount_cannot_be_zero
                ValidationReason.AccountRequired -> R.string.error_account_required
                ValidationReason.AccountNotFound -> R.string.error_account_not_found
                ValidationReason.SourceAccountNotFound -> R.string.error_source_account_not_found
                ValidationReason.DestinationAccountNotFound -> R.string.error_destination_account_not_found
                ValidationReason.TransferAmountMustBePositive -> R.string.error_transfer_amount_must_be_positive
                ValidationReason.AccountsMustDiffer -> R.string.error_accounts_must_differ
            },
        )
        is AppError.Network -> context.getString(R.string.error_network)
        is AppError.Database -> context.getString(R.string.error_database)
        is AppError.Conflict -> context.getString(R.string.error_conflict)
        is AppError.Unknown -> context.getString(R.string.error_unknown)
    }
}
