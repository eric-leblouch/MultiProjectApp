package io.hfactory.multi.data

import com.ubeeko.hfactory.entities.HEntityRegistry

class MultiRegistry extends HEntityRegistry {
  val systemEntity        = registerEntity[System]
  val deviceEntity        = registerEntity[Device]
  val sensorEntity        = registerEntity[Sensor]
}
