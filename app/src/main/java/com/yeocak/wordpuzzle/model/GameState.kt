package com.yeocak.wordpuzzle.model

enum class GameState(
    val isThreadsRunning: Boolean,
    val isCalculateRunning: Boolean,
    val isClockRunning: Boolean,
    val isClickable: Boolean,
    val isAutoRunnable: Boolean
) {
    PREPARING(false,false,false,false,true),
    LAUNCHING(true,true,true,false,true),
    RUNNING(true,true,true,true,true),
    PAUSED(false,false,false,false,false),
    FINISHED(false,false,false,false,false)
}