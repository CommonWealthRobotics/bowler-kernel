package com.commonwealthrobotics.bowlerkernel.scripthost

import com.commonwealthrobotics.bowlerkernel.util.CallbackLatch
import com.commonwealthrobotics.proto.script_host.SessionClientMessage
import com.commonwealthrobotics.proto.script_host.SessionServerMessage
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.selects.SelectBuilder
import java.util.concurrent.atomic.AtomicLong

class RequestMediator(val out: SendChannel<SessionServerMessage>) {
    var nextRequest = AtomicLong(0)
    val responseHandlers: MutableMap<Long, (SessionClientMessage) -> Unit> = mutableMapOf()

    inline fun <T, R> SelectBuilder<R>.handleRequest(
            latch: CallbackLatch<T, SessionClientMessage>,
            crossinline build: SessionServerMessage.Builder.(Long, T) -> R
    ) {
        latch.onReceive {
            val msg = SessionServerMessage.newBuilder()
            val id = nextRequest.getAndIncrement()
            val r = build(msg, id, it.input)
            out.send(msg.build())
            responseHandlers[id] = it.callback
            r
        }
    }

    fun handleResponse(id: Long, msg: SessionClientMessage) {
        responseHandlers.remove(id)?.invoke(msg)
    }
}

inline fun SendChannel<SessionServerMessage>.mediate(run: RequestMediator.()-> Unit) {
    run(RequestMediator(this))
}