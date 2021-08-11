//
//  ContactAndSupportView.swift
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

struct ContactAndSupportView: View {
    @EnvironmentObject private var bridgeManager: SingleStudyAppManager
    @EnvironmentObject private var viewModel: StudyInfoViewModel
        
    var body: some View {
        VStack(alignment: .leading, spacing: 25) {
            Spacer()
            section(title: Text("General Support"),
                    body: Text("For general questions about the study or to withdraw from the study, please contact:"),
                    contacts: viewModel.supportContacts)
            withdrawalInfoView()
            LineView()
            section(title: Text("Your Participant Rights"),
                    body: Text("For questions about your rights as a research participant, please contact:"),
                    contacts: viewModel.irbContacts)
            Spacer()
        }
        .padding(.horizontal, 26)
        .foregroundColor(.textForeground)
        .background(Color.sageWhite)
        .padding(.all, 16)
    }
    
    // MARK: Section Header
    
    @ViewBuilder
    private func section(title: Text, body: Text, contacts: [StudyContact]) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            title
                .font(.poppinsFont(18, relativeTo: .title2, weight: .regular))
                .padding(.bottom, 8)
            body
                .font(.latoFont(12, relativeTo: .footnote, weight: .regular))
                .fixedSize(horizontal: false, vertical: true)
        }
        .frame(maxWidth: .infinity)
        
        ForEach(contacts) {
            contactView($0)
                .padding(.horizontal, 16)
        }
    }
    
    // MARK: Protocol ID
    
    private func protocolIdLabel() -> Text {
        Text("IRB Protocol ID: ") + Text(viewModel.study?.irbProtocolId ?? "")
    }
    
    // MARK: Contact Info
    
    @ViewBuilder
    private func contactView(_ contact: StudyContact) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            VStack(alignment: .leading, spacing: 2) {
                Text(contact.name)
                    .font(.poppinsFont(14, relativeTo: .title2, weight: .bold))
                contact.position
                    .font(.latoFont(12, relativeTo: .title3, weight: .regular))
            }
            .padding(.leading, 26)
            if let phone = contact.phone {
                contactValue(phone, contactType: .phone)
            }
            if let email = contact.email {
                contactValue(email, contactType: .email)
            }
            if contact.isIRB {
                protocolIdLabel()
                    .font(.latoFont(12, relativeTo: .title3, weight: .regular))
                    .padding(.leading, 26)
            }
        }
    }
    
    @ViewBuilder
    private func contactValue(_ value: String, contactType: ContactType) -> some View {
        
        let imageName = contactType.rawValue
        let label = label(for: contactType)
        let url = url(value, contactType: contactType)
        
        Link(destination: url) {
            Label(title: {
                Text(value)
                    .font(.latoFont(12, relativeTo: .title3, weight: .regular))
            },
            icon: {
                Image("\(imageName).foreground", label: label)
                    .background(Image(decorative: "\(imageName).background")
                                    .foregroundColor(.accentColor))
            })
        }
    }
    
    private func label(for contactType: ContactType) -> Text {
        switch contactType {
        case .phone:
            return Text("Phone")
        case .email:
            return Text("Email")
        }
    }
    
    private func url(_ value: String, contactType: ContactType) -> URL {
        switch contactType {
        case .phone:
            return URL(string: "tel:\(value.components(separatedBy: .phoneDigits.inverted).joined())")!
        case .email:
            return URL(string: "mailto:\(value)")!
        }
    }
    
    private enum ContactType : String, CaseIterable {
        case phone, email
    }
    
    // MARK: Withdrawal Info
    
    @ViewBuilder
    private func withdrawalInfoView() -> some View {
        VStack(alignment: .leading, spacing: 16) {
            if let phone = viewModel.participantPhone {
                withdrawalHeader(Text("To withdraw from this study, you’ll need the Study ID and the phone number you registered with:"))
                withdrawalRow(Text("Study ID: "), bridgeManager.studyId!)
                withdrawalRow(Text("Registration Phone Number: "), phone.nationalFormat ?? phone.number)
            }
            else {
                withdrawalHeader(Text("To withdraw from this study, you’ll need the following info:"))
                withdrawalRow(Text("Study ID: "), bridgeManager.studyId!)
                withdrawalRow(Text("Participant ID: "), viewModel.participantId ?? "")
            }
        }
        .font(.latoFont(12, relativeTo: .body, weight: .regular))
        .padding(.top, 19)
        .padding(.horizontal, 32)
        .padding(.bottom, 26)
        .frame(maxWidth: .infinity)
        .background(Color.accentColor)
        .cornerRadius(10)
    }
    
    @ViewBuilder
    private func withdrawalHeader(_ label: Text) -> some View {
        label
            .fixedSize(horizontal: false, vertical: true)
    }
    
    private func withdrawalRow(_ label: Text, _ value: String) -> Text {
        label.bold() + Text(value)
    }
}

extension CharacterSet {
    static let phoneDigits: CharacterSet = .init(charactersIn: "0123456789")
}

struct ContactAndSupportView_Previews: PreviewProvider {
    static var previews: some View {
        ContactAndSupportView()
            .environmentObject(SingleStudyAppManager(appId: kPreviewStudyId))
            .environmentObject(StudyInfoViewModel(isPreview: true))    
    }
}
