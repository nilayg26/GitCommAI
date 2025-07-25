# 🚀 GitCommAI

<div align="center">
  <img src="https://nilayg26.github.io/Animation/gitcommailogocompressed_11zon.jpg" alt="GitCommAI Logo" width="300"/>
</div>

**Your GitHub Companion**  
News, Chat, AI & GitHub — All Under One Roof
An Android app that brings TechNews, GitHub, AI, and secure chat together seamlessly.

---

## 🚀 How did I do it?

* 🔐 **GitHub OAuth Integration**
  Implemented `GitHub OAuth` using `Firebase` to authenticate users. Successfully linked and extracted user GitHub profile data including public & private repos within the app.

* 🤖 **AI Chatbot with Text Recognition**
  Built a fully responsive `AI Chatbot` using `Google's AI Studio`, integrated with `Google ML Kit’s Text Recognition v2 API` for extracting text from images (supports both live camera input and gallery images).

* 💬 **End-to-End Encrypted Chat**
  Developed a complete `e2e encrypted chat system`:

  * User `search`, `selection`, and `chat`
  * Used `RSA encryption` with preselected prime numbers
  * On-device generated `private-public key pairs` uniquely tied to each `chatroom ID` (never stored or exposed to the server)
  * All `encrypted messages` are securely stored in `Firebase Firestore`.

* 🎨 **Smooth, Stateful UI with Lottie**
  Created a responsive, `stateful` & `state-aware UI` using `Jetpack Compose`.
  Integrated `Lottie animations` fetched from personally hosted `GitHub Pages`, then saved to device `ROM` using `SharedPreferences` to minimize mobile data usage.

* ✅ **Live and Fully Functional**
  The app is `live`, fully `hosted`, and completely `usable`.

---


## 🌟 Potential Impact

This app is designed to **streamline the developer workflow** by seamlessly integrating multiple powerful features:

* 🔗 `GitHub OAuth` for repository tracking and profile linking
* 🤖 `AI Chat Bot` for instant assistance and query resolution
* 🧠 `Text Recognition` to extract code or content from images effortlessly
* 📰 `Tech News` to keep developers updated with the latest trends
* 🔐 `Secure Chat` for encrypted communication and collaboration

Together, these features **empower developers to code, collaborate, and learn more efficiently**—all within a single, unified platform.

---

## ✨ Features
📱 **Modern User Interface**
The UI is modern, supports both Dark and Light modes, and features thoughtfully placed animations.

🔐 **GitHub OAuth Authentication**  
Login securely using your GitHub account.

📰 **Daily Tech News Feed**  
Stay updated with daily tech news from the industry.

💬 **End-to-End RSA Encrypted Chat**  
- Unique key pairs generated per chatroom  
- Each character in every word of a message is being encrypted.  
- Private keys are produced inside the app and servers have no access to it.  

📸 **GitCommAI (AI Chat Bot) + Text Recognition (Powered by Google's ML Kit)**  
Seamlessly integrated into the AI chat page:  
- ⭐️ Chat with AI Bot about code, tech or anything you want!
- 📸 Detects text from Live Captured Photo or from a pic in your Gallery
- 🔎 Detected text is **Auto-Pasted** to your Prompt Text field for instant AI interaction 

👤 **GitHub Profile Integration**  
Get a clean dashboard of your GitHub stats:  
- Avatar, username, public & private repos  
- Followers, following  
- Repository details (name, description, URL, issues enabled, updated time, etc).

## 📸 Some Screenshots

| Login | Tech News Feed | GitChat |
|--------|----------------|---------|
| <img src="https://nilayg26.github.io/Animation/GitCommAISamplePics01.jpeg" width="200"/> | <img src="https://nilayg26.github.io/Animation/GitCommAISamplePics20.jpeg" width="200"/> | <img src="https://nilayg26.github.io/Animation/GitCommAISamplePics14.jpeg" width="200"/> |

| Chat Message | GitCommAI (AI Bot) | GitHub Profile |
|---------------|---------------------|----------------|
| <img src="https://nilayg26.github.io/Animation/GitCommAISamplePics17.jpeg" width="200"/> | <img src="https://nilayg26.github.io/Animation/GitCommAISamplePics10.jpeg" width="200"/> | <img src="https://nilayg26.github.io/Animation/GitCommAISamplePics07.jpeg" width="200"/> |

| Loading Screen | Account Page Direction | News Page Direction |
|----------------|----------------|----------------|
| <img src="https://nilayg26.github.io/Animation/GitCommAISamplePics02.jpeg" width="200"/> | <img src="https://nilayg26.github.io/Animation/GitCommAISamplePics25.jpeg" width="200"/> | <img src="https://nilayg26.github.io/Animation/GitCommAISamplePics26.jpeg" width="200"/> |



## 💬 End To End Encryption (RSA Based) 
<img src="https://nilayg26.github.io/Animation/GitCommAISamplePics21.jpeg" width="500"/>


## 🚀 Use it on your Android device!

First release GitCommAI v1.0.0-alpha is out! Try Yourself!
   [Click to Download latest version of GitCommAI](https://github.com/nilayg26/GitCommAI/releases/download/v1.0.0-alpha/gitcommai-v1.0.0alpha.apk)
  
