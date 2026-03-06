// Persistence.swift — данни, мрежа, кеш, хранилище, viewmodels

import Foundation
import Combine

// MARK: - API response wrappers

struct ApiResponse: Decodable {
    let root: TreeNode?
    enum CodingKeys: String, CodingKey { case root = "items" }
}

struct SearchApiResponse: Decodable {
    let data: SearchData?
}

struct SearchData: Decodable {
    let items: [TreeNode]?
}

// MARK: - TreeNode

struct TreeNode: Codable, Identifiable {
    let nodeId: Int?
    let text:    String?
    let leaf:    Bool
    let gsm:     String?
    let email:   String?
    let pod:     String?
    let pict:    String?
    let glavpod: Int?
    let dlag:    String?
    let children: [TreeNode]?

    enum CodingKeys: String, CodingKey {
        case nodeId = "id"
        case text, leaf, gsm, email, pod, pict, glavpod, dlag
        case children = "items"
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        nodeId   = try c.decodeIfPresent(Int.self,    forKey: .nodeId)
        text     = try c.decodeIfPresent(String.self, forKey: .text)
        leaf     = (try? c.decode(Bool.self,          forKey: .leaf)) ?? false
        gsm      = try c.decodeIfPresent(String.self, forKey: .gsm)
        email    = try c.decodeIfPresent(String.self, forKey: .email)
        pod      = try c.decodeIfPresent(String.self, forKey: .pod)
        pict     = try c.decodeIfPresent(String.self, forKey: .pict)
        glavpod  = try c.decodeIfPresent(Int.self,    forKey: .glavpod)
        dlag     = try c.decodeIfPresent(String.self, forKey: .dlag)
        children = try c.decodeIfPresent([TreeNode].self, forKey: .children)
    }

    // За Identifiable — ключ по-уникален от nodeId сам по себе си
    var id: String { "\(nodeId ?? -1)|\(text ?? "")|" + (pod ?? "") }

    var imageUrl: URL? {
        guard let pict = pict, !pict.isEmpty, let glavpod = glavpod else { return nil }
        return URL(string: "https://vasil.iag.bg/upload/\(glavpod)/\(pict)")
    }

    var isEmployee: Bool { leaf || (children?.isEmpty ?? true) }
}

// MARK: - Кеш (UserDefaults)

enum CacheStore {
    static func save(key: String, data: Data) {
        UserDefaults.standard.set(data, forKey: "cache_\(key)")
    }
    static func load(key: String) -> Data? {
        UserDefaults.standard.data(forKey: "cache_\(key)")
    }
}

// MARK: - API Service

enum APIError: Error {
    case badURL
}

struct APIService {
    private static let base = "https://vasil.iag.bg/"
    private let session = URLSession.shared
    private let decoder = JSONDecoder()

    private func fetch<T: Decodable>(_ path: String) async throws -> T {
        guard let url = URL(string: Self.base + path) else { throw APIError.badURL }
        let (data, _) = try await session.data(from: url)
        return try decoder.decode(T.self, from: data)
    }

    func getIag() async throws -> [TreeNode] {
        let r: ApiResponse = try await fetch("tel/v7/iag_empl")
        return r.root.map { [$0] } ?? []
    }
    func getRdg() async throws -> [TreeNode] {
        let r: ApiResponse = try await fetch("tel/v7/rdg_empl")
        return r.root.map { [$0] } ?? []
    }
    func getDp() async throws -> [TreeNode] {
        let r: ApiResponse = try await fetch("tel/v7/dp_dgs_empl")
        return r.root.map { [$0] } ?? []
    }
    func searchByName(first: String, last: String) async throws -> [TreeNode] {
        let f = first.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? first
        let l = last.addingPercentEncoding(withAllowedCharacters:  .urlQueryAllowed) ?? last
        let r: SearchApiResponse = try await fetch("all_empl/imeAndFam?strIme=\(f)&strFam=\(l)")
        return r.data?.items ?? []
    }
    func searchByGSM(_ gsm: String) async throws -> [TreeNode] {
        let g = gsm.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? gsm
        let r: SearchApiResponse = try await fetch("all_empl/byGSM?number=\(g)")
        return r.data?.items ?? []
    }
}

// MARK: - Резултат от заявка

enum FetchResult<T> {
    case success(T)
    case error(Error, T?)
}

