# **The Goal**

![Sample](https://github.com/sergiocasero/sweet_home_3d_ha_lightning/raw/main/media/sample.gif "Sample")

The goal of this plugin is to generate all the possible combinations of images of a plane created in Sweet Home 3D to, in real time, see the status of the lights in the home assistant.

To achieve this we need to generate a yaml file with the structure of "picture-elements" and generate all the possible combinations of images based on the lights, for example, if we have 3 lights "living_room, kitchen, bed_room", we need to generate:

| light.living_room | light.kitchen | light.bedroom |
| ------------ | ------------ | ------------ |
| 0 | 0 | 0 |
| 0 | 0 | 1 |
| 0 | 1 | 0 |
| 0 | 1 | 1 |
| 1 | 0 | 0 |
| 1 | 0 | 1 |
| 1 | 1 | 0 |
| 1 | 1 | 1 |

As we can see, for 3 lights, there are 2^3 possible combinations, if we have 10 lights, there would be 2^10 combinations -> 1024 images... and **we don't want to generate all the images manually!!**

# **How to use it:**
1. Download the latest release in .jar and copy to Sweet Home 3D "plugins" folder (under MACOS it's the subfolder "Library/Application Support/eTeks/Sweet Home 3D/plugins" of your user folder)
2. Give the lights the same name as in Home Assistant, for example, light.living_room
3. Go to Tools -> Home Assistant Lightning
4. There are 4 modifiable fields:
- Path: Path where the images will be generated
- Image Width: Width of the images to generate (recommended: 380)
- Image Height: Height of the images to generate (recommended: 285)
- Quality: Image quality (recommended: high)
5. Generate images
6. Move the images to /config/www/planes
7. Create a card of type picture-elements and paste the generated yaml.
9.Enjoy!!

# **TODOs**

- Real-time display of the generation status
- Migrate project to IntelliJ + kotlin?
- Error handling
- Refactorize the code, now it's a botched job
