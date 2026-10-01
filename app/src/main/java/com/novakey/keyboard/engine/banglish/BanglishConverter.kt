package com.novakey.keyboard.engine.banglish

import com.novakey.keyboard.engine.bangla.BanglaPhoneticEngine

object BanglishConverter {

    fun convert(text: String): String {
        return BanglaPhoneticEngine.transliterate(text)
    }

    fun getCandidates(rawWord: String): List<String> {
        return BanglaPhoneticEngine.getCandidates(rawWord)
    }
}
