package src.assignment2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author sehall
 * @author Jack Hosking
 * Student ID: 16932920
 */

public class AnimalPatient implements Comparable<AnimalPatient> {
    private String species;
    private String name;
    private ImageIcon image;
    private Date dateLastSeen;
    private int priority;
    private String symptoms;
    private String treatment;
    private JPanel displayPanel;

    public AnimalPatient(String species, String name)
    {
        this(species, name, new Date());
    }

    public AnimalPatient(String species, String name, Date dateLastSeen)
    {
        this.species = species;
        this.name = name;
        this.dateLastSeen = dateLastSeen;
        symptoms = "unknown";
        treatment = null;
        image = null;
        priority = 1;
    }

    public void updateDate(Date date)
    {
        dateLastSeen = date;
        if(displayPanel != null)
            displayPanel.repaint();
    }

    public String getName()
    {   return name;
    }

    public String getSpecies()
    {   return species;
    }

    public void setSymptoms(String symptoms)
    {
        this.symptoms = symptoms;
    }

    public String getSymptoms()
    {   return symptoms;
    }

    public String getTreatment()
    {   return treatment;
    }

    public void setTreatment(String treatment)
    {   this.treatment = treatment;
    }

    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    public void loadImage(String location)
    {
        image = new ImageIcon(location);
        if(displayPanel != null)
            displayPanel.repaint();
    }

    public String getImage() {
        return image == null ? null : image.getDescription();
    }

    public JPanel getDisplayPanel()
    {
        if(displayPanel == null)
            displayPanel = new DisplayPanel();
        return displayPanel;
    }

    public String toString()
    {   SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return name+"("+species+"), priortiy = "+priority+", Last seen: "+simpleDateFormat.format(dateLastSeen);
    }

    public Date getDateLastSeen() {
        return dateLastSeen;
    }

    public void setDateLastSeen(Date dateLastSeen) {
        this.dateLastSeen = dateLastSeen;
    }

    @Override
    public int compareTo(AnimalPatient arg0) {
        // Same priority then order by date
        // if date is same then arg0 patient is placed behind 'this' patient
        if(this.getPriority() == arg0.getPriority()) {
            //TODO: check this
            return arg0.getDateLastSeen().compareTo(this.getDateLastSeen()) >= 0 ? -1 : 1;
        }
        return this.getPriority() > arg0.getPriority() ? 1 : -1;
    }

    private class DisplayPanel extends JPanel implements ChangeListener,DocumentListener
    {
        private JTextArea symptomsArea, treatmentArea;
        private JSlider prioritySlider;
        private JLabel nameLabel;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        public DisplayPanel()
        {
            super(new BorderLayout());
            super.setPreferredSize(new Dimension(600,600));


            String date = simpleDateFormat.format(dateLastSeen);

            nameLabel = new JLabel("DATE/TIME SEEN: "+date+" - "+name+
                    ", ["+species+"]",SwingConstants.CENTER);
            nameLabel.setFont(new Font("Comic Sans",Font.BOLD,14));
            super.add(nameLabel,BorderLayout.NORTH);


            super.add(new DrawPanel(), BorderLayout.CENTER);

            symptomsArea = new JTextArea(symptoms,10,20);
            symptomsArea.getDocument().addDocumentListener(this);
            symptomsArea.setBorder(BorderFactory.createTitledBorder("Symptoms"));
            treatmentArea = new JTextArea(treatment,10,20);
            treatmentArea.getDocument().addDocumentListener(this);
            treatmentArea.setBorder(BorderFactory.createTitledBorder("Treatment"));
            JPanel eastPanel = new JPanel();
            eastPanel.setLayout(new GridLayout(2,1));
            eastPanel.add(new JScrollPane(symptomsArea));
            eastPanel.add(new JScrollPane(treatmentArea));

            super.add(eastPanel, BorderLayout.EAST);

            JPanel southPanel = new JPanel();
            southPanel.setLayout(new BoxLayout(southPanel,BoxLayout.X_AXIS));

            prioritySlider = new JSlider(JSlider.HORIZONTAL, 1, 10, getPriority());
            prioritySlider.setBorder(BorderFactory.createTitledBorder("Priority Level"));
            prioritySlider.setMajorTickSpacing(1);
            prioritySlider.setPaintLabels(true);
            prioritySlider.setPaintTicks(true);
            prioritySlider.addChangeListener((ChangeListener)this);
            //southPanel.add(new JLabel("Priority:"));
            southPanel.add(prioritySlider);

            super.add(southPanel,BorderLayout.SOUTH);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            priority = prioritySlider.getValue();
        }

        @Override
        public void insertUpdate(DocumentEvent e)
        {   if(e.getDocument() == symptomsArea.getDocument())
        {
            symptoms = symptomsArea.getText();
        }
            if(e.getDocument() == treatmentArea.getDocument())
            {
                treatment = treatmentArea.getText();
            }
        }

        @Override
        public void removeUpdate(DocumentEvent e)
        {   insertUpdate(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e)
        {   insertUpdate(e);
        }

        private class DrawPanel extends JPanel
        {
            public DrawPanel()
            {   super();
                super.setPreferredSize(new Dimension(300,400));
                super.setBackground(Color.WHITE);
            }
            public void paintComponent(Graphics g)
            {   super.paintComponent(g);
                String date = simpleDateFormat.format(dateLastSeen);
                nameLabel.setText("DATE/TIME SEEN: "+date+" - "+name+", ["+species+"]");
                if(image != null && image.getIconWidth() > 0)
                {    Image i = image.getImage().getScaledInstance(getWidth(), getHeight(), Image.SCALE_FAST);
                    image.setImage(i);
                    image.paintIcon(this, g, 0, 0);
                }
                else
                    g.drawString("No Picture for "+name, getWidth()/2-50, getHeight()/2-50);
            }
        }
    }
    public static void main(String[] args)
    {
        AnimalPatient animal = new AnimalPatient("Cat", "Ciri");
        animal.loadImage("ciri.jpg");
        System.out.println(animal);
        JFrame frame = new JFrame("Seths - Vet of cute Animals");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(animal.getDisplayPanel());
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dimension = toolkit.getScreenSize();
        int screenHeight = dimension.height;
        int screenWidth = dimension.width;
        frame.pack();             //resize frame apropriately for its content
        //positions frame in center of screen
        frame.setLocation(new Point((screenWidth/2)-(frame.getWidth()/2),
                (screenHeight/2)-(frame.getHeight()/2)));
        frame.setVisible(true);
    }

}
