package org.sagebionetworks.bridge.kmm.shared.repo

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.sagebionetworks.bridge.kmm.shared.BaseTest
import org.sagebionetworks.bridge.kmm.shared.cache.ResourceDatabaseHelper
import org.sagebionetworks.bridge.kmm.shared.cache.ResourceResult
import org.sagebionetworks.bridge.kmm.shared.getTestClient
import org.sagebionetworks.bridge.kmm.shared.models.StudyActivityEvent
import org.sagebionetworks.bridge.kmm.shared.models.StudyActivityEventList
import org.sagebionetworks.bridge.kmm.shared.testDatabaseDriver
import kotlin.test.*

class ScheduleTimelineRepoTest: BaseTest() {

    private val testHour = 12//Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    private val lastHourString = (testHour - 1).toString().padStart(2,'0')
    private val currentHourString = testHour.toString().padStart(2,'0')
    private val nextHourString = (testHour + 1).toString().padStart(2,'0')

    private val scheduleJson = """{
  "duration": "P4D",
  "assessments": [
    {
      "key": "711cde8b711cde8b",
      "guid": "6GvgSebKPGM5tGT5t-gw7COj",
      "appId": "sage-assessment-test",
      "identifier": "dccs",
      "revision": 5,
      "label": "Shape-Color Sorting",
      "minutesToComplete": 3,
      "colorScheme": {
        "background": "#FFFFFF",
        "foreground": "#F5F5F5",
        "activated": "#CDCDCD",
        "inactivated": "#D8D8D8",
        "type": "ColorScheme"
      },
      "configUrl": "https://ws.sagebridge.org/v1/assessments/6GvgSebKPGM5tGT5t-gw7COj/config",
      "type": "AssessmentInfo"
    },
    {
      "key": "6e06751e6e06751e",
      "guid": "eGhiQTT2a6SCCmjTod6CDb0t",
      "appId": "sage-assessment-test",
      "identifier": "Assessment_Test_1",
      "revision": 1,
      "label": "Assessment Test 1",
      "minutesToComplete": 0,
      "configUrl": "https://ws.sagebridge.org/v1/assessments/eGhiQTT2a6SCCmjTod6CDb0t/config",
      "type": "AssessmentInfo"
    }
  ],
  "sessions": [
    {
      "guid": "iErUfmT0AzIe7DIiwBzFtxZW",
      "label": "One time 3 day session",
      "startEventId": "timeline_retrieved",
      "performanceOrder": "sequential",
      "timeWindowGuids": [
        "pFWgzjk-7wOa3GSDbvpy-JDc"
      ],
      "notifications": [
        {
          "notifyAt": "after_window_start",
          "allowSnooze": true,
          "offset": "PT8H",
          "interval": "P1D",
          "message": {
            "lang": "en",
            "subject": "Reminder One time 3 day session",
            "message": "Reminder Text",
            "type": "NotificationMessage"
          },
          "type": "NotificationInfo"
        }
      ],
      "type": "SessionInfo"
    },
    {
      "guid": "p-oBPW7EDXSEt_ZrXcFd0fAD",
      "label": "Daily Sessions",
      "startEventId": "timeline_retrieved",
      "performanceOrder": "sequential",
      "minutesToComplete": 3,
      "timeWindowGuids": [
        "VbyQ7IElHmGZ-jcelaEy2NCE",
        "h2wxlJbf7DRYsBgdcQ3exCh7",
        "41j9nQyyeNVcHAJJB0d1vl2j"
      ],
      "notifications": [
        {
          "notifyAt": "after_window_start",
          "allowSnooze": true,
          "message": {
            "lang": "en",
            "subject": "Reminder Title",
            "message": "Reminder Text",
            "type": "NotificationMessage"
          },
          "type": "NotificationInfo"
        }
      ],
      "type": "SessionInfo"
    }
  ],
  "schedule": [
    {
      "instanceGuid": "mvhyXpjhxHDp7R9fyH02SA",
      "startDay": 0,
      "endDay": 0,
      "startTime": "$lastHourString:00",
      "expiration": "PT30M",
      "persistent": true,
      "assessments": [
        {
          "refKey": "711cde8b711cde8b",
          "instanceGuid": "FowEZJgOmdxO-UKA5-ZqLw",
          "type": "ScheduledAssessment"
        }
      ],
      "timeWindowGuid": "VbyQ7IElHmGZ-jcelaEy2NCE",
      "refGuid": "p-oBPW7EDXSEt_ZrXcFd0fAD",
      "type": "ScheduledSession"
    },
    {
      "instanceGuid": "QaqwdxDjRc5FzUIAhBHRwA",
      "startDay": 0,
      "endDay": 0,
      "startTime": "$currentHourString:00",
      "expiration": "PT1H",
      "assessments": [
        {
          "refKey": "711cde8b711cde8b",
          "instanceGuid": "84cIJmOLnSN4vtAkGfPmQw",
          "type": "ScheduledAssessment"
        }
      ],
      "timeWindowGuid": "h2wxlJbf7DRYsBgdcQ3exCh7",
      "refGuid": "p-oBPW7EDXSEt_ZrXcFd0fAD",
      "type": "ScheduledSession"
    },
    {
      "instanceGuid": "SL97UWo1ZQEA1tQKKVFkUA",
      "startDay": 0,
      "endDay": 0,
      "startTime": "$nextHourString:00",
      "expiration": "PT1H",
      "assessments": [
        {
          "refKey": "711cde8b711cde8b",
          "instanceGuid": "e21215jfezzHCKvIp2TyyA",
          "type": "ScheduledAssessment"
        }
      ],
      "timeWindowGuid": "41j9nQyyeNVcHAJJB0d1vl2j",
      "refGuid": "p-oBPW7EDXSEt_ZrXcFd0fAD",
      "type": "ScheduledSession"
    },
    {
      "instanceGuid": "GMJvrk1HuOmw7Uote5J4sw",
      "startDay": 0,
      "endDay": 2,
      "startTime": "00:00",
      "expiration": "P3D",
      "assessments": [
        {
          "refKey": "6e06751e6e06751e",
          "instanceGuid": "fR4uTueyQSMDEumdMAZdnA",
          "type": "ScheduledAssessment"
        }
      ],
      "timeWindowGuid": "pFWgzjk-7wOa3GSDbvpy-JDc",
      "refGuid": "iErUfmT0AzIe7DIiwBzFtxZW",
      "type": "ScheduledSession"
    },
    {
      "instanceGuid": "lVJ-cmbE3cxDWvXcc61tWw",
      "startDay": 1,
      "endDay": 1,
      "startTime": "$lastHourString:00",
      "expiration": "PT30M",
      "persistent": true,
      "assessments": [
        {
          "refKey": "711cde8b711cde8b",
          "instanceGuid": "mv8cxYLK8vrgWQ2F7Um71Q",
          "type": "ScheduledAssessment"
        }
      ],
      "timeWindowGuid": "VbyQ7IElHmGZ-jcelaEy2NCE",
      "refGuid": "p-oBPW7EDXSEt_ZrXcFd0fAD",
      "type": "ScheduledSession"
    },
    {
      "instanceGuid": "XlZ3SrLpmEQ2E8PuUUcs7g",
      "startDay": 1,
      "endDay": 1,
      "startTime": "$currentHourString:00",
      "expiration": "PT1H",
      "assessments": [
        {
          "refKey": "711cde8b711cde8b",
          "instanceGuid": "GgQVjdjyI0uksdnuKpd6SA",
          "type": "ScheduledAssessment"
        }
      ],
      "timeWindowGuid": "h2wxlJbf7DRYsBgdcQ3exCh7",
      "refGuid": "p-oBPW7EDXSEt_ZrXcFd0fAD",
      "type": "ScheduledSession"
    },
    {
      "instanceGuid": "-B_yTKp8eTGK7NY_qJ0UTA",
      "startDay": 1,
      "endDay": 1,
      "startTime": "$nextHourString:00",
      "expiration": "PT1H",
      "assessments": [
        {
          "refKey": "711cde8b711cde8b",
          "instanceGuid": "evLwKAYAD_f0Ecln4QmY_Q",
          "type": "ScheduledAssessment"
        }
      ],
      "timeWindowGuid": "41j9nQyyeNVcHAJJB0d1vl2j",
      "refGuid": "p-oBPW7EDXSEt_ZrXcFd0fAD",
      "type": "ScheduledSession"
    },
    {
      "instanceGuid": "6YyBNuEPir0Vkd3a-crQbA",
      "startDay": 2,
      "endDay": 2,
      "startTime": "$lastHourString:00",
      "expiration": "PT30M",
      "persistent": true,
      "assessments": [
        {
          "refKey": "711cde8b711cde8b",
          "instanceGuid": "d5zbbpFUcGeganSy_TJIxg",
          "type": "ScheduledAssessment"
        }
      ],
      "timeWindowGuid": "VbyQ7IElHmGZ-jcelaEy2NCE",
      "refGuid": "p-oBPW7EDXSEt_ZrXcFd0fAD",
      "type": "ScheduledSession"
    },
    {
      "instanceGuid": "CDWlxjraDB_yn_jUl0YagA",
      "startDay": 2,
      "endDay": 2,
      "startTime": "$currentHourString:00",
      "expiration": "PT1H",
      "assessments": [
        {
          "refKey": "711cde8b711cde8b",
          "instanceGuid": "8W35QTL5ph2DAnd1lvnejA",
          "type": "ScheduledAssessment"
        }
      ],
      "timeWindowGuid": "h2wxlJbf7DRYsBgdcQ3exCh7",
      "refGuid": "p-oBPW7EDXSEt_ZrXcFd0fAD",
      "type": "ScheduledSession"
    },
    {
      "instanceGuid": "wSFtq35JbCog5s4TXXMNRw",
      "startDay": 2,
      "endDay": 2,
      "startTime": "$nextHourString:00",
      "expiration": "PT1H",
      "assessments": [
        {
          "refKey": "711cde8b711cde8b",
          "instanceGuid": "GYMXqSOe1JFKifK2XWx2Gg",
          "type": "ScheduledAssessment"
        }
      ],
      "timeWindowGuid": "41j9nQyyeNVcHAJJB0d1vl2j",
      "refGuid": "p-oBPW7EDXSEt_ZrXcFd0fAD",
      "type": "ScheduledSession"
    },
    {
      "instanceGuid": "T8X0XUUwUwcr3k6zEh7cBw",
      "startDay": 3,
      "endDay": 3,
      "startTime": "$lastHourString:00",
      "expiration": "PT30M",
      "persistent": true,
      "assessments": [
        {
          "refKey": "711cde8b711cde8b",
          "instanceGuid": "tFtJNUMcUssPezzwKmrB2w",
          "type": "ScheduledAssessment"
        }
      ],
      "timeWindowGuid": "VbyQ7IElHmGZ-jcelaEy2NCE",
      "refGuid": "p-oBPW7EDXSEt_ZrXcFd0fAD",
      "type": "ScheduledSession"
    },
    {
      "instanceGuid": "ud-UBuDNBFHDtDrx9YodFA",
      "startDay": 3,
      "endDay": 3,
      "startTime": "$currentHourString:00",
      "expiration": "PT1H",
      "assessments": [
        {
          "refKey": "711cde8b711cde8b",
          "instanceGuid": "oJp7v2cGXZHVZlLhy1lLuw",
          "type": "ScheduledAssessment"
        }
      ],
      "timeWindowGuid": "h2wxlJbf7DRYsBgdcQ3exCh7",
      "refGuid": "p-oBPW7EDXSEt_ZrXcFd0fAD",
      "type": "ScheduledSession"
    },
    {
      "instanceGuid": "CukddPZ9eXREe7lxuL0cXQ",
      "startDay": 3,
      "endDay": 3,
      "startTime": "$nextHourString:00",
      "expiration": "PT1H",
      "assessments": [
        {
          "refKey": "711cde8b711cde8b",
          "instanceGuid": "Q2QqJtScMENGF23bmizvjQ",
          "type": "ScheduledAssessment"
        }
      ],
      "timeWindowGuid": "41j9nQyyeNVcHAJJB0d1vl2j",
      "refGuid": "p-oBPW7EDXSEt_ZrXcFd0fAD",
      "type": "ScheduledSession"
    }
  ],
  "type": "Timeline"
}"""
    
