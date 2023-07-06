# Image To  Braille

A tool that converts images to Braille text and vice versa.

## How to install
You can clone this repo, copy the contents of ImageToBraille.java or [download the jar file](https://github.com/SkyVerseMc/ImageToBraille/releases/latest).

## How to use it

### As a standalone app

Call the jar file like this:
`java -jar ImageToBraille.jar C:\path\to\image.png <arguments>`

|Argument|Role|
|--|--|
| `<path>` |If the target is a text file, then the output will be an image, otherwise the output will be the braille text.|
|`noinvert`|Dots correspond to dark pixels, used for white backgrounds.
|`width <value> height <value>`|Size the image before processing. You must specify both width and height, not just one of them.
|`contrast <number between 1 and 254>`|Sets the luminance at which the pixel becomes darker or lighter.
|`output <path to file>`|Writes the result to a text file.

You can add a dash before the arguments but this will not change anything.

### As a library
As a util class, you can use static access to methods.

Here are a few examples:
#### Turn an image into Braille text
```java
List<String> lines = ImageToBraille.fromPath("C:/path/to/image.png", true, 128);
```

You can also resize the image:
```java
List<String> lines = ImageToBraille.fromPath("C:/path/to/image2.png", 200, 250, false, 156);
```

Or use an image object:
```java
BufferedImage image = ImageIO.read(new  File("C:/path/to/image3.png");
List<String> lines = ImageToBraille.fromPath(image, false, 156);
```

#### To turn Braille text to image
```java 
String brailleLines = "⠀⠀⠀⠀⠀⠀⢀⣠⣤⣶\n" +
					  "⣀⣀⣤⣴⣾⣿⣿⣿⣿\n";
BufferedImage image = ImageToBraille.fromBraille(brailleLines);
```
Also works if `brailleLines` is a list of string:
```java 
List<String> brailleLines = new ArrayList<String>();
list.add("⠀⠀⠀⠀⠀⠀⢀⣠⣤⣶");
list.add("⣀⣀⣤⣴⣾⣿⣿⣿⣿");
// More lines

BufferedImage image = ImageToBraille.fromBraille(brailleLines);
```
And then you can save it with this (but you can do it differently):
```java
ImageToBraille.save(image, false, "C:/path/to/image.png");
```
