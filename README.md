# MOys
Platform-independent static OS
## Features
- System doesn't have any real loops - it fully relies on built view and callbacks
- All apps run in a view tree
- Supports custom app type from JARs
## Inspiration
This system is inspired by some aspects of Android. Also it heavilly relies on alive-in-moment principe: app builds it's UI on start and only needs to update it on user callbacks.
## How to run
### Linux
1. Download latest OS version from releases
2. Install Java
``` bash
sudo apt update
sudo apt install default-jdk
```
3. Install X11 (or use Wayland)
``` bash
sudo apt install xorg
```
4. Navigate to OS folder and run this command
``` bash
java -cp ".:*" MainSystemServiceKt
```
## Windows
1. Download latest OS version from releases
2. Install WSL
``` bash
wsl --install
```
3. Open WSL and try running xeyes (to check if you have X11 support natively). If an error appears install [VcXsrv](https://sourceforge.net/projects/vcxsrv/), configure and run it. Then click on it in taskbar and click "Open log". There find `DISPLAY=some_ip`. After that paste this in WSL (don't forget to replace IP): `export DISPLAY=that_ip`
4. Install Java
``` bash
sudo apt update
sudo apt install default-jdk
```
5. Navigate to OS folder and run this command
``` bash
java -cp ".:*" MainSystemServiceKt
```
## Building
IntelliJ Idea is recommended to build this project. Directory `system` contains the OS wrapper itself. While `sdk` contains SDK to build your own apps.
### Build OS
Clone this repository and open it in IntelliJ Idea. Wait for the Gradle to sync and build the project and after that proceed to `Build->Build artefacts`. There build systemClasses.MainSystemService. After that do what is said in previous chapter to launch it (built OS is in folder `out/artefacts`).
### Creating your own app
Build SDK from this repository (previos sub-chapter but build other artefact).  
Then create new IntelliJ Idea **KOTLIN** project with **JAVA 17** and select **GRADLE** as project builder. If you don't follow this instruction your project will be built but final app won't be usable.  
Wait for Gradle to sync and build. Add new folder called `libs` and add your built SDK there.  
Add this to your `build.gradle.kts`:
``` kotlin
dependencies {
   compileOnly(files("libs/sdk.jar")
}
```
Resync Gradle. Create your own Kotlin class that inherites from Activity. Here's an example fot it:
``` kotlin
class ExampleApp(
    override val gs: GraphicServiceI,
    override val storage: StorageServiceI,
    override val deviceManager: DeviceManagerI
) : Activity {
    override fun main() {
        gs.setContent(true){
            Text(modifier = Modifier.fillMaxSize(), text = "Hello world", textSize = 14, textColor = Color.PINK, parent = this)
        }
        gs.redraw()
    }
}
```
Create an artefact for your app in `Files->Project Structure->Artefacts` (click +, specify your app class (yourapp.main) an click OK). Build the artefacts.  
Find your built app, add icon and manifest.json to it. Manifest example:
``` json
{
  "app_id": "com.example.your_app_id",
  "app_name": "My App",
  "version": "1.0.0",
  "icon_file_name": "icon.png",
  "jar_file_name": "MyApp.jar",
  "activity_name": "MainActivity",
  "libs": []
}
```
Pack these 3 files into zip and change the extension to `jarp`. Your app is ready.