    private val assessmentInstanceGuid = "oJp7v2cGXZHVZlLhy1lLuw"

    private val adherenceRecordjson = "{\n" +
            "   \"items\":[\n" +
            "      {\n" +
            "         \"instanceGuid\":\""+ assessmentInstanceGuid + "\",\n" +
            "         \"startedOn\":\"2021-05-12T23:44:54.319Z\",\n" +
            "         \"finishedOn\":\"2021-05-12T23:44:54.319Z\",\n" +
            "         \"eventTimestamp\":\"2021-05-12T23:44:51.872Z\",\n" +
            "         \"type\":\"AdherenceRecord\"\n" +
            "      }\n" +
            "   ],\n" +
            "   \"total\":1,\n" +
            "   \"type\":\"PagedResourceList\"\n" +
            "}"

    private val adherenceRecordDay2json = "{\n" +
            "   \"items\":[\n" +
            "      {\n" +
            "         \"instanceGuid\":\"fR4uTueyQSMDEumdMAZdnA\",\n" +
            "         \"startedOn\":\"2021-05-12T23:44:54.319Z\",\n" +
            "         \"finishedOn\":\"2021-05-12T23:44:54.319Z\",\n" +
            "         \"eventTimestamp\":\"2021-05-12T23:44:51.872Z\",\n" +
            "         \"type\":\"AdherenceRecord\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"instanceGuid\":\"8W35QTL5ph2DAnd1lvnejA\",\n" +
            "         \"startedOn\":\"2021-05-12T23:44:54.319Z\",\n" +
            "         \"finishedOn\":\"2021-05-12T23:44:54.319Z\",\n" +
            "         \"eventTimestamp\":\"2021-05-12T23:44:51.872Z\",\n" +
            "         \"type\":\"AdherenceRecord\"\n" +
            "      }\n" +
            "   ],\n" +
            "   \"total\":2,\n" +
            "   \"type\":\"PagedResourceList\"\n" +
            "}"


    private fun getActivityEventList(timeStamp: Instant): StudyActivityEventList {
        return StudyActivityEventList(items = listOf(StudyActivityEvent(eventId = "timeline_retrieved", timestamp = timeStamp)))
    }

    private fun getTodayInstant(hour: Int = testHour): Instant {
        val nowDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val todayNoonDateTime = LocalDateTime(year = nowDateTime.year, month = nowDateTime.month, dayOfMonth = nowDateTime.dayOfMonth, hour = hour, minute = 0, second = 0)
        return todayNoonDateTime.toInstant(TimeZone.currentSystemDefault())
    }

