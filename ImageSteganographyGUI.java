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

                // Check if the image is large enough for the message
                int maxCapacity = (img.getWidth() * img.getHeight() * 3) / 8;
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

    // Keeps original encryption logic exactly the same for compatibility
    private void hideMessage(BufferedImage img, String message) {
        int width = img.getWidth();
        int height = img.getHeight();
        int n = 0, m = 0, z = 0;

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

            n = (n + 1) % width;
            m = (m + 1) % height;
            z = (z + 1) % 3;
        }

        // Add terminator
        int pixel = img.getRGB(n, m);
        int alpha = (pixel >> 24) & 0xff;
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = pixel & 0xff;

        if (z == 0) {
            red = 0; // NULL character as terminator
        } else if (z == 1) {
            green = 0; // NULL character as terminator
        } else {
            blue = 0; // NULL character as terminator
        }

        pixel = (alpha << 24) | (red << 16) | (green << 8) | blue;
        img.setRGB(n, m, pixel);
    }

    private String decryptMessage(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        // For backward compatibility, try two decryption methods

        // First, try original method
        String original = decryptOriginalMethod(img);

        // If the original method gives a reasonable result, use it
        if (original != null && !original.isEmpty() && isReadableText(original)) {
            return original;
        }

        // Otherwise, try multiple decryption methods to handle different images
        String[] attempts = {
                decryptOriginalMethodWithCleanup(img),
                decryptFallbackMethod(img)
        };

        // Return the most readable result
        String bestResult = "";
        int bestScore = -1;

        for (String attempt : attempts) {
            int readabilityScore = getReadabilityScore(attempt);
            if (readabilityScore > bestScore) {
                bestScore = readabilityScore;
                bestResult = attempt;
            }
        }

        return bestResult;
    }

    private String decryptOriginalMethod(BufferedImage img) {
        // This exactly matches your original algorithm
        int width = img.getWidth();
        int height = img.getHeight();
        int n = 0, m = 0, z = 0;
        StringBuilder message = new StringBuilder();

        // We'll read until we find a terminator or 500 characters max
        // to avoid infinite loops but still handle most messages
        for (int i = 0; i < 500; i++) {
            if (n >= width || m >= height) break;

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

            // Check for NULL terminator
            if (pixelValue == 0) {
                break;
            }

            char c = (char) pixelValue;
            message.append(c);

            n = (n + 1) % width;
            m = (m + 1) % height;
            z = (z + 1) % 3;
        }

        return message.toString();
    }

    private String decryptOriginalMethodWithCleanup(BufferedImage img) {
        String raw = decryptOriginalMethod(img);
        // Remove non-printable characters
        return raw.replaceAll("[^\\x20-\\x7E]", "");
    }

    private String decryptFallbackMethod(BufferedImage img) {
        // Alternative method with different pixel traversal
        int width = img.getWidth();
        int height = img.getHeight();
        int n = 0, m = 0, z = 0;
        StringBuilder message = new StringBuilder();

        for (int i = 0; i < Math.min(300, width * height); i++) {
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

            // Only add printable ASCII characters
            if (pixelValue >= 32 && pixelValue <= 126) {
                char c = (char) pixelValue;
                message.append(c);
            } else if (pixelValue == 0) {
                break; // Found terminator
            }

            z = (z + 1) % 3;
            if (z == 0) {
                n++;
                if (n >= width) {
                    n = 0;
                    m++;
                    if (m >= height) break;
                }
            }
        }

        return message.toString();
    }

    // Helper method to determine if text is readable
    private boolean isReadableText(String text) {
        if (text == null || text.isEmpty()) return false;

        // Check if most characters are printable ASCII
        int printable = 0;
        for (char c : text.toCharArray()) {
            if (c >= 32 && c <= 126) {
                printable++;
            }
        }

        return (double) printable / text.length() > 0.8; // 80% readability threshold
    }

    // Helper method to score readability
    private int getReadabilityScore(String text) {
        if (text == null) return -1;

        int score = 0;

        // More printable characters = better score
        for (char c : text.toCharArray()) {
            if (c >= 32 && c <= 126) {
                score++;
            } else {
                score--; // Penalize non-printable characters
            }
        }

        // Penalize extremely long outputs as they're likely noise
        if (text.length() > 200) {
            score -= (text.length() - 200);
        }

        return score;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ImageSteganographyGUI());
    }
}