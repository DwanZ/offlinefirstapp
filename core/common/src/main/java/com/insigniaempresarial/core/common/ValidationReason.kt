package com.insigniaempresarial.core.common

enum class ValidationReason {
    AmountCannotBeZero,
    AccountRequired,
    AccountNotFound,
    SourceAccountNotFound,
    DestinationAccountNotFound,
    TransferAmountMustBePositive,
    AccountsMustDiffer,
}
