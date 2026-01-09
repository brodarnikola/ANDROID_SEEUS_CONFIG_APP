package hr.sil.android.ble.scanner.model.event

import hr.sil.android.ble.scanner.model.device.BLEDevice

/**
 * @author mfatiga
 */
data class BLEDeviceEvent<out D>(
        /**
         * The device that caused this event.
         */
        val bleDevice: BLEDevice<D>,

        /**
         * Device event type.
         */
        val eventType: BLEDeviceEventType,

        /**
         * Timestamp at which the event occurred (in milliseconds)
         * - taken from the last packet timestamp if it was caused by an adv. packet
         * - current system timestamp if the event was not caused by a packet (HEARTBEAT or LOST event)
         */
        val timestamp: Long)