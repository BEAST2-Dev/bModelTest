package bmodeltest.app.tools;


import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import beast.base.core.BEASTVersion2;
import beastfx.app.inputeditor.BeautiConfig;
import beastfx.app.inputeditor.BeautiDoc;
import beastfx.app.inputeditor.BEASTObjectDialog;
import beastfx.app.inputeditor.BEASTObjectPanel;
import beastfx.app.tools.Application;
import beastfx.app.tools.LogAnalyser;
import beastfx.app.util.LogFile;
import beastfx.app.util.Utils;
import bmodeltest.evolution.substitutionmodel.NucleotideRevJumpSubstModel;
import bmodeltest.evolution.substitutionmodel.NucleotideRevJumpSubstModel.ModelSet;
import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.core.Input.Validate;
import beast.base.inference.parameter.IntegerParameter;
import beast.base.inference.parameter.RealParameter;
import beast.base.core.Log;
import beast.base.evolution.substitutionmodel.Frequencies;
import beast.base.inference.Runnable;
import beast.pkgmgmt.BEASTClassLoader;

@Description("Analyses bModelTest log and list useful stats such as 95% HPDs of model indicators")
public class BModelAnalyser extends Runnable {
	public Input<LogFile> traceFileInput = new Input<>("file","trace log file containing output of a bModelTest analysis", Validate.REQUIRED);
	public Input<String> prefixInput = new Input<>("prefix", "prefix of the entry in the log file containing the substitution model trace (default 'substmodel')" , "substmodel");
	public Input<Integer> burninInput = new Input<>("burnin", "percentage of the log file to disregard as burn-in (default 10)" , 10);
	public Input<ModelSet> modelSetInput = new Input<>("modelSet", "Which set of models to choose, should be the same as used in the BEAST XML that generated the log file", 
			ModelSet.transitionTransversionSplit, ModelSet.values());
	public Input<Boolean> useBrowseInput = new Input<>("useBrowserForVisualisation", "use default web browser for visualising the dot graph. "
			+ "Since not all browsers support all features, the alternative is to use an internal viewer, which requires an up to date Java 8 version.", true);
	
	
	double max = 0;
	
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
		
