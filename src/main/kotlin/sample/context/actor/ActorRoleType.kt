package sample.context.actor

/** Role of the actor. */
enum class ActorRoleType {
    /** Anonymous user. (the actor who does not have specific information such as an ID) */
    ANONYMOUS,
    /** User. (mainly customers in BtoC, staff in BtoB) */
    USER,
    /** Internal User. (mainly staff in BtoC, staff managers in BtoB) */
    INTERNAL,
    /** System Administrator. (IT system staff or staff of the system management company) */
    ADMINISTRATOR,
    /** System. (automatic processing on the system) */
    SYSTEM;

    fun isAnonymous(): Boolean = this == ANONYMOUS

    fun isSystem(): Boolean = this == SYSTEM

    fun notSystem(): Boolean = !isSystem()
}
