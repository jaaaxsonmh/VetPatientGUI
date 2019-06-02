package src.assignment2;

/**
 * @author Jack Hosking
 * Student ID: 16932920
 */

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class VetGUI extends JPanel {

    private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    private JPanel drawPanel;
    private JFrame frame;
    JLabel waitList = new JLabel("", SwingConstants.CENTER);

    private int width = 800;
    private AnimalProcessor processor;


    public VetGUI() {
        super(new BorderLayout());

        drawPanel = new JPanel();
        drawPanel.setLayout(new GridBagLayout());
        JLabel nullXML = new JLabel("Please load an xml file", SwingConstants.CENTER);
        nullXML.setFont(new Font("Comic Sans", Font.BOLD, 24));
        drawPanel.add(nullXML);
        drawPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        drawPanel.setPreferredSize(new Dimension(width, 600));
        drawPanel.setBackground(Color.WHITE);
        add(drawPanel);

        JPanel buttonPanel = new JPanel();
        JButton newPatient = new JButton("New Patient");
        newPatient.addActionListener((action) -> newPatient());
        buttonPanel.add(newPatient);

        JButton seeLater = new JButton("See Later");
        seeLater.addActionListener((action) -> seeLater());
        buttonPanel.add(seeLater);

        JButton release = new JButton("Release");
        release.addActionListener((action) -> release());
        buttonPanel.add(release);

        JButton loadXML = new JButton("Load XML");
        loadXML.addActionListener((action) -> loadXML());
        buttonPanel.add(loadXML);

        JButton saveXML = new JButton("Save XML");
        saveXML.addActionListener((action) -> saveXML());
        buttonPanel.add(saveXML);

        JButton updatePic = new JButton("Update Pic");
        updatePic.addActionListener((action) -> updatePic());
        buttonPanel.add(updatePic);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void updatePanel() {
        if (this.processor != null) {
            remove(this.drawPanel);
            this.drawPanel = processor.getNextAnimal().getDisplayPanel();
            add(drawPanel, BorderLayout.CENTER);

            waitList.setText("Animals waiting to be seen: "
                    + processor.animalsLeftToProcess());
            add(waitList, BorderLayout.NORTH);
            revalidate();
            repaint();
        }
    }


    private void seeLater() {
        if (this.processor != null) {
            AnimalPatient animalPatient = processor.releaseAnimal();
            System.out.println(animalPatient);
            animalPatient.updateDate(new Date());
            animalPatient.setPriority(10);
            processor.addAnimal(animalPatient);
            updatePanel();
        }
    }

    private void newPatient() {
        try {
            JDialog dialog = new JDialog(frame, "Enter a new Patient");
            dialog.setSize(300,300);
            dialog.setLocationRelativeTo(null);

            String name = null;
            String species = (String)JOptionPane.showInputDialog(
                    frame,
                    "Enter new patient species.",
                    "Enter a patient",
                    JOptionPane.PLAIN_MESSAGE,
                    null
                    ,
                    null
                    ,
                    "Enter species");

            if(species != null){
                name = (String)JOptionPane.showInputDialog(
                        frame,
                        "Enter new patient name",
                        "Enter a patient",
                        JOptionPane.PLAIN_MESSAGE,
                        null
                        ,
                        null
                        ,
                        "Enter Name");
            }

            if ((species != null) && (species.length() > 0) && (name !=null) && (name.length() > 0)) {
                AnimalPatient animalPatient = new AnimalPatient(species, name, new Date());
                System.out.println(animalPatient);
                if(this.processor != null){
                    processor.addAnimal(animalPatient);
                }  else {
                    processor = new AnimalProcessor();
                    processor.addAnimal(animalPatient);
                }
                updatePanel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void release() {
        if (this.processor != null) {
            processor.releaseAnimal();
            updatePanel();
        }
    }

    private void loadXML() {
        JFileChooser jFileChooser = new JFileChooser(new File("."));
        int j = jFileChooser.showOpenDialog(null);
        if (j == JFileChooser.APPROVE_OPTION) {
            File file = jFileChooser.getSelectedFile();
            try {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                builderFactory.setNamespaceAware(true);
                builderFactory.setValidating(true);
                builderFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
                DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
                //parse input stream
                Document document = documentBuilder.parse(file);
                processor = new AnimalProcessor();
                processor.loadAnimalsFromXML(document);
                updatePanel();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void saveXML() {
        if (this.processor != null) {
            JFileChooser chooser = new JFileChooser(new File("."));
            int i = chooser.showSaveDialog(null);
            if (i == JFileChooser.APPROVE_OPTION) {
                try {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setNamespaceAware(true);
                    DocumentBuilder documentBuilder = factory.newDocumentBuilder();
                    Document newDocument = documentBuilder.getDOMImplementation().createDocument(null, "animals", null);
                    newDocument.setXmlVersion("1.0");
                    newDocument.setXmlStandalone(true);

                    Element root = newDocument.getDocumentElement();
                    root.setAttribute("xsi:noNamespaceSchemaLocation", "animals.xsd");
                    root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");

                    //For AnimalP animal : processor.getWaitList()
                    // addAnimal(... ... ... );

                    processor.getWaitList().stream().forEach((animal) -> addAnimal(newDocument, root, animal));

                    FileOutputStream output = new FileOutputStream(chooser.getSelectedFile().getAbsolutePath());
                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    transformer.setOutputProperties(new Properties());
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    transformer.transform(new DOMSource(newDocument), new StreamResult(output));
                    output.flush();
                    output.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    private void addAnimal(Document newDocument, Element root, AnimalPatient animal) {
        Element tag = newDocument.createElement("animal");
        tag.setAttribute("name", animal.getName());
        tag.setAttribute("species", animal.getSpecies());
        //Save int (priority) as string (text content), as is parsed in loadFileFromXML to int anyway.
        tag.setAttribute("priority", "" + animal.getPriority());

        //Add Picture
        if (animal.getImage() != null) {
            Element picURL = newDocument.createElement("picURL");
            picURL.setTextContent(animal.getImage());
            tag.appendChild(picURL);
        }

        if (animal.getSymptoms() != null) {
            Element symptoms = newDocument.createElement("symptoms");
            symptoms.setTextContent(animal.getSymptoms());
            tag.appendChild(symptoms);
        }

        if (animal.getTreatment() != null) {
            Element treatment = newDocument.createElement("treatment");
            treatment.setTextContent(animal.getTreatment());
            tag.appendChild(treatment);
        }

        Element dateLastSeen = newDocument.createElement("dateSeen");
        dateLastSeen.setTextContent(simpleDateFormat.format(animal.getDateLastSeen()));
        tag.appendChild(dateLastSeen);

        root.appendChild(tag);

    }

    private void updatePic() {
        if (this.processor != null) {
            JFileChooser chooser = new JFileChooser(new File("."));
            FileFilter imageFilter = new FileNameExtensionFilter(
                    "Image files", ImageIO.getReaderFileSuffixes());

            chooser.setFileFilter(imageFilter);
            int i = chooser.showOpenDialog(null);
            if (i == JFileChooser.APPROVE_OPTION) {

                try {
                    File file = chooser.getSelectedFile();
                    String imageFile = file.getAbsolutePath();
                    System.out.println(imageFile);
                    processor.getNextAnimal().loadImage(imageFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void main(String[] args) {
        VetGUI vetGUI = new VetGUI();
        vetGUI.frame = new JFrame("Jack - VET GUI");

        // kill all threads when frame closes
        vetGUI.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        vetGUI.frame.getContentPane().add(vetGUI);
        vetGUI.frame.pack();

        // position the frame in the middle of the screen
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenDimension = tk.getScreenSize();
        Dimension frameDimension = vetGUI.frame.getSize();
        vetGUI.frame.setLocation((screenDimension.width - frameDimension.width) / 2,
                (screenDimension.height - frameDimension.height) / 2);
        vetGUI.frame.setVisible(true);
        // now display something while the main thread is still alive
    }
}
