package sample.context.actor

import java.util.*

/** User in the use case. */
data class Actor(
        val id: String,
        val name: String,
        val roleType: ActorRoleType,
        val locale: Locale = Locale.getDefault(),
        /** Connection channel name of the actor */
        val channel: String? = null,
        /** External information to identify an actor. (including the IP) */
        val source: String? = null
) {
    companion object {
        /** Anonymous user */
        val Anonymous =
                Actor(
                        id = "unknown",
                        name = "unknown",
                        roleType = ActorRoleType.ANONYMOUS,
                        locale = Locale.getDefault()
                )

        /** System user */
        val System =
                Actor(
                        id = "system",
                        name = "system",
                        roleType = ActorRoleType.SYSTEM,
                        locale = Locale.getDefault()
                )
    }
}
