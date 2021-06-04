/**
* Bridge Server API
* No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
*
* OpenAPI spec version: 0.21.18
* 
*
* NOTE: This class is auto generated by the swagger code generator program.
* https://github.com/swagger-api/swagger-codegen.git
* Do not edit the class manually.
*/
package org.sagebionetworks.bridge.kmm.shared.models

import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName

/**
 * They type of notification to provide relative to a session’s time window. This enum specifies when the notification should be shown to the participant.  |Status|Description| |---|---| |after_window_start|Issue the notification the `offset` after the window starts| |before_window_end|Issue the notification the `offset` before the window ends|
 * Values: "after_window_start","before_window_end"
 */
@Serializable
enum class NotificationType(val serialName: String? = null) {

    @SerialName("after_window_start")
    AFTER_WINDOW_START,

    @SerialName("before_window_end")
    BEFORE_WINDOW_END;

}

