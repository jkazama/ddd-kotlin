package sample.context.lock

enum class IdLockType {
    READ,
    WRITE;

    fun isRead(): Boolean = !isWrite()

    fun isWrite(): Boolean = this == WRITE
}
