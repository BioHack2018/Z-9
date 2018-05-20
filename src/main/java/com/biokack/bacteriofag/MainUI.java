package com.biokack.bacteriofag;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.biojava.nbio.alignment.Alignments;
import org.biojava.nbio.alignment.Alignments.PairwiseSequenceScorerType;
import org.biojava.nbio.alignment.SimpleGapPenalty;
import org.biojava.nbio.alignment.template.GapPenalty;
import org.biojava.nbio.alignment.template.PairwiseSequenceScorer;
import org.biojava.nbio.core.alignment.matrices.SubstitutionMatrixHelper;
import org.biojava.nbio.core.alignment.template.SubstitutionMatrix;
import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.biojava.nbio.core.sequence.io.FastaReaderHelper;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SpringUI
@Theme("valo")
public class MainUI extends UI {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2482982131208331546L;
	private VerticalLayout mainVL;
	private Button sequenceAlignmentBtn;
	private Map<String, double[]> runPairwiseScorers = new HashMap<String, double[]>();
	private TextField folderPathTF;
	private Label resultLabel;
	private long databaseLength;
	private long queryLength;
	private double p_value = 0.05;
	private ProteinSequence mainProtein;

	@Override
	protected void init(VaadinRequest request) {
		setSizeFull();
		createMainLayout();
		createFolderPathTF();
		createFileWindowButton();
	}

	private void createFolderPathTF() {
		folderPathTF = new TextField();
		folderPathTF.setWidth(30, Unit.PERCENTAGE);
		folderPathTF.setCaption("Select folder path with fasta files");
		mainVL.addComponent(folderPathTF);
	}

	private void createFileWindowButton() {
		sequenceAlignmentBtn = new Button("Sequence Alignment");
		sequenceAlignmentBtn.addClickListener(e -> {
			String folderPath = folderPathTF.getValue();
			generate(folderPath);
		});
		mainVL.addComponent(sequenceAlignmentBtn);
	}

	private void generate(String folderPath) {
		File folder = new File(folderPath);
		File[] listOfAllItemsInFolder = folder.listFiles();
		List<File> listOfFiles = new ArrayList<>();

		for (int i = 0; i < listOfAllItemsInFolder.length; i++) {
			if (listOfAllItemsInFolder[i].isFile()) {
				listOfFiles.add(listOfAllItemsInFolder[i]);
			}
		}
		List<ProteinSequence> proteinSequenceList = new ArrayList<ProteinSequence>();
		Map<ProteinSequence, String> proteinMap = new HashMap<ProteinSequence, String>();
		int i = 0;
		for (File file : listOfFiles) {
			Map<ProteinSequence, String> protTemp = loadAnotherFasta(file);
			proteinMap.put(protTemp.keySet().iterator().next(), protTemp.get(protTemp.keySet().iterator().next()));
			ProteinSequence protein0 = protTemp.keySet().iterator().next();
			if (i == 0) {
				mainProtein = protein0;
			} else {
				proteinSequenceList.add(protein0);
			}
			i++;
		}

		GapPenalty gapPenalty = new SimpleGapPenalty();
		int gop = 10;
		int extend = 1;
		gapPenalty.setExtensionPenalty(extend);
		gapPenalty.setOpenPenalty(gop);

		SubstitutionMatrix<AminoAcidCompound> blosum80 = SubstitutionMatrixHelper.getBlosum80();
		for (ProteinSequence ps : proteinSequenceList) {
			List<ProteinSequence> proteinSeqList = new ArrayList<ProteinSequence>();
			proteinSeqList.add(mainProtein);
			proteinSeqList.add(ps);
			List<PairwiseSequenceScorer<ProteinSequence, AminoAcidCompound>> allPairsScorers = Alignments
					.getAllPairsScorers(proteinSeqList, PairwiseSequenceScorerType.LOCAL, gapPenalty, blosum80);

			double[] score = Alignments.runPairwiseScorers(allPairsScorers);

			String key = proteinMap.get(ps);
			runPairwiseScorers.put(key, score);
		}

		for (String key : runPairwiseScorers.keySet()) {
			for (double runPairwiseScore : runPairwiseScorers.get(key)) {
				createResultLabel(key, runPairwiseScore);
			}
		}

		// resultLabel.setValue("" + runPairwiseScorers[0]);
	}

	private Map<ProteinSequence, String> loadAnotherFasta(File file) {
		Map<ProteinSequence, String> sequenceWithName = new HashMap<ProteinSequence, String>();
		LinkedHashMap<String, DNASequence> dnaSeq = null;
		LinkedHashMap<String, ProteinSequence> protSeq;
		ProteinSequence protein;
		try {
			try {
				dnaSeq = FastaReaderHelper.readFastaDNASequence(file);
				String key = dnaSeq.keySet().iterator().next();
				DNASequence dna = dnaSeq.get(key);
				protein = dna.getRNASequence().getProteinSequence();
				sequenceWithName.put(protein, key);
			} catch (Exception e) {
				protSeq = FastaReaderHelper.readFastaProteinSequence(file);
				String key = protSeq.keySet().iterator().next();
				protein = protSeq.get(key);
				sequenceWithName.put(protein, key);
			}
			return sequenceWithName;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void createResultLabel(String name, double runPairwiseScore) {
		resultLabel = new Label();
		resultLabel.setValue(name + ": " + runPairwiseScore + "");
		mainVL.addComponent(resultLabel);
	}

	private void createMainLayout() {
		mainVL = new VerticalLayout();
		mainVL.setWidth(100, Unit.PERCENTAGE);
		mainVL.setHeightUndefined();
		mainVL.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		mainVL.addComponent(new Label("<h1>#9 PHAGE FINDERS</h1>", ContentMode.HTML));
		setContent(mainVL);
	}
}