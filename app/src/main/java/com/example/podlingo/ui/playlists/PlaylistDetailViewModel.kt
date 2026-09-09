package com.example.podlingo.ui.playlists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.podlingo.data.local.dao.PlaylistEpisodeItem
import com.example.podlingo.data.repository.PlaylistRepository
import com.example.podlingo.ui.player.QuizSessionController
import com.example.podlingo.ui.player.VocabQuizState
import com.example.podlingo.ui.player.WordCheckTab
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playlistRepository: PlaylistRepository,
    private val quizSessionController: QuizSessionController,
) : ViewModel() {

    private val playlistId: String = checkNotNull(savedStateHandle["playlistId"])

    val playlistName: StateFlow<String> = playlistRepository.getPlaylist(playlistId)
        .map { it?.name ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val episodes: StateFlow<List<PlaylistEpisodeItem>> = playlistRepository.getEpisodes(playlistId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun removeEpisode(episodeId: String) {
        viewModelScope.launch { playlistRepository.removeEpisode(playlistId, episodeId) }
    }

    val quiz: StateFlow<VocabQuizState?> = quizSessionController.quiz
    val noUnknownWordsEvent: SharedFlow<Unit> = quizSessionController.noUnknownWordsEvent

    fun startQuiz(episodeId: String) {
        viewModelScope.launch { quizSessionController.start(episodeId) }
    }

    fun onQuizTabSelected(tab: WordCheckTab) = quizSessionController.onTabSelected(tab)

    fun onQuizAnswerSelected(answer: String) {
        viewModelScope.launch { quizSessionController.onAnswerSelected(answer) }
    }

    fun onQuizSkip() = quizSessionController.onSkip()

    fun onQuizWordToggled(word: String) = quizSessionController.onWordToggled(word)

    fun onQuizSelectAllToggled() = quizSessionController.onSelectAllToggled()

    fun onQuizContinue() {
        viewModelScope.launch { quizSessionController.onContinue() }
    }

    fun onQuizDismissed() = quizSessionController.onDismissed()
}
