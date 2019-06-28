package com.centyllion.client.controller.model

import bulma.BulmaElement
import bulma.NoContextController
import com.centyllion.model.ApplicableBehavior
import com.centyllion.model.Grain
import com.centyllion.model.Simulator
import kotlin.properties.Delegates

abstract class SimulatorViewController(simulator: Simulator) : NoContextController<Simulator, BulmaElement>() {

    override var data: Simulator by Delegates.observable(simulator) { _, _, _ ->
        refresh()
    }

    override var readOnly: Boolean by Delegates.observable(false) { _, old, new ->
        // nothing to do here
    }

    abstract fun oneStep(applied: List<ApplicableBehavior>)

    open var selectedGrain: Grain? = null

}
