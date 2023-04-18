package com.yeocak.wordpuzzle.ui.customview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.res.ResourcesCompat
import com.yeocak.wordpuzzle.R
import com.yeocak.wordpuzzle.model.Directions
import com.yeocak.wordpuzzle.model.FrozenType
import com.yeocak.wordpuzzle.model.GameState
import com.yeocak.wordpuzzle.model.LetterPaint
import com.yeocak.wordpuzzle.model.LetterVisualType
import com.yeocak.wordpuzzle.model.PuzzleField
import com.yeocak.wordpuzzle.model.RowValues
import com.yeocak.wordpuzzle.model.SingleLetter
import com.yeocak.wordpuzzle.utils.Letters
import com.yeocak.wordpuzzle.utils.OnSwipeTouchListener
import com.yeocak.wordpuzzle.utils.decodeToBitmap
import com.yeocak.wordpuzzle.utils.drawTextCentered
import com.yeocak.wordpuzzle.utils.getRandomColorInt
import com.yeocak.wordpuzzle.utils.parcelable
import com.yeocak.wordpuzzle.utils.parcelableArray
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.min

class WordPuzzleGameView @JvmOverloads constructor(
	context: Context, attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback {

	private var surfaceViewThread: SurfaceViewThread? = null
	private var clockThread: ClockThread? = null

	private var puzzleField: PuzzleField

	private val letterListLock = Mutex()
	private val _letterList = mutableListOf<SingleLetter>()

	private fun <T> useLetterList(usage: suspend MutableList<SingleLetter>.() -> T): T {
		return runBlocking {
			letterListLock.withLock {
				usage(_letterList)
			}
		}
	}

	private var onCurrentWordChangeListener: ((word: String) -> Unit)? = null
	private var onGameStateChangeListener: ((state: GameState) -> Unit)? = null
	private var onSwipeListener: ((direction: Directions) -> Unit)? = null

	private var isStartRowsSend = false

	private val selectedLettersLock = Mutex()
	private var _selectedLetters = mutableSetOf<SingleLetter>()

	private fun <T> useSelectedLetters(usage: suspend MutableSet<SingleLetter>.() -> T): T {
		return runBlocking {
			selectedLettersLock.withLock {
				val oldList = mutableSetOf<SingleLetter>().apply {
					addAll(_selectedLetters)
				}

				val result = usage(_selectedLetters)

				// Check if list changed
				if (oldList.size == _selectedLetters.size) {
					oldList.forEachIndexed { index, old ->
						if (_selectedLetters.elementAt(index) != old) {
							// Changed
							val newValue = _selectedLetters.map { letter ->
								letter.character
							}.joinToString("")
							onCurrentWordChangeListener?.invoke(newValue)
						}
					}
				} else {
					// Changed
					val newValue = _selectedLetters.map { letter ->
						letter.character
					}.joinToString("")
					onCurrentWordChangeListener?.invoke(newValue)
				}

				return@withLock result
			}
		}
	}

	// region -Globals-

	var gameState = GameState.PREPARING
		private set(value) {
			val temp = field
			field = value
			if (value != temp) {
				onGameStateChangeListener?.invoke(value)
			}
		}

	fun pauseGame() {
		if (gameState != GameState.FINISHED) {
			gameState = GameState.PAUSED
		}
	}

	fun unpauseGame() {
		if (gameState != GameState.PAUSED) return
		gameState = GameState.RUNNING

		surfaceViewThread = SurfaceViewThread().apply {
			start()
		}

		val clock = clockThread
		if (clock == null || !clock.isAlive) {
			clockThread = ClockThread().apply {
				start()
			}
		}

	}

	val currentWord: String
		get() {
			return useSelectedLetters {
				map { letter ->
					letter.character
				}.joinToString("")
			}
		}

	var letterAddingFrequency = 5000L

	var startRowCount = 0

	fun setOnCurrentWordChangeListener(listener: (word: String) -> Unit) {
		onCurrentWordChangeListener = listener
	}

	fun setOnGameStateChangeListener(listener: (state: GameState) -> Unit) {
		onGameStateChangeListener = listener
	}

	fun setOnSwipeListener(listener: (direction: Directions) -> Unit) {
		onSwipeListener = listener
	}

	fun popCurrentWord() {
		var selectedColumnSet: Set<Int> = emptySet()
		useLetterList allLetters@{
			useSelectedLetters selectedLetters@{
				selectedColumnSet = _selectedLetters.map { letter ->
					letter.column
				}.toSet()
				this@selectedLetters.forEach { selectedLetter ->
					if (selectedLetter.frozenType is FrozenType.NotFrozen) {
						this@allLetters.remove(selectedLetter)
					} else {
						selectedLetter.frozenType = FrozenType.NotFrozen
						selectedLetter.isSelected = false
					}
				}
				this@selectedLetters.clear()
			}
			selectedColumnSet.forEach { column ->
				calculateLetterPositionsOfColumn(column)
			}
		}
	}

	fun cancelCurrentWord() {
		useSelectedLetters {
			this.forEach { letter ->
				letter.isSelected = false
			}
			this.clear()
		}
	}

	fun sendRandomLetter() {
		val randomColumn = getRandomColumn()
		sendLetter(randomColumn)
	}

	fun sendLetterRow() {
		for (a in 0 until puzzleField.columnNumber) {
			sendLetter(a)
		}
	}

	fun sendLetter(column: Int) {
		val letter = Letters.getRandomLetterByRatio()
		val color = resources.getRandomColorInt()
		val paint = LetterPaint(
			color, puzzleField.fontSize
		)
		val row = getRandomRowValues(column)
		val frozenType = FrozenType.getRandomType()

		val newSingleLetter = SingleLetter(
			letter.char,
			LetterVisualType.getTypeOfLetter(letter),
			paint,
			false,
			frozenType,
			column,
			row
		)

		useLetterList {
			add(newSingleLetter)
		}
	}

	// endregion

	override fun onSaveInstanceState(): Parcelable {
		val bundle = Bundle()
		bundle.putParcelable("superState", super.onSaveInstanceState())
		bundle.putParcelableArray("letterList", _letterList.toTypedArray())
		bundle.putBoolean("isStartRowsSend", isStartRowsSend)
		bundle.putSerializable("gameState", gameState)
		return bundle
	}

	override fun onRestoreInstanceState(state: Parcelable) {
		if (state is Bundle) {
			val list = state.parcelableArray<SingleLetter>("letterList")?.toList()
			useLetterList {
				addAll(list.orEmpty())
			}
			isStartRowsSend = state.getBoolean("isStartRowsSend")
			gameState = state.getSerializable("gameState") as? GameState ?: GameState.PREPARING
			super.onRestoreInstanceState(state.parcelable("superState"))
			return
		}
		super.onRestoreInstanceState(state)
	}

	// region Styles

	private var circleOriginalFrozenBitmap: Bitmap? = null
	private var rectOriginalFrozenBitmap: Bitmap? = null
	private var circleAffectedFrozenBitmap: Bitmap? = null
	private var rectAffectedFrozenBitmap: Bitmap? = null

	private var backgroundColor = ResourcesCompat.getColor(
		resources, R.color.black, null
	)

	private val backgroundPaint = Paint().apply {
		color = backgroundColor
		isAntiAlias = true
	}

	private val unselectedLetterTextPaint = Paint().apply {
		color = backgroundColor
		isAntiAlias = true
		style = Paint.Style.FILL
		textSize = 0f
	}

	private var fieldColor = ResourcesCompat.getColor(
		resources, R.color.purple_200, null
	)

	private val fieldPaint = Paint().apply {
		color = fieldColor
		isAntiAlias = true
	}

	private val emptyPaint = Paint().apply {
		isAntiAlias = true
	}

	private val srcOutPaint = Paint().apply {
		xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
		isAntiAlias = true
	}

	private val topLineColor = ResourcesCompat.getColor(
		resources, R.color.red, null
	)

	private val topLinePaint = Paint().apply {
		strokeWidth = 10f
		style = Paint.Style.FILL_AND_STROKE
		color = topLineColor
		isAntiAlias = true
	}

	private var fpsCounterColor = ResourcesCompat.getColor(
		resources, R.color.chinese_yellow, null
	)

	private val fpsCounterPaint = Paint().apply {
		isAntiAlias = true
		color = fpsCounterColor
		style = Paint.Style.FILL
		textSize = 50f
	}

	init {
		context.theme.obtainStyledAttributes(
			attrs, R.styleable.WordPuzzleGameView, 0, 0
		).apply {
			// Attributes
			val columnNumber = getInt(R.styleable.WordPuzzleGameView_column_number, 0)
			val rowNumber = getInt(R.styleable.WordPuzzleGameView_row_number, 0)
			backgroundColor =
				getColor(R.styleable.WordPuzzleGameView_backgroundColor, backgroundColor)

			puzzleField = PuzzleField(
				columnNumber, rowNumber
			)
			backgroundPaint.color = backgroundColor
			unselectedLetterTextPaint.color = backgroundColor
			startRowCount = getInt(R.styleable.WordPuzzleGameView_start_row_count, 0)
			recycle()

			if (holder != null) {
				holder.addCallback(this@WordPuzzleGameView)
			}

			OnSwipeTouchListener.createHorizontalSwipeDetector(
				context, this@WordPuzzleGameView
			) { direction ->
				if (gameState == GameState.RUNNING) {
					onSwipeListener?.invoke(direction)
				}
			}
		}
	}

	// endregion

	override fun surfaceCreated(p0: SurfaceHolder) {
		if (gameState.isAutoRunnable) {
			gameState = GameState.LAUNCHING
		}
		if (surfaceViewThread == null) {
			surfaceViewThread = SurfaceViewThread()
			surfaceViewThread?.start()
		}
		if (clockThread == null && gameState.isClockRunning) {
			clockThread = ClockThread()
			clockThread?.start()
		}
	}

	override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
		puzzleField.setBounds(width, height)
		unselectedLetterTextPaint.textSize = puzzleField.fontSize
		useLetterList {
			forEach { letter ->
				letter.paint.textSize = puzzleField.fontSize
			}
		}
		if (gameState.isAutoRunnable) {
			gameState = GameState.RUNNING
		}

		// Create bitmaps again with new sizes
		val originalFrozenBitmap = R.drawable.original_frozen.decodeToBitmap(resources)
		val affectedFrozenBitmap = R.drawable.affected_frozen.decodeToBitmap(resources)

		circleOriginalFrozenBitmap = originalFrozenBitmap?.cropCircular(puzzleField.letterSize)
		rectOriginalFrozenBitmap = originalFrozenBitmap?.cropRoundRect(puzzleField.letterSize)
		circleAffectedFrozenBitmap = affectedFrozenBitmap?.cropCircular(puzzleField.letterSize)
		rectAffectedFrozenBitmap = affectedFrozenBitmap?.cropRoundRect(puzzleField.letterSize)

		originalFrozenBitmap?.recycle()
		affectedFrozenBitmap?.recycle()
	}

	override fun surfaceDestroyed(p0: SurfaceHolder) {
		surfaceViewThread?.requestExitAndWait()
		surfaceViewThread = null
	}

	private fun calculate() {
		useLetterList {
			// Calculate next value of position
			forEach { letter ->
				letter.calculateNextRowValue(0.05f)
			}
			// Calculate next original frozen state
			forEach { letter ->
				if (!letter.row.isStable() && letter.frozenType !is FrozenType.OriginalFrozen) return@forEach
				val nearLetters = getNearLetters(letter).filter { nearLetter ->
					nearLetter.frozenType !is FrozenType.OriginalFrozen
				}

				(letter.frozenType as? FrozenType.OriginalFrozen)?.let { originalFrozenType ->
					nearLetters.forEach { foundLetter ->
						if (!originalFrozenType.affectList.contains(foundLetter)) {
							foundLetter.frozenType = FrozenType.AffectedFrozen
							originalFrozenType.affectList.add(foundLetter)
						}
					}
				}
			}
		}
		if (checkIfGameEnded()) {
			gameState = GameState.FINISHED
		}
	}

	private fun Canvas.drawField() {
		// Draw field background
		/*drawRect(
			puzzleField.fieldCoordinates, fieldPaint
		)*/
		// Draw top line
		drawLine(
			0f,
			puzzleField.verticalPadding,
			width.toFloat(),
			puzzleField.verticalPadding,
			topLinePaint
		)
		// Draw all letters
		useLetterList {
			forEach { letter ->
				drawLetter(letter)
			}
		}
	}

	private fun Canvas.drawLetter(letter: SingleLetter) {
		val center = puzzleField.getLetterCenter(letter.row, letter.column)
		val backgroundPaint: Paint
		val textPaint: Paint
		if (letter.isSelected) {
			backgroundPaint = letter.paint.selectedBackground
			textPaint = letter.paint.selectedText
		} else {
			backgroundPaint = letter.paint.unselectedBackground
			textPaint = unselectedLetterTextPaint
		}

		val coordinates = puzzleField.getLetterCoordinates(letter.row, letter.column)
		when (letter.visualType) {
			LetterVisualType.RECT -> {
				drawRoundRect(coordinates, ROUND_RECT_RADIUS, ROUND_RECT_RADIUS, backgroundPaint)
			}

			LetterVisualType.CIRCLE -> {
				val radius = puzzleField.letterSize / 2
				drawCircle(center.x, center.y, radius, backgroundPaint)
			}
		}

		drawFrozenType(letter, coordinates)
		drawTextCentered(letter.character.toString(), center.x, center.y, textPaint)
	}

	private fun Canvas.drawFrozenType(letter: SingleLetter, coordinates: RectF) {
		val typeBitmap = when {
			letter.frozenType is FrozenType.OriginalFrozen && letter.visualType == LetterVisualType.RECT -> rectOriginalFrozenBitmap
			letter.frozenType is FrozenType.OriginalFrozen && letter.visualType == LetterVisualType.CIRCLE -> circleOriginalFrozenBitmap
			letter.frozenType is FrozenType.AffectedFrozen && letter.visualType == LetterVisualType.RECT -> rectAffectedFrozenBitmap
			letter.frozenType is FrozenType.AffectedFrozen && letter.visualType == LetterVisualType.CIRCLE -> circleAffectedFrozenBitmap
			else -> null
		}
		typeBitmap ?: return
		drawBitmap(typeBitmap, null, coordinates, emptyPaint)
	}

	private fun Canvas.drawFrozenType(letter: SingleLetter) {
		val coordinates = puzzleField.getLetterCoordinates(letter.row, letter.column)
		drawFrozenType(letter, coordinates)
	}

	private fun Bitmap.cropCircular(imageSize: Float): Bitmap {
		val sentBitmap =
			Bitmap.createScaledBitmap(this, imageSize.toInt(), imageSize.toInt(), true)
		val bitmapArea = Bitmap.createBitmap(
			imageSize.toInt(),
			imageSize.toInt(),
			Bitmap.Config.ARGB_8888
		)
		val canvas = Canvas(bitmapArea)
		canvas.drawCircle(imageSize / 2, imageSize / 2, imageSize / 2, emptyPaint)
		canvas.drawBitmap(sentBitmap, 0.0f, 0.0f, srcOutPaint)
		sentBitmap.recycle()
		return bitmapArea
	}

	private fun Bitmap.cropRoundRect(imageSize: Float): Bitmap {
		val sentBitmap =
			Bitmap.createScaledBitmap(this, imageSize.toInt(), imageSize.toInt(), true)
		val bitmapArea = Bitmap.createBitmap(
			imageSize.toInt(),
			imageSize.toInt(),
			Bitmap.Config.ARGB_8888
		)
		val canvas = Canvas(bitmapArea)
		val rect = RectF(0f, 0f, imageSize, imageSize)
		canvas.drawRoundRect(rect, ROUND_RECT_RADIUS, ROUND_RECT_RADIUS, emptyPaint)
		canvas.drawBitmap(sentBitmap, 0.0f, 0.0f, srcOutPaint)
		sentBitmap.recycle()
		return bitmapArea
	}


	private inner class SurfaceViewThread : Thread() {

		override fun run() {
			var frameTime = 0L
			try {
				do {
					holder ?: return
					val frameStartTime = System.nanoTime()
					val canvas: Canvas? = holder.lockCanvas()
					if (canvas != null) {
						try {
							if (gameState.isCalculateRunning) {
								calculate()
							}
							canvas.drawColor(backgroundColor) // clear screen
							canvas.drawField()

							// Show fps if active
							if (SHOW_FPS_COUNTER) {
								if (frameTime == 0L) frameTime = 1L
								val frameTimeWithoutSleep = 1_000 / frameTime
								val fps =
									min(frameTimeWithoutSleep, MAX_FRAME_TIME.toLong()).toString()
								canvas.drawText(
									fps,
									0,
									fps.length,
									0f,
									fpsCounterPaint.textSize,
									fpsCounterPaint
								)
							}
						} finally {
							holder.unlockCanvasAndPost(canvas)
						}
					}

					// If faster than the max FPS, limit fps
					frameTime = (System.nanoTime() - frameStartTime) / 1_000_000
					if (frameTime < MAX_FRAME_FREQUENCY) {
						try {
							sleep(MAX_FRAME_FREQUENCY - frameTime)
						} catch (e: InterruptedException) {
							// ignore
						}
					}
				} while (gameState.isThreadsRunning)
			} catch (e: Exception) {
				e.printStackTrace()
				Log.e(LOG_TAG, "Exception while locking/unlocking")
			}
		}


		fun requestExitAndWait() {
			try {
				if (gameState.isAutoRunnable) {
					gameState = GameState.PREPARING
				}
				join()
			} catch (_: InterruptedException) {

			}
		}
	}

	private fun checkIfGameEnded(): Boolean {
		return useLetterList {
			this.any { letter ->
				letter.row.isStable() && letter.row.goal >= puzzleField.rowNumber
			}
		}
	}

	private var lastTouchDownLetter: SingleLetter? = null
	override fun onTouchEvent(event: MotionEvent?): Boolean {
		if (event?.action == MotionEvent.ACTION_DOWN) {
			if (gameState.isClickable) {
				useLetterList {
					forEach { letter ->
						val letterHitbox =
							puzzleField.getLetterCoordinates(letter.row, letter.column)
						if (event.x > letterHitbox.left && event.x < letterHitbox.right && event.y > letterHitbox.top && event.y < letterHitbox.bottom) {
							lastTouchDownLetter = letter
							return@forEach
						}
					}
				}
			}
		}
		if (event?.action == MotionEvent.ACTION_UP) {
			if (gameState.isClickable) {
				useLetterList {
					forEach { letter ->
						val letterHitbox =
							puzzleField.getLetterCoordinates(letter.row, letter.column)
						if (event.x > letterHitbox.left && event.x < letterHitbox.right && event.y > letterHitbox.top && event.y < letterHitbox.bottom && letter == lastTouchDownLetter) {
							onClickSingleLetter(letter)
							return@forEach
						}
					}
				}
			}

			performClick()
		}

		return true
	}

	override fun performClick(): Boolean {
		return super.performClick()
	}

	private fun onClickSingleLetter(letter: SingleLetter) {
		letter.isSelected = !letter.isSelected
		useSelectedLetters {
			if (letter.isSelected) {
				this.add(letter)
			} else {
				this.remove(letter)
			}
		}
	}

	private fun getRandomColumn() = (0 until puzzleField.columnNumber).random()

	private fun getAvailableRow(column: Int): Int {
		return useLetterList {
			val columnLetters = filter { letter ->
				letter.column == column
			}.sortedByDescending { letter ->
				letter.row.goal
			}

			val topLetter = columnLetters.firstOrNull()

			topLetter ?: return@useLetterList 0
			return@useLetterList topLetter.row.goal + 1
		}
	}

	private fun getRandomRowValues(column: Int): RowValues.BetweenRows {
		val row = getAvailableRow(column)
		return RowValues.BetweenRows(
			values = Pair(puzzleField.firstRowOfScreen, puzzleField.firstRowOfScreen + 1),
			//values = Pair(puzzleField.rowNumber, puzzleField.rowNumber + 1),
			betweenRatio = 0f,
			goal = row
		)
	}

	/**
	 * You must use this funcion with: [useLetterList]!"
	 */
	private suspend fun MutableList<SingleLetter>.calculateLetterPositionsOfColumn(column: Int) {
		if (!letterListLock.isLocked) throw Exception("You must use this funcion with: \"useLetterList\"!")

		val columnLetters = this.filter { letter ->
			letter.column == column
		}.sortedBy { letter ->
			letter.row.goal
		}
		columnLetters.forEachIndexed { index, letter ->
			if (letter.row.goal == index) {
				// In correct place
				return@forEachIndexed
			} else {
				when (val currentRow = letter.row) {
					is RowValues.BetweenRows -> {
						letter.row.goal = index
					}

					is RowValues.StableRow -> {
						letter.row = RowValues.BetweenRows(
							values = Pair(currentRow.value - 1, currentRow.value),
							betweenRatio = 0f,
							goal = index
						)
					}
				}
			}
		}
	}

	/**
	 * You must use this funcion with: [useLetterList]!"
	 */
	private suspend fun MutableList<SingleLetter>.getNearLetters(originalLetter: SingleLetter): List<SingleLetter> {
		if (!originalLetter.row.isStable()) return emptyList()

		val foundTop = this.find { searchingLetter ->
			searchingLetter.column == originalLetter.column &&
					searchingLetter.row.goal == originalLetter.row.goal + 1 &&
					searchingLetter.row.isStable()
		}
		val foundDown = this.find { searchingLetter ->
			searchingLetter.column == originalLetter.column &&
					searchingLetter.row.goal == originalLetter.row.goal - 1 &&
					searchingLetter.row.isStable()
		}
		val foundLeft = this.find { searchingLetter ->
			searchingLetter.column == originalLetter.column - 1 &&
					searchingLetter.row.goal == originalLetter.row.goal &&
					searchingLetter.row.isStable()
		}
		val foundRight = this.find { searchingLetter ->
			searchingLetter.column == originalLetter.column + 1 &&
					searchingLetter.row.goal == originalLetter.row.goal &&
					searchingLetter.row.isStable()
		}

		return mutableListOf(
			foundTop, foundDown, foundLeft, foundRight
		).filterNotNull()
	}

	private inner class ClockThread : Thread() {

		override fun run() {
			if (!isStartRowsSend) {
				repeat(startRowCount) {
					try {
						sendLetterRow()
						sleep(500)
					} catch (e: InterruptedException) {
						// ignore
					}
				}
			}
			isStartRowsSend = true
			while (gameState.isThreadsRunning) {
				try {
					sleep(letterAddingFrequency)
					if (gameState.isClockRunning) {
						sendRandomLetter()
					}
				} catch (e: InterruptedException) {
					// ignore
				}
			}
		}
	}

	companion object {
		private const val MAX_FRAME_TIME = 60
		private const val MAX_FRAME_FREQUENCY = 1000 / MAX_FRAME_TIME
		private const val LOG_TAG = "WordPuzzleGameView"

		private const val ROUND_RECT_RADIUS = 15f
		private const val SHOW_FPS_COUNTER = true
	}
}