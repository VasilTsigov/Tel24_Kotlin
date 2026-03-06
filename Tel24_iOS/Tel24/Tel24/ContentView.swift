// ContentView.swift — всички SwiftUI изгледи

import SwiftUI
import ContactsUI

// MARK: - Root

struct ContentView: View {
    @State private var showSplash = true

    var body: some View {
        if showSplash {
            SplashView { showSplash = false }
        } else {
            MainTabView()
        }
    }
}

// MARK: - SplashView

struct SplashView: View {
    let onContinue: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            Spacer()

            Image("Logo")
                .resizable()
                .scaledToFit()
                .frame(width: 240, height: 192)

            Text("Телефонен указател\nна служители по горите")
                .font(.title3.weight(.bold))
                .multilineTextAlignment(.center)
                .foregroundColor(.accentColor)
                .padding(.top, 28)

            Text("инж. Васил Цигов")
                .font(.subheadline)
                .foregroundColor(Color(.darkGray))
                .padding(.top, 10)

            Text("2026 г.")
                .font(.footnote)
                .foregroundColor(Color(.systemGray))
                .padding(.top, 4)

            Spacer()

            Button(action: onContinue) {
                Text("Продължи ...")
                    .font(.body.weight(.medium))
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.accentColor)
                    .foregroundColor(.white)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            .padding(.horizontal, 32)
            .padding(.bottom, 32)
        }
        .background(Color(.systemBackground))
    }
}

// MARK: - MainTabView

struct MainTabView: View {
    var body: some View {
        TabView {
            TreeView(source: .iag)
                .tabItem { Label("ИАГ", systemImage: "building.2") }
            TreeView(source: .rdg)
                .tabItem { Label("РДГ", systemImage: "tree") }
            TreeView(source: .dp)
                .tabItem { Label("ДП", systemImage: "leaf") }
            SearchView()
                .tabItem { Label("Търсене", systemImage: "magnifyingglass") }
        }
    }
}

// MARK: - TreeView

struct TreeView: View {
    let source: DataSource

    @StateObject private var vm = TreeViewModel()
    @State private var expandedIds: Set<String> = []
    @State private var selectedEmployee: TreeNode? = nil

    var body: some View {
        NavigationView {
            ZStack {
                if vm.isLoading && vm.items.isEmpty {
                    ProgressView("Зареждане...")
                } else {
                    List {
                        ForEach(flatItems, id: \.itemId) { item in
                            if item.isDept {
                                DeptRow(
                                    node: item.node,
                                    level: item.level,
                                    isExpanded: expandedIds.contains(item.itemId)
                                ) {
                                    toggle(item.itemId)
                                }
                            } else {
                                EmpRow(node: item.node, level: item.level) {
                                    selectedEmployee = item.node
                                }
                            }
                        }
                    }
                    .listStyle(.plain)
                    .overlay(alignment: .bottom) {
                        if vm.isLoading {
                            ProgressView().padding(.bottom, 8)
                        }
                    }
                }
            }
            .navigationTitle(source.rawValue)
            .navigationBarTitleDisplayMode(.inline)
            .sheet(item: $selectedEmployee) { emp in
                EmployeeDetailView(node: emp)
            }
            .overlay(alignment: .bottom) {
                if let msg = vm.message {
                    Text(msg)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .padding(.horizontal)
                        .padding(.bottom, 4)
                }
            }
        }
        .onAppear {
            if vm.items.isEmpty { vm.load(source: source) }
        }
        .onReceive(vm.$items) { newItems in
            for node in newItems where !node.isEmployee {
                expandedIds.insert(rootId(node))
            }
        }
    }

    // ─── Плосък списък (flatten + filter по expand) ──────────────────────────

    private struct FlatItem {
        let node: TreeNode
        let level: Int
        let isDept: Bool
        let itemId: String   // path-базиран уникален ключ
    }

    private var flatItems: [FlatItem] {
        let all = buildAll(vm.items, level: 0, parentPath: "")
        var result: [FlatItem] = []
        var collapsedDepth: Int? = nil

        for item in all {
            if let cd = collapsedDepth {
                if item.level > cd { continue }
                else { collapsedDepth = nil }
            }
            result.append(item)
            if item.isDept && !expandedIds.contains(item.itemId) {
                collapsedDepth = item.level
            }
        }
        return result
    }

