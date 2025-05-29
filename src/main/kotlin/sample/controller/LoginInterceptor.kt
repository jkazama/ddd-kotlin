package sample.controller

import org.aspectj.lang.annotation.After
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.stereotype.Component
import sample.context.actor.Actor
import sample.context.actor.ActorRoleType
import sample.context.actor.ActorSession

/**
 * AOPInterceptor relates a login user with thread local. low: It is a dummy because no
 * authentication function is provided.
 */
@Aspect
@Component
class LoginInterceptor {

    @Before("execution(* sample.controller.*Controller.*(..))")
    fun bindUser() {
        ActorSession.bind(Actor(id = "sample", name = "sample", roleType = ActorRoleType.USER))
    }

    @Before("execution(* sample.controller.admin.*Controller.*(..))")
    fun bindAdmin() {
        ActorSession.bind(Actor(id = "admin", name = "admin", roleType = ActorRoleType.INTERNAL))
    }

    @Before("execution(* sample.controller.system.*Controller.*(..))")
    fun bindSystem() {
        ActorSession.bind(Actor.System)
    }

    @After("execution(* sample.controller..*Controller.*(..))")
    fun unbind() {
        ActorSession.unbind()
    }
}
