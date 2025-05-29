package sample.context

import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Component
import sample.context.actor.Actor
import sample.context.actor.ActorSession
import sample.context.spring.ObjectProviderAccessor
import sample.context.uid.IdGenerator

/** The access to the domain infrastructure layer component which is necessary in handling it. */
interface DomainHelper {

    /** Return a login user. */
    fun actor(): Actor = ActorSession.actor()

    /** Returns the timestamp utility. */
    fun time(): Timestamper

    /** Returns the message accessor. */
    fun msg(): MessageAccessor

    /** Returns the ID generator. */
    fun uid(): IdGenerator

    @Component
    class DomainHelperImpl(
            private val time: ObjectProvider<Timestamper>,
            private val uid: ObjectProvider<IdGenerator>,
            private val msg: ObjectProvider<MessageAccessor>,
            private val accessor: ObjectProviderAccessor
    ) : DomainHelper {

        override fun time(): Timestamper = accessor.bean(time, Timestamper::class.java)

        override fun msg(): MessageAccessor = accessor.bean(msg, MessageAccessor::class.java)

        override fun uid(): IdGenerator = accessor.bean(uid, IdGenerator::class.java)
    }
}