    private func buildAll(_ nodes: [TreeNode], level: Int, parentPath: String) -> [FlatItem] {
        var result: [FlatItem] = []
        for (i, node) in nodes.enumerated() {
            let isDept = !node.isEmployee
            let itemId = "\(parentPath)/\(i)_\(node.nodeId ?? -1)"
            result.append(FlatItem(node: node, level: level, isDept: isDept, itemId: itemId))
            if isDept, let children = node.children {
                result += buildAll(children, level: level + 1, parentPath: itemId)
            }
        }
        return result
    }

    private func rootId(_ node: TreeNode) -> String {
        let idx = vm.items.firstIndex { $0.nodeId == node.nodeId && $0.text == node.text } ?? 0
        return "/\(idx)_\(node.nodeId ?? -1)"
    }

    private func toggle(_ id: String) {
        if expandedIds.contains(id) { expandedIds.remove(id) }
        else { expandedIds.insert(id) }
    }
}

// MARK: - DeptRow

struct DeptRow: View {
    let node: TreeNode
    let level: Int
    let isExpanded: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 8) {
                Image(systemName: "chevron.right")
                    .rotationEffect(.degrees(isExpanded ? 90 : 0))
                    .animation(.easeInOut(duration: 0.15), value: isExpanded)
                    .foregroundStyle(.secondary)
                    .font(.caption.weight(.semibold))
                Text(node.text ?? "")
                    .font(.body.weight(.semibold))
                    .foregroundStyle(.primary)
                Spacer()
            }
            .padding(.vertical, 6)
            .padding(.leading, CGFloat(level) * 16)
        }
        .buttonStyle(.plain)
        .listRowBackground(Color(.secondarySystemBackground).opacity(0.6))
    }
}

// MARK: - EmpRow

struct EmpRow: View {
    let node: TreeNode
    let level: Int
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 10) {
                AsyncImage(url: node.imageUrl) { phase in
                    switch phase {
                    case .success(let img):
                        Color.clear
                            .frame(width: 54, height: 54)
                            .overlay(
                                img.resizable()
                                    .aspectRatio(contentMode: .fill)
                                    .offset(y: 8)
                            )
                            .clipShape(Circle())
                    default:
                        Image(systemName: "person.circle.fill")
                            .resizable()
                            .frame(width: 54, height: 54)
                            .foregroundStyle(.secondary)
                    }
                }

                VStack(alignment: .leading, spacing: 2) {
                    Text(node.text ?? "")
                        .font(.subheadline)
                        .foregroundStyle(.primary)
                    if let dlag = node.dlag, !dlag.isEmpty {
                        Text(dlag)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
                Spacer()
            }
            .padding(.vertical, 4)
            .padding(.leading, 52 + CGFloat(level) * 16)
        }
        .buttonStyle(.plain)
    }
}

// MARK: - SearchView

struct SearchView: View {
    @StateObject private var vm = SearchViewModel()
    @State private var query = ""
    @State private var selectedEmployee: TreeNode? = nil

    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Поле за търсене
                HStack {
                    Image(systemName: "magnifyingglass")
                        .foregroundStyle(.secondary)
                    TextField("Иван Иванов или 088…", text: $query)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()
                        .onChange(of: query) { vm.search($0) }
                    if !query.isEmpty {
                        Button { query = "" } label: {
                            Image(systemName: "xmark.circle.fill")
                                .foregroundStyle(.secondary)
                        }
                    }
                }
                .padding(10)
                .background(Color(.systemGray6))
                .clipShape(RoundedRectangle(cornerRadius: 10))
                .padding()

