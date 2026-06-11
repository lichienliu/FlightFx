package com.michaelliu.flightfx.domain.model

data class Flight(
    /** 航班號 */
    val flightNo: String,
    /** 航司名 */
    val airlineName: String,
    /** 航司 logo URL */
    val airlineLogoUrl: String?,
    /** 預計時間 "HH:mm" */
    val scheduledTime: String,
    /** 實際時間 "HH:mm" */
    val actualTime: String?,
    /** 改點時間 "HH:mm" */
    val changedTime: String?,
    val isDeparture: Boolean,
    /** 對方機場代碼 */
    val airportCode: String,
    /** 對方機場名稱 */
    val airportName: String,
    /** 登機門 */
    val gate: String?,
    /** 報到島 */
    val checkInIsland: String?,
    /** 報到櫃台 */
    val checkInCounter: String?,
    /** 機型短碼 */
    val planeType: String?,
    val status: FlightStatus,
    /** 延誤原因 */
    val delayCause: String?,
) {
    val isCancelled: Boolean get() = status == FlightStatus.Cancelled
}
