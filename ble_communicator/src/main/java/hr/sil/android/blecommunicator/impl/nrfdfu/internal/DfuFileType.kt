/* SWISS INNOVATION LAB CONFIDENTIAL
*
* www.swissinnolab.com
* __________________________________________________________________________
*
* [2016] - [2017] Swiss Innovation Lab AG
* All Rights Reserved.
*
* @author mfatiga
*
* NOTICE:  All information contained herein is, and remains
* the property of Swiss Innovation Lab AG and its suppliers,
* if any.  The intellectual and technical concepts contained
* herein are proprietary to Swiss Innovation Lab AG
* and its suppliers and may be covered by E.U. and Foreign Patents,
* patents in process, and are protected by trade secret or copyright law.
* Dissemination of this information or reproduction of this material
* is strictly forbidden unless prior written permission is obtained
* from Swiss Innovation Lab AG.
*/

package hr.sil.android.blecommunicator.impl.nrfdfu.internal

/**
 * @author mfatiga
 */
object DfuFileType {
    /**
     *
     *
     * The file contains a new version of Soft Device.
     *
     *
     *
     * Since DFU Library 7.0 all firmware may contain an Init packet. The Init packet is required if Extended Init Packet is used by the DFU bootloader (SDK 7.0+)..
     * The Init packet for the bootloader must be placed in the .dat file.
     *
     */
    const val TYPE_SOFT_DEVICE = 0x01

    /**
     *
     *
     * The file contains a new version of Bootloader.
     *
     *
     *
     * Since DFU Library 7.0 all firmware may contain an Init packet. The Init packet is required if Extended Init Packet is used by the DFU bootloader (SDK 7.0+).
     * The Init packet for the bootloader must be placed in the .dat file.
     *
     */
    const val TYPE_BOOTLOADER = 0x02

    /**
     *
     *
     * The file contains a new version of Application.
     *
     *
     *
     * Since DFU Library 0.5 all firmware may contain an Init packet. The Init packet is required if Extended Init Packet is used by the DFU bootloader (SDK 7.0+).
     * The Init packet for the application must be placed in the .dat file.
     *
     */
    const val TYPE_APPLICATION = 0x04

    /**
     *
     *
     * A ZIP file that consists of more than 1 file. Since SDK 8.0 the ZIP Distribution packet is a recommended way of delivering firmware files. Please, see the DFU documentation for
     * more details. A ZIP distribution packet may be created using the 'nrf utility' command line application, that is a part of Master Control Panel 3.8.0.
     * For backwards compatibility this library supports also ZIP files without the manifest file. Instead they must follow the fixed naming convention:
     * The names of files in the ZIP must be: **softdevice.hex** (or .bin), **bootloader.hex** (or .bin), **application.hex** (or .bin) in order
     * to be read correctly. Using the Soft Device v7.0.0+ the Soft Device and Bootloader may be updated and sent together. In case of additional application file included,
     * the service will try to send Soft Device, Bootloader and Application together (which is not supported currently) and if it fails, send first SD+BL, reconnect and send the application
     * in the following connection.
     *
     *
     *
     * Since the DFU Library 0.5 you may specify the Init packet, that will be send prior to the firmware. The init packet contains some verification data, like a device type and
     * revision, application version or a list of supported Soft Devices. The Init packet is required if Extended Init Packet is used by the DFU bootloader (SDK 7.0+).
     * In case of using the compatibility ZIP files the Init packet for the Soft Device and Bootloader must be in the 'system.dat' file while for the application
     * in the 'application.dat' file (included in the ZIP). The CRC in the 'system.dat' must be a CRC of both BIN contents if both a Soft Device and a Bootloader is present.
     *
     */
    const val TYPE_AUTO = 0x00
}