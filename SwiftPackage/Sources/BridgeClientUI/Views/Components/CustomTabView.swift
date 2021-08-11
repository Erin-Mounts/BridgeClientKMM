//
//  CustomTabView.swift
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

public protocol TabItem : Identifiable, Hashable {
    var rawValue: String { get }
    func title() -> Text
}

public extension TabItem {
    var id: String { rawValue }
    var iconName: String { rawValue }
}

public struct CustomTabView<Tab : TabItem, Content : View>: View {
    @Binding public var selectedTab : Tab
    private let tabs: [Tab]
    private let placement: VerticalAlignment
    private let bundle: Bundle?
    private let titles: [Tab : Text]
    private let content: (Tab) -> Content
    
    public init(selectedTab: Binding<Tab>, tabs: [Tab], placement: VerticalAlignment, bundle: Bundle? = nil, @ViewBuilder content: @escaping (Tab) -> Content) {
        self._selectedTab = selectedTab
        self.tabs = tabs
        self.placement = placement
        self.bundle = bundle
        self.titles = tabs.reduce([Tab: Text]()) { (dict, tab) -> [Tab: Text] in
            var dict = dict
            dict[tab] = tab.title()
            return dict
        }
        self.content = content
    }
    
    public var body: some View {
        GeometryReader { geometry in
            VStack(spacing: 0) {
                if placement == .bottom {
                    content(selectedTab)
                        .frame(maxHeight: .infinity)
                    Divider()
                        .background(Color.hexDEDEDE)
                }
                
                tabBar()
                
                if placement == .top {
                    content(selectedTab)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                        .gesture(
                            DragGesture().updating($translation) { value, state, _ in
                                state = value.translation.width
                            }.onEnded { value in
                                let offset = value.translation.width / geometry.size.width * 1.25
                                let newIndex = (CGFloat(tabs.firstIndex(of: selectedTab)!) - offset).rounded()
                                self.selectedTab = tabs[min(max(Int(newIndex), 0), tabs.count - 1)]
                            }
                        )
                }
            }
        }
    }
    
    // Keeps track of how much did user swipe left or right.
    @GestureState private var translation: CGFloat = 0
    
    // The size of the tab buttons
    private let buttonSize: CGFloat = 64
    
    @ViewBuilder
    private func tabBar() -> some View {
        HStack(alignment: .bottom, spacing: 0) {
            ForEach(tabs) { tab in
                let isSelected = self.selectedTab == tab
                VStack(spacing: 0) {
                    tabButton(for: tab, isSelected: isSelected)
                        .frame(height: buttonSize, alignment: .bottom)
                        .frame(minWidth: buttonSize)
                    if placement == .top {
                        Rectangle()
                            .fill(isSelected ? Color.accentColor : Color.hexE5E5E5)
                            .frame(height: 4)
                    }
                }
                .frame(minWidth: 0, maxWidth: .infinity)
            }
        }
        .background(backgroundColor().edgesIgnoringSafeArea(.all))
    }
    
    @ViewBuilder
    private func tabButton(for tab: Tab, isSelected: Bool) -> some View {
        let imageTopOffset: CGFloat = (placement == .bottom) ? 4 : 0
        Button(action: { self.selectedTab = tab }) {
            ZStack(alignment: .bottom) {
                ZStack {
                    Image(decorative: "\(tab.iconName).selected", bundle: bundle)
                        .foregroundColor(.accentColor)
                        .opacity(isSelected ? 1 : 0)
                    Image(decorative: tab.iconName, bundle: bundle)
                }
                .padding(.top, imageTopOffset)
                .frame(height: buttonSize - imageTopOffset, alignment: .top)
                titles[tab]!
                    .font(.latoFont(10, weight: isSelected ? .bold : .regular))
                    .foregroundColor(.textForeground)
                    .padding(.bottom, 12)
            }
        }
        .accessibility(label: titles[tab]!)
        .accessibility(addTraits: isSelected ? [.isSelected] : [.isButton])
        .accessibility(removeTraits: isSelected ? [.isButton] : [.isSelected])
    }
    
    private func backgroundColor() -> Color {
        placement == .bottom ? .hexFDFDFD : .screenBackground
    }
}

struct ExampleTabView : View {
    let placement: VerticalAlignment
    
    @State var selectedTab : Tab = .profile
    
    enum Tab : String, CaseIterable, TabItem {
        case profile, notifications, settings
        
        func title() -> Text {
            switch self {
            case .profile:
                return Text("PROFILE")
            case .notifications:
                return Text("NOTIFICATIONS")
            case .settings:
                return Text("SETTINGS")
            }
        }
    }
    
    var body: some View {
        CustomTabView(selectedTab: $selectedTab, tabs: Tab.allCases, placement: placement) { tab in
            switch tab {
            case .profile:
                Text("Showing Profile")
            case .notifications:
                Text("Showing Notifications")
            case .settings:
                Text("Showing Settings")
            }
        }
        .onAppear {
            print("whoohoo")
        }
    }
}

struct CustomTabView_Previews: PreviewProvider {
    static var previews: some View {
        Group {
            ExampleTabView(placement: .top)
            ExampleTabView(placement: .top)
                .previewDevice("iPhone 12")
            ExampleTabView(placement: .bottom)
            ExampleTabView(placement: .bottom)
                .previewDevice("iPhone 12")
        }
    }
}

