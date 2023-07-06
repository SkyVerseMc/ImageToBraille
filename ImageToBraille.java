import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class ImageToBraille {


	public static void main(String[] args) {

		String path;
		boolean invert = true;
		int contrast = 127;
		int width = 0;
		int height = 0;
		boolean writeToFile = false;
		String saveAs = "";

		if (args.length < 1) {

			System.err.println("You must at least specify an image.");
			return;
		}

		path = args[0];

		for (int i = 1; i < args.length; i++) {

			switch (args[i].toLowerCase().replaceAll("-", "")) {

			case "noinvert":
				invert = false;
				break;

			case "width":
				if (args.length > i + 1) {

					width = Integer.parseInt(args[i + 1]);
					i++;

				} else {

					System.err.println("You must specify the resized image's width.");
					return;
				}
				break;

			case "height":
				if (args.length > i + 1) {

					height = Integer.parseInt(args[i + 1]);
					i++;

				} else {

					System.err.println("You must specify the resized image's height.");
					return;
				}
				break;

			case "contrast":
				if (args.length > i + 1) {

					contrast = Integer.parseInt(args[i + 1]);
					i++;

				} else {

					System.err.println("You must specify a value (1-254).");
					return;
				}
				break;

			case "output":
				if (args.length > i + 1) {

					writeToFile = true;
					saveAs = args[i + 1];
					i++;

				} else {

					System.err.println("You must specify the output file path.");
					return;
				}
				break;

			default:
				break;
			}
		}

		if (!path.endsWith(".txt")) {

			List<String> output = (width > 0 && height > 0 ? fromPath(path, width, height, invert, contrast) : fromPath(path, invert, contrast));

			try {

				if (writeToFile) {

					PrintWriter printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(saveAs)), StandardCharsets.UTF_8));

					for (String line : output) {

						printwriter.println(line);
					}

					printwriter.close();

					System.out.println("Done.");

				} else {

					for (String line : output) {

						System.out.println(line);
					}
				}

			} catch(Exception e) {

				System.err.println("Unable to save output.");
				e.printStackTrace();
			}

		} else {

			try {

				BufferedReader reader = new BufferedReader(new FileReader(path));
				String line;
				StringBuilder brailleText = new StringBuilder();

				while ((line = reader.readLine()) != null) {

					brailleText.append(line + "\n");
				}
				reader.close();

				saveImage(brailleText.toString(), saveAs.substring(saveAs.lastIndexOf(".")), invert, saveAs);

			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}


	public static void saveImage(String text, String format, boolean invert, String saveAs) {

		saveImage(fromBraille(text, invert), format, invert, saveAs);
	}

	public static void saveImage(List<String> text, String format, boolean invert, String saveAs) {

		saveImage(fromBraille(text, invert), format, invert, saveAs);
	}

	public static void saveImage(BufferedImage image, String format, boolean invert, String saveAs) {

		try {

			ImageIO.write(image, "png", new File(saveAs));

		} catch (IOException e) {

			e.printStackTrace();
		}
	}


	public static List<String> fromPath(String path, boolean invert, int contrast) {

		try {

			return fromImage(ImageIO.read(new File(path)), invert, contrast);

		} catch (IOException e) {

			e.printStackTrace();
		}
		return null;
	}


	public static List<String> fromPath(String path, int width, int height, boolean invert, int contrast) {

		try {

			return fromImage(resizeImage(ImageIO.read(new File(path)), width, height), invert, contrast);

		} catch (IOException e) {

			e.printStackTrace();
		}
		return null;
	}


	public static List<String> fromImage(BufferedImage image, boolean invert, int contrast ) {

		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		int rows = imageHeight / 4;
		int columns = imageWidth / 2;

		List<String> lines = new ArrayList<String>(rows);

		for (int row = 0; row < rows; row++) {

			StringBuilder line = new StringBuilder(columns);

			for (int column = 0; column < columns; column++) {

				boolean[] pixelData = new boolean[8];
				int index = 0;

				for (int y = 0; y < 4; y++) {

					for (int x = 0; x < 2; x++) {

						try {

							int pixelRGB = image.getRGB(column * 2 + x, row * 4 + y);
							Color pixelColor = new Color(pixelRGB);

							int luminance = (int)(0.2126 * pixelColor.getRed() + 0.7152 * pixelColor.getGreen() + 0.0722 * pixelColor.getBlue());

							pixelData[index] = (invert ? luminance > contrast : luminance < contrast);
							index++;

						} catch (Exception e) {

							break;
						}
					}
				}

				line.append(toBraille(pixelData[0], pixelData[1], 
						pixelData[2], pixelData[3], 
						pixelData[4], pixelData[5], 
						pixelData[6], pixelData[7]));
			}

			lines.add(line.toString());
		}

		return lines;
	}


	private static String toBraille(boolean p_1, boolean p_2, 
			boolean p_3, boolean p_4, 
			boolean p_5, boolean p_6, 
			boolean p_7, boolean p_8) {

		return String.valueOf(getBrailleChar(new boolean[] {p_1, p_3,
				p_5, p_2, 
				p_4, p_6, 
				p_7, p_8}));
	}


	private static char getBrailleChar(boolean[] points) {

		int code = 0x2800;

		for (int i = 0; i < points.length; i++) {

			if (points[i]) code |= (1 << i);
		}

		return (char) code;
	}


	public static BufferedImage resizeImage(BufferedImage image, int width, int height) {

		Image resultingImage = image.getScaledInstance(width, height, Image.SCALE_DEFAULT);
		BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);

		return outputImage;
	}


	public static BufferedImage fromBraille(List<String> brailleLines, boolean... invert) {

		StringBuilder text = new StringBuilder();

		for (String line : brailleLines) {

			text.append(line + "\n");
		}

		return fromBraille(text.toString(), invert);
	}


	public static BufferedImage fromBraille(String brailleLines, boolean... invert) {

		boolean invertColors = false;

		if (invert.length > 0) {

			invertColors = invert[0];
		}

		int imageWidth = brailleLines.split("\n")[0].length() * 2;
		int imageHeight = brailleLines.split("\n").length * 4 + 1;

		brailleLines = brailleLines.replaceAll("\n", "");

		int columns = imageWidth / 2;
		int rows = imageHeight / 4;

		BufferedImage reconstructedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);

		int charIndex = 0;

		for (int row = 0; row < rows; row++) {

			for (int column = 0; column < columns; column++) {

				char brailleChar = brailleLines.charAt(charIndex);
				boolean[] points = getPointsFromBrailleChar(brailleChar);

				for (int y = 0; y < 4; y++) {

					for (int x = 0; x < 2; x++) {

						int pixelX = column * 2 + x;
						int pixelY = row * 4 + y;
						int pixelIndex = y * 2 + x;

						boolean isPixelSet = (invertColors ? !points[pixelIndex] : points[pixelIndex]);

						int pixelRGB = isPixelSet ? 0xFFFFFF : 0x000000;

						reconstructedImage.setRGB(pixelX, pixelY, pixelRGB);
					}
				}

				charIndex++;
			}
		}

		return reconstructedImage;
	}


	private static boolean[] getPointsFromBrailleChar(char brailleChar) {

		boolean[] points = new boolean[8];
		int code = (int) brailleChar;

		for (int i = 0; i < points.length; i++) {

			points[i] = ((code >> i) & 1) == 1;
		}

		return new boolean[] {points[0], points[3], points[1], points[4], points[2], points[5], points[6], points[7]};
	}
}
