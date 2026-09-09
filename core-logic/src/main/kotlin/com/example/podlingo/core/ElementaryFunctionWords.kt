package com.example.podlingo.core

/**
 * Closed-class function words - articles, pronouns, the irregular forms of "be"/"do"/"have",
 * common prepositions/conjunctions/modals - that are unambiguously elementary but show up
 * inconsistently in CEFR vocabulary lists. Those lists are built for content vocabulary, not
 * grammar: "the" being present says nothing about whether "is", "was", or "been" also made the
 * cut (in the bundled Oxford list, none of them did, since they're irregular inflections of "be"
 * that no suffix-stripping stemmer could derive from it either). Treating an omission from that
 * data as "hardest, unranked" - [WordDifficultyRepository]'s fallback for content words - is
 * wrong here: this is a closed, finite set with no real ambiguity about how hard "is" is.
 *
 * Also includes the contracted spellings of those same words ("isn't", "I'll", "wouldn't") - the
 * Oxford lists never contain contractions (they're not dictionary headwords), and neither
 * [EnglishStemmer] nor [IrregularVerbForms] can derive one, since a contraction doesn't share a
 * suffix or an irregular-inflection relationship with its expansion. Each one only combines
 * words that are already elementary on their own (a pronoun/aux/modal + "not"/"is"/"have"/"will"/
 * "would"), so the contraction is elementary too.
 */
object ElementaryFunctionWords {

    fun contains(normalizedWord: String): Boolean = WORDS.contains(normalizedWord)

    private val WORDS = setOf(
        // Articles
        "a", "an", "the",
        // Personal / possessive / demonstrative / relative pronouns
        "i", "you", "he", "she", "it", "we", "they",
        "me", "him", "her", "us", "them",
        "my", "your", "his", "its", "our", "their", "mine", "yours", "hers", "ours", "theirs",
        "this", "that", "these", "those", "who", "whom", "whose", "which", "what",
        // "be" - irregular
        "am", "is", "are", "was", "were", "been", "being",
        // "have" - irregular
        "has", "had", "having",
        // "do" - irregular
        "does", "did", "doing", "done",
        // Common prepositions
        "to", "of", "in", "on", "at", "by", "for", "with", "about", "against", "between",
        "into", "through", "during", "before", "after", "above", "below", "from", "up", "down",
        "over", "under",
        // Common conjunctions
        "and", "but", "or", "nor", "so", "because", "if", "though", "although", "while",
        // Common modals
        "can", "could", "will", "would", "shall", "should", "may", "might", "must",
        // Negative contractions - aux/modal + "not"
        "isn't", "aren't", "wasn't", "weren't",
        "hasn't", "haven't", "hadn't",
        "don't", "doesn't", "didn't",
        "can't", "couldn't", "won't", "wouldn't",
        "shouldn't", "mustn't", "mightn't", "shan't", "needn't", "ain't",
        // Pronoun + verb contractions
        "i'm", "you're", "we're", "they're",
        "he's", "she's", "it's", "that's", "there's", "here's", "what's", "who's",
        "i've", "you've", "we've", "they've",
        "i'll", "you'll", "he'll", "she'll", "it'll", "we'll", "they'll",
        "i'd", "you'd", "he'd", "she'd", "it'd", "we'd", "they'd",
        "let's",
    )
}
