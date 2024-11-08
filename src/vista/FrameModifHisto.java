package vista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.border.EmptyBorder;
import modelo.ExtractorDePixel;
import modelo.Histograma;

public class FrameModifHisto extends JFrame {
    private SmallPanel panel1;
    private SmallPanel panel2;
    private Image img1; 
    // Method to convert the image to grayscale
    private Image convertToGrayscale(Image img) {
        BufferedImage bufferedImg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics g = bufferedImg.getGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();

        for (int y = 0; y < bufferedImg.getHeight(); y++) {
            for (int x = 0; x < bufferedImg.getWidth(); x++) {
                int rgb = bufferedImg.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xff;
                int red = (rgb >> 16) & 0xff;
                int green = (rgb >> 8) & 0xff;
                int blue = rgb & 0xff;
                // Calculate grayscale using the luminosity method
                int gray = (int) (0.3 * red + 0.59 * green + 0.11 * blue);
                int grayRgb = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
                bufferedImg.setRGB(x, y, grayRgb);
            }
        }
        return bufferedImg;
    }
 // Inicializar con una imagen válida si es necesario
    private Image img2; // Inicializar con una imagen válida si es necesario
    private JPanel histogramPanel1;
    private JPanel histogramPanel2;
    private JPanel mainPanel;
    private JSlider fmin;
    private JLabel fminL;
    private JSlider fmax;
    private JLabel fmaxL;
    private JSlider pot;
    private JLabel potL;
    private double[] piac;
    private double[] piac_orig;

    public FrameModifHisto(boolean modo, int selector, Image img) {
        this.img1 = img;
        initComponents(modo, selector);
    }

    public void initComponents(boolean modo, int selector) {
        setLayout(new BorderLayout());

        mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximizamos el JFrame

        // JPanel para disminuir el tamaño del slider
        JPanel panelSup = new JPanel();

        // Inicializar el slider
        if (!modo) {
            fminL = new JLabel("Fmin=100");
            fmin = new JSlider(JSlider.HORIZONTAL, 0, 255, 100);
            fmin.setMajorTickSpacing(100);
            fmin.setMinorTickSpacing(50);
            fmin.addChangeListener(e -> {
                int currentFmin = fmin.getValue();
                int currentFmax = fmax.getValue();
                int currentPot = (selector == 4) ? pot.getValue() : 1;
                Desp_histo(selector, currentFmin, currentFmax, currentPot);
                fminL.setText("Fmin=" + currentFmin);
            });

            fmin.setPaintTicks(true);
            fmin.setPaintLabels(true);
            panelSup.add(fminL);
            panelSup.add(fmin);

            if (selector == 1 || selector == 5) {
                initFmaxSlider(selector, panelSup);
            } else if (selector == 2 || selector == 3 || selector == 4) {
                initAlphaSlider(selector, panelSup);
            }
        }

        // Inicializar los paneles de imágenes
        panel1 = new SmallPanel(convertToGrayscale(img1));
        panel1.setPreferredSize(new Dimension(420, 370));
        panel2 = new SmallPanel(img2);
        panel2.setPreferredSize(new Dimension(420, 370));
        
        // Suponiendo que panel1 es un JPanel que contiene un JLabel
JLabel label = (JLabel) panel1.getComponent(0); // Obtiene el primer componente del panel (suponiendo que es un JLabel)
ImageIcon icon = (ImageIcon) label.getIcon(); // Obtiene el icono del JLabel

    Histograma his1 = new Histograma(icon.getImage()); // Crear el Histograma

        his1.ejecutarTodo(4);
        piac_orig = his1.getPiac();
        histogramPanel1 = new PanelHistograma(his1.getPi(), Color.GRAY, "Histograma");
        histogramPanel2 = new PanelHistograma(new Histograma(img2).getPi(), Color.GRAY, "Histograma");
        histogramPanel1.setPreferredSize(new Dimension(600, 500)); // Ajusta el tamaño deseado
histogramPanel2.setPreferredSize(new Dimension(600, 500)); // Ajusta el tamaño deseado

        
        // Añadir los paneles de imágenes
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(panel1, gbc);

        gbc.gridx = 1;
        mainPanel.add(panel2, gbc);

        // Añadir los histogramas
gbc.gridy = 1;
gbc.weighty = 1.5;  // Aumenta el peso vertical del histograma para hacerlo más grande
gbc.weightx = 1.0;  // Mantiene el ancho completo
gbc.fill = GridBagConstraints.BOTH;  // Expande ambos paneles para llenar el espacio

// Ajuste del histograma en el primer panel
gbc.gridx = 0;
mainPanel.add(histogramPanel1, gbc);

// Ajuste del histograma en el segundo panel
gbc.gridx = 1;
mainPanel.add(histogramPanel2, gbc);

        // Añadir los botones
        gbc.gridy = 2;
        gbc.weighty = 0.1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        gbc.gridx = 0;
        JButton changeImageButton1 = new JButton("Cargar imagen 1");
        mainPanel.add(changeImageButton1, gbc);
        
        gbc.gridx = 1;
        JButton EjecutarOperacion = new JButton("Ejecutar");
        mainPanel.add(EjecutarOperacion, gbc);

        // Action listeners para los botones
       EjecutarOperacion.addActionListener(e -> {
    if (img1 == null) {
        JOptionPane.showMessageDialog(null, "Carga la imagen 1 antes de ejecutar la operación.");
    } else {
        if (modo) {
            realizar_accion();
            // Generar y mostrar el histograma para img2 después de realizar la acción
            updateHistogramForImage(img2, histogramPanel2);
        } else {
            int currentPot = (selector == 4) ? pot.getValue() : 1;
            Desp_histo(selector, fmin.getValue(), fmax.getValue(), currentPot);
            // Generar y mostrar el histograma para img2 después de aplicar el desplazamiento
            updateHistogramForImage(img2, histogramPanel2);
        }
    }
});
        
        

        changeImageButton1.addActionListener(e -> changeImage(1, mainPanel));

        // Añadir el panel principal al frame
        add(mainPanel, BorderLayout.CENTER);
        add(panelSup, BorderLayout.NORTH);

        // Configurar la ventana
        setTitle("Recorrido del Histograma");
        pack();
        setVisible(true);
    }

