package sample.context.actor

/** The actor session of the thread local scope. */
object ActorSession {
    private val actorLocal = ThreadLocal<Actor>()

    /** Relate a actor with a actor session. */
    fun bind(actor: Actor) {
        actorLocal.set(actor)
    }

    /** Unbind a actor session. */
    fun unbind() {
        actorLocal.remove()
    }

    /** Return an effective actor. When You are not related, an anonymous is returned. */
    fun actor(): Actor = actorLocal.get() ?: Actor.Anonymous
}
