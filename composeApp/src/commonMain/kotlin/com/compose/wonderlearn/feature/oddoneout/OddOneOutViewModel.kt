package com.compose.wonderlearn.feature.oddoneout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.wonderlearn.domain.ProgressRepository
import com.compose.wonderlearn.domain.VocabularyItem
import com.compose.wonderlearn.domain.VocabularyRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val GROUP_SIZE = 3
private const val REVEAL_DELAY_MS = 1300L

data class OddOneOutState(
  val options: List<VocabularyItem> = emptyList(),
  val oddId: String? = null,
  val solved: Boolean = false,
  val wrongId: String? = null,
  val score: Int = 0,
  val loading: Boolean = true,
)

class OddOneOutViewModel(
  private val vocabulary: VocabularyRepository,
  private val progress: ProgressRepository,
) : ViewModel() {

  private val _state = MutableStateFlow(OddOneOutState())
  val state: StateFlow<OddOneOutState> = _state.asStateFlow()

  private var pool: List<VocabularyItem> = emptyList()

  init {
    viewModelScope.launch {
      pool = vocabulary.randomItems(POOL_SIZE)
      newRound()
    }
  }

  fun newRound() {
    val byCategory = pool.groupBy { it.categoryId }
    val groupCategories = byCategory.filterValues { it.size >= GROUP_SIZE }.keys
    if (groupCategories.isEmpty() || byCategory.size < 2) {
      _state.value = _state.value.copy(loading = false)
      return
    }
    val groupCategory = groupCategories.random()
    val group = byCategory.getValue(groupCategory).shuffled().take(GROUP_SIZE)
    val oddCategory = byCategory.keys.filter { it != groupCategory }.random()
    val odd = byCategory.getValue(oddCategory).random()

    _state.value = _state.value.copy(
      options = (group + odd).shuffled(),
      oddId = odd.id,
      solved = false,
      wrongId = null,
      loading = false,
    )
  }

  fun onSelect(item: VocabularyItem) {
    val current = _state.value
    if (current.solved || current.loading) return
    if (item.id == current.oddId) {
      _state.value = current.copy(solved = true, wrongId = null, score = current.score + 1)
      viewModelScope.launch {
        progress.recordCorrectAnswer()
        delay(REVEAL_DELAY_MS)
        newRound()
      }
    } else {
      _state.value = current.copy(wrongId = item.id)
    }
  }

  private companion object {
    const val POOL_SIZE = 1000
  }
}
