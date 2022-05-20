import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.image.BufferedImage;
public class ColorPicker {

    JFrame window;
    int[] screenResolution = {0, 0};
    int[] pickerWindowSize = {340,180}; // 17:9 ratio
    BufferedImage screenshot;


    public static void main(String[] args) { new ColorPicker(); }
    public ColorPicker() {

        // first, get absolute resolution
        GraphicsDevice[] screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        // bc of multiple monitors, we need to loop through all screens and add up the dimensions
        for (GraphicsDevice screen : screens) {
            screenResolution[0] += screen.getDisplayMode().getWidth();
            screenResolution[1] += screen.getDisplayMode().getHeight();
        }

        // next, take a screenshot
        Robot robot = null;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            JOptionPane.showMessageDialog(null, "Unable to capture screen.", "Error", JOptionPane.WARNING_MESSAGE);
            System.exit(1);
        }
        screenshot = robot.createScreenCapture(new Rectangle(screenResolution[0], screenResolution[1]));

        // now create the picker window
        window = new JFrame("Color Picker");
        window.setUndecorated(true);
        window.setSize(screenResolution[0], screenResolution[1]);
        window.setAlwaysOnTop(true); // render above taskbar
        window.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));

        // Now, we must have three layers. from bottom to top,
        // layer 1 is the JFrame, layer 2 is the image, and
        // layer 3 is the picker.

        // make image layer (2)
        JPanel imageFrame = new JPanel(new BorderLayout());
        imageFrame.add(new JLabel(new ImageIcon(screenshot)));

        // make picker layer (3)
        JPanel pickerFrame = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(
                        updatePreview(MouseInfo.getPointerInfo().getLocation()),
                        0, 0, null);
            }
        };
        pickerFrame.setSize(pickerWindowSize[0], pickerWindowSize[1]);
        pickerFrame.setBorder(new LineBorder(Color.black, 1));
        pickerFrame.setLayout(null);
        pickerFrame.setLocation(MouseInfo.getPointerInfo().getLocation());

        // add layers
        window.add(pickerFrame);
        window.add(imageFrame);

        // add listeners

        window.addMouseListener(new MouseListener() {
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1)
                    getHex(e.getPoint());
            }

            public void mouseReleased(MouseEvent e) {

            }

            public void mouseEntered(MouseEvent e) {

            }

            public void mouseExited(MouseEvent e) {

            }

            public void mouseClicked(MouseEvent e) {
            }

        });
        window.addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 27) System.exit(0);
            }

        });
        window.addMouseMotionListener(new MouseAdapter() {
            public void mouseMoved(MouseEvent e) {

                pickerFrame.setLocation(e.getPoint().x - ( pickerWindowSize[0] / 2 ),
                        e.getPoint().y - pickerWindowSize[1]);
                pickerFrame.repaint();
            }
        });

        // finishing touch
        window.setVisible(true);

    }
    public BufferedImage updatePreview(Point point) {
        BufferedImage preview = new BufferedImage(17,9,1);

        // render the (actual size) preview
        for (int x = -8; x < 9; x++)
            for (int y = -4; y < 5; y++)
                preview.setRGB(8+x,4+y,
                        screenshot.getRGB(point.x + x, point.y + y)
                );

        // now scale up for the preview and add the picker rectangle
        Image scaledPreviewImage = preview.getScaledInstance(pickerWindowSize[0], pickerWindowSize[1], Image.SCALE_DEFAULT);

        // scaledPreview is an image. However, we need a BufferedImage to be able to draw.
        // create a new BufferedImage and draw the Image onto it.
        BufferedImage scaledPreview = new BufferedImage(pickerWindowSize[0], pickerWindowSize[1], BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = scaledPreview.getGraphics();
        g.drawImage(scaledPreviewImage,0,0,null);
        // now draw the rect. no stroke width, so we have to draw 3 rects.
        g.setColor(Color.black);
        for (int i = 0; i < 3; i++) {
            g.drawRect(((pickerWindowSize[0]/2)-10)+i, ((pickerWindowSize[1]/2)-10)+i, 19-(i*2), 19-(i*2));
        }
        g.dispose();

        return scaledPreview;
    }
    public void getHex(Point cursor) {
        // get rgb and convert to hex
        int p = screenshot.getRGB(cursor.x, cursor.y);
        String r = Integer.toHexString((p >> 16) & 0xff);
        String g = Integer.toHexString((p >> 8) & 0xff);
        String b = Integer.toHexString(p & 0xff);

        // add leading zeros
        if(r.length() == 1) r = 0 + r;
        if(g.length() == 1) g = 0 + g;
        if(b.length() == 1) b = 0 + b;

        // combine
        String hex = "#" + r + g + b;

        // write to clipboard
        StringSelection data = new StringSelection(hex);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(data, data);
        System.exit(0);
    }

}
