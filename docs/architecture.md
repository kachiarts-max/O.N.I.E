# ONIE v0.1 — Architecture

```text
User
  ↓
VoiceManager
  ↓
ONIEBrain
  ↓
Response
  ↓
TextToSpeech
  ↓
User
```

Future architecture:

```text
Wake Word
   ↓
Voice Gateway
   ↓
ONIEBrain
   ├── Memory
   ├── AI Provider
   ├── Intent/Planning
   └── Action Router
          ├── Device
          ├── Apps
          ├── Web
          └── External Services
```

`ONIEBrain` must not directly execute sensitive device actions.
Actions will pass through a permission/security layer.
