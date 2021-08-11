//
//  HistoryView.swift
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
import BridgeClient
import SharedMobileUI

public struct HistoryView: View {
    @EnvironmentObject private var bridgeManager: SingleStudyAppManager
    @StateObject private var viewModel: HistoryViewModel = .init()
    @SwiftUI.Environment(\.assessmentInfoMap) private var assessmentInfoMap: AssessmentInfoMap

    private let previewRecords: [AssessmentRecord]
    init(_ previewRecords: [AssessmentRecord]) {
        self.previewRecords = previewRecords
    }
    
    public init() {
        self.previewRecords = []
    }
    
    public var body: some View {
        ScreenBackground {
            VStack(spacing: 21) {
                header()
                ScrollView {
                    LazyVStack(spacing: 16) {
                        ForEach(viewModel.records) { record in
                            card(record)
                        }
                    }
                    .padding(.bottom, 20)
                }// end scrollview
            }
        }
        .onAppear {
            viewModel.onAppear(studyId: bridgeManager.studyId!, previewRecords: previewRecords)
        }
    }
    
    @ViewBuilder
    private func header() -> some View {
        VStack(spacing: 10) {
            ZStack(alignment: .top) {
                Image(decorative: "history.hero", bundle: .module)
                    .padding(.top, 12)
                VStack(spacing: -6) {
                    Text("\(viewModel.minutes)")
                        .font(.poppinsFont(21))
                        .foregroundColor(.textForeground)
                    Text("minutes", bundle: .module)
                        .font(.poppinsFont(10))
                        .foregroundColor(.textForeground)
                }
            }
            Text("Thank you for your contributions!", bundle: .module)
                .font(.playfairDisplayFont(16, relativeTo: .title2, weight: .regular))
                .foregroundColor(.textForeground)
        }
        .padding(.top, 16)
    }
    
    @ViewBuilder
    private func card(_ record: AssessmentRecord) -> some View {
        HStack(alignment: .top, spacing: 0) {
            rightSide(record)
            leftSide(record)
        }
        .padding(.horizontal, 31)
    }
    
    @ViewBuilder
    private func rightSide(_ record: AssessmentRecord) -> some View {
        assessmentInfoMap.color(for: record.info)
            .overlay(assessmentInfoMap.icon(for: record.info))
            .frame(width: 114)
            .frame(minHeight: 112)
    }
    
    @ViewBuilder
    private func leftSide(_ record: AssessmentRecord) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            CompletedDotView(color: assessmentInfoMap.color(for: record.info))
                .padding(.bottom, 2)
            Text(DateFormatter.string(from: record.finishedOn, timeZone: record.timeZone, dateStyle: .medium, timeStyle: .none))
                .foregroundColor(.textForeground)
                .font(.poppinsFont(10, relativeTo: .caption, weight: .regular))
            assessmentInfoMap.title(for: record.info)
                .font(.latoFont(16, relativeTo: .title, weight: .regular))
                .foregroundColor(.textForeground)
                .frame(maxWidth: .infinity, alignment: .leading)
            Text(durationFormatter.string(from: Double(record.minutes * 60)) ?? "")
                .foregroundColor(.textForeground)
                .font(.poppinsFont(10, relativeTo: .caption, weight: .regular))
        }
        .padding(.leading, 16)
        .padding(.top, 8)
    }
    
    private let durationFormatter: DateComponentsFormatter = {
        let formatter = DateComponentsFormatter()
        formatter.unitsStyle = .short
        formatter.allowedUnits = .minute
        return formatter
    }()
}

extension DateFormatter {
    static func string(from date: Date, timeZone: TimeZone, dateStyle: Style, timeStyle: Style) -> String {
        let formatter = DateFormatter()
        formatter.timeStyle = timeStyle
        formatter.dateStyle = dateStyle
        formatter.timeZone = timeZone
        return formatter.string(from: date)
    }
}

struct HistoryView_Previews: PreviewProvider {
    static var previews: some View {
        HistoryView(historyPreviews)
            .environmentObject(SingleStudyAppManager(appId: kPreviewStudyId))
    }
}
 
let historyPreviews: [AssessmentRecord] = [
    .init(assessmentInfo: .init(identifier: "number-match"),
          instanceGuid: "record1", startedOn: Date().addingTimeInterval(-360), finishedOn: Date()),
    .init(assessmentInfo: .init(identifier: "memory-for-sequences"),
          instanceGuid: "record2", startedOn: Date().addingTimeInterval(-480), finishedOn: Date()),
    .init(assessmentInfo: .init(identifier: "spelling"),
          instanceGuid: "record3", startedOn: Date().addingTimeInterval(-120), finishedOn: Date()),
    .init(assessmentInfo: .init(identifier: "vocabulary"),
          instanceGuid: "record4", startedOn: Date().addingTimeInterval(-420), finishedOn: Date())
]
