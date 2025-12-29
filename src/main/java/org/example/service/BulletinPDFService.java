package org.example.service;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.sun.javafx.font.FontFactory;
import org.example.dao.DirecteurDAO;
import org.example.dao.EtudiantDAO;
import org.example.dao.GroupeDAO;
import org.example.dao.NoteDAO;
import org.example.model.Directeur;
import org.example.model.Etudiant;
import org.example.model.Groupe;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class BulletinPDFService {

    private final NoteDAO noteDAO;
    private final EtudiantDAO etudiantDAO;
    private final GroupeDAO groupeDAO;
    private final DirecteurDAO directeurDAO;

    // Informations du directeur connecté
    private Directeur directeur;
    private int directeurId;

    public BulletinPDFService(int directeurId) {
        this.noteDAO = new NoteDAO();
        this.etudiantDAO = new EtudiantDAO();
        this.groupeDAO = new GroupeDAO();
        this.directeurDAO = new DirecteurDAO();
        this.directeurId = directeurId;

        // Récupérer les informations du directeur
        this.directeur = directeurDAO.findById(directeurId).orElse(null);
    }

    public File generateBulletinPDF(int etudiantId, int anneeScolaire, String semestre) throws IOException {
        // Récupérer les données de l'étudiant
        Optional<Etudiant> etudiantOpt = etudiantDAO.findById(etudiantId);
        if (!etudiantOpt.isPresent()) {
            throw new IllegalArgumentException("Étudiant non trouvé");
        }

        Etudiant etudiant = etudiantOpt.get();

        // Récupérer le groupe de l'étudiant
        Groupe groupe = null;
        if (etudiant.getGroupeId() > 0) {
            groupe = groupeDAO.findById(etudiant.getGroupeId());
        } else if (etudiant.getGroupe() != null) {
            groupe = etudiant.getGroupe();
        }

        // Récupérer les notes calculées
        NoteDAO.BulletinResultat bulletin = noteDAO.calculerBulletinEtudiant(etudiantId, anneeScolaire, semestre);

        // Créer le fichier
        String fileName = String.format("Bulletin_%s_%s_%d.pdf",
                etudiant.getNom(), etudiant.getPrenom(), System.currentTimeMillis());
        File file = new File(fileName);

        // Générer le PDF
        try (FileOutputStream fos = new FileOutputStream(file);
             PdfWriter writer = new PdfWriter(fos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf, PageSize.A4)) {

            // Réduire les marges pour plus d'espace
            document.setMargins(20, 20, 20, 20);

            // Police
            PdfFont font = PdfFontFactory.createFont("Helvetica");
            document.setFont(font);

            // 1. LOGO ET EN-TÊTE
            addHeaderWithLogo(document, etudiant, groupe, anneeScolaire, semestre);

            // 2. LIGNE "BULLETIN DE NOTES" avec bordure basse
            addTitleSection(document);

            // 3. INFORMATIONS ÉTUDIANT
            addStudentInfo(document, etudiant, groupe);

            // 4. TABLEAU DES NOTES
            addNotesTable(document, bulletin);

            // 5. MOYENNE ET DÉCISION
            addMoyenneDecision(document, bulletin);

            // 6. SIGNATURE
            addSignatureSection(document);

        }

        return file;
    }

    private void addHeaderWithLogo(Document document, Etudiant etudiant, Groupe groupe, int anneeScolaire, String semestre) {
        try {
            // Créer une table pour l'en-tête avec 3 colonnes
            // CORRECTION: Utiliser UnitValue.createPercentArray au lieu de setWidth avec tableau
            float[] columnWidths = {30, 40, 30}; // Pourcentages pour les 3 colonnes
            Table headerTable = new Table(UnitValue.createPercentArray(columnWidths));
            headerTable.setWidth(UnitValue.createPercentValue(100));
            headerTable.setMarginBottom(10);

            // --- COLONNE 1: Informations académiques à GAUCHE ---
            Cell leftCell = new Cell();
            leftCell.setBorder(Border.NO_BORDER);
            leftCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            leftCell.setPadding(5);

            if (directeur != null) {
                // Académie
                String academie = directeur.getAcademie() != null ? directeur.getAcademie() : "None";
                Paragraph academiePara = new Paragraph("Académie : " + academie)
                        .setFontSize(10)
                        .setBold()
                        .setMarginBottom(2);
                leftCell.add(academiePara);

                // Direction
                String direction = directeur.getDirection() != null ? directeur.getDirection() : "None";
                Paragraph directionPara = new Paragraph("Direction : " + direction)
                        .setFontSize(10)
                        .setBold()
                        .setMarginBottom(2);
                leftCell.add(directionPara);
            } else {
                leftCell.add(new Paragraph("Académie : Non spécifié")
                        .setFontSize(10)
                        .setBold()
                        .setMarginBottom(2));
                leftCell.add(new Paragraph("Direction : Non spécifié")
                        .setFontSize(10)
                        .setBold()
                        .setMarginBottom(2));
            }
            headerTable.addCell(leftCell);

            // --- COLONNE 2: Logo MINISTRE au CENTRE (PARFAITEMENT CENTRÉ) ---
            Cell centerCell = new Cell();
            centerCell.setBorder(Border.NO_BORDER);
            centerCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            centerCell.setHorizontalAlignment(HorizontalAlignment.CENTER); // IMPORTANT: Centrer horizontalement
            centerCell.setPadding(5);

            // Charger l'image depuis les ressources
            try {
                InputStream imageStream = getClass().getResourceAsStream("/images/ministre.png");
                if (imageStream != null) {
                    byte[] imageBytes = imageStream.readAllBytes();
                    Image logo = new Image(com.itextpdf.io.image.ImageDataFactory.create(imageBytes));
                    logo.setWidth(110);  // Légèrement plus grand
                    logo.setHeight(95);   // Légèrement plus grand

                    // Créer un paragraphe centré pour contenir l'image
                    Paragraph imageParagraph = new Paragraph()
                            .setTextAlignment(TextAlignment.CENTER)
                            .add(logo);

                    centerCell.add(imageParagraph);
                    imageStream.close();
                } else {
                    // Si l'image n'est pas trouvée
                    centerCell.add(new Paragraph("MINISTÈRE")
                            .setFontSize(14)
                            .setBold()
                            .setTextAlignment(TextAlignment.CENTER));
                }
            } catch (Exception e) {
                e.printStackTrace();
                centerCell.add(new Paragraph("MINISTÈRE")
                        .setFontSize(14)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER));
            }
            headerTable.addCell(centerCell);

            // --- COLONNE 3: Année scolaire à DROITE ---
            Cell rightCell = new Cell();
            rightCell.setBorder(Border.NO_BORDER);
            rightCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            rightCell.setHorizontalAlignment(HorizontalAlignment.RIGHT);
            rightCell.setPadding(5);

            String nomEcole = "N/A";
            if (directeur != null && directeur.getNomEcole() != null) {
                nomEcole = directeur.getNomEcole();
            }
            // Année scolaire en grand et gras
            rightCell.add(new Paragraph("Établissement : " + nomEcole)
                    .setBold()
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.RIGHT));
            Paragraph anneeScolaireText = new Paragraph(anneeScolaire + "/" + (anneeScolaire + 1))
                    .setFontSize(11)  // Plus grand
                    .setBold()
                    .setTextAlignment(TextAlignment.RIGHT);
            rightCell.add(anneeScolaireText);

            headerTable.addCell(rightCell);

            document.add(headerTable);


        } catch (Exception e) {
            e.printStackTrace();
            // Fallback: En-tête simple
            Paragraph title = new Paragraph("BULLETIN DE NOTES")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);
        }
    }

    private void addTitleSection(Document document) {

        try {
            PdfFont timesNewRomanBold = PdfFontFactory.createFont("Times-Bold");
            // Titre avec bordure basse
            Paragraph title = new Paragraph("BULLETIN DE NOTES")
                    .setFont(timesNewRomanBold)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER);

            // Ajouter une bordure basse
            Table borderTable = new Table(1);
            borderTable.setWidth(UnitValue.createPercentValue(100));

            Cell titleCell = new Cell();
            titleCell.setBorder(Border.NO_BORDER);
            titleCell.add(title);
            borderTable.addCell(titleCell);

            Cell borderCell = new Cell();
            borderCell.setBorderBottom(new SolidBorder(1));
            borderCell.setHeight(7);
            borderCell.setBorderTop(Border.NO_BORDER);
            borderCell.setBorderLeft(Border.NO_BORDER);
            borderCell.setBorderRight(Border.NO_BORDER);
            borderTable.addCell(borderCell);

            document.add(borderTable);

        } catch (IOException e) {
            // Fallback: utiliser une police par défaut
            e.printStackTrace();
            Paragraph title = new Paragraph("BULLETIN DE NOTES")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(3);

        }


    }

    private void addStudentInfo(Document document, Etudiant etudiant, Groupe groupe) {
        Table infoTable = new Table(2);
        infoTable.setWidth(UnitValue.createPercentValue(100));
        infoTable.setMarginBottom(20);

        // Ajouter une bordure basse à tout le tableau
        infoTable.setBorderBottom(new SolidBorder(1));

        // Colonne gauche - largeur fixe pour le contenu
        Cell leftCell = new Cell();
        leftCell.setBorder(Border.NO_BORDER);
        leftCell.setPaddingBottom(10);
        leftCell.setPaddingTop(10);
        leftCell.setWidth(UnitValue.createPercentValue(80));

        // Établissement
        String nomEcole = "N/A";
        if (directeur != null && directeur.getNomEcole() != null) {
            nomEcole = directeur.getNomEcole();
        }

        leftCell.add(new Paragraph("Nom : " + etudiant.getNom())
                .setFontSize(11)
                .setMarginTop(5));
        leftCell.add(new Paragraph("Prénom : " + etudiant.getPrenom())
                .setFontSize(11)
                .setMarginTop(5));

        // Groupe
        String groupeInfo = "N/A";
        if (groupe != null) {
            groupeInfo = groupe.getMatricule() != null ? groupe.getMatricule() : "Groupe " + groupe.getId();
        }
        leftCell.add(new Paragraph("Groupe : " + groupeInfo)
                .setFontSize(11)
                .setMarginTop(5));

        infoTable.addCell(leftCell);

        // Colonne droite - largeur fixe pour aligner le texte
        Cell rightCell = new Cell();
        rightCell.setBorder(Border.NO_BORDER);
        rightCell.setPaddingBottom(10);
        rightCell.setPaddingTop(10);
        rightCell.setWidth(UnitValue.createPercentValue(20));

        // Espacement pour aligner avec la première ligne de gauche
        rightCell.add(new Paragraph(" ")
                .setFontSize(11));

        rightCell.add(new Paragraph("Né le : " + formatDate(etudiant.getDateNaissance()))
                .setFontSize(11)
                .setMarginTop(5));

        rightCell.add(new Paragraph("CIN : " + (etudiant.getCin() != null ? etudiant.getCin() : "N/A"))
                .setFontSize(11)
                .setMarginTop(5));

        // Niveau
        String niveauInfo = "N/A";
        if (groupe != null && groupe.getNiveau() != null) {
            niveauInfo = formatNiveau(groupe.getNiveau().toString());
        }
        rightCell.add(new Paragraph("Niveau : " + niveauInfo)
                .setFontSize(11)
                .setMarginTop(5));

        infoTable.addCell(rightCell);

        document.add(infoTable);
    }
    private void addNotesTable(Document document, NoteDAO.BulletinResultat bulletin) {
        if (bulletin == null || bulletin.getModuleNotes() == null || bulletin.getModuleNotes().isEmpty()) {
            Paragraph noNotes = new Paragraph("Aucune matière assignée")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(new DeviceRgb(156, 163, 175))
                    .setItalic()
                    .setMarginTop(20);
            document.add(noNotes);
            return;
        }

        // Créer le tableau avec 4 colonnes
        float[] columnWidths = {4, 1, 1, 1};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(20);

        // Style des bordures
        Border cellBorder = new SolidBorder(new DeviceRgb(229, 231, 235), 1);

        // En-tête du tableau
        Color headerBgColor = new DeviceRgb(243, 244, 246);

        // Colonne 1: Nom module (aligné à gauche)
        Cell header1 = new Cell();
        header1.add(new Paragraph("Nom module").setBold());
        header1.setBackgroundColor(headerBgColor);
        header1.setBorder(cellBorder);
        header1.setTextAlignment(TextAlignment.LEFT);
        header1.setPadding(6);
        table.addCell(header1);

        // Colonne 2: Note (1)
        Cell header2 = new Cell();
        header2.add(new Paragraph("Note (1)").setBold());
        header2.setBackgroundColor(headerBgColor);
        header2.setBorder(cellBorder);
        header2.setTextAlignment(TextAlignment.CENTER);
        header2.setPadding(6);
        header2.setWidth(50);
        table.addCell(header2);

        // Colonne 3: Coef (2)
        Cell header3 = new Cell();
        header3.add(new Paragraph("Coef (2)").setBold());
        header3.setBackgroundColor(headerBgColor);
        header3.setBorder(cellBorder);
        header3.setTextAlignment(TextAlignment.CENTER);
        header3.setPadding(6);
        header3.setWidth(50);
        table.addCell(header3);

        // Colonne 4: Note Cie (1)*(2)
        Cell header4 = new Cell();
        header4.add(new Paragraph("Note Cie (1)*(2)").setBold());
        header4.setBackgroundColor(headerBgColor);
        header4.setBorder(cellBorder);
        header4.setTextAlignment(TextAlignment.CENTER);
        header4.setPadding(6);
        header4.setWidth(70);
        table.addCell(header4);

        // Données
        double sommeNotesCie = 0;
        int sommeCoefficients = 0;
        int modulesAvecNotes = 0;

        for (NoteDAO.ModuleNote note : bulletin.getModuleNotes()) {
            // Hauteur des cellules (h-10 comme en CSS)
            int cellHeight = 25; // 2.5rem ≈ 25px

            // Nom du module (aligné à gauche)
            String moduleNom = note.getModuleNom() != null ? note.getModuleNom() : "Module inconnu";
            Cell nomCell = new Cell();
            nomCell.add(new Paragraph(moduleNom).setFontSize(10));
            nomCell.setBorder(cellBorder);
            nomCell.setTextAlignment(TextAlignment.LEFT);
            nomCell.setPadding(6);
            nomCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            nomCell.setHeight(cellHeight);
            table.addCell(nomCell);

            // Note module
            Cell noteCell = new Cell();
            if (note.getNoteModule() != null) {
                String noteStr = String.format("%.2f /20", note.getNoteModule());
                noteCell.add(new Paragraph(noteStr).setFontSize(10));
                modulesAvecNotes++;
            } else {
                noteCell.add(new Paragraph("").setFontSize(10));
            }
            noteCell.setBorder(cellBorder);
            noteCell.setTextAlignment(TextAlignment.CENTER);
            noteCell.setPadding(6);
            noteCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            noteCell.setHeight(cellHeight);
            table.addCell(noteCell);

            // Coefficient
            Cell coeffCell = new Cell();
            coeffCell.add(new Paragraph(String.valueOf(note.getCoefficient())).setFontSize(10));
            coeffCell.setBorder(cellBorder);
            coeffCell.setTextAlignment(TextAlignment.CENTER);
            coeffCell.setPadding(6);
            coeffCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            coeffCell.setHeight(cellHeight);
            table.addCell(coeffCell);

            // Note CIE
            Cell noteCieCell = new Cell();
            if (note.getNoteCie() != null) {
                String noteCieStr = String.format("%.2f", note.getNoteCie());
                noteCieCell.add(new Paragraph(noteCieStr).setFontSize(10));
                sommeNotesCie += note.getNoteCie();
                sommeCoefficients += note.getCoefficient();
            } else {
                noteCieCell.add(new Paragraph("").setFontSize(10));
            }
            noteCieCell.setBorder(cellBorder);
            noteCieCell.setTextAlignment(TextAlignment.CENTER);
            noteCieCell.setPadding(6);
            noteCieCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            noteCieCell.setHeight(cellHeight);
            table.addCell(noteCieCell);
        }

        document.add(table);
    }

    private void addMoyenneDecision(Document document, NoteDAO.BulletinResultat bulletin) {
        Table resultTable = new Table(2);
        resultTable.setWidth(UnitValue.createPercentValue(40));
        resultTable.setMarginBottom(30);

        Border cellBorder = new SolidBorder(new DeviceRgb(229, 231, 235), 1);

        // Calculer la moyenne
        String moyenneStr = "N/A";
        String decision = "Non évalué";

        if (bulletin != null && bulletin.getMoyenneGenerale() > 0) {
            moyenneStr = String.format("%.2f", bulletin.getMoyenneGenerale());
            decision = bulletin.getDecision() != null ? bulletin.getDecision() : "Non évalué";
        }

        // Ligne 1: Moyenne Générale
        Cell moyenneLabelCell = new Cell();
        moyenneLabelCell.add(new Paragraph("Moyenne Générale /20").setBold().setFontSize(11));
        moyenneLabelCell.setBorder(cellBorder);
        moyenneLabelCell.setPadding(8);
        moyenneLabelCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        resultTable.addCell(moyenneLabelCell);

        Cell moyenneValueCell = new Cell();
        moyenneValueCell.add(new Paragraph(moyenneStr).setFontSize(11));
        moyenneValueCell.setBorder(cellBorder);
        moyenneValueCell.setPadding(8);
        moyenneValueCell.setTextAlignment(TextAlignment.CENTER);
        moyenneValueCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        resultTable.addCell(moyenneValueCell);

        // Ligne 2: Décision
        Cell decisionLabelCell = new Cell();
        decisionLabelCell.add(new Paragraph("Décision").setBold().setFontSize(11));
        decisionLabelCell.setBorder(cellBorder);
        decisionLabelCell.setPadding(8);
        decisionLabelCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        resultTable.addCell(decisionLabelCell);

        Cell decisionValueCell = new Cell();
        decisionValueCell.add(new Paragraph(decision).setFontSize(11));

        // Couleurs selon la décision
        if ("Admis".equals(decision)) {
            decisionValueCell.setBackgroundColor(new DeviceRgb(220, 252, 231));
        } else if ("Non Admis".equals(decision) || "Redoublant".equals(decision)) {
            decisionValueCell.setBackgroundColor(new DeviceRgb(254, 226, 226));
        } else {
            decisionValueCell.setBackgroundColor(new DeviceRgb(243, 244, 246));
        }

        decisionValueCell.setBorder(cellBorder);
        decisionValueCell.setPadding(8);
        decisionValueCell.setTextAlignment(TextAlignment.CENTER);
        decisionValueCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        resultTable.addCell(decisionValueCell);

        document.add(resultTable);
    }

    private void addSignatureSection(Document document) {
        // Texte de signature
        Paragraph signatureText = new Paragraph()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10);

        Text faitA = new Text("Fait à : ...................................... , ");
        Text le = new Text("le : ......................................");

        signatureText.add(faitA);
        signatureText.add(le);

        document.add(signatureText);


        // Créer une ligne avec le texte "DIRECTEUR D'ETABLISSEMENT" souligné
        Table signatureTable = new Table(1);
        signatureTable.setWidth(UnitValue.createPercentValue(100));
        signatureTable.setHorizontalAlignment(HorizontalAlignment.CENTER);
        signatureTable.setMarginTop(3);

        // Cellule sans bordure avec texte centré
        Cell signatureCell = new Cell();
        signatureCell.setBorder(Border.NO_BORDER); // Pas de bordure
        signatureCell.setPaddingBottom(3);
        signatureCell.setHorizontalAlignment(HorizontalAlignment.CENTER);

        // Paragraphe avec texte souligné
        Paragraph directeurText = new Paragraph("DIRECTEUR D'ETABLISSEMENT")
                .setFontSize(11)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setUnderline(); // Souligner le texte

        signatureCell.add(directeurText);
        signatureTable.addCell(signatureCell);

        document.add(signatureTable);
    }

    // Méthodes utilitaires
    private String formatDate(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private String formatNiveau(String niveau) {
        if (niveau == null) return "N/A";

        return niveau.replace("_", " ")
                .replace("PREMIERE", "1ère")
                .replace("DEUXIEME", "2ème")
                .replace("TROISIEME", "3ème")
                .replace("QUATRIEME", "4ème")
                .replace("ANNEE", "année");
    }


}