                ZStack {
                    if vm.isLoading {
                        ProgressView()
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                    } else if query.isEmpty {
                        VStack(spacing: 16) {
                            Image(systemName: "magnifyingglass")
                                .font(.system(size: 48))
                                .foregroundStyle(.quaternary)
                            VStack(spacing: 8) {
                                Text("Как се търси:")
                                    .font(.headline)
                                    .foregroundStyle(.secondary)
                                Group {
                                    Label("Две букви с интервал: \"ив ив\"", systemImage: "person")
                                    Label("По две думи: \"Иван Иванов\"", systemImage: "person.text.rectangle")
                                    Label("По ГСМ номер (мин. 5 цифри)", systemImage: "phone")
                                }
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                            }
                        }
                        .padding(.horizontal, 32)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                    } else if vm.results.isEmpty {
                        Text("Няма резултати")
                            .foregroundStyle(.secondary)
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                    } else {
                        List {
                            ForEach(vm.results) { emp in
                                EmpRow(node: emp, level: 0) {
                                    selectedEmployee = emp
                                }
                            }
                        }
                        .listStyle(.plain)
                    }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)

                if let msg = vm.message {
                    Text(msg)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .padding(.bottom, 8)
                }
            }
            .navigationTitle("Търсене")
            .navigationBarTitleDisplayMode(.inline)
            .sheet(item: $selectedEmployee) { emp in
                EmployeeDetailView(node: emp)
            }
        }
    }
}

// MARK: - EmployeeDetailView (Bottom sheet)

struct EmployeeDetailView: View {
    let node: TreeNode
    @Environment(\.dismiss) private var dismiss

    @State private var showPhoto    = false
    @State private var showContacts = false

    private var hasPhone: Bool { !(node.gsm?.isEmpty ?? true)   && node.gsm?.hasPrefix("Няма") == false }
    private var hasEmail: Bool { !(node.email?.isEmpty ?? true) && node.email?.hasPrefix("Няма") == false }

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {

                    // Снимка
                    AsyncImage(url: node.imageUrl) { phase in
                        switch phase {
                        case .success(let img):
                            img.resizable()
                                .aspectRatio(contentMode: .fill)
                                .frame(width: 110, height: 110)
                                .clipShape(Circle())
                                .onTapGesture { if node.imageUrl != nil { showPhoto = true } }
                        default:
                            Image(systemName: "person.circle.fill")
                                .resizable()
                                .frame(width: 110, height: 110)
                                .foregroundStyle(.secondary)
                        }
                    }
                    .padding(.top, 8)

                    // Основни данни
                    VStack(spacing: 4) {
                        Text(node.text ?? "")
                            .font(.title3.weight(.semibold))
                            .multilineTextAlignment(.center)
                        if let dlag = node.dlag, !dlag.isEmpty {
                            Text(dlag)
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                                .multilineTextAlignment(.center)
                        }
                        if let pod = node.pod, !pod.isEmpty {
                            Text(pod)
                                .font(.caption)
                                .foregroundStyle(.secondary)
                                .multilineTextAlignment(.center)
                        }
                    }
                    .padding(.horizontal)

                    // Контактна информация
                    VStack(spacing: 0) {
                        if hasPhone, let gsm = node.gsm {
                            ContactRow(icon: "phone", label: gsm)
                        }
                        if hasEmail, let email = node.email {
                            ContactRow(icon: "envelope", label: email)
                        }
                    }
                    .background(Color(.secondarySystemBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .padding(.horizontal)

                    // Бутони за действия
                    HStack(spacing: 12) {
                        ActionButton(title: "Обади се", icon: "phone.fill", disabled: !hasPhone) {
                            if let gsm = node.gsm, let url = URL(string: "tel:\(gsm)") {
                                UIApplication.shared.open(url)
                            }
                        }
                        ActionButton(title: "SMS", icon: "message.fill", disabled: !hasPhone) {
                            if let gsm = node.gsm, let url = URL(string: "sms:\(gsm)") {
                                UIApplication.shared.open(url)
                            }
                        }
                        ActionButton(title: "Имейл", icon: "envelope.fill", disabled: !hasEmail) {
                            if let email = node.email,
                               let url = URL(string: "mailto:\(email)") {
                                UIApplication.shared.open(url)
                            }
                        }
                        ActionButton(title: "Контакт", icon: "person.badge.plus", disabled: !hasPhone && !hasEmail) {
                            showContacts = true
                        }
                    }
                    .padding(.horizontal)
                }
                .padding(.bottom, 32)
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Затвори") { dismiss() }
                }
            }
        }
        .fullScreenCover(isPresented: $showPhoto) {
            if let url = node.imageUrl {
                PhotoViewerView(url: url)
            }
        }
        .sheet(isPresented: $showContacts) {
            AddContactSheet(node: node)
        }
    }
}

