package sample.context.orm

enum class MatchMode {
    ANYWHERE,
    END,
    START;

    fun parse(pattern: String): String =
            when (this) {
                ANYWHERE -> "%$pattern%"
                END -> "$pattern%"
                START -> "%$pattern"
            }
}
