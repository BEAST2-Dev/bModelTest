package beast.app.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.javafx.util.Utils;

import beast.app.beauti.BeautiConfig;
import beast.app.beauti.BeautiDoc;
import beast.app.draw.BEASTObjectDialog;
import beast.app.draw.BEASTObjectPanel;
import beast.app.util.Application;
import beast.app.util.ConsoleApp;
import beast.app.util.LogFile;
import beast.core.Description;
import beast.core.Input;
import beast.core.Input.Validate;
import beast.core.parameter.IntegerParameter;
import beast.core.parameter.RealParameter;
import beast.evolution.substitutionmodel.Frequencies;
import beast.evolution.substitutionmodel.NucleotideRevJumpSubstModel;
import beast.core.Runnable;
import beast.util.LogAnalyser;

@Description("Analyses bModelTest log and list useful stats such as 95% HPDs of model indicators")
public class BModelAnalyser extends Runnable {
	public Input<LogFile> traceFileInput = new Input<>("file","trace log file containing output of a bModelTest analysis", Validate.REQUIRED);
	public Input<String> prefixInput = new Input<>("prefix", "prefix of the entry in the log file containing the substitution model trace (default 'substmodel')" , "substmodel");
	public Input<Integer> burninInput = new Input<>("burnin", "percentage of the log file to disregard as burn-in (default 10)" , 10);

	
	@Override
	public void initAndValidate() {
		int burnin = burninInput.get();
		if (burnin >= 100) {
			throw new IllegalArgumentException("burnin is a percentage and should not be larger than 100");
		}
		
	}

	@Override
	public void run() throws Exception {
		File file = traceFileInput.get();
		String prefix = prefixInput.get();
		int burnin = burninInput.get();
		if (burnin < 0) {
			burnin = 0;
		}
		
		System.err.println("REPLACE FOR V2.4.1 in BModelAnalyser.run(): LogAnalyser analyser = new LogAnalyser(file.getAbsolutePath(), burnin, false, false);");
		// for v2.4.0 LogAnalyser analyser = new LogAnalyser(file.getAbsolutePath(), burnin);
		LogAnalyser analyser = new LogAnalyser(file.getAbsolutePath(), burnin, false, false);
		
		for (String label : analyser.getLabels()) {
			if (label.startsWith(prefix)) {
				Double [] trace = analyser.getTrace(label);
				processTrace(label, trace);
			}
		}
	}

