# dj-link-now-playing
Sniff Pioneer DJ-Link traffic to write out the currently playing track's metadata in real time, for easy ingestion into displays/overlays such as in OBS or other streaming studios.

# Requirements
* Two Pioneer DJ-LINK capable decks 
    * e.g. CDJ-2000nexus, CDJ-2000NXS2, XDJ-1000, others
* Pioneer DJ-LINK capable mixer 
    * e.g. DJM-900NXS, DJM-900NXS2 (this is required in order to figure out which player is currently audible ("on-air"))
* A computer to run this program, connected to the same network as your Pioneer gear (LAN preferred, WiFi seems to work too)

# Installation
1. Clone the repo:
```
git clone git@github.com:aphexcx/dj-link-now-playing.git
```

2. Run with gradle:
```
$ ./gradlew run
```

If all is well, you'll see this output!
```
Starting a Gradle Daemon, 2 incompatible Daemons could not be reused, use --status for details

> Task :run

New device: DJM-900NXS2
New device: CDJ-2000NXS2
New device: CDJ-2000NXS2
VirtualCdj started as device 1
Track(id=464, title=Acme, artist=Adip Kiyoi, Khairy Ahmed, art=AlbumArt[artReference=DataReference[player:3, slot:USB_SLOT, rekordboxId:448], size=1535 bytes], precedingTrackPlayedAtBpm=null)
```

# OBS Setup
Now that you've got it running, you just need to set up your overlays to read from the files we're generating.
## Artist names
1. Add a **Text** element with the following settings:

![OBS track title setting](https://i.imgur.com/jIyQSiW.png)

2. Make sure the *Read from file* option is checked and that it's reading from the `<output-folder>/nowplaying-artist.txt` file.

## Track titles
Follow the **Artist names** instructions, but use the `<output-folder>/nowplaying-track.txt` file.

## Album Art
1. Add an **Image Slide Show** with the following settings:

![OBS Image Slide Show settings](https://i.imgur.com/zbs6cEh.png)

2. You'll need to make sure to add the `<output-folder>/art/` folder to the Image Files list. Make sure there's nothing else in that list.

# Configuration
The configuration file is located in `config.yml`.
Here's an exhaustive list of configuration options and their explanations. 

```yaml
# All files (track title, artist, album art, tracklists etc) will be continuously written and updated in this directory.
# Note that art will be written to the art/ subfolder here so you can use the folder with OBS's Image Slide Show.
output-folder: "/Users/user_name/obs/"

# This is what now-playing will show while there is no track playing or while there are multiple tracks playing (e.g. when you're in the mix.)
empty-track:
  artist: "QUARANTRANCE • Episode #6"
  title: "twitch.tv/aphexcx"

# The path to an image file that will be shown for the empty track.
empty-track-album-art-path: "/Users/afik_cohen/obs/image.png"

# We'll strip these out of track titles when displayed, to make things a bit cleaner.
remove-these-from-track-titles: [ " - Extended Mix",
                                  " (Extended Mix)",
                                  "(Extended Mix)" ]
```

# Caveats
* This program was not tested with more than two decks; it probably won't work with three, and definitely not with 4 decks because beat-link, the underlying library I use, acts as a "virtual cdj" and takes up one of the four available player slots on a network
* For that same reason, **you cannot have Rekordbox running at the same time as this program**, because the program tries to use the same port as rekordbox. This may work if you have rekordbox running on a different computer, but I have not tested this. So, just play off of a USB or SD card, not off of rekordbox via DJ-Link.
* It'll probably work on Windows, sure, why not? I only tested this on Mac OSX Catalina though. 🤷‍♀️ Try it and let me know!