    private void initFmaxSlider(int selector, JPanel panelSup) {
        fmaxL = new JLabel("Fmax=100");
        fmax = new JSlider(JSlider.HORIZONTAL, 0, 255, 100);
        fmax.setMajorTickSpacing(100);
        fmax.setMinorTickSpacing(50);
        fmax.addChangeListener(e -> {
            int currentFmin = fmin.getValue();
            Desp_histo(selector, currentFmin, fmax.getValue(), 1);
            fmaxL.setText("Fmax=" + fmax.getValue());
        });

        fmax.setPaintTicks(true);
        fmax.setPaintLabels(true);
        panelSup.add(fmaxL);
        panelSup.add(fmax);
    }

    private void initAlphaSlider(int selector, JPanel panelSup) {
        String labelText = (selector == 2) ? "Alpha=0.001" : "Alpha=100";
        fmaxL = new JLabel(labelText);
        fmax = new JSlider(JSlider.HORIZONTAL, 1, 2000, 1);
        fmax.setMajorTickSpacing(1000);
        fmax.setMinorTickSpacing(500);
        fmax.addChangeListener(e -> {
            int currentFmin = fmin.getValue();
            Desp_histo(selector, currentFmin, fmax.getValue(), 1);
            fmaxL.setText(labelText.replace("Alpha=", "Alpha=" + (fmax.getValue() / 1000.0)));
        });

        fmax.setPaintTicks(true);
        fmax.setPaintLabels(true);
        panelSup.add(fmaxL);
        panelSup.add(fmax);
    }

    private void Desp_histo(int selector, int fmin, int fmax, int pot) {
        // Reiniciar el piac
        piac = piac_orig.clone();
        
        int ancho = img1.getWidth(null);
        int alto = img1.getHeight(null);
        ExtractorDePixel ex = new ExtractorDePixel();
        int[] mtz1 = ex.handlepixels(img1, 0, 0, ancho, alto);
        int[] pixeles = new int[ancho * alto];
        double alpha;

        // Se hacen los tipos de desplazamiento del histograma
        for (int i = 0; i < piac.length; i++) {
            switch (selector) {
                case 1: // Uniforme
                    piac[i] = (fmax - fmin) * piac[i] - fmin;
                    break;
                case 2: // Exponencial 
                    alpha = fmax / 1000.00;
                    piac[i] = fmin - (Math.log(1 - piac[i]) / alpha);
                    break;
                case 3: // Rayleigh
                    piac[i] = fmin + Math.sqrt(2 * Math.pow(fmax, 2) * Math.log(1 / (1 - piac[i])));
                    break;
                case 4: // Hiperbólica Raíces
                    double pt = pot / 100.00;
                    piac[i] = Math.pow((((Math.pow(fmax, (1 / pt)) - Math.pow(fmin, (1 / pt))) * piac[i]) + Math.pow(fmin, (1 / pt))), pt);
                    break;
                case 5: // Hiperbólica Logarítmica
                    if (fmin == 0) {
                        piac[i] = 0;
                    } else {
                        piac[i] = fmin * (Math.pow(fmax / fmin, piac[i]));
                    }
                    break;
            }
            piac[i] = Math.max(0, Math.min(255, piac[i]));
        }

        // Función para sustituir unicamente los píxeles
        for (int i = 0; i < alto; i++) {
            for (int j = 0; j < ancho; j++) {
                int index = i * ancho + j;
                int rojo = (mtz1[index] & 0x00ff0000) >> 16;
                int verde = (mtz1[index] & 0x0000ff00) >> 8;
                int azul = mtz1[index] & 0x000000ff;

                int pixel = (rojo + verde + azul) / 3;
                pixel = (int) piac[pixel];
                pixeles[index] = new Color(pixel, pixel, pixel).getRGB();
            }
        }

        img2 = createImageFromPixels(ancho, alto, pixeles);
        changeImage(2, mainPanel);
    }

