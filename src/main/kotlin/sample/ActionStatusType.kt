package sample

/** Processing status concept for various types of actions. */
enum class ActionStatusType {
    UNPROCESSED,
    PROCESSING,
    PROCESSED,
    CANCELLED,
    ERROR;

    fun isFinish(): Boolean = this in FINISH_TYPES

    fun isUnprocessing(): Boolean = this in UNPROCESSING_TYPES

    fun isUnprocessed(): Boolean = this in UNPROCESSED_TYPES

    companion object {
        val FINISH_TYPES = listOf(PROCESSED, CANCELLED)
        val UNPROCESSING_TYPES = listOf(UNPROCESSED, ERROR)
        val UNPROCESSED_TYPES = listOf(UNPROCESSED, PROCESSING, ERROR)
    }
}
