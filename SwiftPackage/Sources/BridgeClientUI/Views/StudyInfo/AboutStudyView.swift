//
//  AboutStudyView.swift
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
import SharedMobileUI

struct AboutStudyView: View {
    @EnvironmentObject private var viewModel: StudyInfoViewModel
    
    @State private var isPresentingPrivacyNotice: Bool = false
    @State private var privacyNoticeTab: PrivacyNotice.Category = .weWill
    
    private let horizontalPadding: CGFloat = 26
    
    // MARK: About
    
    var body: some View {
        VStack(spacing: 0) {
            mainAboutView()
            privacyPolicyButton()
        }
    }
    
    @ViewBuilder
    private func mainAboutView() -> some View {
        VStack(alignment: .leading, spacing: 26) {
            aboutHeader()
            aboutBody()
                .foregroundColor(.textForeground)
            LineView()
                .padding(.horizontal, horizontalPadding)
            ForEach(viewModel.studyContacts) { contact in
                studyInfo(contact)
                    .padding(.horizontal, horizontalPadding)
            }
            Spacer()
        }
        .background(Color.sageWhite)
        .padding(.all, 16)
    }
    
    @ViewBuilder
    private func aboutHeader() -> some View {
        if let logo = viewModel.logo {
            logo
        }
        else {
            Text(viewModel.institutionName.localizedUppercase)
                .foregroundColor(viewModel.foregroundColor)
                .padding(.vertical, 28)
                .padding(.horizontal, horizontalPadding)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(viewModel.backgroundColor)
        }
    }
    
    @ViewBuilder
    private func aboutBody() -> some View {
        Text(viewModel.title)
            .font(.playfairDisplayFont(18, relativeTo: .title, weight: .regular))
            .padding(.top, 6)
            .padding(.horizontal, horizontalPadding)
        if let details = viewModel.details {
            Text(details)
                .font(.latoFont(15, relativeTo: .body, weight: .regular))
                .fixedSize(horizontal: false, vertical: true)
                .padding(.horizontal, horizontalPadding)
        }
    }
    
    @ViewBuilder
    private func studyInfo(_ contact: StudyContact) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(contact.name)
                .font(.latoFont(14, relativeTo: .title2, weight: .bold))
            contact.position
                .font(.latoFont(12, relativeTo: .title3, weight: .regular))
        }
        .foregroundColor(.textForeground)
    }
    
    @ViewBuilder
    private func privacyPolicyButton() -> some View {
        Button(action: showPrivacyNotice) {
            Label("Review Privacy Notice", image: "privacy.notice.icon")
        }
        .buttonStyle(RoundedButtonStyle())
        .padding(.top, 4)
        .padding(.horizontal, horizontalPadding)
        .padding(.bottom, 32)
        .fullScreenCover(isPresented: $isPresentingPrivacyNotice) {
            VStack(alignment: .leading, spacing: 0) {
                Button(action: hidePrivacyNotice) {
                    Label("Back", systemImage: "arrow.left")
                        .font(.latoFont(14, relativeTo: .title2, weight: .regular))
                        .foregroundColor(.textForeground)
                        .padding(.horizontal, 16)
                }
                PrivacyNoticeView(selectedTab: $privacyNoticeTab)
            }
            .background(Color.screenBackground.edgesIgnoringSafeArea(.top))
        }
    }
    
    private func showPrivacyNotice() {
        isPresentingPrivacyNotice = true
    }
    
    private func hidePrivacyNotice() {
        isPresentingPrivacyNotice = false
    }
}

struct AboutStudyView_Previews: PreviewProvider {
    static var previews: some View {
        AboutStudyView()
            .environmentObject(SingleStudyAppManager(appId: kPreviewStudyId))
            .environmentObject(StudyInfoViewModel(isPreview: true))        
    }
}
