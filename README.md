# Android Technical Test

## Objective

This is a Kotlin-based Android application designed to work seamlessly as it features both offline and online capabilities.
It implements an offline-first architecture, allowing users to create, update, delete, and sync pupil data even without network access.

## Features

- **Offline-First Architecture**: The app is designed to function without an internet connection, storing data locally and syncing when online.
- **Pupil Management**: Users can view, add, update, and delete pupil records.
- **Data Synchronization**: Changes made while offline are synchronized with the server when the device is back online.
- **Error Handling**: The app gracefully handles errors from the API, including network issues and validation errors.
- **User Interface**: A simple and intuitive UI for managing pupil records.
- **Map Integration**: Fetching latLng coordinates with OpenStreetMap, and viewing pupil locations on a map using local map app.
- **Image Upload**: Users can upload images for each pupil, which are stored using cloudinary.
- **Network Interception**: The app includes a network interceptor to cache, retry and log API requests and responses, aiding it's offline capability and in debugging.

### Architecture Overview

MVVM (Model-View-ViewModel) architecture is used to separate concerns and enhance testability. The app consists of:
- **Model**: Represents the data structure and business logic, including data classes for Pupil and API responses.
- **View**: The UI components that display data to the user, such as Activities and Fragments.
- **ViewModel**: Acts as a bridge between the View and Model, handling UI-related data in a lifecycle-conscious way.
- **Repository**: Manages data operations, providing a clean API for data access to the rest of the application. It handles both local database operations and network requests.

### Real World Simulation

The API attempts to simulate real world usage in several ways:

1. Occasionally real web services go down due to any number of reasons. The Technical Test API will occasionally throw errors. Your app will need to deal with this.
2. To simulate bad network connectivity or the server being under intense load, the Technical Test API will sometimes take a few seconds to respond.
3. To simulate other users creating, updating and deleting data the Technical Test API will sometimes create, update or delete pupils from its internal database.

### Design Choices & Assumptions

- **Temporal Data**: The app uses a local database (Room) to temporarily store newly created pupil data, ensuring that users can access records without an internet connection. Data is synced with the server when the network is available and the temporal one is then deleted.
- **Boolean Flags**: Boolean flags are used in the app, this is to indicate the status of pupil records (e.g., 'isNew', 'isSynced'). Which helps in managing the state of data effectively.
- **Error Handling**: Error messages from the API are parsed into a structured model for user-friendly logging and debugging.

## Download and Installation
1. Clone the repository:
   ```bash
   git clone

2. Navigate to the project directory:
   ```bash
   cd android-technical-test
   ```
3. Open the project in Android Studio.

4. Ensure you have the necessary SDKs and dependencies installed.
5. Run the application on an emulator or physical device.
   ```bash
   ./gradlew build
   ```
   
## APK Download
You can download the APK file from the following link: https://drive.google.com/file/d/1sTCYZ0lGpLzR2DtMPQ1MHuhsg36JZ3qR/view?usp=sharing

## Thanks!