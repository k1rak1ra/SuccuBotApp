package net.k1ra.succubotapp.features.robotStatus.model

import java.time.LocalDateTime
import java.time.LocalTime

data class RobotStatus(
    val did: String,
    val lastCleaned: LocalDateTime,
    val dailyCleaningTime: LocalTime,
    val dailyAutoCleanEnabled: Boolean,
    val runningCleaningCycle: Boolean, //1
    val unk2: Boolean, //2: ?? true when free, false when charging, false when running, true when charged in dock
    val gotoCharge: Boolean, //3
    val command: String, //4: chargego, smart
    val status: String, //5: standby, charging, relocating (suction off, moving to new location), goto_charge, sleep, smart (vacuuming)
    val continueCleaningAfterCharge: Boolean, //27
    val minutesCleanedLast: Int, //6
    val errCode: Int, //28 0 = none, 32 = mop missing, 1024 = stuck
    val squareMetersCleanedLast: Int, //7
    val battery: Int, //8 0-100%
    val suctionPower: SuctionPowers, //9 closed, gentle, normal, strong
    val waterFlow: WaterFlowLevels, //10 low, middle, high
    val unk112: String, //112
    val unk101: String, //101
    val volume: VolumeLevels, //120 base64 encoded hex, last 4 digits is volume from 0000 to 6400
    val unk15: String, //15
    val name: String
)