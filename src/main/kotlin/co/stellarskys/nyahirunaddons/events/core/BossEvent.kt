package co.stellarskys.nyahirunaddons.events.core

import co.stellarskys.stella.api.events.Event

sealed class BossEvent {
    class PhaseEvent(
        val phase: Int,
        val phaseEvent: String
        //phaseEvent List
        //Start <P1,P2,P3,P4,P5>
        //Enraged <P1,P2>
        //Thunder <P2>
        //Goldor <P3>
        //Drop <P4>
        //Ragnarock <P5>
        //End <P1,P2,P3,P4,P5>
    ) : Event()
}