    private fun getLocalDateTime(now: Instant, hour: Int, days: Int = 0): LocalDateTime {
        val timeZone = TimeZone.currentSystemDefault()
        val nowDateTime = now.plus(days, DateTimeUnit.DAY, timeZone).toLocalDateTime(timeZone)
        return LocalDateTime(year = nowDateTime.year, month = nowDateTime.month, dayOfMonth = nowDateTime.dayOfMonth, hour = hour, minute = 0, second = 0)
    }

    private fun getTestScheduleTimelineRepo(adherenceRecordJson: String = "", timeStamp: Instant, timelineJson: String = scheduleJson ) : ScheduleTimelineRepo {
        val databaseHelper = ResourceDatabaseHelper(testDatabaseDriver())
        val adherenceRecordRepo = AdherenceRecordRepo(getTestClient(adherenceRecordJson), databaseHelper, MainScope())
        val eventJson = Json.encodeToString(getActivityEventList(timeStamp))
        val activityEventsRepo = ActivityEventsRepo(getTestClient(eventJson), databaseHelper, MainScope())
        return ScheduleTimelineRepo(adherenceRecordRepo, activityEventsRepo, getTestClient(timelineJson), databaseHelper, MainScope())
    }

    @Test
    fun testScheduledSessionsDay1() {
        runTest {
            val eventTimeStamp = Clock.System.now().minus(DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            val repo = getTestScheduleTimelineRepo(timeStamp = eventTimeStamp)

            val resourceResult = repo.getSessionsForDay("sage-assessment-test", getTodayInstant()).firstOrNull { it is ResourceResult.Success }

            assertTrue(resourceResult is ResourceResult.Success)
            val sessionList = resourceResult.data.scheduledSessionWindows
            assertNotNull(sessionList)
            assertEquals(3, sessionList.size)

            //First session should be the 3 day session that started yesterday
            val session1 = sessionList[0]
            assertEquals("One time 3 day session", session1.sessionInfo.label)
            val notification1 = session1.notifications?.firstOrNull()
            assertNotNull(notification1)
            //assertEquals(notification1.scheduleOn)

            //Second session should have started in current hour
            val session2 = sessionList[1]
            assertEquals("Daily Sessions", session2.sessionInfo.label)
            assertEquals("XlZ3SrLpmEQ2E8PuUUcs7g", session2.instanceGuid)
            assertFalse(session2.isInPast(getTodayInstant()))
            assertTrue(session2.isAvailableNow(getTodayInstant()))
            assertFalse(session2.isInFuture(getTodayInstant()))

            //Third session will be starting next hour
            val session3 = sessionList[2]
            assertEquals("Daily Sessions", session3.sessionInfo.label)
            assertEquals("-B_yTKp8eTGK7NY_qJ0UTA", session3.instanceGuid)
            assertFalse(session3.isInPast(getTodayInstant()))
            assertFalse(session3.isAvailableNow(getTodayInstant()))
            assertTrue(session3.isInFuture(getTodayInstant()))
        }
    }

    @Test
    fun testScheduledSessionsDay1TodayAndNotifications() {
        runTest {
            val eventTimeStamp = Clock.System.now().minus(DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            val repo = getTestScheduleTimelineRepo(timeStamp = eventTimeStamp)

            val resourceResult = repo.getSessionsForDay(
                "sage-assessment-test",
                getTodayInstant(),
                true
            ).firstOrNull { it is ResourceResult.Success }

            assertTrue(resourceResult is ResourceResult.Success)
            val sessionList = resourceResult.data.scheduledSessionWindows
            assertNotNull(sessionList)
            assertEquals(3, sessionList.size)

            //First session should be the 3 day session that started yesterday
            val session1 = sessionList[0]
            assertEquals("One time 3 day session", session1.sessionInfo.label)

            //Second session should have started in current hour
            val session2 = sessionList[1]
            assertEquals("Daily Sessions", session2.sessionInfo.label)
            assertEquals("XlZ3SrLpmEQ2E8PuUUcs7g", session2.instanceGuid)

            //Third session will be starting next hour
            val session3 = sessionList[2]
            assertEquals("Daily Sessions", session3.sessionInfo.label)
            assertEquals("-B_yTKp8eTGK7NY_qJ0UTA", session3.instanceGuid)

            val notifications = resourceResult.data.notifications
            assertNotNull(notifications)
            assertEquals(9, notifications.size)
        }
    }

    @Test
    fun testScheduledSessionsDay1TodayAndTomorrow() {
        runTest {
            val eventTimeStamp = Clock.System.now().minus(DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            val repo = getTestScheduleTimelineRepo(timeStamp = eventTimeStamp)

            val resourceResult = repo.getSessionsForDay(
                "sage-assessment-test",
                getTodayInstant(),
                includeAllNotifications = true,
                alwaysIncludeNextDay = true
            ).firstOrNull { it is ResourceResult.Success }

            assertTrue(resourceResult is ResourceResult.Success)
            val sessionList = resourceResult.data.scheduledSessionWindows
            assertNotNull(sessionList)
            assertEquals(6, sessionList.size)

            //First session should be the 3 day session that started yesterday
            val session1 = sessionList[0]
            assertEquals("One time 3 day session", session1.sessionInfo.label)

            //Second session should have started in current hour
            val session2 = sessionList[1]
            assertEquals("Daily Sessions", session2.sessionInfo.label)
            assertEquals("XlZ3SrLpmEQ2E8PuUUcs7g", session2.instanceGuid)

            //Third session will be starting next hour
            val session3 = sessionList[2]
            assertEquals("Daily Sessions", session3.sessionInfo.label)
            assertEquals("-B_yTKp8eTGK7NY_qJ0UTA", session3.instanceGuid)

            // Next day is shown b/c it was explicitly asked to be included

            val session4 = sessionList[3]
            assertEquals("Daily Sessions", session4.sessionInfo.label)
            assertEquals("6YyBNuEPir0Vkd3a-crQbA", session4.instanceGuid)

            val session5 = sessionList[4]
            assertEquals("Daily Sessions", session5.sessionInfo.label)
            assertEquals("CDWlxjraDB_yn_jUl0YagA", session5.instanceGuid)

            val session6 = sessionList[5]
            assertEquals("Daily Sessions", session6.sessionInfo.label)
            assertEquals("wSFtq35JbCog5s4TXXMNRw", session6.instanceGuid)

            val notifications = resourceResult.data.notifications
            assertNotNull(notifications)
            assertEquals(9, notifications.size)
        }
    }

    @Test
    fun testScheduledSessionsDay1TodayAndTomorrow_NoNotifications() {
        runTest {
            val eventTimeStamp = Clock.System.now().minus(DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            val repo = getTestScheduleTimelineRepo(timeStamp = eventTimeStamp)

            val resourceResult = repo.getSessionsForDay(
                "sage-assessment-test",
                getTodayInstant(),
                includeAllNotifications = false,
                alwaysIncludeNextDay = true
            ).firstOrNull { it is ResourceResult.Success }

            assertTrue(resourceResult is ResourceResult.Success)
            val sessionList = resourceResult.data.scheduledSessionWindows
            assertNotNull(sessionList)
            assertEquals(6, sessionList.size)

            //First session should be the 3 day session that started yesterday
            val session1 = sessionList[0]
            assertEquals("One time 3 day session", session1.sessionInfo.label)

            //Second session should have started in current hour
            val session2 = sessionList[1]
            assertEquals("Daily Sessions", session2.sessionInfo.label)
            assertEquals("XlZ3SrLpmEQ2E8PuUUcs7g", session2.instanceGuid)

            //Third session will be starting next hour
            val session3 = sessionList[2]
            assertEquals("Daily Sessions", session3.sessionInfo.label)
            assertEquals("-B_yTKp8eTGK7NY_qJ0UTA", session3.instanceGuid)

            // Next day is shown b/c it was explicitly asked to be included

            val session4 = sessionList[3]
            assertEquals("Daily Sessions", session4.sessionInfo.label)
            assertEquals("6YyBNuEPir0Vkd3a-crQbA", session4.instanceGuid)

            val session5 = sessionList[4]
            assertEquals("Daily Sessions", session5.sessionInfo.label)
            assertEquals("CDWlxjraDB_yn_jUl0YagA", session5.instanceGuid)

            val session6 = sessionList[5]
            assertEquals("Daily Sessions", session6.sessionInfo.label)
            assertEquals("wSFtq35JbCog5s4TXXMNRw", session6.instanceGuid)
        }
    }

    @Test
    fun testScheduledSessionsDay1NoUpNextOnToday() {
        runTest {
            val eventTimeStamp = Clock.System.now().minus(DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            val repo = getTestScheduleTimelineRepo(timeStamp = eventTimeStamp)

            val todayInstant = getTodayInstant().plus(90, DateTimeUnit.MINUTE, TimeZone.currentSystemDefault())
            val resourceResult = repo.getSessionsForDay("sage-assessment-test", todayInstant).firstOrNull { it is ResourceResult.Success }

            assertTrue(resourceResult is ResourceResult.Success)
            val sessionList = resourceResult.data.scheduledSessionWindows
            assertNotNull(sessionList)
            assertEquals(5, sessionList.size)

            //First session should be the 3 day session that started yesterday
            val session1 = sessionList[0]
            assertEquals("One time 3 day session", session1.sessionInfo.label)
            assertFalse(session1.isCompleted)

            //Second session should have started today
            val session2 = sessionList[1]
            assertEquals("Daily Sessions", session2.sessionInfo.label)
            assertEquals("-B_yTKp8eTGK7NY_qJ0UTA", session2.instanceGuid)
            assertFalse(session2.isCompleted)

            // Next day is shown b/c the current day is finished or available

            val session3 = sessionList[2]
            assertEquals("Daily Sessions", session3.sessionInfo.label)
            assertEquals("6YyBNuEPir0Vkd3a-crQbA", session3.instanceGuid)

            val session4 = sessionList[3]
            assertEquals("Daily Sessions", session4.sessionInfo.label)
            assertEquals("CDWlxjraDB_yn_jUl0YagA", session4.instanceGuid)

            val session5 = sessionList[4]
            assertEquals("Daily Sessions", session5.sessionInfo.label)
            assertEquals("wSFtq35JbCog5s4TXXMNRw", session5.instanceGuid)
        }
    }

    @Test
    fun testScheduledSessionsDay2WithCompletion() {
        runTest {
            val studyId = "sage-assessment-test"
            val eventTimeStamp = Clock.System.now().minus(2, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            val repo = getTestScheduleTimelineRepo(adherenceRecordDay2json, timeStamp = eventTimeStamp)
            repo.adherenceRecordRepo.loadRemoteAdherenceRecords(studyId)

            val todayInstant = getTodayInstant().plus(90, DateTimeUnit.MINUTE, TimeZone.currentSystemDefault())
            val resourceResult = repo.getSessionsForDay(studyId, todayInstant).firstOrNull { it is ResourceResult.Success }
            assertTrue(resourceResult is ResourceResult.Success)
            val sessionList = resourceResult.data.scheduledSessionWindows
            assertNotNull(sessionList)
            assertEquals(6, sessionList.size)

            //First session should be the 3 day session that started yesterday
            val session1 = sessionList[0]
            assertEquals("One time 3 day session", session1.sessionInfo.label)
            assertTrue(session1.isCompleted)

            //Second session should have started today
            val session2 = sessionList[1]
            assertEquals("Daily Sessions", session2.sessionInfo.label)
            assertEquals("CDWlxjraDB_yn_jUl0YagA", session2.instanceGuid)
            assertTrue(session2.isCompleted)

            //Third session should have started today
            val session3 = sessionList[2]
            assertEquals("Daily Sessions", session3.sessionInfo.label)
            assertEquals("wSFtq35JbCog5s4TXXMNRw", session3.instanceGuid)
            assertFalse(session3.isCompleted)

            // Next day is shown b/c the current day is finished or available

            //Fourth session will start tomorrow
            val session4 = sessionList[3]
            assertEquals("Daily Sessions", session4.sessionInfo.label)
            assertEquals("T8X0XUUwUwcr3k6zEh7cBw", session4.instanceGuid)

            //Fifth session will start tomorrow
            val session5 = sessionList[4]
            assertEquals("Daily Sessions", session5.sessionInfo.label)
            assertEquals("ud-UBuDNBFHDtDrx9YodFA", session5.instanceGuid)

            //Sixth session will start tomorrow
            val session6 = sessionList[5]
            assertEquals("Daily Sessions", session6.sessionInfo.label)
            assertEquals("CukddPZ9eXREe7lxuL0cXQ", session6.instanceGuid)
        }
    }

    @Test
    fun testScheduledSessionsDay3() {
        runTest {
            val eventTimeStamp = Clock.System.now().minus(3, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            val repo = getTestScheduleTimelineRepo(timeStamp = eventTimeStamp)
            val resourceResult = repo.getSessionsForDay("sage-assessment-test", getTodayInstant()).firstOrNull { it is ResourceResult.Success }

            assertTrue(resourceResult is ResourceResult.Success)
            val sessionList = resourceResult.data.scheduledSessionWindows
            assertNotNull(sessionList)
            assertEquals(2, sessionList.size)

            //First session should have started in current hour
            val session2 = sessionList[0]
            assertEquals("Daily Sessions", session2.sessionInfo.label)
            assertEquals("ud-UBuDNBFHDtDrx9YodFA", session2.instanceGuid)
            assertFalse(session2.isCompleted)

            //Second session will be starting next hour
            val session3 = sessionList[1]
            assertEquals("Daily Sessions", session3.sessionInfo.label)
            assertEquals("CukddPZ9eXREe7lxuL0cXQ", session3.instanceGuid)

        }
    }

    @Test
    fun testScheduledSessionsDay3WithCompletion() {
        runTest {
            val studyId = "sage-assessment-test"
            val eventTimeStamp = Clock.System.now().minus(3, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            val repo = getTestScheduleTimelineRepo(adherenceRecordjson, timeStamp = eventTimeStamp)
            repo.adherenceRecordRepo.loadRemoteAdherenceRecords(studyId)
            val resourceResult = repo.getSessionsForDay(studyId, getTodayInstant()).firstOrNull { it is ResourceResult.Success }
            assertTrue(resourceResult is ResourceResult.Success)
            val sessionList = resourceResult.data.scheduledSessionWindows
            assertNotNull(sessionList)
            assertEquals(2, sessionList.size)

            //First session should have started in current hour and be completed
            val session2 = sessionList[0]
            assertEquals("Daily Sessions", session2.sessionInfo.label)
            assertEquals("ud-UBuDNBFHDtDrx9YodFA", session2.instanceGuid)
            assertTrue(session2.isCompleted)

            //Second session will be starting next hour
            val session3 = sessionList[1]
            assertEquals("Daily Sessions", session3.sessionInfo.label)
            assertEquals("CukddPZ9eXREe7lxuL0cXQ", session3.instanceGuid)

        }
    }

    @Test
    fun testScheduledSessionsDay4() {
        runTest {
            val eventTimeStamp = Clock.System.now().minus(4, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            val repo = getTestScheduleTimelineRepo(timeStamp = eventTimeStamp)
            val resourceResult = repo.getSessionsForDay("sage-assessment-test", getTodayInstant()).firstOrNull { it is ResourceResult.Success }

            assertTrue(resourceResult is ResourceResult.Success)
            val sessionList = resourceResult.data.scheduledSessionWindows
            assertNotNull(sessionList)
            assertTrue(sessionList.isEmpty())
        }
    }

    @Test
    fun testPastSessionsDay0() {
        runTest {
            val eventTimeStamp = Clock.System.now()
            //         Clock.System.now().minus(DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            val repo = getTestScheduleTimelineRepo(timeStamp = eventTimeStamp)
            val resourceResult = repo.getPastSessions("sage-assessment-test", getTodayInstant())
                .firstOrNull { it is ResourceResult.Success }

            assertTrue(resourceResult is ResourceResult.Success)
            val sessionList = resourceResult.data.scheduledSessionWindows
            assertNotNull(sessionList)
            assertEquals(3, sessionList.size)
        }
    }

    @Test
    fun testPastSessionsDay1() {
        runTest {
            val eventTimeStamp =
                Clock.System.now().minus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            val repo = getTestScheduleTimelineRepo(timeStamp = eventTimeStamp)
            val resourceResult = repo.getPastSessions("sage-assessment-test", getTodayInstant())
                .firstOrNull { it is ResourceResult.Success }

            assertTrue(resourceResult is ResourceResult.Success)
            val sessionList = resourceResult.data.scheduledSessionWindows
            assertNotNull(sessionList)
            assertEquals(6, sessionList.size)
        }
    }

    @Test
    fun testPastSessionsDay2WithCompletion() {
        runTest {
            val studyId = "sage-assessment-test"
            val eventTimeStamp =
                Clock.System.now().minus(2, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            val repo =
                getTestScheduleTimelineRepo(adherenceRecordDay2json, timeStamp = eventTimeStamp)
            repo.adherenceRecordRepo.loadRemoteAdherenceRecords(studyId)

            val todayInstant =
                getTodayInstant().plus(90, DateTimeUnit.MINUTE, TimeZone.currentSystemDefault())
            val resourceResult = repo.getPastSessions(studyId, todayInstant)
                .firstOrNull { it is ResourceResult.Success }
            assertTrue(resourceResult is ResourceResult.Success)
            val sessionList = resourceResult.data.scheduledSessionWindows
            assertNotNull(sessionList)
            assertEquals(10, sessionList.size)
            val historyRecordsList = sessionList.flatMap {
                it.assessments.flatMap { assessment -> assessment.history() }
            }.sortedBy { it.finishedOn }

            assertEquals(2, historyRecordsList.size)

            val record1 = historyRecordsList[0]
            assertEquals("Assessment Test 1", record1.assessmentInfo.label)

            val record2 = historyRecordsList[1]
            assertEquals("Shape-Color Sorting", record2.assessmentInfo.label)
        }
    }

    @Test
    fun testPastSessionsDay4() {
        runTest {
            val eventTimeStamp =
                Clock.System.now().minus(4, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            val repo = getTestScheduleTimelineRepo(timeStamp = eventTimeStamp)
            val resourceResult = repo.getPastSessions("sage-assessment-test", getTodayInstant())
                .firstOrNull { it is ResourceResult.Success }

            assertTrue(resourceResult is ResourceResult.Success)
            val sessionList = resourceResult.data.scheduledSessionWindows
            assertNotNull(sessionList)
            assertEquals(13, sessionList.size)
        }
    }

    val anotherTimelineJson = """
        {
          "duration": "P14D",
          "assessments": [
            {
              "key": "dc1233b8dc1233b8",
              "guid": "AJp1u9KvN0Fwbx6uf-xRyPIP",
              "appId": "shared",
              "identifier": "psm",
              "revision": 2,
              "label": "Arranging Pictures",
              "minutesToComplete": 5,
              "colorScheme": {
                "background": "#FFFFFF",
                "foreground": "#CCE5D5",
                "activated": "#CCE5D5",
                "inactivated": "#D8E4DC",
                "type": "ColorScheme"
              },
              "configUrl": "https://ws.sagebridge.org/v1/sharedassessments/AJp1u9KvN0Fwbx6uf-xRyPIP/config",
              "type": "AssessmentInfo"
            },
            {
              "key": "e4039130e4039130",
              "guid": "d6EbPSCFcpCJzO4mTiCQSrHP",
              "appId": "shared",
              "identifier": "flanker",
              "revision": 2,
              "label": "Arrow Matching",
              "minutesToComplete": 3,
              "colorScheme": {
                "background": "#FFFFFF",
                "foreground": "#F4B795",
                "activated": "#F4B795",
                "inactivated": "#FCEDE4",
                "type": "ColorScheme"
              },
              "configUrl": "https://ws.sagebridge.org/v1/sharedassessments/d6EbPSCFcpCJzO4mTiCQSrHP/config",
              "type": "AssessmentInfo"
            },
            {
              "key": "4528d6c04528d6c0",
              "guid": "R8f8_g9WpBPJuAhcc0Umyf6s",
              "appId": "shared",
              "identifier": "dccs",
              "revision": 2,
              "label": "Shape-Color Sorting",
              "minutesToComplete": 3,
              "colorScheme": {
                "background": "#FFFFFF",
                "foreground": "#F4B795",
                "activated": "#F4B795",
                "inactivated": "#FCEDE4",
                "type": "ColorScheme"
              },
              "configUrl": "https://ws.sagebridge.org/v1/sharedassessments/R8f8_g9WpBPJuAhcc0Umyf6s/config",
              "type": "AssessmentInfo"
            }
          ],
          "sessions": [
            {
              "guid": "LcbizpKRfjGryeOgL-dq6phM",
              "label": "First Day",
              "startEventId": "timeline_retrieved",
              "performanceOrder": "sequential",
              "minutesToComplete": 6,
              "timeWindowGuids": [
                "iQkggiyz8gmMUxvDLGqyZYxn"
              ],
              "notifications": [],
              "type": "SessionInfo"
            },
            {
              "guid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "label": "Follow Up -  4x per day",
              "startEventId": "timeline_retrieved",
              "performanceOrder": "sequential",
              "minutesToComplete": 6,
              "timeWindowGuids": [
                "xfPTY8eQe4hbdNy7eUaRuqtd",
                "JirIBgyvRJaW0tTUeptwQ_0r",
                "OHqSYvPx9rOQLDLHejLBo_1E",
                "z42qs-Vd6FuTTAx2ctFxSaum"
              ],
              "notifications": [
                {
                  "notifyAt": "after_window_start",
                  "allowSnooze": false,
                  "message": {
                    "lang": "en",
                    "subject": " Card Sort and Arrow Match",
                    "message": "Do this lots of times every day!",
                    "type": "NotificationMessage"
                  },
                  "type": "NotificationInfo"
                }
              ],
              "type": "SessionInfo"
            },
            {
              "guid": "0nj3jtK6AjUKdC81RgOipi0v",
              "label": "Available Once with No Expiration",
              "startEventId": "timeline_retrieved",
              "performanceOrder": "sequential",
              "minutesToComplete": 5,
              "timeWindowGuids": [
                "sAd_34IW9L8wJyljqcM5cYsa"
              ],
              "notifications": [
                {
                  "notifyAt": "after_window_start",
                  "offset": "PT2H",
                  "interval": "P1D",
                  "allowSnooze": false,
                  "message": {
                    "lang": "en",
                    "subject": " Arranging Pictures",
                    "message": "Gonna remind you every day until you do it!",
                    "type": "NotificationMessage"
                  },
                  "type": "NotificationInfo"
                }
              ],
              "type": "SessionInfo"
            }
          ],
          "totalMinutes": 323,
          "totalNotifications": 53,
          "schedule": [
            {
              "instanceGuid": "nOxPwjY7NHUT3TTycwKEBA",
              "startDay": 0,
              "endDay": 0,
              "startTime": "00:00",
              "expiration": "P1D",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "klU4SLUA3apbUxic3cykVQ",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "lnR2J28l5MpMtnWeciS55A",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "iQkggiyz8gmMUxvDLGqyZYxn",
              "refGuid": "LcbizpKRfjGryeOgL-dq6phM",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "ThAm_QQhXzmDTSqZvwId9w",
              "startDay": 0,
              "endDay": 13,
              "startTime": "08:00",
              "expiration": "P14D",
              "assessments": [
                {
                  "refKey": "dc1233b8dc1233b8",
                  "instanceGuid": "xEkZsIYvq2mQdTHVWNC5Xg",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "sAd_34IW9L8wJyljqcM5cYsa",
              "refGuid": "0nj3jtK6AjUKdC81RgOipi0v",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "dZgGdCY1yGjgEsR_2dCpDw",
              "startDay": 1,
              "endDay": 1,
              "startTime": "08:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "_pPk5VQVq3uemYEoJ8OZ_Q",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "3uwtBKYdExTO97eMEVsJpQ",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "xfPTY8eQe4hbdNy7eUaRuqtd",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "X6EZVkCKn2IAxCH2IjtAOg",
              "startDay": 1,
              "endDay": 1,
              "startTime": "12:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "d28POzI3XpmOgkFgltf3pg",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "mulYDhB4tOKbs6rZOfz-7w",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "JirIBgyvRJaW0tTUeptwQ_0r",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "VrHccNtXWCYCF6HevtugoA",
              "startDay": 1,
              "endDay": 1,
              "startTime": "16:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "1R6m6NeXHv90aG41pOk5fw",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "HZsQVB-6MGzfPwPvYcP95g",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "OHqSYvPx9rOQLDLHejLBo_1E",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "OKqyj8n5JS9sUJulgkV1NA",
              "startDay": 1,
              "endDay": 1,
              "startTime": "20:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "25kHhZnS7jFaTXZl0Xbz4Q",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "tLiKXia7Cs7pGeHxJMIEAw",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "z42qs-Vd6FuTTAx2ctFxSaum",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "DniX7C5U0B4xypVYye86KQ",
              "startDay": 2,
              "endDay": 2,
              "startTime": "08:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "zNui-Hv8u-g5mRZUrs21lg",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "aqN8J_R4JBHs73QeqnqdKg",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "xfPTY8eQe4hbdNy7eUaRuqtd",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "uHHW76vdPKXE5Lq0gwFbUg",
              "startDay": 2,
              "endDay": 2,
              "startTime": "12:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "NlBlOK-pCaKAjwAuPS1ZLg",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "oAtt_EIkCqegIrPUQCQxgg",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "JirIBgyvRJaW0tTUeptwQ_0r",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "TwPdGgYbWFjEgokjLq9VzQ",
              "startDay": 2,
              "endDay": 2,
              "startTime": "16:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "VXcBliKtA64Wr2mSWzLb8Q",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "Fd0qCduc37nJunnnmVu75A",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "OHqSYvPx9rOQLDLHejLBo_1E",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "lwD2tuHL4JepunrK9zQE_w",
              "startDay": 2,
              "endDay": 2,
              "startTime": "20:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "6ZcnE6LVosL928vypRBPWQ",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "PdYRPyOBNOfz_VIaHe9esg",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "z42qs-Vd6FuTTAx2ctFxSaum",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "kyPblY4nsFlEAqul-WelaA",
              "startDay": 3,
              "endDay": 3,
              "startTime": "08:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "osh6I_Wzxkndc_tpKEfB2w",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "boYNPXsPE4ai_vENvUgtmQ",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "xfPTY8eQe4hbdNy7eUaRuqtd",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "j1bTWsXY7QhNfSl6w-GEcQ",
              "startDay": 3,
              "endDay": 3,
              "startTime": "12:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "sZ2Sm8FYLGiQzWmexfUAdw",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "fr5vMvMXcL8LwdCDNQpGqQ",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "JirIBgyvRJaW0tTUeptwQ_0r",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "MA0SvIZ4vR2izxAYtUJk7Q",
              "startDay": 3,
              "endDay": 3,
              "startTime": "16:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "pDMIsxyusxHVRbF1FVfH7Q",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "xhNXG0JdVRg0O6cAe3KRQw",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "OHqSYvPx9rOQLDLHejLBo_1E",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "6rBTmQ9FW9eWS7FNo5Oj1g",
              "startDay": 3,
              "endDay": 3,
              "startTime": "20:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "gHT05Oe9mFU-xj0q25wN7w",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "0IkHusqnLqiadrdWICsvnA",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "z42qs-Vd6FuTTAx2ctFxSaum",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "l6SlqzbU0AXVmnwBux6TIQ",
              "startDay": 4,
              "endDay": 4,
              "startTime": "08:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "sAHlSYRMh3jPsNqWv9VzWg",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "5WiTXzoJ_EAvw1kjL62oDw",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "xfPTY8eQe4hbdNy7eUaRuqtd",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "P5EI9DMKQ82nC3MOB3eNiw",
              "startDay": 4,
              "endDay": 4,
              "startTime": "12:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "Yg5MMqns4TY5ShIJsD6QXg",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "XUpSJYCybbV6_Xuqw8QzPg",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "JirIBgyvRJaW0tTUeptwQ_0r",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "giGvPYyUMuwRskfpdMYn8A",
              "startDay": 4,
              "endDay": 4,
              "startTime": "16:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "mb1t4XUFd0WrEkXi52VxKQ",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "EciubUN8oiiAuFQbvJhGZg",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "OHqSYvPx9rOQLDLHejLBo_1E",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "uD0_MjFe1wzOnyx5cQH0xQ",
              "startDay": 4,
              "endDay": 4,
              "startTime": "20:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "yTV26Sm_rVc3ohDofTdN3w",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "DP6ga887iKJNhLDIm8SMvg",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "z42qs-Vd6FuTTAx2ctFxSaum",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "rFjZK6ochbO1sw9X6d0enA",
              "startDay": 5,
              "endDay": 5,
              "startTime": "08:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "6uIWF7mF98vu7XMqcbrCrQ",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "ul5DOHOu9pYvzuNRhCC5NQ",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "xfPTY8eQe4hbdNy7eUaRuqtd",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "Jw70ZIG0qScw9MLff8ZInw",
              "startDay": 5,
              "endDay": 5,
              "startTime": "12:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "7gHQHIeZ43w7dn0ZW5i7ng",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "OJtEP7brdOI2HLa72UvgSA",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "JirIBgyvRJaW0tTUeptwQ_0r",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "iOWd51qEH5zcr1PuCfQyZg",
              "startDay": 5,
              "endDay": 5,
              "startTime": "16:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "6bfouyinff5JK7j4HhX9HQ",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "StmDAnf8rHgiDuqibIre8Q",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "OHqSYvPx9rOQLDLHejLBo_1E",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "NLTGtgQpLhle2vfAqGvRCw",
              "startDay": 5,
              "endDay": 5,
              "startTime": "20:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "0I92_tNUQXjBybGsC38kFQ",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "iKYAbHbwLwXFGkWZTW3umw",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "z42qs-Vd6FuTTAx2ctFxSaum",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "vdJZbg_euSqgq6tngiPgew",
              "startDay": 6,
              "endDay": 6,
              "startTime": "08:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "WiewSVfeXU-8K4IqzL2NvA",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "pdCvSWR9Hgde6FTFejTtmw",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "xfPTY8eQe4hbdNy7eUaRuqtd",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "pwkI7DwAg0Q6JCODdAvhAA",
              "startDay": 6,
              "endDay": 6,
              "startTime": "12:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "HfwU00vIVKuIWXVba-hpZg",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "NOLmzt6xRsoD3QBKduMmtA",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "JirIBgyvRJaW0tTUeptwQ_0r",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "aWPy6fkzC5dgZuvCWT959g",
              "startDay": 6,
              "endDay": 6,
              "startTime": "16:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "QiQNoZ_h4iSgZm44lqdnrA",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "icQftXOS5z-2eX00-xFF2w",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "OHqSYvPx9rOQLDLHejLBo_1E",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "W3KoWzXOblPaMgdx2wQ4nA",
              "startDay": 6,
              "endDay": 6,
              "startTime": "20:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "Bv5pT51bor6h0AAdolp7zg",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "kxhwHygQvnxOJuELwOqKUw",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "z42qs-Vd6FuTTAx2ctFxSaum",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "ZQsM6-v0EdAo48LzWcmSLw",
              "startDay": 7,
              "endDay": 7,
              "startTime": "08:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "uimIAIegSUqQpikBEyy2LA",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "OUd2V12hX2lMjyEdG8-I_Q",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "xfPTY8eQe4hbdNy7eUaRuqtd",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "yNQL_AbVhm2vZ_IApPqC1A",
              "startDay": 7,
              "endDay": 7,
              "startTime": "12:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "QFtnoMn_dqckMXvRxtSFWA",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "lhbEo6OGejw0q4NOrOp3aQ",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "JirIBgyvRJaW0tTUeptwQ_0r",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "SD4ShKOLpViV_bJ1FLXR5g",
              "startDay": 7,
              "endDay": 7,
              "startTime": "16:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "rE8h-JV66rR2dniZ5houpg",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "kUzQsSGGVS1T68icS6b5qA",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "OHqSYvPx9rOQLDLHejLBo_1E",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "-CrkR8n59kdiafSm0BWK1g",
              "startDay": 7,
              "endDay": 7,
              "startTime": "20:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "JXxPevwI_whQ6Sr9y8YvHw",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "9CGb8gcQ0k47SzWGiK-BVA",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "z42qs-Vd6FuTTAx2ctFxSaum",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "NQMZjXUJMs0FrIsDInfOVQ",
              "startDay": 8,
              "endDay": 8,
              "startTime": "08:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "xM1SKHCkKaBL2FR9j20HTQ",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "JAhLzwiNvwipBUsLZFCQog",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "xfPTY8eQe4hbdNy7eUaRuqtd",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "GNOfp11mqzcRZSRWumyGeg",
              "startDay": 8,
              "endDay": 8,
              "startTime": "12:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "LwW5d1SpFu1B8SRfq36o6Q",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "IFM2Nxpah4BSlLarlXnDlw",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "JirIBgyvRJaW0tTUeptwQ_0r",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "QM9ByC-ShFk_cOxB4JRT3A",
              "startDay": 8,
              "endDay": 8,
              "startTime": "16:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "zMEThMD2tlCwloim9XUebg",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "MMkexyhrj7otrjXSk_TQgg",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "OHqSYvPx9rOQLDLHejLBo_1E",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "g9Oah5HWUEjl16MAm4b6Ww",
              "startDay": 8,
              "endDay": 8,
              "startTime": "20:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "-IJMoJCgF0GHJ3Zl2uNWIw",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "NVw4mjQyyPoi7n7cRXkQSQ",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "z42qs-Vd6FuTTAx2ctFxSaum",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "Qf3NqFMI56a5NE-vKHnVKQ",
              "startDay": 9,
              "endDay": 9,
              "startTime": "08:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "xD9d-i6_aubng4-AEKevEg",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "VmIr5__LQS0NthYqao5aYg",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "xfPTY8eQe4hbdNy7eUaRuqtd",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "Vnp-eZGLbhid4jj6-f_jMg",
              "startDay": 9,
              "endDay": 9,
              "startTime": "12:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "Xk1orYddnmKauVxQsL76rw",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "x8RfGeUemtVwhiS8Zwev7A",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "JirIBgyvRJaW0tTUeptwQ_0r",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "jMXilzbGJ1ZTybKNktikGw",
              "startDay": 9,
              "endDay": 9,
              "startTime": "16:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "jFy7CGtLuYRvO8TTcCBIdQ",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "gCbX_qAMQ33UVPGHK0uoxQ",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "OHqSYvPx9rOQLDLHejLBo_1E",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "U1e_gR8nKJlGdt242jRPzQ",
              "startDay": 9,
              "endDay": 9,
              "startTime": "20:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "oPKrUNiruOOnIOm8ch2ShA",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "928g6IGgE0DfhV4y6qxQJA",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "z42qs-Vd6FuTTAx2ctFxSaum",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "MQgwjGGZ8P0iSdDBrjTs7w",
              "startDay": 10,
              "endDay": 10,
              "startTime": "08:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "sEwFpPm5iJG0GSnZrjlCXw",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "0XScd5OazChs7MzqRDx6Kg",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "xfPTY8eQe4hbdNy7eUaRuqtd",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "A9rn_uuoALJJN8NePWX79w",
              "startDay": 10,
              "endDay": 10,
              "startTime": "12:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "cegx83y1GPnsFLxek_npvQ",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "Qc4kJSFEomml0fDladIV2A",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "JirIBgyvRJaW0tTUeptwQ_0r",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "QNTCjA8neW1a6XLcYXCMBA",
              "startDay": 10,
              "endDay": 10,
              "startTime": "16:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "Qd4TCmUcV1Dne59eEJ0pTQ",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "_6V5zeERZ6aihyjs1Egk3w",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "OHqSYvPx9rOQLDLHejLBo_1E",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "I4VqNO8pxDTJP-y_MdVMxw",
              "startDay": 10,
              "endDay": 10,
              "startTime": "20:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "ogbJglyy5tddkxlk2XMxhA",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "q0WLeSn-39wHBfMcD75hjw",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "z42qs-Vd6FuTTAx2ctFxSaum",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "FaXKUDUzfOa7K8enQziSXg",
              "startDay": 11,
              "endDay": 11,
              "startTime": "08:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "dmDtuVmQZDH_Ws6h1odDlA",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "rPRLDL3SVel8c4r7b4D87w",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "xfPTY8eQe4hbdNy7eUaRuqtd",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "syA10fqf9pC9yFH2v_-Jog",
              "startDay": 11,
              "endDay": 11,
              "startTime": "12:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "jsnn7YiWKTgvGOGZPX1i0A",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "_OOcPbo4p-uIjBOZInfmeg",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "JirIBgyvRJaW0tTUeptwQ_0r",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "OnJfYYJWOdDo1kB8a_cwGg",
              "startDay": 11,
              "endDay": 11,
              "startTime": "16:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "sPVvUJqFdOycKb4cAbXNpw",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "dpIiccPQ84cLP2ifCuOuOw",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "OHqSYvPx9rOQLDLHejLBo_1E",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "pUHJMB8me5RZSwBvh0aVRQ",
              "startDay": 11,
              "endDay": 11,
              "startTime": "20:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "4VCVcSJzuvuDYag4EnLnwQ",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "V_s05749lRanOyLZhcYazA",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "z42qs-Vd6FuTTAx2ctFxSaum",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "05yo3I9_J0JXoRyDnK9ozg",
              "startDay": 12,
              "endDay": 12,
              "startTime": "08:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "4yBkow7BnIax1p97JC8QYQ",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "Zr4n4fIwmSYDZgZH9bIjMA",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "xfPTY8eQe4hbdNy7eUaRuqtd",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "yU8j4qPFXzzmYftY4wXAjg",
              "startDay": 12,
              "endDay": 12,
              "startTime": "12:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "LOBh3fLoMEWDmFz46nlQlg",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "FjmWzew0kMMXsqKy1Ks_jw",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "JirIBgyvRJaW0tTUeptwQ_0r",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "KabvPi8pVsxCYcgms6TAtQ",
              "startDay": 12,
              "endDay": 12,
              "startTime": "16:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "p9TzGv6SrGl0ZSXaeSj0pA",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "3a7mSQD1QMRxzYoc9bxnSQ",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "OHqSYvPx9rOQLDLHejLBo_1E",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "wNocyu1jziUvzMMw_hdlUA",
              "startDay": 12,
              "endDay": 12,
              "startTime": "20:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "rSSyVsICpv6v9NwxnNUhKw",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "xcOSja0GnB86hjvqz4HSvg",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "z42qs-Vd6FuTTAx2ctFxSaum",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "OwSRw6xhV6DGe4spox1Clg",
              "startDay": 13,
              "endDay": 13,
              "startTime": "08:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "l8nmXbz8tKbDgT0eCruGQQ",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "P5w4az6-1GB120RrHEZStg",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "xfPTY8eQe4hbdNy7eUaRuqtd",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "00BFDR2exM4DOndrFuEpvg",
              "startDay": 13,
              "endDay": 13,
              "startTime": "12:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "PgKxoCQLKEUxzJ-XH_UxIA",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "eZHC_oiMIFHr-8BdqNZylg",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "JirIBgyvRJaW0tTUeptwQ_0r",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "2ncJd1Uv2XTEfuZ5CvzBTg",
              "startDay": 13,
              "endDay": 13,
              "startTime": "16:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "dd6e_3l_6JnkjWrVlEg1cQ",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "HW49kzZE1NugNwacioARYg",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "OHqSYvPx9rOQLDLHejLBo_1E",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            },
            {
              "instanceGuid": "susczRAQQeGx4g5zmpmZlw",
              "startDay": 13,
              "endDay": 13,
              "startTime": "20:00",
              "expiration": "PT60M",
              "assessments": [
                {
                  "refKey": "4528d6c04528d6c0",
                  "instanceGuid": "sLECLaa5cmdDQS45lqv3Zg",
                  "type": "ScheduledAssessment"
                },
                {
                  "refKey": "e4039130e4039130",
                  "instanceGuid": "bKq0rlcoM4xNB3MohJ3bJg",
                  "type": "ScheduledAssessment"
                }
              ],
              "timeWindowGuid": "z42qs-Vd6FuTTAx2ctFxSaum",
              "refGuid": "zqRboBa7aDwWGLMbqUZMiuX-",
              "type": "ScheduledSession"
            }
          ],
          "type": "Timeline"
        }
    """.trimIndent()

    @Test
    fun testAnotherScheduleDay0() {
        runTest {
            val eventTimeStamp = Clock.System.now()
            val repo = getTestScheduleTimelineRepo(timeStamp = eventTimeStamp, timelineJson = anotherTimelineJson)

            val now = getTodayInstant(17)
            val resourceResult = repo.getSessionsForDay("sage-assessment-test", now).firstOrNull { it is ResourceResult.Success }

            assertTrue(resourceResult is ResourceResult.Success)
            val sessionList = resourceResult.data.scheduledSessionWindows
            assertNotNull(sessionList)
            assertEquals(6, sessionList.size)

            //First session should be the first day session that is available at midnight
            val session1 = sessionList[0]
            assertEquals(getLocalDateTime(now, 0), session1.startDateTime)
            assertEquals(getLocalDateTime(now, 0, 1), session1.endDateTime)
            assertEquals(0, session1.startDateTime.hour)
            assertEquals(0, session1.startDateTime.minute)
            assertFalse(session1.hasStartTimeOfDay)
            assertFalse(session1.hasEndTimeOfDay)

            // Second session should be the first day session that is available at 8:00 until end of study
            val session2 = sessionList[1]
            assertEquals(getLocalDateTime(now, 8), session2.startDateTime)
            assertEquals(getLocalDateTime(now, 8, 14), session2.endDateTime)
            assertTrue(session2.hasStartTimeOfDay)
            assertFalse(session1.hasEndTimeOfDay)

            // Third session should be available tomorrow at 8:00AM for 1 hour
            val session3 = sessionList[2]
            assertEquals(getLocalDateTime(now, 8, 1), session3.startDateTime)
            assertEquals(getLocalDateTime(now, 9, 1), session3.endDateTime)
            assertTrue(session3.hasStartTimeOfDay)
            assertTrue(session3.hasEndTimeOfDay)

            // Fourth session should be available tomorrow at 12:00PM for 1 hour
            val session4 = sessionList[3]
            assertEquals(getLocalDateTime(now, 12, 1), session4.startDateTime)
            assertEquals(getLocalDateTime(now, 13, 1), session4.endDateTime)
            assertTrue(session4.hasStartTimeOfDay)
            assertTrue(session4.hasEndTimeOfDay)

            // Fifth session should be available tomorrow at 4:00PM for 1 hour
            val session5 = sessionList[4]
            assertEquals(getLocalDateTime(now, 16, 1), session5.startDateTime)
            assertEquals(getLocalDateTime(now, 17, 1), session5.endDateTime)
            assertTrue(session5.hasStartTimeOfDay)
            assertTrue(session5.hasEndTimeOfDay)

            // Sixth session should be available tomorrow at 8:00PM for 1 hour
            val session6 = sessionList[5]
            assertEquals(getLocalDateTime(now, 20, 1), session6.startDateTime)
            assertEquals(getLocalDateTime(now, 21, 1), session6.endDateTime)
            assertTrue(session6.hasStartTimeOfDay)
            assertTrue(session6.hasEndTimeOfDay)

        }
    }

}

