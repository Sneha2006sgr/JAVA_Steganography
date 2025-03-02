# Image Steganography GUI

## Overview
This project is a Java-based Image Steganography application that provides a very simple Graphical User Interface (GUI) to encrypt a secret message into an image and decrypt it later. The application allows users to select an image, input a message, and encode the message into the image or retrieve the hidden message from an already encoded image.

The purpose of the project is to demonstrate basic steganography concepts, where data is concealed within an image without altering its visual appearance.

---

## Features
- **Browse Images**: Select input images directly from your system using a file browser.
- **Encrypt Messages**: Hide secret messages into an image file.
- **Decrypt Messages**: Retrieve and display the hidden messages from an encoded image.
- Friendly interface using **Swing-based GUI**.

---

## How It Works
1. **Encryption**:
   - The user inputs a message and selects an image.
   - The message is encoded into the image by modifying the pixel values.
   - The encoded image is saved to an output file.
   
2. **Decryption**:
   - The user selects an image encoded with a secret message.
   - The application reads and decodes the hidden message from the image.

---

## Key Functional Components
1. **GUI Elements**:
   - **Text Fields**: For selecting input and output image paths and entering the message to encode.
   - **Buttons**: For browsing files, encrypting messages, and decrypting messages.
   - **Text Area**: For displaying the retrieved hidden message after decryption.

2. **Core Functions**:
   - *hideMessage*: Embeds the message into the image's pixel data.
   - *decryptMessage*: Extracts the encoded message from the image.
   - *storeMessageLength*: Stores the message length in the first 4 pixels to allow correct decoding.
   - *retrieveMessageLength*: Reads the message length during decryption.

3. **Pixel Modification**:
   - The application uses the RGB values of image pixels to store the encoded data.

4. **Swing Listeners**:
   - Supports responsiveness by adding functionality for button clicks and other user actions.

---

## Requirements
### Software
- **Java Development Kit (JDK)** version 8 or higher.
- A Java IDE, such as IntelliJ IDEA, Eclipse, or NetBeans (optional for development).

### Libraries
- **javax.swing** for creating the GUI.
- **java.awt** for handling graphical components.
- **javax.imageio** for reading and writing image files.

---

## How to Run
1. Clone or download the project files into your local system.
2. Compile the `ImageSteganographyGUI.java` file using the following command:
```shell script
javac ImageSteganographyGUI.java
```
3. Run the program to launch the GUI:
```shell script
java ImageSteganographyGUI
```
4. Follow the on-screen instructions:
   - Browse and select an image file.
   - Enter a secret message to encrypt it into the image.
   - Or, select an encoded image to decrypt and retrieve the hidden message.

---

## File Structure
- **ImageSteganographyGUI.java**: The main Java file containing both the GUI and the core functionality for encoding and decoding messages within images.

---

## Limitations
- The size of the message is limited by the image capacity, i.e., the number of pixels available for encoding.
- Only standard image file formats (e.g., `.png`, `.jpg`, `.bmp`) are supported.
- The encoding and decoding process assumes that the image does not undergo lossy compression after encoding, as this would destroy the hidden data.

---

## Future Improvements
- Add support for securely encrypting the message using passwords or keys.
- Expand compatibility to handle more image file formats.
- Implement error-checking to ensure stealthiness of the embedded message.
- Allow batch processing of multiple images/messages.

---

## Credits
Created by Sneha Gond. This project demonstrates fundamental steganography techniques and serves as an educational tool for learning about image-based data hiding.

Enjoy working with this steganography application and exploring the art of hiding messages in plain sight!