	private void processTrace(String label, Double[] trace) {
		System.out.println(label);
		int [] model = new int[trace.length];
		for (int i = 0; i < trace.length; i++) {
			model[i] = (int) (double) (trace[i] + 0.5);
		}
		
		Map<Integer, Integer> countMap = new HashMap<>();
		for (int i : model) {
			if (!countMap.containsKey(i)) {
				countMap.put(i, 0);
			}
			countMap.put(i, countMap.get(i) + 1);
		}

		List<Integer> models = new ArrayList<>();
		for (Integer i : countMap.keySet().toArray(new Integer[]{})) {
			models.add(i);
		}
		Set<Integer> isIn95HPD = new HashSet<>();
		List<Integer> models2 = new ArrayList<>();
		models2.addAll(models);
		
		Collections.sort(models, (i1,i2)-> {
			return countMap.get(i1).compareTo(countMap.get(i2));
		});
		
		int treshold = 95 * trace.length / 100;
		int sum = 0;
		NumberFormat formatter = new DecimalFormat("##0.00");     
		for (int i = models.size() - 1; i >= 0 && sum < treshold; i--) {
			int current = models.get(i);
			int contribution = countMap.get(current);
			sum += contribution;
			double con = 100*(contribution + 0.0)/trace.length;
			if (con < 10) {
				System.out.print(" ");
			}
			System.out.print(formatter.format(con) + "% " );
			System.out.print(formatter.format(100*(sum + 0.0)/trace.length) + "% " );
			System.out.println(current);
			isIn95HPD.add(current);
		}
		System.out.println();
		
		String dotty = toDotty(models2, countMap, isIn95HPD, trace.length);
		//System.out.println(dotty);
		
		
		String jsPath = getJavaScriptPath();
		
		
		
		String html = "<html>\n" + 
				"<header>\n" + 
				"<script data-main='" + jsPath + "/bower_components/main' src='" + jsPath + "/bower_components/requirejs/require.js'></script>\n" + 
				" \n" + 
				"<script>\n" + 
				"requirejs.config({\n" + 
				"    //By default load any module IDs from js/lib\n" + 
				"    baseUrl: '"+jsPath+"',\n" + 
				"    //except, if the module ID starts with 'app',\n" + 
				"    //load it from the js/app directory. paths\n" + 
				"    //config is relative to the baseUrl, and\n" + 
				"    //never includes a '.js' extension since\n" + 
				"    //the paths config could be for a directory.\n" + 
				"    paths: {\n" + 
				"        d3: 'bower_components/d3/d3',\n" + 
				"        'dot-checker': 'bower_components/graphviz-d3-renderer/dist/dot-checker',\n" + 
				"        'layout-worker': 'bower_components/graphviz-d3-renderer/dist/layout-worker',\n" + 
				"        worker: 'bower_components/requirejs-web-workers/src/worker',\n" + 
				"        renderer: 'bower_components/graphviz-d3-renderer/dist/renderer'\n" + 
				"    }\n" + 
				"});\n" + 
				"</script>\n" + 
				"\n" + 
				"</header>\n" + 
				"<body>\n" + 
				"\n" + 
				"\n" + 
				"<svg id='graph' width='1224' height='1024'></svg>\n" + 
				"\n" + 
				"<script>\n" + 
				"require(['renderer'],\n" + 
				"  function (renderer) {\n" + 
				"\n" + 
				"  dotSource = '" + dotty.replaceAll("\n", "\\\\\n")+ "';\n" + 
				"  // initialize svg stage\n" + 
				"  renderer.init('#graph');\n" + 
				"\n" + 
				"  // update stage with new dot source\n" + 
				"  renderer.render(dotSource);\n" + 
				"});\n" + 
				"\n" + 
				"</script>\n" + 
				"\n" + 
				"</body>";
		try {
	        FileWriter outfile = new FileWriter(jsPath + "/bModelTest.html");
	        outfile.write(html);
	        outfile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new js.ConsoleApp("BModelAnalyser", jsPath);
	}
	
	private String getJavaScriptPath() {
		String classpath = System.getProperty("java.class.path");
		String[] classpathEntries = classpath.split(File.pathSeparator);
		String FILESEP = "/";
		if (Utils.isWindows()) {
			FILESEP = "\\\\";
		}
		for (String pathEntry : classpathEntries) {
			File parentFile = (new File(pathEntry)).getParentFile();
			if (parentFile.getName().toLowerCase().equals("lib")) {
				parentFile = parentFile.getParentFile();
			}
			String parent = parentFile.getPath();
			String s = parent + FILESEP + "js" + FILESEP +
					"bower_components" + FILESEP + "requirejs" + FILESEP +
					"require.js";
			System.out.print("Trying >" + s + "< ");
			if (new File(s).exists()) {
				System.out.println("Got it!");
				return parent + FILESEP + "js";
			}
			System.out.println("No luck ");
		}
		// TODO Auto-generated method stub
		return null;
	}

	private String toDotty(List<Integer> models, Map<Integer, Integer> countMap, Set<Integer> isIn95HPD, int n) {
		
		Frequencies frequencies = new Frequencies();
		frequencies.initByName("frequencies", "0.25 0.25 0.25 0.25");
		NucleotideRevJumpSubstModel sm = new NucleotideRevJumpSubstModel();
		sm.initByName("rates", new RealParameter("1.0 1.0 1.0 1.0 1.0 1.0"), "modelIndicator", new IntegerParameter("0"), "frequencies", frequencies,
				"modelSet", NucleotideRevJumpSubstModel.ModelSet.transitionTransversionSplit);

	
		
		StringBuilder b = new StringBuilder();
		b.append("digraph {\n");
			b.append(" node [fontsize=\"9\", style=\"solid\", color=\"#0000FF60\"];\n");
			b.append(" \n");
			
			double sum = 0;
			for (int current : models) {
				int contribution = countMap.get(current);
				double con = Math.sqrt((contribution + 0.0)/n);
				sum += con;
			}
			sum = 1.5 * sum / models.size();
		
			for (int i = 0; i < sm.getModelCount(); i++) {
				int [] model = sm.getModel(i);
				int modelID = 0;
				int k = 1;
				for (int j = model.length - 1; j >= 0; j--) {
					modelID += (model[j]+1) * k;
					k = k * 10;
				}

				int current = modelID;
				int contribution = countMap.containsKey(current)? countMap.get(current) : 0;
				double con = Math.sqrt((contribution + 0.0)/n) / sum;
				b.append(current + " [width=" + con + ", height=" + con+", fixedsize=\"true\"" + (isIn95HPD.contains(current) ? "" : ", color=\"#FF000060\"")+ "];\n");
			}
			b.append(" \n");
			b.append(sm.toDottyGraphOnly());
			b.append("\n");
			b.append("111111 [label=\"JC69/F81\"]\n");
			b.append("121121 [label=\"K80\\nHKY\"]\n");
			b.append("123456 [label=\"SYM\\nGTR\"]\n");
			b.append("121131 [label=\"121131\\nTN93\"]\n");
			b.append("123341 [label=\"123341\\nTIM\"]\n");
			b.append("123421 [label=\"123421\\nTVM\"]\n");
			b.append("123321 [label=\"123321\\nK81\"]\n");
			b.append("}\n");
		return b.toString();
	}

	static ConsoleApp app;
	public static void main(String[] args) throws Exception {
		BModelAnalyser analyser = new BModelAnalyser();
		analyser.setID("Analyses bModelTest trace logs");
		analyser.traceFileInput.setValue(new LogFile("someTrace.log"), analyser);
	
		if (args.length == 0) {
			// create BeautiDoc and beauti configuration
			BeautiDoc doc = new BeautiDoc();
			doc.beautiConfig = new BeautiConfig();
			doc.beautiConfig.initAndValidate();
					
			// create panel with entries for the application
			BEASTObjectPanel panel = new BEASTObjectPanel(analyser, analyser.getClass(), doc);
			
			// wrap panel in a dialog
			BEASTObjectDialog dialog = new BEASTObjectDialog(panel, null);
	
			// show the dialog
			if (dialog.showDialog()) {
				dialog.accept(analyser, doc);
				// create a console to show standard error and standard output
				app = new ConsoleApp("BModelAnalyser", 
						"BModelAnalyser: " + analyser.traceFileInput.get().getPath(),
						null
						);
				analyser.initAndValidate();
				analyser.run();
			}
			return;
		}

		Application main = new Application(analyser);
		main.parseArgs(args, false);
		analyser.initAndValidate();
		analyser.run();
	}

}


