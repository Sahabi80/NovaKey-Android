package com.novakey.keyboard.engine.grammar

data class GrammarIssue(
    val original: String,
    val replacement: String,
    val ruleTitle: String,
    val explanation: String
)

data class GrammarResult(
    val originalText: String,
    val correctedText: String,
    val issues: List<GrammarIssue>
)
