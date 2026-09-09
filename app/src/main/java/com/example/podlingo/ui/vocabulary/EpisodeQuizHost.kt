package com.example.podlingo.ui.vocabulary

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.podlingo.ui.player.VocabQuizState
import com.example.podlingo.ui.player.WordCheckTab
import com.example.podlingo.ui.strings.LocalAppStrings
import kotlinx.coroutines.flow.SharedFlow

/** Wires a [QuizSessionController][com.example.podlingo.ui.player.QuizSessionController]'s state to the screen: shows [VocabQuizDialog] while a quiz is open, and toasts when Quiz was requested for an episode with nothing to quiz on. Shared by every episode-list screen that offers the "Quiz" row action, and by the player's end-of-episode quiz prompt. */
@Composable
fun EpisodeQuizHost(
    quiz: VocabQuizState?,
    noUnknownWordsEvent: SharedFlow<Unit>,
    onTabSelected: (WordCheckTab) -> Unit,
    onAnswerSelected: (String) -> Unit,
    onSkip: () -> Unit,
    onWordToggled: (String) -> Unit,
    onSelectAllToggled: () -> Unit,
    onContinue: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    LaunchedEffect(Unit) {
        noUnknownWordsEvent.collect {
            Toast.makeText(context, strings.noUnknownWordsToQuizMessage, Toast.LENGTH_SHORT).show()
        }
    }
    quiz?.let {
        VocabQuizDialog(
            quiz = it,
            onTabSelected = onTabSelected,
            onAnswerSelected = onAnswerSelected,
            onSkip = onSkip,
            onWordToggled = onWordToggled,
            onSelectAllToggled = onSelectAllToggled,
            onContinue = onContinue,
            onDismiss = onDismiss,
        )
    }
}
