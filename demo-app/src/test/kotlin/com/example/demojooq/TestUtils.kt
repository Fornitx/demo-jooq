package com.example.demojooq

import org.apache.commons.lang3.RandomStringUtils

object TestUtils {
    fun randomString(n: Int = 12): String = RandomStringUtils.randomAlphabetic(n)
}