// MARK: - Repository

class EmployeeRepository {
    private let api     = APIService()
    private let decoder = JSONDecoder()
    private let encoder = JSONEncoder()

    func getIag() async -> FetchResult<[TreeNode]> { await tree(key: "iag_data")  { try await self.api.getIag() } }
    func getRdg() async -> FetchResult<[TreeNode]> { await tree(key: "rdg_data")  { try await self.api.getRdg() } }
    func getDp()  async -> FetchResult<[TreeNode]> { await tree(key: "dp_data")   { try await self.api.getDp()  } }

    func searchByName(first: String, last: String) async -> FetchResult<[TreeNode]> {
        await tree(key: "search_\(first)_\(last)") { try await self.api.searchByName(first: first, last: last) }
    }
    func searchByGSM(_ gsm: String) async -> FetchResult<[TreeNode]> {
        await tree(key: "search_gsm_\(gsm)") { try await self.api.searchByGSM(gsm) }
    }

    private func tree(key: String, fetch: () async throws -> [TreeNode]) async -> FetchResult<[TreeNode]> {
        do {
            let items = try await fetch()
            if !items.isEmpty, let data = try? encoder.encode(items) {
                CacheStore.save(key: key, data: data)
            }
            return .success(items)
        } catch {
            let cached = CacheStore.load(key: key)
                .flatMap { try? decoder.decode([TreeNode].self, from: $0) }
            return .error(error, cached)
        }
    }
}

// MARK: - DataSource enum

enum DataSource: String, CaseIterable {
    case iag = "ИАГ"
    case rdg = "РДГ"
    case dp  = "ДП"
}

// MARK: - TreeViewModel

@MainActor
class TreeViewModel: ObservableObject {
    @Published var items:     [TreeNode] = []
    @Published var isLoading: Bool       = false
    @Published var message:   String?    = nil

    private let repo = EmployeeRepository()

    func load(source: DataSource) {
        Task {
            isLoading = true
            message   = nil

            let result: FetchResult<[TreeNode]>
            switch source {
            case .iag: result = await repo.getIag()
            case .rdg: result = await repo.getRdg()
            case .dp:  result = await repo.getDp()
            }

            switch result {
            case .success(let data):
                items = data
            case .error(_, let cached):
                if let cached {
                    items   = cached
                    message = "Офлайн – кеширани данни"
                } else {
                    message = "Грешка при зареждане. Проверете връзката."
                }
            }
            isLoading = false
        }
    }
}

// MARK: - SearchViewModel

@MainActor
class SearchViewModel: ObservableObject {
    @Published var results:   [TreeNode] = []
    @Published var isLoading: Bool       = false
    @Published var message:   String?    = nil

    private let repo = EmployeeRepository()
    private var searchTask: Task<Void, Never>?

    // Търсене по "Иван Иванов" или по ГСМ (мин. 5 цифри)
    private let nameRegex = try! NSRegularExpression(pattern: "^\\p{L}+\\s\\p{L}+$")
    private let gsmRegex  = try! NSRegularExpression(pattern: "^\\d{5,}$")

    func search(_ query: String) {
        searchTask?.cancel()
        let q = query.trimmingCharacters(in: .whitespaces)
        guard q.count >= 3 else {
            results = []
            message = nil
            return
        }

        searchTask = Task {
            try? await Task.sleep(nanoseconds: 500_000_000)
            guard !Task.isCancelled else { return }

            isLoading = true
            message   = nil

            let range = NSRange(q.startIndex..., in: q)
            let result: FetchResult<[TreeNode]>

            if nameRegex.firstMatch(in: q, range: range) != nil {
                let parts = q.split(separator: " ", maxSplits: 1).map(String.init)
                result = await repo.searchByName(first: parts[0], last: parts[1])
            } else if gsmRegex.firstMatch(in: q, range: range) != nil {
                result = await repo.searchByGSM(q)
            } else {
                results   = []
                isLoading = false
                return
            }

            switch result {
            case .success(let data):
                results = data
            case .error(_, let cached):
                if let cached {
                    results = cached
                    message = "Офлайн – кеширани резултати"
                } else {
                    results = []
                    message = "Неуспешно търсене. Проверете връзката."
                }
            }
            isLoading = false
        }
    }
}
