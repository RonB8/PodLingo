package com.example.podlingo.ui.player

import com.example.podlingo.config.AppDefaults
import com.example.podlingo.data.repository.WordKnowledgeRepository
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * The on-demand vocabulary quiz's state machine ("Quiz" menu item on an episode row, and the
 * end-of-episode "Quiz yourself?" prompt in the player) - one instance per owning ViewModel
 * (unscoped, so each screen gets its own quiz session rather than sharing one across the app).
 * Same Quiz-tab/Simple-tab "Word Check" shape as [com.example.podlingo.ui.player.WordCheckState],
 * just for a single flat pool of words rather than cascading difficulty tiers. Pulled out of
 * [com.example.podlingo.ui.history.HistoryViewModel] so every other episode-list screen (Saved, a
 * podcast's episode list, a playlist) and [PlayerViewModel] can offer the same quiz without
 * re-implementing this state machine.
 */
class QuizSessionController @Inject constructor(
    private val vocabQuizBuilder: VocabQuizBuilder,
    private val wordKnowledgeRepository: WordKnowledgeRepository,
) {
    private val _quiz = MutableStateFlow<VocabQuizState?>(null)
    val quiz: StateFlow<VocabQuizState?> = _quiz.asStateFlow()

    /** One-shot: Quiz was requested for an episode with nothing unknown left to quiz on. */
    private val _noUnknownWordsEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val noUnknownWordsEvent: SharedFlow<Unit> = _noUnknownWordsEvent.asSharedFlow()

    suspend fun start(episodeId: String) {
        val words = vocabQuizBuilder.unknownWordsInEpisode(episodeId)
        if (words.isEmpty()) {
            _noUnknownWordsEvent.tryEmit(Unit)
            return
        }
        // Defaults to the Simple tab when nothing got a buildable question (e.g. not enough real
        // distractors exist yet), same as Word Check - there's always something the user can act on.
        val questions = vocabQuizBuilder.buildQuizQuestions(words)
        _quiz.value = VocabQuizState(
            tab = if (questions.isNotEmpty()) WordCheckTab.QUIZ else WordCheckTab.SIMPLE,
            words = words,
            questions = questions,
        )
    }

    fun onTabSelected(tab: WordCheckTab) {
        _quiz.update { it?.copy(tab = tab) }
    }

    suspend fun onAnswerSelected(answer: String) {
        val current = _quiz.value ?: return
        if (current.answeredThisQuestion != null) return
        val question = current.questions.getOrNull(current.currentIndex) ?: return
        val isCorrect = answer == question.correctAnswer
        if (isCorrect) wordKnowledgeRepository.markKnown(question.word)
        _quiz.update { state ->
            state?.copy(
                answeredThisQuestion = answer,
                quizCorrectWords = if (isCorrect) state.quizCorrectWords + question.word else state.quizCorrectWords,
                // Pre-select it in the Simple tab, same as if the user had tapped it there - still
                // freely editable from that tab afterwards.
                tapSelected = if (isCorrect) state.tapSelected else state.tapSelected + question.word,
            )
        }
        delay(AppDefaults.START_QUIZ_AUTO_ADVANCE_DELAY_MS)
        advanceQuestion()
    }

    /** Leaves the word's status untouched and moves on, same as a normal advance - it stays in [VocabQuizState.words] for the Simple tab to decide on. */
    fun onSkip() {
        val current = _quiz.value ?: return
        if (current.answeredThisQuestion != null) return
        advanceQuestion()
    }

    /** Steps to the next question, or - once the questions run out - switches to the Simple tab so the user finalizes with Continue. */
    private fun advanceQuestion() {
        _quiz.update { state ->
            if (state == null) return@update state
            val nextIndex = state.currentIndex + 1
            if (nextIndex < state.questions.size) {
                state.copy(currentIndex = nextIndex, answeredThisQuestion = null)
            } else {
                state.copy(tab = WordCheckTab.SIMPLE)
            }
        }
    }

    /** Toggles the Simple tab's "I don't know this" selection - a no-op for a word already resolved correctly via the Quiz tab. */
    fun onWordToggled(word: String) {
        _quiz.update { state ->
            if (state == null || word in state.quizCorrectWords) return@update state
            val selected = if (word in state.tapSelected) state.tapSelected - word else state.tapSelected + word
            state.copy(tapSelected = selected)
        }
    }

    /** Toggles between selecting every still-undecided word and clearing the selection. */
    fun onSelectAllToggled() {
        _quiz.update { state ->
            if (state == null) return@update state
            val toggleable = (state.words - state.quizCorrectWords).toSet()
            val allSelected = toggleable.isNotEmpty() && state.tapSelected.containsAll(toggleable)
            state.copy(tapSelected = if (allSelected) emptySet() else toggleable)
        }
    }

    /** Finalizes the session: every word not already resolved correctly in the Quiz tab gets a known/unknown status from the Simple tab's selection, then the dialog closes. */
    suspend fun onContinue() {
        val state = _quiz.value ?: return
        for (word in state.words) {
            if (word in state.quizCorrectWords) continue
            if (word in state.tapSelected) wordKnowledgeRepository.markUnknown(word) else wordKnowledgeRepository.markKnown(word)
        }
        _quiz.value = null
    }

    fun onDismissed() {
        _quiz.value = null
    }
}
