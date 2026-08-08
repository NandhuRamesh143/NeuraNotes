# Neuracet Notes

A modern, minimalistic note-taking Android app built specifically for the Neuracet AI club. The app is designed to streamline meeting notes by automatically generating AI summaries and publishing them directly to our Discord server.

## 🚀 Features

- **Google Authentication:** Secure, one-tap sign-in using Firebase Auth.
- **Cloud Sync:** All notes are instantly saved and synced across devices using Cloud Firestore.
- **AI Summarization:** Powered by the Gemini API, the app automatically generates concise, highly detailed summaries of your meeting notes the moment you save them.
- **Discord Integration:** A single tap sends the note's title, AI summary, and a snippet of the full text directly to a designated Discord channel.

## 🛠 Tech Stack

### Android App
- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Database:** Firebase Cloud Firestore
- **Authentication:** Firebase Auth (Google Sign-In)
- **Networking:** Retrofit & OkHttp
- **Architecture:** MVVM with Coroutines & StateFlow

### Backend (Discord Webhook)
- **Environment:** Node.js (Express)
- **Deployment:** Vercel (Serverless Functions)

## 💻 Local Setup

If you want to build and run this project locally, you'll need to set up a few things:

### 1. Firebase Configuration
You need your own Firebase project to handle the database and authentication.
1. Create a project in the Firebase Console.
2. Enable **Firestore** and **Google Sign-In** (under Authentication).
3. Add an Android app to your project and download the `google-services.json` file.
4. Place the `google-services.json` file inside the `app/` directory of this repository.
5. Make sure to add your machine's SHA-1 fingerprint to the Firebase Console so Google Sign-In works on your emulator/device.

### 2. Environment Variables
Create a file named `.env` in the root directory (or update your `local.properties`) and add your Gemini API key so the summarization engine works:
```properties
GEMINI_API_KEY="your_api_key_here"
```

### 3. Backend Deployment
To avoid paying for Firebase Cloud Functions, the Discord webhook router is built as a standalone Express server.
1. Navigate to the `functions/` folder.
2. Use the Vercel CLI to deploy it: `npx vercel --prod`
3. Add your Discord Webhook URL to Vercel: `npx vercel env add DISCORD_WEBHOOK_URL`
4. Copy the production URL Vercel gives you and replace the hardcoded URL in `NotesViewModel.kt`.

## 🤝 Contributing
Feel free to open issues or submit pull requests if you want to improve the app or add new features for the club.
