//
//  LocalNotificationManager.swift
//
//  Copyright © 2021 Sage Bionetworks. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
// 1.  Redistributions of source code must retain the above copyright notice, this
// list of conditions and the following disclaimer.
//
// 2.  Redistributions in binary form must reproduce the above copyright notice,
// this list of conditions and the following disclaimer in the documentation and/or
// other materials provided with the distribution.
//
// 3.  Neither the name of the copyright holder(s) nor the names of any contributors
// may be used to endorse or promote products derived from this software without
// specific prior written permission. No license is granted to the trademarks of
// the copyright holders even if such marks are included in this software.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//

import SwiftUI
import UserNotifications
import BridgeClient

fileprivate let kAllowSnoozeKey = "allowSnooze"
fileprivate let maxTotalNotifications = 60

open class LocalNotificationManager : NSObject, UNUserNotificationCenterDelegate {
    
    open private(set) var scheduledNotificationCategory = "org.sagebionetworks.ScheduledSession"
    
    open var notifications: [NativeScheduledNotification] = []
    open var maxScheduledSessionNotifications: Int = .max
    
    open func setupNotifications(_ notifications: [NativeScheduledNotification]) {
        self.notifications = notifications
        UIApplication.shared.applicationIconBadgeNumber = 0
        UNUserNotificationCenter.current().getNotificationSettings { settings in
            // Do nothing if the app did not ask or the participant did not provide
            // permission to send notifications.
            guard settings.authorizationStatus == .authorized else { return }
            UNUserNotificationCenter.current().getPendingNotificationRequests { oldRequests in
                // Use dispatch async to put this work on the next run loop and ensure that
                // we are creating this on the main thread. Has to be the main thread b/c
                // kotlin objects are not thread-safe.
                DispatchQueue.main.async {
                    // Just refresh everything by removing all pending requests.
                    let requestIds: [String] = oldRequests.compactMap {
                        $0.content.categoryIdentifier == self.scheduledNotificationCategory ? $0.identifier : nil
                    }
                    UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: requestIds)
                    // and then re-adding or adding new.
                    let requests = self.createRequests()
                    requests[..<min(self.maxRequests, requests.count)].forEach {
                        UNUserNotificationCenter.current().add($0)
                    }
                }
            }
        }
    }
    
    private var maxRequests: Int {
        min(maxTotalNotifications, maxScheduledSessionNotifications)
    }
    
    private var maxTotalScheduledSessionRequests: Int {
        min(maxRequests, notifications.count)
    }
    
    open func createRequests() -> [UNNotificationRequest] {
        let builders = notifications[..<maxTotalScheduledSessionRequests]
            .map { buildRequests(for: $0) }
            .flatMap { $0 }
            .sorted()
        return builders[..<min(maxRequests, builders.count)]
            .map { $0.buildNotificationRequest()}
    }
    
    open func buildContent(for notification: NativeScheduledNotification) -> UNNotificationContent {
        // Set up the notification
        let content = UNMutableNotificationContent()
        content.sound = UNNotificationSound.default
        content.badge = UIApplication.shared.applicationIconBadgeNumber + 1 as NSNumber;
        content.categoryIdentifier = scheduledNotificationCategory
        content.threadIdentifier = notification.instanceGuid
        if let message = notification.message {
            content.title = message.subject
            content.body = message.message
        }
        content.userInfo = [kAllowSnoozeKey : notification.allowSnooze]
        return content
    }
    
    private func buildRequests(for notification: NativeScheduledNotification) -> [RequestBuilder] {
        let content = buildContent(for: notification)
        let calendar = Calendar.current
        
        // If it doesn't repeat then we are done.
        guard let repeatInterval = notification.repeatInterval,
              let repeatUntil = notification.repeatUntil,
              let startInstant = calendar.date(from: notification.scheduleOn),
              let endInstant = calendar.date(from: repeatUntil)
        else {
            return [RequestBuilder(content: content,
                                   scheduleOn: notification.scheduleOn,
                                   instanceGuid: notification.instanceGuid,
                                   repeats: false)]
        }
        
        // If it repeats daily and will continue for more that the total number of allowed
        // requests, then schedule it to repeat.
        let maxCount = maxRequests / (notifications.count > 0 ? notifications.count : 1)
        let numDays = calendar.dateComponents([.day], from: notification.scheduleOn, to: repeatUntil).day ?? 0
        if repeatInterval.day == 1, numDays > maxCount {
            var scheduleOn = DateComponents()
            scheduleOn.hour = notification.scheduleOn.hour
            scheduleOn.minute = notification.scheduleOn.minute
            return [RequestBuilder(content: content,
                                   scheduleOn: scheduleOn,
                                   instanceGuid: notification.instanceGuid,
                                   repeats: true)]
        }
        
        // Otherwise, we need to advance the date by the repeat interval until we hit the max
        // allowed number of requests for this scheduled session instance.
        var requests = [RequestBuilder]()
        var scheduleInstant = startInstant
        repeat {
            let scheduleOn = calendar.dateComponents([.year, .month, .day, .hour, .minute], from: scheduleInstant)
            requests.append(RequestBuilder(content: content,
                                           scheduleOn: scheduleOn,
                                           instanceGuid: notification.instanceGuid,
                                           repeats: false))
            scheduleInstant = calendar.date(byAdding: repeatInterval, to: scheduleInstant) ?? .distantFuture
        } while scheduleInstant < endInstant && requests.count < maxCount
        
        return requests
    }
    
    private func removeAllPendingNotifications(_ completionHandler: @escaping (() -> Void)) {
        UNUserNotificationCenter.current().getPendingNotificationRequests { (requests) in
            let requestIds: [String] = requests.compactMap {
                guard $0.content.categoryIdentifier == self.scheduledNotificationCategory else { return nil }
                return $0.identifier
            }
            UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: requestIds)
            completionHandler()
        }
    }
    
    private class RequestBuilder : Comparable {

        let content: UNNotificationContent
        let trigger: UNCalendarNotificationTrigger
        let scheduleInstant: Date
        let instanceGuid: String
        
        init(content: UNNotificationContent,
             scheduleOn: DateComponents,
             instanceGuid: String,
             repeats: Bool) {
            var triggerOn = repeats ? DateComponents() : scheduleOn
            triggerOn.hour = scheduleOn.hour
            triggerOn.minute = scheduleOn.minute
            self.trigger = UNCalendarNotificationTrigger(dateMatching: triggerOn, repeats: repeats)
            self.instanceGuid = instanceGuid
            self.scheduleInstant = Calendar.current.date(from: scheduleOn) ?? .distantFuture
            self.content = content
        }
        
        func buildNotificationRequest() -> UNNotificationRequest {
            UNNotificationRequest(identifier: "\(self.instanceGuid)|\(self.scheduleInstant)", content: content, trigger: trigger)
        }
        
        static func < (lhs: LocalNotificationManager.RequestBuilder, rhs: LocalNotificationManager.RequestBuilder) -> Bool {
            lhs.scheduleInstant < rhs.scheduleInstant
        }
        
        static func == (lhs: LocalNotificationManager.RequestBuilder, rhs: LocalNotificationManager.RequestBuilder) -> Bool {
            lhs.scheduleInstant == rhs.scheduleInstant && lhs.instanceGuid == rhs.instanceGuid
        }
    }
}

