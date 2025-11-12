# 🎭 NPCSystem

> A simple and feature-rich NPC system for Minecraft 1.21.10+ using the new Mannequin's.

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.10+-brightgreen.svg)](https://www.minecraft.net/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![Spigot](https://img.shields.io/badge/Spigot-Compatible-yellow.svg)](https://www.spigotmc.org/)

---

## ✨ Features

- 🧍 **Mannequin-Based NPCs** - Uses Minecraft's native Mannequin entities introduced in 1.21.9
- 🎨 **Custom Skins** - Apply player skins via Mojang API with texture caching
- 🪞 **Mirror Skin Mode** - NPCs can dynamically show the viewer's own skin
- 💬 **Dynamic Holograms** - Up to 3 lines of customizable floating text above NPCs
- 👀 **Eye Contact** - NPCs can track and look at nearby players in real-time
- ⚡ **Interactive Actions** - Configure NPCs to execute commands, open GUIs or send players to other servers
- 💾 **Dual Storage** - Choose between YAML files or MySQL database
- 🔒 **Immovable NPCs** - NPCs stay in place with no AI, gravity or collision

---

## 📋 Requirements

| Requirement | Version |
|-------------|---------|
| 🎮 Minecraft | 1.21.10+ |
| 🖥️ Server Software | Spigot, Paper or compatible fork |
| ☕ Java | 21 or higher |
| 📦 HikariCP | 6.2.1 |
| 🗄️ MySQL Connector | 9.1.0 |

---

## 📥 Installation

1. ⬇️ Download `NPCSystem-1.2.jar` from releases
2. 📚 Download required libraries:
   - [HikariCP-6.2.1.jar](https://repo1.maven.org/maven2/com/zaxxer/HikariCP/6.2.1/HikariCP-6.2.1.jar)
   - [mysql-connector-j-9.1.0.jar](https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.1.0/mysql-connector-j-9.1.0.jar)
3. 📂 Place all three JARs in your `plugins/` folder
4. 🚀 Start your server to generate the configuration
5. ⚙️ Configure `plugins/NPCSystem/config.yml`
6. 🔄 Restart your server

---

## 🎮 Commands & Usage

### 📌 Help Command

When you type `/npc help` or just `/npc`, you'll see:

```
§6§l=== NPC Commands ===
§e/npc create <id> §7- Create a new NPC
§e/npc delete <id> §7- Delete an NPC
§e/npc skin <id> <player|mirror> §7- Set NPC skin
§e/npc hologram <id> <add|remove|clear> §7- Manage hologram
§e/npc action <id> <type> <value> §7- Set NPC action
§e/npc eyecontact <id> <true|false> §7- Toggle eye contact
§e/npc list §7- List all NPCs
§e/npc teleport <id> §7- Teleport to NPC
```

### 🎪 Example: Creating a Shop NPC

Let's create a complete shop NPC step by step:

```
Step 1: Create the NPC at your location
/npc create shopkeeper

Step 2: Give it a custom skin
/npc skin shopkeeper Notch

Step 3: Add a hologram above it
/npc hologram shopkeeper add &6&l⚡ Shop Keeper ⚡
/npc hologram shopkeeper add &7Right-click to browse
/npc hologram shopkeeper add &a&o↓ Click me! ↓

Step 4: Make it execute a shop command when clicked
/npc action shopkeeper command shop open %player%

Step 5: Enable eye contact so it looks at players
/npc eyecontact shopkeeper true
```

**Result:** 🎉 You now have a fully functional shop NPC that:
- ✅ Has Notch's skin
- ✅ Displays a 3-line hologram with colors
- ✅ Opens a shop when clicked
- ✅ Follows nearby players with its eyes

---

## 🎨 Hologram Formatting

Holograms support **Minecraft color codes** using `&`:

| Code | Color | Example |
|------|-------|---------|
| `&0` | Black | `&0Black Text` |
| `&1` | Dark Blue | `&1Dark Blue Text` |
| `&2` | Dark Green | `&2Dark Green Text` |
| `&3` | Dark Aqua | `&3Dark Aqua Text` |
| `&4` | Dark Red | `&4Dark Red Text` |
| `&5` | Dark Purple | `&5Dark Purple Text` |
| `&6` | Gold | `&6Gold Text` |
| `&7` | Gray | `&7Gray Text` |
| `&8` | Dark Gray | `&8Dark Gray Text` |
| `&9` | Blue | `&9Blue Text` |
| `&a` | Green | `&aGreen Text` |
| `&b` | Aqua | `&bAqua Text` |
| `&c` | Red | `&cRed Text` |
| `&d` | Light Purple | `&dLight Purple Text` |
| `&e` | Yellow | `&eYellow Text` |
| `&f` | White | `&fWhite Text` |
| `&l` | **Bold** | `&lBold Text` |
| `&o` | *Italic* | `&oItalic Text` |
| `&n` | <u>Underline</u> | `&nUnderlined Text` |
| `&m` | ~~Strikethrough~~ | `&mStrikethrough Text` |

### 🌟 Example Holograms

```
Welcome NPC
/npc hologram welcome add &6&l✦ &e&lWELCOME &6&l✦
/npc hologram welcome add &7To our amazing server!
/npc hologram welcome add &a&oClick for starter kit

Quest NPC
/npc hologram quest add &5&l⚔ Quest Master ⚔
/npc hologram quest add &d&oNew quests available!

Teleporter NPC
/npc hologram portal add &b&l➤ Spawn Teleporter ➤
/npc hologram portal add &3Right-click to teleport
```

---

## ⚙️ Configuration

### config.yml

```
storage:
# Storage type: YAML or MYSQL
type: YAML

# MySQL settings (only used if type is MYSQL)
mysql:
host: localhost
port: 3306
database: minecraft
username: root
password: ''

defaults:
hologram-distance: 10.0       # Distance at which holograms are visible
interaction-distance: 5.0     # Maximum distance to interact with NPC
eye-contact-range: 10.0       # Range at which NPC tracks players
```

---

## 🎯 Action Types

Configure what happens when a player **right-clicks** an NPC:

| Action Type | Description | Example |
|-------------|-------------|---------|
| 🚫 **NONE** | No action | `/npc action npc1 none` |
| ⚙️ **COMMAND** | Execute console command | `/npc action npc1 command give %player% diamond 1` |
| 📦 **GUI** | Open a GUI *(coming soon)* | `/npc action npc1 gui shop` |
| 🌐 **SERVER** | Send to BungeeCord server | `/npc action npc1 server lobby` |

### 💡 Command Examples

```
Give player 64 diamonds
/npc action reward command give %player% diamond 64

Teleport player to spawn
/npc action spawn command spawn %player%

Broadcast a message
/npc action announcer command say %player% clicked the announcer!

Send player to lobby server (BungeeCord)
/npc action portal server lobby

Send player to minigames server
/npc action minigames server games
```

---

## 🔐 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `npc.admin` | 👑 Full access to all NPC commands | OP |
| `npc.create` | ➕ Create NPCs | OP |
| `npc.delete` | ❌ Delete NPCs | OP |
| `npc.skin` | 🎨 Change NPC skins | OP |
| `npc.hologram` | 💬 Manage holograms | OP |
| `npc.action` | ⚡ Set NPC actions | OP |
| `npc.eyecontact` | 👀 Toggle eye contact | OP |
| `npc.list` | 📋 List all NPCs | OP |
| `npc.teleport` | 🚀 Teleport to NPCs | OP |

---

## 🛠️ Building from Source

### Requirements

- ☕ Java Development Kit (JDK) 21+
- 🐘 Gradle 8.0+

### Build Steps

```
git clone https://github.com/Sypherox/NPCSystem.git
cd NPCSystem
./gradlew clean build
```

📦 The compiled JAR will be in `build/libs/NPCSystem-1.2.jar`

---

## 🐛 Troubleshooting

### ❌ NPCs not spawning

- ✅ Ensure you're running Minecraft **1.21.9 or higher**
- ✅ Check server console for errors
- ✅ Verify world name in storage matches actual world name

### 🎨 Skins not loading

- ✅ Check internet connection (plugin fetches skins from Mojang API)
- ✅ Verify player name is correct and exists
- ✅ Check console for API errors or rate limiting

### 🗄️ MySQL connection errors

- ✅ Verify MySQL credentials in `config.yml`
- ✅ Ensure MySQL server is running and accessible
- ✅ Check that database exists and user has proper permissions
- ✅ Test connection with MySQL client first

---

## 💬 Support

Need help? Found a bug? Have a suggestion?

- 🐛 **GitHub Issues**: [Report a bug](https://github.com/Sypherox/NPCSystem/issues)
- 💬 **Discord**: [Join our Discord](https://dsc.gg/sypherox)

---

## 📜 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

---

## 🎉 Credits

- **👨‍💻 Developer**: Sypherox
- **🎮 Minecraft Version**: 1.21.10
- **🔧 Built with**: Spigot API, HikariCP, MySQL Connector
- **💡 Special Thanks**: Mojang for the Mannequin entity type!

---

## 📝 Changelog

### 🚀 Version 1.2 (Current)

- ✨ Initial release
- 🧍 Mannequin-based NPC system
- 🎨 Skin fetching via Mojang API with texture caching
- 💬 Dynamic holograms (up to 3 lines)
- 👀 Eye contact system with real-time tracking
- ⚡ Multiple action types (command, GUI & server)
- 💾 Dual storage support (YAML/MySQL)
- 🎮 Complete command system with tab completion
- 🔒 Immovable & invulnerable NPCs

---

<div align="center">

**⭐ If you like this plugin, please give it a star! ⭐**

Made with ❤️ by [Sypherox](https://github.com/Sypherox)

</div>