		int  instance = 0;
		for (String label : analyser.getLabels()) {
			if (label.startsWith(prefix)) {
				Double [] trace = analyser.getTrace(label);
				processTrace(label, trace, instance);
				instance++;
			}
		}
	}

	private void processTrace(String label, Double[] trace, int instance) {
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
		StringBuilder b = new StringBuilder();
		b.append("<tr><th>posterior support</th><th>cumulative support</th><th>model</th></tr>");
		int i = 0;
		for (i = models.size() - 1; i >= 0 && sum < treshold; i--) {
			int current = models.get(i);
			int contribution = countMap.get(current);
			sum += contribution;
			double con = 100*(contribution + 0.0)/trace.length;
			if (con < 10) {
				System.out.print(" ");
				b.append(" ");
			}
			System.out.print(formatter.format(con) + "% " );
			System.out.print(formatter.format(100*(sum + 0.0)/trace.length) + "% " );
			System.out.println(current);
			b.append("<tr" + (i%2 == 0 ? "" : " class='alt'")+ "><td>" + formatter.format(con) + "% " + "</td>");
			b.append("<td>" + formatter.format(100*(sum + 0.0)/trace.length) + "% </td>" );
			b.append("<td>" + current + "</td></tr>\n");
			isIn95HPD.add(current);
		}
		System.out.println();

		b.append("<tr class='ruled'><td/><td/><td/></tr>\n");
		int listed = isIn95HPD.size();
		while (i >= 0) {
			int current = models.get(i);
			int contribution = countMap.get(current);
			sum += contribution;
			double con = 100*(contribution + 0.0)/trace.length;
			if (con > 0.1) {
				b.append("<tr" + (i%2 == 0 ? "" : " class='alt'")+ "><td>" + formatter.format(con) + "% " + "</td>");
				b.append("<td>" + formatter.format(100*(sum + 0.0)/trace.length) + "% </td>" );
				b.append("<td>" + current + "</td></tr>\n");
				listed++;
			}
			i--;
		}
		
		
		String dotty = toDotty(models2, countMap, isIn95HPD, trace.length);
		String svg = toSVG(models2, countMap, isIn95HPD, trace.length);
		String dotty2 = dotty;
		dotty2 = dotty2.replaceAll("\n", "\\\\\n");
		dotty2 = dotty2.replaceAll("'", "&quot;");
		String jsPath = getJavaScriptPath();
		try {
	        FileWriter outfile = new FileWriter(jsPath + "/bModelTest" + instance + ".dot");
	        outfile.write(dotty);
	        outfile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String log = traceFileInput.get().getName();
		String header = "<h2> File: " + log + " item: " + label + "</h2>";

		String html = "<html>\n" + 
				"<title>BEAST " + new BEASTVersion2().getVersionString() + ": BModelAnalyser</title>\n" +
				"<header>\n" + 
				"<link rel='stylesheet' type='text/css' href='css/style.css'>\n" +
/*				"<script data-main='" + jsPath + "/main' src='" + jsPath + "/requirejs/require.js'></script>\n" + 
				" \n" + 
				"<script>\n" + 
				"requirejs.config({\n" + 
				"    //By default load any module IDs from js-directory\n" + 
				"    baseUrl: '"+jsPath+"',\n" + 
				"    //except, if the module ID starts with 'app',\n" + 
				"    //load it from the js/app directory. paths\n" + 
				"    //config is relative to the baseUrl, and\n" + 
				"    //never includes a '.js' extension since\n" + 
				"    //the paths config could be for a directory.\n" + 
				"    paths: {\n" + 
				"        d3: 'd3/d3',\n" + 
				"        'dot-checker': 'graphviz-d3-renderer/dist/dot-checker',\n" + 
				"        'layout-worker': 'graphviz-d3-renderer/dist/layout-worker',\n" + 
				"        worker: 'requirejs-web-workers/src/worker',\n" + 
				"        renderer: 'graphviz-d3-renderer/dist/renderer'\n" + 
				"    }\n" + 
				"});\n" +
				"</script>\n" +
*/				"\n" +
				"</header>\n" +
				"<body>\n" +
				header + "\n" +
				"<div>Models with blue circles are inside 95%HPD, red outside, and without circles have " + (max > 0 ? "at most " : "") + formatter.format(max) + "% support.</div>\n" + 
				"<table><tr class='x'><td class='x'><table>" + b.toString() + "</table>"
						+ "   <div style=\"height:" + (1024 - 82 - listed * 29) + "\"></div></td>\n" +
				"<td class='x'>" +
				svg
				//+ (!Utils.isWindows() ? svg : "<svg id='graph' width='1224' height='1024'></svg>")
				+ "</td></table>\n" +
				"\n" +
/*				"<div id=\"img\" onclick=\"continueExecution()\">Create downloadable image</div>\n" + 
				"\n" + 
				"<script>\n" + 
				"  continueExecution = function() {\n" + 
				
				"var html = d3.select(\"svg\")\n" + 
				"        .attr(\"title\", \"bModelTest\")\n" + 
				"        .attr(\"version\", 1.1)\n" + 
				"        .attr(\"xmlns\", \"http://www.w3.org/2000/svg\")\n" + 
				"        .node().parentNode.innerHTML;\n" + 
				"d3.select(\"#img\")\n" + 
				"        .html(\"Right-click on this preview and choose Save as<br />Left-Click to dismiss<br />\")\n" + 
				"        .append(\"img\")\n" + 
				"        .attr(\"src\", \"data:image/svg+xml;base64,\"+ btoa(html));\n" + 
				"}\n" + 
				"require(['renderer'],\n" + 
				"  function (renderer) {\n" + 
				"\n" + 
				//"var client = new XMLHttpRequest();\n" + 
				//"client.open('GET', 'bModelTest" + instance + ".dot');\n" + 
				//"client.onreadystatechange = function() {\n" + 
				//"  dotSource = client.responseText;\n" +
				" dotSource = '" + dotty2 + "'\n" +
				"  // initialize svg stage\n" + 
				"  renderer.init('#graph');\n" + 
				"\n" + 
				"  // update stage with new dot source\n" + 
				"  renderer.render(dotSource);\n" + 
				//"}\n" + 
				//"client.send();\n" + 
				"\n" + 
				"});\n" + 
*/				"</script>\n" + 
				"\n" + 
				"</body>\n" +
				"</html>";
		
		try {
	        FileWriter outfile = new FileWriter(jsPath + "/bModelTest" + instance + ".html");
	        outfile.write(html);
	        outfile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (useBrowseInput.get()) {
			try {
				Application.openUrl("file://" + jsPath + "/bModelTest" + instance + ".html");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// test for JavaFX
			boolean isJavaFxAvailable;
			try {
			  BEASTClassLoader.forName("javafx.embed.swing.JFXPanel");
			  isJavaFxAvailable = true;
			} catch (ClassNotFoundException e) {
			  isJavaFxAvailable = false;
			}
			if (!isJavaFxAvailable) {
				JOptionPane.showMessageDialog(null, "It looks like JavaFX is not available.\n "
						+ "To fix this, you can install the JDK from Oracle, available here\n"
						+ "http://www.oracle.com/technetwork/java/javase/downloads/index.html\n\n"
						+ "The webviewer will be started, but expect things to not show properly.>");
			}
			new bmodeltest.app.tools.WebViewer("BModelAnalyser", "file://" + jsPath + "/bModelTest" + instance + ".html");
		}
	}
	

	
	private String getJavaScriptPath() {
		String classpath = System.getProperty("java.class.path");
		String[] classpathEntries = classpath.split(File.pathSeparator);
		String FILESEP = "/";
		if (Utils.isWindows()) {
			FILESEP = "\\\\";
		}
		for (String pathEntry : classpathEntries) {
			// Log.info("Trying >" + pathEntry + "< ");
			if (new File(pathEntry).getName().toLowerCase().equals("bmodeltest.addon.jar")) {
				Log.debug.println("Got it!");
				File parentFile = (new File(pathEntry)).getParentFile().getParentFile();
				String parent = parentFile.getPath();
				String jsPath = parent + FILESEP + "js";
				if (new File(jsPath).exists()) {
					return jsPath;
				}
				// perhaps a space in the file name (especially on OS X)
				jsPath = jsPath.replaceAll("%20"," ");
				return jsPath;
			}
			//Log.debug.println("No luck ");
		}
		String jsPath = System.getProperty("user.dir") + FILESEP + "js";
		if (Utils.isWindows()) {
			jsPath = jsPath.replaceAll("\\\\","/");
			jsPath = "/" + jsPath;
		}

		
		//Log.debug.println("Using default: " + jsPath);
		return jsPath;
	}

	private String toSVG(List<Integer> models, Map<Integer, Integer> countMap, Set<Integer> isIn95HPD, int n) {
		double scale = 100;
		int maxWidth = 1200;
		double VGAP = 0.3;
		double HGAP = 0.05;
		Frequencies frequencies = new Frequencies();
		frequencies.initByName("frequencies", "0.25 0.25 0.25 0.25");
		NucleotideRevJumpSubstModel sm = new NucleotideRevJumpSubstModel();
		sm.initByName("rates", new RealParameter("1.0 1.0 1.0 1.0 1.0 1.0"), "modelIndicator", new IntegerParameter("0"), "frequencies", frequencies,
				"modelSet", modelSetInput.get());

		double sum = 0;
		for (int current : models) {
			int contribution = countMap.get(current);
			double con = Math.sqrt((contribution + 0.0)/n);
			sum += con;
		}
		sum = 1.5 * sum / models.size();
		max = 0;
		
		StringBuilder b = new StringBuilder();

		
		int N = sm.getModelCount();
		String [] label = new String[N];
		String [] atts = new String[N];
		int [] nodeCount = new int[7];
		double [] offset = new double[7];
		double [] lastcon = new double[7];
		double [] maxcon = new double[7];
		double [] x = new double[N];
		double [] radius = new double[N];

		
//		List<Integer>[]level = new ArrayList[6];
//		for (int i = 0; i < 6; i++) {
//			level[i] = new ArrayList<>();
//		}
//		for (int i = 0; i < N; i++) {
//			int g = sm.getGroupCount(i);
//			level[g - 1].add(i); 
//		}
//
//		for (int i = 4; i > 0; i--) {
//			int [] map = new int[N];
//			for (int i1 = 0; i1 < level[i+1].size(); i1++) {
//				map[level[i+1].get(i1)] = i1;
//			}
//			for (int i1 = 0; i1 < level[i].size(); i1++) {
//				int p1 = level[i].get(i1);
//				for (int i2 = 0; i2 < level[i].size(); i2++) {
//					int p2 = level[i].get(i2);
//					if (p1 != p2) {
//						int cur = countCrosses(i1, i2, p1, p2, sm, map);
//						int swapped = countCrosses(i1, i2, p2, p1, sm, map);
//						if (swapped < cur) {
//							level[i].set(i1, p2);
//							level[i].set(i2, p1);
//						}
//					}
//				}
//			}
//		}
//	
//		List<Integer> order = new ArrayList<>();
//		for (int i = 0; i < 6; i++) {
//			order.addAll(level[i]);
//		}
		
		for (int q = 0; q < N; q++) {
			int i = q;//order.get(q);
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
			switch (current) {
				case 111111 : label[i] = "JC69/F81"; break;
				case 121121 : label[i] = "K80/HKY"; break;
				case 123456 : label[i] = "SYM/GTR"; break;
				case 121131 : label[i] = "121131/TN93"; break;
				case 123341 : label[i] = "123341/TIM"; break;
				case 123425 : label[i] = "123425/TVM"; break;
				case 123321 : label[i] = "123321/K81"; break;
				default : label[i] = current + "";
			}
			
			if (con <= 0.25) {
				atts[i] =  (isIn95HPD.contains(current) ? "c95hpd" : "csmall");
				radius[i] = 0.25;
				max = Math.max(max, 100 * (contribution + 0.0)/n);
			} else {
				atts[i] =  (isIn95HPD.contains(current) ? "c95hpd" : "cdefault");
				radius[i] = con;
			}
			
			con = radius[i];
			
			int g = sm.getGroupCount(i);
			x[i] = offset[g] + HGAP + con/2 + lastcon[g]/2;
			offset[g] += HGAP + con/2 + lastcon[g]/2;
			lastcon[g] = con;
			maxcon[g] = Math.max(maxcon[g], con);
			nodeCount[sm.getGroupCount(i)]++;
		}

		double [] h = new double[7];
		h[0] = VGAP * scale;
		for (int i = 1; i < 7; i++) {
			h[i] = h[i-1] + (maxcon[i-1]/2.0 + maxcon[i]/2.0 + VGAP) * scale;
		}
		
		// center
		double max = 0;
		for (double d : offset) {
			max = Math.max(d, max);
		}
		double [] y = new double[N];
		for (int i = 0; i < sm.getModelCount(); i++) {
			int g = sm.getGroupCount(i);
			double delta = (max - offset[g]) / 2.0;
			x[i] += delta;
			x[i] *= maxWidth/max;
			radius[i] *= scale / 2.0;
			y[i] = h[g];
		}		
	
		// draw lines
		for (int i = 0; i < sm.getModelCount(); i++) {
			int g = sm.getGroupCount(i);
			for (int j : sm.getSplitCanditates(i)) {				
				b.append("<!--" + i + " => " + j + "-->\n");
				appendSVGLine(b, x[i], y[i], radius[i], x[j], y[j], radius[j], h[g] + (maxcon[g]/2.0 +VGAP/2.0)* scale);
			}
		}
			
		// draw circles
		for (int i = 0; i < sm.getModelCount(); i++) {
			b.append("<circle cx=\""+ x[i] + "\" cy=\""+ y[i] + "\" r=\""+ radius[i] +"\" class=\"" + atts[i] + "\"/>\n");
			b.append("<text x=\""+ x[i] + "\" y=\""+ (y[i]+4) + "\" class=\"btext\">" + label[i] + "</text>\n");
		}		
				
		String svgStart = 
		"<svg width=\""+(maxWidth+100)+"px\" height=\"1000px\">\n" +
		"  <defs>\n" +
		"  <style  type=\"text/css\">\n" +
		"  .btext {fill:#000080;font-size:8pt;font-family:arial;text-anchor:middle;text-align:center;}\n" +
		"  .csmall {fill:none;stroke-width:1.5;stroke:none;}\n" +
		"  .c95hpd {fill:none;stroke-width:1.5;stroke:#0000ff;}\n" +
		"  .cdefault {fill:none;stroke-width:1.5;stroke:#ff0000;}\n" +
		" .line {fill:none;stroke-width:1;stroke:#a0a0a0;}\n" +
		"  </style>\n" +
	    "    <marker id=\"arrow\" markerWidth=\"15\" markerHeight=\"15\" refX=\"8\" refY=\"4.5\" orient=\"auto\" markerUnits=\"strokeWidth\">\n" +
	    "      <path d=\"M0,0 l0,13.5 l13.5,-6.75 z\" fill=\"#a0a0a0\" />\n" +
		"    </marker>\n" +
		"  </defs>\n" +
		"<rect width=\"100%\" height=\"100%\" style=\"fill:white\"/>\n";
		return svgStart + b.toString() + "</svg>";
	}
	
	private int countCrosses(int i1, int i2, int p1, int p2, NucleotideRevJumpSubstModel sm, int [] map) {
		int crosses = 0;
		for (int c1 : sm.getSplitCanditates(p1)) {
			int pos1 = map[c1];
			for (int c2 : sm.getSplitCanditates(p2)) {
				int pos2 = map[c2];
				if (i1 < i2 && pos1 > pos2) {
					crosses++;
				}
				if (i1 > i2 && pos1 < pos2) {
					crosses++;
				}
			}
		}
		return crosses;
	}

	private void appendSVGLine(StringBuilder b, double x1, double y1, double r1, double x2, double y2, double r2, double mid) {
		double phi1 = Math.atan2(y1 - y2, x2 - x1);
		phi1 = (Math.PI/2 + phi1)/2;
		double cx1 = x1 + r1 * Math.sin(phi1);
		double cy1 = y1 + r1 * Math.cos(phi1);
		
		double phi2 = -Math.abs(Math.atan2(y2 - y1, x1 - x2));
		phi2 = Math.PI - (Math.PI/2 + phi2)/2;
		double cx2 = x2 + (r2 + 4) * Math.sin(phi2);
		double cy2 = y2 + (r2 + 4) * Math.cos(phi2);
		
		b.append("<path d=\"M" + cx1+ " " + cy1);
		x1 = x1 + 2 * (cx1 - x1); 
		y1 = y1 + 2 * (cy1 - y1); 
		x2 = x2 + 2 * (cx2 - x2); 
		y2 = y2 + 2 * (cy2 - y2); 
//		y1 = y1 + 2 * (r1); 
//		y2 = y2 - 2 * (r2); 
		b.append(" C"+x1+","+mid);
		if (Math.abs(x1-x2) > 250) {
			b.append(" " + (cx1+(cx2-cx1)/2) + " " + (mid-5) + " " + (cx1+2*(cx2-cx1)/3) + " " + (mid+5) + " S");
		}
		b.append(" " + x2+ "," + mid +" " + cx2+" "+ cy2 + "\" ");
		b.append(" class=\"line\" marker-end=\"url(#arrow)\"/>\n");
	}

	private String toDotty(List<Integer> models, Map<Integer, Integer> countMap, Set<Integer> isIn95HPD, int n) {
		
		Frequencies frequencies = new Frequencies();
		frequencies.initByName("frequencies", "0.25 0.25 0.25 0.25");
		NucleotideRevJumpSubstModel sm = new NucleotideRevJumpSubstModel();
		sm.initByName("rates", new RealParameter("1.0 1.0 1.0 1.0 1.0 1.0"), "modelIndicator", new IntegerParameter("0"), "frequencies", frequencies,
				"modelSet", modelSetInput.get());

	
		
		StringBuilder b = new StringBuilder();
		b.append("digraph {\n");
			b.append(" graph [mindist=0.0, nodesep=0.25, ranksep=0.4]\n;");
			b.append(" node [fontsize=\"9\", style=\"solid\", color=\"#0000FF60\"];\n");
			b.append(" \n");
			
			double sum = 0;
			for (int current : models) {
				int contribution = countMap.get(current);
				double con = Math.sqrt((contribution + 0.0)/n);
				sum += con;
			}
			sum = 1.5 * sum / models.size();
			max = 0;
			
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
				if (con <= 0.1) {
					b.append(current + " [width=0.25, height=0.25, fixedsize=\"true\",color=\"#FFFFFF\"];\n");
					max = Math.max(max, 100 * (contribution + 0.0)/n);
				} else {
					b.append(current + " [width=" + con + ", height=" + con +", fixedsize=\"true\"" + (isIn95HPD.contains(current) ? "" : ", color=\"#FF000060\"") +"];\n");
				}
			}
			b.append(" \n");
			b.append(sm.toDottyGraphOnly());
			b.append("\n");
			b.append("111111 [label=\"JC69/F81\"]\n");
			b.append("121121 [label=\"K80\\nHKY\"]\n");
			b.append("123456 [label=\"SYM\\nGTR\"]\n");
			b.append("121131 [label=\"121131\\nTN93\"]\n");
			b.append("123341 [label=\"123341\\nTIM\"]\n");
			b.append("123425 [label=\"123425\\nTVM\"]\n");
			b.append("123321 [label=\"123321\\nK81\"]\n");
			b.append("}\n");
		return b.toString();
	}

	// static ConsoleApp app;
	public static void main(String[] args) throws Exception {
		BModelAnalyser analyser = new BModelAnalyser();
		analyser.setID("Analyses bModelTest trace logs");
		analyser.traceFileInput.setValue(new LogFile("someTrace.log"), analyser);

		new Application(analyser, analyser.getID(), args);
	}

}