// MARK: - Помощни компоненти в EmployeeDetailView

private struct ContactRow: View {
    let icon: String
    let label: String
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .foregroundColor(.blue)
                .frame(width: 24)
            Text(label)
                .font(.body)
            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        Divider().padding(.leading, 52)
    }
}

private struct ActionButton: View {
    let title: String
    let icon: String
    let disabled: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(spacing: 4) {
                Image(systemName: icon)
                    .font(.title3)
                Text(title)
                    .font(.caption2)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 10)
            .background(disabled ? Color(.systemGray5) : Color.blue.opacity(0.1))
            .foregroundColor(disabled ? .secondary : .blue)
            .clipShape(RoundedRectangle(cornerRadius: 10))
        }
        .disabled(disabled)
    }
}

// MARK: - PhotoViewerView

struct PhotoViewerView: View {
    let url: URL
    @Environment(\.dismiss) private var dismiss

    @State private var scale: CGFloat = 1.0
    @State private var lastScale: CGFloat = 1.0

    var body: some View {
        ZStack(alignment: .topTrailing) {
            Color.black.ignoresSafeArea()

            AsyncImage(url: url) { phase in
                switch phase {
                case .success(let img):
                    img.resizable()
                        .aspectRatio(contentMode: .fit)
                        .scaleEffect(scale)
                        .gesture(
                            MagnificationGesture()
                                .onChanged { v in scale = max(1, lastScale * v) }
                                .onEnded   { _ in lastScale = scale }
                        )
                        .onTapGesture(count: 2) {
                            withAnimation { scale = scale > 1 ? 1 : 2.5; lastScale = scale }
                        }
                case .empty:
                    ProgressView().tint(.white)
                default:
                    Image(systemName: "photo")
                        .font(.largeTitle)
                        .foregroundColor(.white)
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)

            Button {
                dismiss()
            } label: {
                Image(systemName: "xmark.circle.fill")
                    .font(.title)
                    .foregroundColor(Color.white.opacity(0.8))
                    .padding()
            }
        }
    }
}

// MARK: - AddContactSheet (CNContactViewController)

struct AddContactSheet: UIViewControllerRepresentable {
    let node: TreeNode
    @Environment(\.dismiss) private var dismiss

    func makeUIViewController(context: Context) -> UINavigationController {
        let contact = CNMutableContact()
        // Разделяне на името на части
        let parts = (node.text ?? "").split(separator: " ", maxSplits: 1).map(String.init)
        contact.givenName  = parts.first ?? (node.text ?? "")
        contact.familyName = parts.count > 1 ? parts[1] : ""
        if let dlag = node.dlag { contact.jobTitle = dlag }
        if let pod  = node.pod  { contact.organizationName = pod }
        if let gsm  = node.gsm, !gsm.isEmpty {
            contact.phoneNumbers = [CNLabeledValue(label: CNLabelWork,
                                                   value: CNPhoneNumber(stringValue: gsm))]
        }
        if let email = node.email, !email.isEmpty {
            contact.emailAddresses = [CNLabeledValue(label: CNLabelWork,
                                                     value: email as NSString)]
        }
        let vc = CNContactViewController(forNewContact: contact)
        vc.delegate = context.coordinator
        return UINavigationController(rootViewController: vc)
    }

    func updateUIViewController(_ uiViewController: UINavigationController, context: Context) {}

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    class Coordinator: NSObject, CNContactViewControllerDelegate {
        let parent: AddContactSheet
        init(_ parent: AddContactSheet) { self.parent = parent }
        func contactViewController(_ viewController: CNContactViewController,
                                   didCompleteWith contact: CNContact?) {
            parent.dismiss()
        }
    }
}
