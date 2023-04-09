package com.yeocak.wordpuzzle.utils

fun getRandomLetter() = Letters.values().random()

fun Char.isVowel() = this == 'A' || this == 'E' || this == 'I' || this == 'İ' ||
        this == 'O' || this == 'Ö' || this == 'U' || this == 'Ü'

fun Letters.isVowel() = this.char.isVowel()

enum class Letters(val point: Int) {
    A(1),
    B(3),
    C(4),
    Ç(4),
    D(3),
    E(1),
    F(7),
    G(5),
    Ğ(8),
    H(5),
    I(2),
    İ(1),
    J(10),
    K(1),
    L(1),
    M(2),
    N(1),
    O(2),
    Ö(7),
    P(5),
    R(1),
    S(2),
    Ş(4),
    T(1),
    U(2),
    Ü(3),
    V(7),
    Y(3),
    Z(4);

    val char: Char
        get() = this.name.first()
}