    private Image createImageFromPixels(int ancho, int alto, int[] pixeles) {
        return createImage(new MemoryImageSource(ancho, alto, pixeles, 0, ancho));
    }

    private void realizar_accion() {
        // Reiniciar valores
        piac = piac_orig.clone();

        int ancho = img1.getWidth(null);
        int alto = img1.getHeight(null);
        ExtractorDePixel ex = new ExtractorDePixel();
        int[] mtz1 = ex.handlepixels(img1, 0, 0, ancho, alto);
        int[] pixeles = new int[ancho * alto];

        for (int i = 0; i < piac.length; i++) {
            piac[i] *= 255;
        }

        // Función para sustituir unicamente los píxeles
        for (int i = 0; i < alto; i++) {
            for (int j = 0; j < ancho; j++) {
                int index = i * ancho + j;
                int rojo = (mtz1[index] & 0x00ff0000) >> 16;
                int verde = (mtz1[index] & 0x0000ff00) >> 8;
                int azul = mtz1[index] & 0x000000ff;

                int pixel = (rojo + verde + azul) / 3;
                pixel = (int) piac[pixel];
                pixeles[index] = new Color(pixel, pixel, pixel).getRGB();
            }
        }

        img2 = createImageFromPixels(ancho, alto, pixeles);
        changeImage(2, mainPanel);
    }

    private void changeImage(int imageNumber, JPanel mainPanel) {
        SmallPanel panel;
        JPanel histogramPanel;
        GridBagConstraints gbc = new GridBagConstraints();
        Color color = new Color(0, 0, 0);
        
        if (imageNumber == 1) {
            cambiarImagen();
            Histograma his = new Histograma(img1);
            his.ejecutarTodo(4);
            panel = panel1;
            histogramPanel1 = new PanelHistograma(his.getPi(), color.GRAY, "Histograma");
            histogramPanel = histogramPanel1;
            gbc.gridx = 0;
            panel.cambiarImg(img1);
        } else {
            panel = panel2;
            histogramPanel = histogramPanel2;
            gbc.gridx = 1;
            panel.cambiarImg(img2);
        }

        // Remover el histograma antiguo
        for (Component comp : mainPanel.getComponents()) {
            if (comp instanceof JPanel &&
                ((GridBagLayout) mainPanel.getLayout()).getConstraints(comp).gridx == gbc.gridx &&
                ((GridBagLayout) mainPanel.getLayout()).getConstraints(comp).gridy == 1) {
                mainPanel.remove(comp);
                break;
            }
        }

        // Añadir el nuevo histograma
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(histogramPanel, gbc);

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void cambiarImagen() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar imagen");

        // Filtro para solo permitir archivos de imagen
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JPEG", "jpg", "jpeg"));
        int resultado = fileChooser.showOpenDialog(null);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            File archivoSeleccionado = fileChooser.getSelectedFile();
            try {
                img1 = ImageIO.read(archivoSeleccionado); // Cargar la imagen

                if (img1 != null) {
                    System.out.println("Imagen cargada correctamente.");
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al cargar la imagen", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // Método para actualizar el histograma del segundo panel
private void updateHistogramForImage(Image img, JPanel histogramPanel) {
    if (img != null) {
        Histograma histograma = new Histograma(img);
        histograma.ejecutarTodo(4); // Generar el histograma
        
        // Crear un nuevo panel de histograma y mantener el original
        JPanel nuevoHistogramaPanel = new PanelHistograma(histograma.getPi(), Color.GRAY, "Histograma");

        // Añadir el nuevo histograma en el segundo panel
        if (histogramPanel2 != null) {
            mainPanel.remove(histogramPanel2); // Eliminar el panel anterior
        }
        histogramPanel2 = nuevoHistogramaPanel; // Actualizar el panel del histograma
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 1; // Asegúrate de colocar en la fila correcta
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;

        // Añadir el nuevo histograma al mainPanel
        mainPanel.add(histogramPanel2, gbc);

        mainPanel.revalidate();
        mainPanel.repaint();
    }
}
}