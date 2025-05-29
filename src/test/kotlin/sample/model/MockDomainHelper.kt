package sample.model

import java.time.LocalDate
import sample.context.DomainHelper
import sample.context.MessageAccessor
import sample.context.MessageAccessor.MessageAccessorMock
import sample.context.Timestamper
import sample.context.uid.IdGenerator

class MockDomainHelper : DomainHelper {

    private val settingMap = mutableMapOf<String, String>()

    override fun time(): Timestamper {
        return Timestamper(LocalDate.of(2014, 11, 18))
    }

    override fun msg(): MessageAccessor {
        return MessageAccessorMock()
    }

    override fun uid(): IdGenerator {
        return IdGenerator()
    }

    fun setting(id: String, value: String): MockDomainHelper {
        settingMap[id] = value
        return this
    }
} 