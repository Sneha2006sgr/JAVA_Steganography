import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageSteganographyGUI {
    private JFrame frame;
    private JTextField inputFileField;
    private JTextField outputFileField;
    private JTextField messageField;
    private JButton browseButton;
    private JButton encryptButton;
    private JButton decryptButton;
    private JTextArea decryptedMessageArea;

    // Add a constant for message length storage
    private static final int MESSAGE_LENGTH_PIXELS = 4; // Store message length in first 4 pixels

    public ImageSteganographyGUI() {
        createGUI();
    }

    private void createGUI() {
        frame = new JFrame("Image Steganography");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());
        inputPanel.setBackground(Color.decode("#F0F0F0")); // light gray

        inputFileField = new JTextField(20);
        outputFileField = new JTextField(20);
        messageField = new JTextField(20);

        browseButton = new JButton("Browse");
        encryptButton = new JButton("Encrypt");
        decryptButton = new JButton("Decrypt");

        inputPanel.add(new JLabel("Input File:"));
        inputPanel.add(inputFileField);
        inputPanel.add(browseButton);

        inputPanel.add(new JLabel("Output File:"));
        inputPanel.add(outputFileField);

        inputPanel.add(new JLabel("Message:"));
        inputPanel.add(messageField);

        inputPanel.add(encryptButton);
        inputPanel.add(decryptButton);

        decryptedMessageArea = new JTextArea(10, 40);
        decryptedMessageArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(decryptedMessageArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        JLabel title = new JLabel("Image Steganography Tool");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setHorizontalAlignment(JLabel.CENTER);
        frame.add(title, BorderLayout.SOUTH);

        browseButton.addActionListener(new BrowseButtonListener());
        encryptButton.addActionListener(new EncryptButtonListener());
        decryptButton.addActionListener(new DecryptButtonListener());

        frame.setVisible(true);
    }

    private class BrowseButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(frame);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                inputFileField.setText(selectedFile.getAbsolutePath());
            }
        }
    }

    private class EncryptButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String inputFile = inputFileField.getText();
            String outputFile = outputFileField.getText();
            String message = messageField.getText();

            if (inputFile.isEmpty() || outputFile.isEmpty() || message.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please fill in all fields.");
                return;
            }

            try {
                BufferedImage img = ImageIO.read(new File(inputFile));

                // Check if the image is large enough for the message (including length info)
                int maxCapacity = (img.getWidth() * img.getHeight() * 3) - (MESSAGE_LENGTH_PIXELS * 3);
                if (message.length() > maxCapacity) {
                    JOptionPane.showMessageDialog(frame,
                            "Message is too long for this image. Maximum length: " + maxCapacity + " characters.");
                    return;
                }

                hideMessage(img, message);
                ImageIO.write(img, "png", new File(outputFile));
                JOptionPane.showMessageDialog(frame, "Encryption successful.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
            }
        }
    }

    private class DecryptButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String inputFile = inputFileField.getText();

            if (inputFile.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please select an input file.");
                return;
            }

            try {
                BufferedImage img = ImageIO.read(new File(inputFile));
                String decryptedMessage = decryptMessage(img);
                decryptedMessageArea.setText(decryptedMessage);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
            }
        }
    }

    private void hideMessage(BufferedImage img, String message) {
        int width = img.getWidth();
        int height = img.getHeight();

        // First, store the message length in the first few pixels
        int messageLength = message.length();
        storeMessageLength(img, messageLength);

        // Start hiding the actual message after the length information
        int n = 0, m = 0, z = 0;

        // Skip the pixels used for length info
        for (int i = 0; i < MESSAGE_LENGTH_PIXELS; i++) {
            z = (z + 3) % 3; // Move to next pixel
            n++;
            if (n >= width) {
                n = 0;
                m++;
            }
        }

        // Now hide the actual message
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            int pixelValue = c;

            int pixel = img.getRGB(n, m);
            int alpha = (pixel >> 24) & 0xff;
            int red = (pixel >> 16) & 0xff;
            int green = (pixel >> 8) & 0xff;
            int blue = pixel & 0xff;

            if (z == 0) {
                red = pixelValue;
            } else if (z == 1) {
                green = pixelValue;
            } else {
                blue = pixelValue;
            }

            pixel = (alpha << 24) | (red << 16) | (green << 8) | blue;
            img.setRGB(n, m, pixel);

            z = (z + 1) % 3;
            if (z == 0) {
                n++;
                if (n >= width) {
                    n = 0;
                    m++;
                    if (m >= height) {
                        m = 0; // Unlikely to happen, but just in case
                    }
                }
            }
        }
    }

    private void storeMessageLength(BufferedImage img, int messageLength) {
        // Store the message length in the first few pixels
        // We'll use 4 pixels (12 color channels), which is enough for large messages

        // Convert the length to bytes
        byte[] lengthBytes = new byte[4];
        lengthBytes[0] = (byte)((messageLength >> 24) & 0xFF);
        lengthBytes[1] = (byte)((messageLength >> 16) & 0xFF);
        lengthBytes[2] = (byte)((messageLength >> 8) & 0xFF);
        lengthBytes[3] = (byte)(messageLength & 0xFF);

        int n = 0, m = 0, z = 0;

        // Store each byte in a color channel
        for (int i = 0; i < 4; i++) {
            int pixel = img.getRGB(n, m);
            int alpha = (pixel >> 24) & 0xff;
            int red = (pixel >> 16) & 0xff;
            int green = (pixel >> 8) & 0xff;
            int blue = pixel & 0xff;

            // Use the unsigned value of the byte
            int byteValue = lengthBytes[i] & 0xFF;

            if (z == 0) {
                red = byteValue;
            } else if (z == 1) {
                green = byteValue;
            } else {
                blue = byteValue;
            }

            pixel = (alpha << 24) | (red << 16) | (green << 8) | blue;
            img.setRGB(n, m, pixel);

            z = (z + 1) % 3;
            if (z == 0) {
                n++;
                if (n >= img.getWidth()) {
                    n = 0;
                    m++;
                }
            }
        }
    }

    private String decryptMessage(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        // First, retrieve the message length
        int messageLength = retrieveMessageLength(img);

        // If message length is negative or unreasonably large, it might not be a steganography image
        if (messageLength < 0 || messageLength > width * height) {
            return "No hidden message found or the image was not encrypted with this tool.";
        }

        StringBuilder message = new StringBuilder();
        int n = 0, m = 0, z = 0;

        // Skip the pixels used for length info
        for (int i = 0; i < MESSAGE_LENGTH_PIXELS; i++) {
            z = (z + 3) % 3; // Move to next pixel
            n++;
            if (n >= width) {
                n = 0;
                m++;
            }
        }

        // Now extract the actual message
        for (int i = 0; i < messageLength; i++) {
            int pixel = img.getRGB(n, m);
            int alpha = (pixel >> 24) & 0xff;
            int red = (pixel >> 16) & 0xff;
            int green = (pixel >> 8) & 0xff;
            int blue = pixel & 0xff;

            int pixelValue;
            if (z == 0) {
                pixelValue = red;
            } else if (z == 1) {
                pixelValue = green;
            } else {
                pixelValue = blue;
            }

            char c = (char) pixelValue;
            message.append(c);

            z = (z + 1) % 3;
            if (z == 0) {
                n++;
                if (n >= width) {
                    n = 0;
                    m++;
                    if (m >= height) {
                        m = 0; // Unlikely to happen, but just in case
                    }
                }
            }
        }

        return message.toString();
    }

    private int retrieveMessageLength(BufferedImage img) {
        // Extract the message length from the first few pixels
        byte[] lengthBytes = new byte[4];
        int n = 0, m = 0, z = 0;

        for (int i = 0; i < 4; i++) {
            int pixel = img.getRGB(n, m);
            int alpha = (pixel >> 24) & 0xff;
            int red = (pixel >> 16) & 0xff;
            int green = (pixel >> 8) & 0xff;
            int blue = pixel & 0xff;

            if (z == 0) {
                lengthBytes[i] = (byte) red;
            } else if (z == 1) {
                lengthBytes[i] = (byte) green;
            } else {
                lengthBytes[i] = (byte) blue;
            }

            z = (z + 1) % 3;
            if (z == 0) {
                n++;
                if (n >= img.getWidth()) {
                    n = 0;
                    m++;
                }
            }
        }

        // Convert bytes back to integer
        return ((lengthBytes[0] & 0xFF) << 24) |
                ((lengthBytes[1] & 0xFF) << 16) |
                ((lengthBytes[2] & 0xFF) << 8) |
                (lengthBytes[3] & 0xFF);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ImageSteganographyGUI());
    }
}