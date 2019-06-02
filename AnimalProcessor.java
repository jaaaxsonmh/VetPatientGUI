package src.assignment2;

/**
 * @author Jack Hosking
 * Student ID: 16932920
 */

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;

public class AnimalProcessor {
    private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    private TreeSet<AnimalPatient> waitList = new TreeSet<>();
    private DOMUtilities domUtilities = new DOMUtilities();
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public AnimalProcessor() {
    }

    public void addAnimal(AnimalPatient animal) {
        waitList.add(animal);
    }

    public AnimalPatient getNextAnimal() {
            return waitList.first();

    }

    public AnimalPatient releaseAnimal() {
        AnimalPatient animal = getNextAnimal();
        waitList.removeIf(patient -> patient == animal);
        return animal;
    }

    public int animalsLeftToProcess() {
        System.out.println(waitList.size());
        return waitList.size();
    }

    public void loadAnimalsFromXML(Document document) {
        document.getDocumentElement().normalize();
        Node rootXMLNode = document.getDocumentElement();
        Collection<Node> animals = domUtilities.getAllChildNodes(rootXMLNode, "animal");

        String species, name;
        AnimalPatient animalPatient;
        for (Node i : animals) {
            species = domUtilities.getAttributeString(i, "species");
            name = domUtilities.getAttributeString(i, "name");
            animalPatient = new AnimalPatient(species, name);
            animalPatient.setPriority(Integer.parseInt(domUtilities.getAttributeString(i, "priority")));

            //Get Date from the text content and parse it into simpleDateFormat.
            for (Node j : domUtilities.getAllChildNodes(i, "dateSeen")) {
                try {
                    animalPatient.setDateLastSeen(simpleDateFormat.parse(domUtilities.getTextContent(j)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            for (Node j : domUtilities.getAllChildNodes(i, "symptoms")) {
                animalPatient.setSymptoms(domUtilities.getTextContent(j));
            }

            for (Node j : domUtilities.getAllChildNodes(i, "treatment")) {
                animalPatient.setTreatment(domUtilities.getTextContent(j));
            }

            for (Node j : domUtilities.getAllChildNodes(i, "picURL")) {
                animalPatient.loadImage(domUtilities.getTextContent(j));
            }

            addAnimal(animalPatient);
        }
    }

    public TreeSet<AnimalPatient> getWaitList() {
        return waitList;
    }

    public static void main(String args[]) {
        System.out.println("============== Animal Processor Test ===============");

        AnimalPatient animal1 = new AnimalPatient("Cat", "Cici");
        animal1.setPriority(3);
        //Pause for 1 second so dateTime is different for each patient.
        try {
            Thread.sleep(1000);
        } catch (Exception ignored) {

        }

        AnimalPatient animal2 = new AnimalPatient("Dog", "Smokey");
        animal2.setPriority(2);
        try {
            Thread.sleep(1000);
        } catch (Exception ignored) {

        }

        AnimalPatient animal3 = new AnimalPatient("Frog", "Bully");
        AnimalPatient animal4 = new AnimalPatient("Horse", "Mustang");
        AnimalPatient animal5 = new AnimalPatient("Monkey", "Cookie");
        AnimalPatient animal6 = new AnimalPatient("Human", "Jack");

        try {
            Thread.sleep(1000);
        } catch (Exception ignored) {

        }
        //will be added behind the animals3-6
        AnimalPatient animal7 = new AnimalPatient("Lynx", "cute cat");

        AnimalProcessor processor = new AnimalProcessor();
        processor.addAnimal(animal1);
        processor.addAnimal(animal2);
        processor.addAnimal(animal3);
        processor.addAnimal(animal4);
        processor.addAnimal(animal5);
        processor.addAnimal(animal6);
        processor.addAnimal(animal7);

        processor.waitList.forEach(System.out::println);

        System.out.println("\nUPDATE: all animals with priority of 1 for a priority of 10.");
        processor.waitList.stream().filter(ap -> ap.getPriority()==1).forEach(ap -> ap.setPriority(10));

        processor.waitList.stream().forEach(System.out::println);

        System.out.println("\nFirst patient in waitlist: " + processor.getNextAnimal());

        System.out.println("\nAnimals waiting to be processed: "
                + processor.animalsLeftToProcess());
        System.out.println("REMOVING ANIMALS");
        int remove = processor.animalsLeftToProcess();
        for (int k = 0; k < remove; k++) {
            processor.releaseAnimal();
        }
        System.out.println("Animals waiting to be processed: " +
                processor.animalsLeftToProcess());

        System.out.println("\n============== Animal Processor Test WITH XML ===============");

        String file = "src/assignment2/AnimalsInVet.xml";
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            builderFactory.setValidating(true);
            builderFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            // parse the input stream
            Document document = builder.parse(file);
            processor.loadAnimalsFromXML(document);
            processor.waitList.stream().forEach(System.out::println);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("\nSee animal at later date: " + processor.getNextAnimal());
        AnimalPatient animal = processor.releaseAnimal();
        animal.updateDate(new Date());
        animal.setPriority(10);
        processor.addAnimal(animal);

        System.out.println("Get next patient: " + processor.getNextAnimal());
        System.out.println("\n==== New WaitList ====");

        processor.waitList.stream().forEach(System.out::println);

        System.out.println("\nFirst patient in waitlist: " + processor.getNextAnimal());

        System.out.println("\nAnimals waiting to be processed: "
                + processor.animalsLeftToProcess());
        System.out.println("REMOVING ANIMALS");
        remove = processor.animalsLeftToProcess();
        for (int k = 0; k < remove; k++) {
            processor.releaseAnimal();
        }
        System.out.println("Animals waiting to be processed: " +
                processor.animalsLeftToProcess());
    }
}
