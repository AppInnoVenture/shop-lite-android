# 🛍️ ShopLite - E-Commerce Product Catalog & Offline Cart

ShopLite is a modern Android e-commerce application. It fetches data from the [DummyJSON API](https://dummyjson.com/docs/products), provides robust offline cart capabilities, and features a clean, edge-to-edge Jetpack Compose UI.

## 📱 Features

* **Product Catalog:** Browse a rich list of products with images, names, prices, and ratings.
* **Smart Search & Filtering:** Debounced global search combined with local category filtering and sorting (Price, Rating, Discount).
* **Detailed Product Views:** Deep dive into product details, including image carousels, stock status, and user reviews.
* **Offline-First Shopping Cart:** Fully functional cart that persists locally. Users can view items, update quantities, delete items, and see accurate subtotals without an internet connection.
* **Robust Offline Support:** Product catalog and images are cached locally. The app seamlessly transitions between online and offline states without jarring flashes or crashes.
* **Bonus Features:** Liked Products (Wishlist), Dark/Light Mode, and Local Currency Conversion.

---

## 🛠️ Architecture Used

The application strictly follows **Clean Architecture** combined with the **MVVM (Model-View-ViewModel)** presentation pattern to enforce the Separation of Concerns (SoC) and SOLID principles.

* **Presentation Layer (`presentation/`)**: Contains Jetpack Compose UI screens, ViewModels, and State classes. ViewModels communicate strictly with the Domain layer using unidirectional data flow.
* **Domain Layer (`domain/`)**: The core business logic containing pure Kotlin Models, Repository Interfaces, and Use Cases. I utilized the **Use Case Wrapper Pattern** (e.g., `CatalogUseCases`, `CartUseCases`) to group related operations and prevent constructor bloat.
* **Data Layer (`data/`)**: Implements the repository interfaces. Contains Room database Entities/DAOs, Retrofit API endpoints, DTOs, and Network Connectivity Managers.

---

## 📦 Libraries Used

* **UI:** Jetpack Compose (Material 3), Navigation Compose (Type-safe routing).
* **Concurrency & Reactive Streams:** Kotlin Coroutines & Flow.
* **Dependency Injection:** Dagger Hilt.
* **Networking:** Retrofit2 & OkHttp3 (with Logging Interceptor).
* **Local Persistence:** Room Database (SQLite) & DataStore (Preferences).
* **Image Loading:** Coil (configured with OkHttp disk caching for offline image viewing).
* **Splash Screen:** AndroidX Core Splashscreen API.

---

## 💾 Local Storage Approach

To achieve the "Offline-First" requirement, **Room Database** acts as the Single Source of Truth (SSOT). 

1. **The Cart:** The `CartScreen` does not rely on network requests. `CartViewModel` continually observes a `Flow<List<CartItem>>` directly from Room. When a user adds an item or changes a quantity, it updates the local database, which immediately pushes the new state to the UI.
2. **The Catalog:** When the app launches, it fetches the catalog from DummyJSON and inserts the entities into Room (`ProductDao`). The UI observes the local database. If the internet drops, the app gracefully falls back to the locally cached catalog.
3. **Preferences:** Android `DataStore` is used to persist lightweight user settings like Theme (Light/Dark) and Currency preferences.

---

## 🧠 Important Design Decisions

1. **Race Condition Prevention (No Flash UX):** 
   A common issue with offline-first apps is the UI briefly flashing a "No Internet" error before the local database emits its data. This is mitigated using an `isDbInitialized` state. The UI remains in a loading state until Room officially emits its first payload, guaranteeing an error is only shown if the database is genuinely empty *and* the network is down.
2. **Combined Search & Category Logic:**
   The DummyJSON API does not natively support combining a search query (`/search?q=`) with a category filter (`/category/`). To provide a premium UX, the app delegates the heavy lifting of the global search to the API, and then performs the category and sorting filters *locally* on the device via Coroutines.
3. **Edge-to-Edge UI with Translucent Navigation:**
   To provide a modern, immersive experience, `enableEdgeToEdge()` is used. Padding isn't applied blindly to containers; instead, standard `WindowInsets` and `contentPadding` are passed into `LazyColumn`/`LazyVerticalGrid` so lists scroll beautifully *behind* the translucent system navigation bars.
4. **Offline Image Caching:**
   Coil is configured at the Application level with a custom `OkHttpClient` and Disk Cache. Once a product image is loaded online, it remains visible even when the device is in Airplane mode.

---

## ⚠️ Known Limitations

* **Pagination:** The DummyJSON API supports `limit` and `skip` pagination. For the scope of this project, the app fetches a generous default batch of products rather than implementing a complex infinite-scrolling Paging3 architecture, prioritizing core cart functionality and stability.
* **Currency Exchange Rates:** The currency converter in Settings uses static, hardcoded multiplier rates for demonstration purposes, rather than connecting to a live forex API.
* **Stock Validation:** Because the cart operates offline, a user could theoretically add an item to their cart offline that becomes "Out of Stock" on the server. The cart relies on the last-known cached stock value.

---

## 🚀 Setup & Build Instructions

1. Clone the repository to your local machine.
2. Open the project in **Android Studio** (Koala or newer recommended).
3. Let Gradle sync and download all required dependencies.
4. Build and run the app on an Emulator or Physical Device running **Minimum API 24** (Android 7.0) or higher.
5. *Testing Offline Mode:* Load the app once while online to populate the catalog. Turn on Airplane Mode, close the app from recent tasks, and reopen it. Browse products, view images, and modify your cart seamlessly.