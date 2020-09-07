package com.commonwealthrobotics.bowlerkernel.scripthost

import com.commonwealthrobotics.bowlerkernel.util.CallbackLatch
import com.commonwealthrobotics.proto.script_host.SessionClientMessage
import com.commonwealthrobotics.proto.script_host.SessionServerMessage
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.selects.SelectBuilder

class RequestMediator(val out: FlowCollector<SessionServerMessage>) {
    var nextRequest: Long = 0
    val responseHandlers: MutableMap<Long, (SessionClientMessage) -> Unit> = mutableMapOf()

    inline fun <T, R> SelectBuilder<R>.handleRequest(
            latch: CallbackLatch<T, SessionClientMessage>,
            crossinline build: SessionServerMessage.Builder.(Long, T) -> R
    ) {
        latch.onReceive {
            val msg = SessionServerMessage.newBuilder()
            val id = nextRequest++
            val r = build(msg, id, it.input)
            out.emit(msg.build())
            responseHandlers[id] = it.callback
            r
        }
    }

    fun handleResponse(id: Long, msg: SessionClientMessage) {
        responseHandlers.remove(id)?.invoke(msg)
    }
}

inline fun FlowCollector<SessionServerMessage>.mediate(run: RequestMediator.()-> Unit) {
    run(RequestMediator(this))
}