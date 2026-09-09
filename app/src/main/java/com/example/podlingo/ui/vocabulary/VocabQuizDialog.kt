package com.example.podlingo.ui.vocabulary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.podlingo.config.AppDefaults
import com.example.podlingo.ui.player.VocabQuizQuestion
import com.example.podlingo.ui.player.VocabQuizState
import com.example.podlingo.ui.player.WordCheckTab
import com.example.podlingo.ui.strings.LocalAppStrings

/**
 * The end-of-episode/on-demand "Word Check" - a Quiz tab (multiple choice, reusing
 * [QuizQuestionOptions]) and a Simple tab (tap the words you don't know), both working through the
 * same word pool - see [VocabQuizState]. Shared between the end-of-episode quiz prompt in the
 * player and every episode-list screen's on-demand "Quiz" menu item via [EpisodeQuizHost]. Same
 * shape as the pre-episode Word Check panel in [com.example.podlingo.ui.player.PlayerScreen], just
 * without cascading difficulty tiers.
 */
@Composable
fun VocabQuizDialog(
    quiz: VocabQuizState,
    onTabSelected: (WordCheckTab) -> Unit,
    onAnswerSelected: (String) -> Unit,
    onSkip: () -> Unit,
    onWordToggled: (String) -> Unit,
    onSelectAllToggled: () -> Unit,
    onContinue: () -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = LocalAppStrings.current
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(strings.wordCheckTitle, style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = strings.dismiss)
                    }
                }
                TabRow(selectedTabIndex = quiz.tab.ordinal) {
                    Tab(
                        selected = quiz.tab == WordCheckTab.QUIZ,
                        onClick = { onTabSelected(WordCheckTab.QUIZ) },
                        text = { Text(strings.wordCheckQuizTab) },
                    )
                    Tab(
                        selected = quiz.tab == WordCheckTab.SIMPLE,
                        onClick = { onTabSelected(WordCheckTab.SIMPLE) },
                        text = { Text(strings.wordCheckSimpleTab) },
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                when (quiz.tab) {
                    WordCheckTab.QUIZ -> VocabQuizTab(quiz, onAnswerSelected, onSkip)
                    WordCheckTab.SIMPLE -> WordCheckSimpleTabContent(
                        words = quiz.words,
                        quizCorrectWords = quiz.quizCorrectWords,
                        tapSelected = quiz.tapSelected,
                        onWordToggled = onWordToggled,
                        onSelectAllToggled = onSelectAllToggled,
                        onContinue = onContinue,
                    )
                }
            }
        }
    }
}

@Composable
private fun VocabQuizTab(quiz: VocabQuizState, onAnswerSelected: (String) -> Unit, onSkip: () -> Unit) {
    val strings = LocalAppStrings.current
    val question = quiz.questions.getOrNull(quiz.currentIndex)
    if (question == null) {
        // Every word here lacks a buildable question (e.g. not enough real distractors exist yet)
        // - nothing to show here, the Simple tab is how this session gets finished.
        Text(
            text = strings.tapWordsExplanation,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    val answered = quiz.answeredThisQuestion
    Text(
        text = strings.questionXOfY(quiz.currentIndex + 1, quiz.questions.size),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(8.dp))
    QuizQuestionOptions(question = question, answered = answered, onAnswerSelected = onAnswerSelected)
    if (answered == null) {
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            Text(strings.skip)
        }
    }
}

/**
 * The word + multiple-choice options for one quiz question, with the right/wrong reveal once
 * [answered] is set - shared by every Word Check-style Quiz tab (the pre-episode assessment and
 * this on-demand/end-of-episode quiz), so they all render questions identically.
 */
@Composable
fun QuizQuestionOptions(
    question: VocabQuizQuestion,
    answered: String?,
    onAnswerSelected: (String) -> Unit,
) {
    Text(question.word, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(16.dp))
    question.options.forEach { option ->
        val isCorrectOption = option == question.correctAnswer
        val containerColor = when {
            answered == null -> MaterialTheme.colorScheme.surfaceVariant
            isCorrectOption -> MaterialTheme.colorScheme.primaryContainer
            option == answered -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
        val contentColor = when {
            answered == null -> MaterialTheme.colorScheme.onSurfaceVariant
            isCorrectOption -> MaterialTheme.colorScheme.onPrimaryContainer
            option == answered -> MaterialTheme.colorScheme.onErrorContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
        Button(
            onClick = { if (answered == null) onAnswerSelected(option) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        ) {
            Text(option)
        }
    }
}

/**
 * The Simple tab shared by every Word Check-style dialog (the pre-episode assessment and this
 * on-demand/end-of-episode quiz) - tap the words you don't know, with words already answered
 * correctly in a Quiz tab shown resolved/read-only.
 */
@Composable
fun WordCheckSimpleTabContent(
    words: List<String>,
    quizCorrectWords: Set<String>,
    tapSelected: Set<String>,
    onWordToggled: (String) -> Unit,
    onSelectAllToggled: () -> Unit,
    onContinue: () -> Unit,
) {
    val strings = LocalAppStrings.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = strings.doYouKnowTheseWordsTitle,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = strings.tapWordsExplanation,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )
        val toggleable = words - quizCorrectWords
        TextButton(onClick = onSelectAllToggled, modifier = Modifier.align(Alignment.End)) {
            Text(if (toggleable.isNotEmpty() && tapSelected.containsAll(toggleable)) strings.deselectAll else strings.selectAll)
        }
        // Capped and independently scrollable so a long word list can never push the Continue
        // button itself off-screen - the header and button always stay put.
        FlowRow(
            modifier = Modifier
                .heightIn(max = AppDefaults.WORD_CHECK_SIMPLE_TAB_MAX_HEIGHT_DP.dp)
                .verticalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            words.forEach { word ->
                if (word in quizCorrectWords) {
                    // Already answered correctly in the Quiz tab - shown resolved, not tappable.
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        leadingIcon = { Icon(Icons.Filled.Check, contentDescription = null) },
                        label = { Text(word) },
                        colors = AssistChipDefaults.assistChipColors(
                            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            disabledLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            disabledLeadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                } else {
                    FilterChip(
                        // Pre-selected if it was answered wrong in the Quiz tab, same as if the user
                        // had tapped it here - still freely editable either way from this tab.
                        selected = word in tapSelected,
                        onClick = { onWordToggled(word) },
                        label = { Text(word) },
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text(strings.continueLabel)
        }
    }
}
