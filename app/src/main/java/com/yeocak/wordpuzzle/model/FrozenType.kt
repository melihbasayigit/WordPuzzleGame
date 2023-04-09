package com.yeocak.wordpuzzle.model

sealed interface FrozenType {
    object NotFrozen : FrozenType
    data class Frozen(val attempt: Int) : FrozenType
}